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
import processing.core.PFont;

public class JavaClient extends PApplet
{
	private static final long serialVersionUID = 1L;
	Vector<PowerDataPoint> dataList = new Vector<PowerDataPoint>();
	private static final int gposx = 100, gposy = 50;
	private PFont f24, f12;
	
	public void setup()
	{
		frame.setTitle("Bluetooth Power Monitor Realtime Graph");
		
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
		f12 = loadFont("SegoeUI-12.vlw");
		f24 = loadFont("SegoeUI-24.vlw");
	}
	
	public void draw()
	{
		int y, py = 0;
		int x, px = 0;
		background(255);
		stroke(0);
		fill(255);
		
		rect(gposx, -1, width - gposx, height - 1 - gposy);
		
		fill(0);
		textFont(f24);
		textAlign(CENTER,CENTER);
		if (dataList.size() > 0)
		{
			text((int)dataList.lastElement().getPower()+"W", gposx/2, height-gposy/2);
		}
		
		for (int i=0; i<dataList.size(); i++)
		{
			y = height - 1 - gposy - (int)dataList.get(i).getPower();
			x = 5*i + gposx;
			if (i > 0)
			{
				line(px, py, x, y);
			}
			px = x;
			py = y;
		}
	}

	public static void main(String[] args)
	{
		PApplet.main(new String[] { "--bgcolor=D4D0C8", "JavaClient" });
	}
	
}
