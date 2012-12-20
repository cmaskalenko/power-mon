import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

public class DataLoader implements Runnable
{
	Vector<PowerDataPoint> data;
	DataInputStream in;
	
	public DataLoader(Vector<PowerDataPoint> data, DataInputStream in)
	{
		this.data = data;
		this.in = in;
	}

	@Override
	public void run()
	{
		while(true)
		{
			data.add(new PowerDataPoint((double)getInt()));
		}
	}
	
	private int getInt()
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
