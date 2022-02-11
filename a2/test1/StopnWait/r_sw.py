import socket
import time

framesize=16
dwsize=8
s_send = socket.socket()  
       
print( "Socket successfully created")

port_send = 2002
port_receive = 2001

s_send.bind(('', port_send))         
print( "socket binded to %s" %(port_send) )
  
s_send.listen(5)      
print( "socket is listening\n")

r_n=0

# checks the parity of the given string; if odd number of 1’s has been found, it returns False else True
def check_parity(num):
	count=0
	for s in num:
		if s=='1':
			count=count+1
	if count%2==0:
		return True
	return False

# checks if there is error in the frame
def corrupted(frame):
	s1=frame.decode("utf-8")
	s2=s1[framesize-dwsize-1:]
	if check_parity(s2):
		return False
	return True

# acknowledgment
def acknowledgement(r_n):
	ack=str(r_n)
	b_ack=bytearray()
	b_ack.extend(map(ord,ack))
	return b_ack

# returns the sequence number of the frame
def seqno(frame):
	if frame[0]==49:
		return 1
	else:
		return 0


while True:
	try:
		s_receive = socket.socket() 
		s_receive.connect(('127.0.0.1', port_receive))
		frame=s_receive.recv(1024)
		s_receive.close()
		print("---------------------------------")
		print("Received Frame = "+frame.decode("utf-8"))
		if corrupted(frame)==False:
			if seqno(frame)==r_n:
				try:
					c, addr = s_send.accept()
					print('Got connection from', addr )
					r_n=(r_n+1)%2
					time.sleep(3)
					c.send(acknowledgement(r_n))
					print("acknowledgement sent for "+acknowledgement(r_n).decode("utf-8")+"\n")
					c.close()
				except InterruptedError as err:
					print(err)
			else:
				print("Expected frame = "+str(r_n)+", Arrived Frame = "+str(seqno(frame))+"\n")
		else:
			print("Frame Corrupted\n")
			
	except ConnectionRefusedError as err:
		print(err)
	except OSError as err2:
		pass