import ErrorDetection as err

# Function to read from the input file and convert to a list of frames
def readfile(filename, no_of_bits):
	# Open the file for reading
	print('\n\nReading file '+filename)
	f=open(filename,'r')
	data=f.read()

	# Now split the data into frames
	list_of_frames=[data[i:i+no_of_bits] for i in range(0, len(data), no_of_bits)]

	# Printing the frames
	print('Codeword frames received: ')
	print(list_of_frames)

	# list_of_frames=data.split('\n')
	# list_of_frames=list_of_frames[0:-1]
	return list_of_frames


# Check for error by lrc
def check_lrc(list_of_frames, no_of_bits):

	# Removing padding
	list_of_frames=[list_of_frames[i][len(err.generator_poly)-1:]
	 for i in range(len(list_of_frames))]

	lrcval=err.lrc(list_of_frames=list_of_frames, no_of_bits=no_of_bits)
	#if the appended value is zero 
	if(int(lrcval,2)==0): 
		print('No error is detected by LRC')
		print('Dataword frames are ')
		print(list_of_frames[0:-1])

	else:
		print('Error is detected by LRC')

# Check for error by vrc
def check_vrc(list_of_frames):

	# Removing padding
	list_of_frames=[list_of_frames[i][len(err.generator_poly)-2:]
	 for i in range(len(list_of_frames))]
	flag=True
	
	for i in range(len(list_of_frames)):
		if(list_of_frames[i].count('1')%2!=0):
			print('Error is detected in frame '+str(i+1)+' by VRC')
			flag=False

	if(flag):
		# No error extract dataword
		print("No error is detected by VRC")
		list_of_frames=[list_of_frames[i][0:-1] for i in range(len(list_of_frames))]
		print('Dataword frames are ')
		print(list_of_frames)

# Check for error by checksum
def check_checksum(list_of_frames, no_of_bits):

	# Removing padding
	list_of_frames=[list_of_frames[i][len(err.generator_poly)-1:] 
	for i in range(len(list_of_frames))]
	
	chksum=err.checksum(list_of_frames=list_of_frames, no_of_bits=no_of_bits)

	if(int(chksum,2)==0): 
		# In case of no error detected then dataword is printed
		print('No error is detected by checksum')
		print('Dataword frames are ')
		print(list_of_frames[0:-1])

	else:
		print('Error is detected by checksum')

# Check for error by crc
def check_crc(list_of_frames, generator):

	flag=True
	for i in range(len(list_of_frames)):
		if(int(err.modulo2div(list_of_frames[i],err.generator_poly),2)!=0):
			print('Error is detected in frame '+str(i+1)+' by CRC')
			flag=False

	if(flag):
		list_of_frames=[list_of_frames[i][0:err.no_of_bits_crc] 
		for i in range(len(list_of_frames))]
		print('Dataword frames are ')
		print(list_of_frames)
		print("No error is detected by CRC")

# Function which combines all module
def modules():
	no_of_bits=err.no_of_bits

	print('\n\n||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||RECEIVER||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n\n')

	list_of_frames=readfile('Transmission_LRC.txt',no_of_bits+len(err.generator_poly)-1)
	check_lrc(list_of_frames, no_of_bits)

	list_of_frames=readfile('Transmission_VRC.txt',no_of_bits+len(err.generator_poly)-1)
	check_vrc(list_of_frames)

	list_of_frames=readfile('Transmission_Checksum.txt',no_of_bits+len(err.generator_poly)-1)
	check_checksum(list_of_frames, no_of_bits)

	list_of_frames=readfile('Transmission_CRC.txt',no_of_bits=err.no_of_bits_crc+len(err.generator_poly)-1)
	check_crc(list_of_frames,generator=err.generator_poly)