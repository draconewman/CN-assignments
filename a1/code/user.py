import sender as se
import receiver as re
import transmission as tm 
import ErrorDetection as err



# Main module to show all 3 cases
# error in which frames and in which positions are manually done 
def case1() :
	
	list_of_frames=(se.readfile('raw_input.txt',no_of_bits=err.no_of_bits))
	print('Case1: All 4 schemes can detect the error')
	se.dataword_to_codeword(list_of_frames, no_of_bits=err.no_of_bits)
	tm.sending_codeword(list_of_frames,no_of_bits=err.no_of_bits, error_list_frames=[0, 1], error_bit_list=[[2], [3]])
	re.modules()
	print('--------------------------------------------------------------------------')

def case2() :

	list_of_frames=(se.readfile('raw_input.txt',no_of_bits=err.no_of_bits))
	print('Case2: Error detected by checksum but not by CRC')
	se.dataword_to_codeword(list_of_frames, no_of_bits=err.no_of_bits)
	tm.sending_codeword(list_of_frames,no_of_bits=err.no_of_bits, error_list_frames=[0], error_bit_list=[[0, 4, 7]])
	#tm.sending_codeword(list_of_frames,no_of_bits=err.no_of_bits, error_list_frames=[0], error_bit_list=[[0, 3, 4]])
	re.modules()
	print('--------------------------------------------------------------------------')

def case3() :

	list_of_frames=(se.readfile('raw_input.txt',no_of_bits=err.no_of_bits))
	print('Case3: Error detected by VRC but not by CRC')
	se.dataword_to_codeword(list_of_frames, no_of_bits=err.no_of_bits)
	tm.sending_codeword(list_of_frames,no_of_bits=err.no_of_bits, error_list_frames=[1], error_bit_list=[[0, 4, 7]])
	#tm.sending_codeword(list_of_frames,no_of_bits=err.no_of_bits, error_list_frames=[0], error_bit_list=[[0, 1, 5, 6, 8]])
	re.modules()
	print('--------------------------------------------------------------------------')


if __name__ == "__main__":

	while True :
		print('1. Error is detected by all four schemes')
		print('2. Error detected by checksum but not by CRC')
		print('3. Error detected by VRC but not by CRC')
	
		a = input('Enter your choice : ')
		ch = int(a)
		if(ch==1) :
			case1()
		elif(ch==2) :
			case2()
		elif(ch==3) :
			case3()
		else :
			print ('Wrong choice !')

		c = input('Enter your Choice? Press y for Yes or any other key to exit : ')
		
		if(c!='y') : 
			print('Exiting....')
			break 
