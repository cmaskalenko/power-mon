import java.io.*;
import javax.microedition.io.*;
import javax.bluetooth.*;

public class TestClient
{
	public final static String MACaddr = "000666495720";

	public static void main(String args[])
	{
		try
		{
			LocalDevice ld = LocalDevice.getLocalDevice();
			DiscoveryAgent da = ld.getDiscoveryAgent();
			UUID u = new UUID("0000110100001000800000805F9B34FB",false);
			
			
			String url = da.selectService(u,ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			
			StreamConnection conn = (StreamConnection) Connector.open(url);

			DataInputStream in = conn.openDataInputStream();

			for (int i=0; i<10; i++)
			{
				System.out.println("Power: "+getInt(in)+ "W");
			}

			conn.close();
		} 
		catch (IOException e) 
		{
			System.err.print(e.toString());
		}
	}
	
	private static int getInt(DataInputStream in)
	{
		int j = 0;
		byte b;
		byte[] bs = new byte[16];
		try
		{
			while((b = in.readByte())!='\r')
			{
				bs[j]= b;
				j++;
			}
		}
		catch (IOException e) 
		{
			System.err.print(e.toString());
		}
		bs[j] = 0;
		return Integer.parseInt(new String(bs).trim());
	}
}