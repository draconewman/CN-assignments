// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
import java.text.*;
import java.util.*;
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
		int count=0;
		int frameNo=0;
		while (true)
		{
			Socket s = null;
			
			try
			{
				// socket object to receive incoming client requests
				s = ss.accept();
				
				System.out.println("A new client is connected : " + s);
				
				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				
				System.out.println("Assigning new thread for this client");
				dos.writeUTF("Connection to server established");
				String crc;
				
				crc=dis.readUTF();
				dos.writeUTF("CRC Received");
				int n=Integer.valueOf(dis.readUTF());
				System.out.println("CRC is"+crc);
				// create a new thread object
				Thread t = new ClientHandler(s, dis, dos,crc,n);

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
	int n;
	boolean[]  sent=new boolean[6000];
	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos,String st,int x)
	{
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		c=new CRC(st);
		n=x;
	}

	@Override
	public void run()
	{
		
		String received;
		String toreturn;
		int count=0;
		int cur=0;
		int lb=0,rb=lb+n;
		while (true)
		{
			rb=lb+n;
			int k=0;
			for(k=0;k<6000;k++)sent[k]=false;
			
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
				
				// creating Date object
				Date date = new Date();
				
				// write on output stream based on the
				// answer from the client
				       System.out.println("Frame Received: "+received);count++;
					    
						int l=received.length();
						int indiv=l/n;
						ArrayList<String> sl=new ArrayList<String>();
						//System.out.println(Integer.toString(l)+" "+Integer.toString(indiv)+" "+Integer.toString((n)));
						int j;
						for(j=0;j<l;j+=indiv)
						{
							if(j+indiv>l)
							{ sl.add(received.substring(j));}
							else
							sl.add(received.substring(j,j+indiv));
							
						}
						
						String s="",acknowledgement="";
						int v;
						int p=Math.min(n,v=sl.size());
						//System.out.println(v+"*");
						for(j=0;j<p;j++)
						{String temp=sl.get(j);
						char chr=temp.charAt(indiv-1);
						 s="";
						s+=chr;
						temp=temp.substring(0,indiv-1);
						
							for(k=lb;k<rb;k++)
							{
								if((Integer.toString(k%(2*n)).equals(s)&&(c.isErrorFree(temp)==true)))
								{
									sent[k]=true;
								}
							}
						
						}
						frameNo=lb;
						for(j=lb;j<=rb;j++)
						{if(sent[j])
							{
								frameNo++;frameNo%=(2*n);
							}
						}
						acknowledgement+=Integer.toString(frameNo);
						//System.out.println(acknowledgement);
						count++;
						dos.writeUTF(acknowledgement);
						for(j=0;j<100;j++)System.out.print("*");
						System.out.println("");
						
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
        String mData=Data;
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
