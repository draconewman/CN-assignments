import java.io.*;
import java.net.*;
import java.util.*;
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
              
              System.out.println("Enter n value: "); //n is no. of bits to represent window size
              int n;
              n=scn.nextInt();

            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");
      
            // establish the connection with server port 5056
            Socket s = new Socket(ip, 5056);
      
            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            
            
            System.out.println(dis.readUTF());
            dos.writeUTF(CRC_POLY);
            
            System.out.println(dis.readUTF());
            dos.writeUTF(Integer.toString(n));

            // the following loop performs the exchange of
            // information between client and client handler
            int count=0,tot=0;long t=0;
                    File f=new File("64bit.txt");
                    FileReader fr=new FileReader(f);
                    BufferedReader r=new  BufferedReader(fr);
                    String  tosend=null;
                ArrayList<String> data=new ArrayList<String>();
                String str;int fn=0;int x=0;
                while(!((str=r.readLine())==null))
                {
                    str=c.getCRCCode(str);
                    str+=Integer.toString(fn);
                    //str+='|';
                    data.add(str);
                    fn++;fn%=(2*n);x++;
                }
                String st="";
                int fl=(data.get(1)).length(),cr=CRC_POLY.length();fl--;
                //System.out.println(Integer.toString(fl)+"!!!!!!!&&&&&&&");
                for(int j=0;j<fl;j++)st+=("0");
                String nst=st;
                while(x%(n)!=0)
                {
                    nst=st;
                    nst+=Integer.toString(fn);
                    data.add(nst);
                    fn++;fn%=(n+1);x++;
                }
                for(int j=0;j<2*n;j++)
                {
                    nst=st;
                    nst+=Integer.toString(fn);
                    data.add(nst);
                    fn++;fn%=(n+1);
                }
                int iframe=0,lframe=n;
                System.out.println(Integer.toString(x)+"&&&&&&&");
            while (true) 
            { //System.out.println("$\n");
                System.out.println(dis.readUTF());
                String modified;
               // String tosend = scn.nextLine();
               tosend="";
                int j;lframe=iframe+n;
                System.out.println(Integer.toString(iframe)+" "+Integer.toString(lframe));
                for(j=iframe;j<lframe;j++)
                {modified=data.get(j);
                    if((er>0)&&(Math.random()<0.1))
                    {
                      modified=modify(modified);er--;
                     }
                     tosend+=modified;
                }  
                  //  System.out.println(l);
            
    
            Instant previous, current, gap;
            previous = Instant.now();
            count+=n;
                dos.writeUTF(tosend);
                System.out.println("$3\n");
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
                int z=Integer.valueOf(received);
                for(j=iframe;j<=lframe;j++)
                {
                    if(j==x)break;
                    if((j%(2*n))==z)break;
                }
                iframe=j;
                 long ns = Duration.between(previous,current).toNanos();
                 t+=ns;
                 System.out.println("RTT: "+Long.toString(ns)+" ns ");
                if(iframe!=lframe)System.out.println("Time out!");
                System.out.println("Acknowledgement for frame: "+received+" received ");
                for(j=0;j<100;j++)System.out.print("*");
                System.out.println("");
                if(x<=iframe)break;
            }
              
            // closing resources
            scn.close();
            dis.close();
            dos.close();
            System.out.println("Total Time taken: "+Long.toString(t)+" ns");
            System.out.println("Average RTT: "+Float.toString((float)t/x)+" ns");
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
    static public  String not(char a)
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
   public  String xor(char a,char b)
    {
        if(a==b)return("0");
        else return("1");
    }

}
}