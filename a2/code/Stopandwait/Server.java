// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
// import java.text.*;
// import java.util.*;
import java.net.*;

// Server class
public class Server
{
	public static void main(String[] args) throws IOException
	{
		// server is listening on port 5056
		ServerSocket ss = new ServerSocket(5056);
		
		// running infinite loop for getting
		// client request
		// int count=0;
		// int frameNo=0;
		while (true)
		{
			Socket s = null;
			
			try
			{
				// socket object to receive incoming client requests
				s = ss.accept();
				
				System.out.println("A new client is connected: " + s);
				
				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				
				System.out.println("Assigning new thread for this client");
				dos.writeUTF("Connection to server established");
				String crc;
				
				crc=dis.readUTF();
				System.out.println("CRC is "+crc);
				// create a new thread object
				Thread t = new ClientHandler(s, dis, dos,crc);

				// Invoking the start() method
				t.start();
				
			}
			catch (Exception e){
				s.close();
				//e.printStackTrace();
			}
		}
	}
}

// ClientHandler class
class ClientHandler extends Thread
{
	// DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
	// DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;
	int frameNo=0;
	CRC c;
	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos,String st)
	{
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		c=new CRC(st);
	}

	@Override
	public void run()
	{
		String received;
		//String toreturn;
		int count=0;
		//int cur=0;
		while (true)
		{
			try {

				// Ask what the user wants
				dos.writeUTF("----");
				
				// receive the answer from client
				received = dis.readUTF();
				
				if(received.equals("Exit"))
				{
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				}
				
			
				
				// write on output stream based on the
				// answer from the client
				       System.out.println("Frame Received: "+received);count++;
					    
						int l=received.length();
						char chr=received.charAt(l-1);
						String s="",acknowledgement="";s+=chr;
						received=received.substring(0,l-1);
						if(Integer.toString(frameNo).equals(s)&&(c.isErrorFree(received)==true)){frameNo++;frameNo%=2;}
						acknowledgement+=Integer.toString(frameNo);
						System.out.println(acknowledgement);count++;
						for(int j=0;j<100;j++)System.out.print("*");
						System.out.println("");
						dos.writeUTF(acknowledgement);
						
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		
		try
		{
			// closing resources
			this.dis.close();
			this.dos.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	static  public class CRC
{
    public int len;
    public String CRC;
    public CRC(String s)
    {
        CRC=s;
        len=s.length();
    }
	public boolean  isErrorFree(String Data)
    {
        //String mData=Data;
        int j,dl,k;
        dl=Data.length();
        
        for(j=0;j<dl-len+1;j++)
        {
            k=0;char ch=Data.charAt(j);
            for(k=0;k<len;k++)
            {
                if(ch=='1')
                {
                    char c=Data.charAt(j+k),d=CRC.charAt(k);
                    Data=Data.substring(0,j+k)+xor(c,d)+Data.substring(j+k+1);
                    
                }
                else
                {
                    break;
                }
               // System.out.println("$\n");
           }
        }
		//System.out.println(Data+"$\n");
        for(j=1-len+dl;j<dl;j++){if(Data.charAt(j)=='0')continue;else return(false);}
        return(true);
    }
   public  String xor(char a,char b)
    {
        if(a==b)return("0");
        else return("1");
    }

}
}
