import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * The thread that waits for and loads power data without blocking the main process
 * @author Carl
 *
 */
public class DataLoader implements Runnable
{
	private Vector<PowerDataPoint> data;
	private DataInputStream in;
	private BufferedWriter out;
	
	/**
	 * Default constructor
	 * @param data Reference to data vector to write to
	 * @param in DataInputStream to read data from
	 * @param out BufferedWriter to write output data to
	 */
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
			// Create new data point object
			p = new PowerDataPoint((double)getInt());
			// Add to data
			data.add(p);
			try
			{
				// Write data to output file
				out.write(p.toString() + "\n");
			}
			catch (IOException e)
			{
				System.err.println("Error while attempting to write to output file.\n");
			}
		}
	}
	
	/**
	 * Gets an integer from the input stream following the output format of the monitor
	 * @return
	 */
	private int getInt()
	{
		int j = 0;
		byte b;
		// Array to load bytes into
		byte[] bs = new byte[16];
		try
		{
			// Read in bytes until carriage return is reached (values are terminated by \r\n)
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
		// Null-terminate string
		bs[j] = 0;
		try
		{
			// Parse integer from String
			return Integer.parseInt(new String(bs).trim());
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

}
