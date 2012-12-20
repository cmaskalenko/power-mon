import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import processing.core.PApplet;

public class JavaClient extends PApplet
{
	private static final long serialVersionUID = 1L;
	Vector<PowerDataPoint> dataList = new Vector<PowerDataPoint>();
	
	public void setup()
	{
		LocalDevice ld;
		DiscoveryAgent da = null;
		String url;
		StreamConnection conn = null;
		DataInputStream in = null;
		
		try
		{
			ld = LocalDevice.getLocalDevice();
			da = ld.getDiscoveryAgent();
		}
		catch (BluetoothStateException e)
		{
			System.err.println(e.toString());
			System.exit(1);
		}
		UUID u = new UUID("0000110100001000800000805F9B34FB",false);
		
		try
		{
			url = da.selectService(u,ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			conn = (StreamConnection) Connector.open(url);
			in = conn.openDataInputStream();
		}
		catch (IOException e)
		{
			System.err.println(e.toString());
			System.exit(1);
		}

		Thread dloader = new Thread(new DataLoader(dataList,in));
		dloader.start();
		
		try
		{
			conn.close();
		}
		catch (IOException e)
		{
			System.err.println(e.toString());
			System.exit(1);
		}
		
		size(800,600);
		background(255);
		stroke(0);
	}
	
	public void draw()
	{
		int y,py=0;
		background(255);
		stroke(0);
		for (int i=0; i<dataList.size(); i++)
		{
			y = height - 1 - (int)dataList.get(i).getPower();
			if (i > 0)
			{
				line(5*(i-1),py,5*i,y);
			}
			
			py = y;
		}
	}

	public static void main(String[] args)
	{
		PApplet.main(new String[] { "--bgcolor=D4D0C8", "JavaClient" });
	}
	
}
