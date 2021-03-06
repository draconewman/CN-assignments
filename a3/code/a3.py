import simpy
import random

offered_load = 0
throughput = 0
throughputA = 0
throughputB = 0
throughputC = 0

numOfMobiles = int (input ("Enter the number of mobiles : "))
print ("Types of CSMA protocol ")
print ("1. p persistent\n2. 1 persistent\n3. non persistent")
choice = int(input("Enter Choice : "))

# class slotSignal: generate slot signal to Reader and tags
Count = 0
class slotSignal:
    slotEvt = 0  # slot event

    def __init__(self, Tslot):
        self.env = env
        self.Tslot = Tslot
        slotSignal.slotEvt = env.event()  # slot event initialization

        # schedule process
        env.process(self.run())

    def run(self):
        while True:
            # periodic slot generation
            yield self.env.timeout(self.Tslot)
            # print("slot begins at t = %4.1f" % (self.env.now))
            slotSignal.slotEvt.succeed()  # trigger event, send slot signal
            slotSignal.slotEvt = env.event()  # slot event initialization


# Packet class
class Packet:
    def __init__(self, env):
        self.arvTime = env.now


# on-off traffic model with Poisson distribution
# on = 10 slots, off = random slots
class PacketGenerator:
    arrivalTime = 10000.0

    def __init__(self, env, Ton, Que):
        self.env = env
        self.Ton = Ton
        self.Que = Que

        # schedule process
        env.process(self.run())

    def run(self):
        global offered_load
        
        yield env.timeout(random.expovariate(1.0 / PacketGenerator.arrivalTime))

        while True:
            # start ON period
            interPktTime = 1
            t = 0.0
            while t < self.Ton:
                yield self.env.timeout(interPktTime)
                self.Que.append(Packet(self.env))
                t += interPktTime
                offered_load += 1

            # start OFF period, wait random time with Poisson distribution
            yield env.timeout(random.expovariate(1.0 / PacketGenerator.arrivalTime))


class Channel:
    # define message events
    succeedMsgEvt = 0
    failMsgEvt = 0
    noReplyMsgEvt = 0

    # collision count
    colCount = 0

    # successful read count
    readCount = 0

    # channel state
    channel = False  # True : Busy, False : Idle

    def __init__(self, env, tSlot):
        # initialize message event
        Channel.succeedMsgEvt = env.event()
        Channel.failMsgEvt = env.event()
        Channel.noReplyMsgEvt = env.event()

        # initialize collision count
        Channel.colCount = 0

        self.env = env
        self.tSlot = tSlot  # time slot

        # schedule process
        env.process(self.run())

    def run(self):
        global Count
        Count = 0
        while True:
            # one slot passed
            yield slotSignal.slotEvt

            # receiving the packets

            # check the collision
            tEpsilon = 0.1
            
            yield self.env.timeout(self.tSlot - tEpsilon)

            # send the feedback 0.1 time unit before the next slot
            if Channel.colCount == 0:
                Channel.noReplyMsgEvt.succeed(value='no_reply')
                Channel.noReplyMsgEvt = env.event()

            if Channel.colCount == 1:
                Count = Count + 1
                Channel.channel = True
                Channel.succeedMsgEvt.succeed(value='ACK')
                Channel.succeedMsgEvt = env.event()
                # print("\nACK : success at t = %4.1f\n" % self.env.now)

            elif Channel.colCount > 1:
                Channel.failMsgEvt.succeed(value='NACK')
                Channel.failMsgEvt = env.event()
                print("\nNACK : fail at t = %4.1f\n" % int(self.env.now))
                # reset collision count
                Count = Count + Channel.colCount
                Channel.colCount = 0
        
class Mobile:
    mobileID = 0
    probability = 0
    if choice == 1:
        probability = 1 # probability used in p-persistent CSMA system, probability * numOfMobiles = 1
    else:
        probability = numOfMobiles #probability used in 1 persistent CSMA system
        
    
    def __init__(self, env):
        # slot period
        self.slotTime = 1  # set initial slot number

        # packet queue
        self.Que = []
        PacketGenerator(env, 10, self.Que)

        # set mobile ID
        self.mID = Mobile.mobileID
        Mobile.mobileID += 1

        # mobile status
        self.status = False  # True for transmitting, False for idle
        self.state = False  # True for ready to transmit, False for not ready to transmit

        self.count = 0

        self.env = env

        # schedule process
        env.process(self.run())

    def run(self):
        while True:
            yield slotSignal.slotEvt

            # have data to transmit
            if len(self.Que) > 0:
                # carrier sensing : Idle
                if not self.carrierSense():
                    if not self.state:
                        p = random.random()  # random probability [0, 1)
                        if choice  == 3:
                            p = 0
                        
                        if p <= Mobile.probability:  # transmit
                            yield self.env.timeout(random.randint(0, 15))  # back-off 1
                            self.state = True
                            pass

                        else:
                            yield self.env.timeout(self.slotTime)  # propagation delay
                            pass

                    else:
                        Channel.colCount += 1

                        ret = yield (Channel.noReplyMsgEvt | Channel.succeedMsgEvt | Channel.failMsgEvt)
                        values_listed = list(ret.values())  # converted to list

                        if values_listed[0] == 'no_reply':  # no collision
                            yield self.env.timeout(self.slotTime)
                            pass

                        elif values_listed[0] == 'ACK':  # no collision
                            self.status = True  # change Mobile's status to transmitting
                            self.transmit()
                            pass

                        elif values_listed[0] == 'NACK':
                            yield self.env.timeout(random.randint(0, 15))
                            # back-off 2: random time 0~15 time slots
                            # self.state = False
                            pass

                else:  # carrier sensing : Busy
                    if self.status:  # Mobile status : transmitting
                        self.transmit()
                        if self.count == 10:
                            # print("\nACK : ID = %d, success at t = %4.1f\n" % (self.mID, self.env.now))
                            self.count = 0
                            Channel.colCount = 0
                            print("\nACK : ID = %d, success at t = %4.1f\n" % (self.mID, self.env.now))
                            self.status = False  # change Mobile's status to idle
                            self.state = False
                            yield slotSignal.slotEvt
                            Channel.channel = False  # change channel's status to Idle
                            pass
                    else:
                        pass
            else:
                yield self.env.timeout(self.slotTime)
                pass

    def carrierSense(self):
        if Channel.channel:
            return True
        else:
            return False

    def transmit(self):
        global throughput
        global throughputA
        global throughputB
        global throughputC
        print("ID = %d, trasnmitting at t = %4.1f" % (self.mID, int(self.env.now)))
        del self.Que[0]
        self.count += 1
        throughput += 1
        if self.mID == 0:
            throughputA += 1
        elif self.mID == 1:
            throughputB += 1
        elif self.mID == 2:
            throughputC += 1


env = simpy.Environment()

sim_time = 10000
slotSig = slotSignal(1)
mSet = [Mobile(env) for i in range(numOfMobiles)]
reader = Channel(env, 1)
env.run(until=sim_time)

print("Throughput : " + str(throughput / sim_time)) # average amount of data bits successfully transmitted per unit time
print("offered load : " + str(offered_load / sim_time)) # measure of the traffic compared to the channel capacity
print ("Collision Count : " + str (Count))