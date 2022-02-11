import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.time.*;
// import java.time.temporal.Temporal;
// import java.time.temporal.ChronoUnit;
//import myCRC.*;
// Client class
public class Client 
{
    public static void main(String[] args) throws IOException 
    {
        boolean resend=false;
        int frameNo=0;
        try
        {
           
            Scanner scn = new Scanner(System.in);
              //Getting CRC polynomial from user
              System.out.println("Enter CRC Polynomial: ");
              String CRC_POLY;
              CRC_POLY=scn.nextLine();
              CRC c=new CRC(CRC_POLY);

              System.out.println("How many erroneous frames you want to send? ");
              int er;
              er=scn.nextInt();
            

            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");
      
            // establish the connection with server port 5056
            Socket s = new Socket(ip, 5056);
      
            // obtaining input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());            
            
            System.out.println(dis.readUTF());
            dos.writeUTF(CRC_POLY);

            // the following loop performs the exchange of
            // information between client and client handler
            int count=0;long t=0;
                    File f=new File("64bit.txt");
                    FileReader fr=new FileReader(f);
                    BufferedReader r=new  BufferedReader(fr);
                    String  tosend=null;
            while (true) 
            {  System.out.println(count+" #");
                System.out.println(dis.readUTF());
                String modified;
               // String tosend = scn.nextLine();
               
                if(!resend){
                try{
                    //tot++;
                    tosend=null;
                    tosend=r.readLine();
                    if(tosend==null)break;
                    //System.out.println("Atleast\n");
                    tosend=c.getCRCCode(tosend);
                    tosend+=Integer.toString(frameNo);
                    //int l=tosend.length();
                    frameNo++;frameNo%=2;
                    //System.out.println(l);
                }
                catch(Exception e){}
            }
            modified=tosend;
            if((er>0)&&(Math.random()<0.1))
            {
                modified=modify(modified);er--;
            }
            count++;
            Instant previous, current;
            previous = Instant.now();
                dos.writeUTF(modified);
                //System.out.println("$3\n");
                // If client sends exit,close this connection 
                // and then break from the while loop
                if(tosend.equals("Exit"))
                {
                    System.out.println("Closing this connection: " + s);
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }
                  
                // printing date or time as requested by client
                String received = dis.readUTF();
                current = Instant.now();
                 long ns = Duration.between(previous,current).toNanos();
                 t+=ns;
                 //System.out.println(" it took "+Long.toString(ns)+"ns to complete");
                if(received.equals(Integer.toString(frameNo))){resend=false;}
                else resend=true;
                System.out.println("RTT: "+Long.toString(ns)+" ns ");
                if(resend)System.out.println("Time out!");
                System.out.println("Acknowledgement for frame: "+received+" received ");
                for(int j=0;j<100;j++)System.out.print("*");
                System.out.println("");
            }
              
            // closing resources
            scn.close();
            dis.close();
            dos.close();
            System.out.println("Total Time taken: "+Long.toString(t)+" ns");
            System.out.println("Average RTT: "+Float.toString((float)t/count)+" ns");
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    static public String modify(String s)
    {
        int l=s.length();
        int j;
        for(j=0;j<l;j++)
        {
            char c=s.charAt(j);
            if(Math.random()<0.1){s=s.substring(0,j)+not(c)+s.substring(j+1);}
        }
        return(s);
    }
    static public String not(char a)
    {
        if(a=='0')return("1");
        else return("0");
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
	public String  getCRCCode(String Data)
    {
        String mData=Data;
        int j,dl,k;
        dl=Data.length();
        for(j=0;j<len-1;j++)
        {
            Data+='0';
        }
        for(j=0;j<dl;j++)
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
               
           }
        }
        //System.out.println("$\n");
        for(j=dl;j<len+dl-1;j++){mData+=(Data.charAt(j));}
        return(mData);
    }
   public String xor(char a,char b)
    {
        if(a==b)return("0");
        else return("1");
    }

}
}