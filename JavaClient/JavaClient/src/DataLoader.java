import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

public class DataLoader implements Runnable
{
	private Vector<PowerDataPoint> data;
	private DataInputStream in;
	private BufferedWriter out;
	
	public DataLoader(Vector<PowerDataPoint> data, DataInputStream in, BufferedWriter out)
	{
		this.data = data;
		this.in = in;
		this.out = out;
	}

	@Override
	public void run()
	{
		PowerDataPoint p;
		while (true)
		{
			p = new PowerDataPoint((double)getInt());
			data.add(p);
			try
			{
				out.write(p.toString() + "\n");
			}
			catch (IOException e)
			{
				System.err.println("Error while attempting to write to output file.\n");
			}
		}
	}
	
	private int getInt()
	{
		int j = 0;
		byte b;
		byte[] bs = new byte[16];
		try
		{
			while((b = in.readByte()) != '\r')
			{
				bs[j] = b;
				j++;
			}
		}
		catch (IOException e) 
		{
			System.err.println("Error: monitor lost power");
			System.exit(1);
		}
		bs[j] = 0;
		try
		{
			return Integer.parseInt(new String(bs).trim());
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

}
