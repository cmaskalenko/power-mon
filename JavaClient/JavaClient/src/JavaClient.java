import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
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

/**
 * The visual data plotting client program for the wireless power monitor
 * @author Carl
 *
 */
public class JavaClient extends PApplet
{
	private static final long serialVersionUID = 1L;
	private Vector<PowerDataPoint> dataList = new Vector<PowerDataPoint>(); // Power data collected
	private static final int gposx = 100, gposy = 50; // Position of the graph offset from the lower left corner
	private PFont f24, f12; // Two sizes of fonts, 12 and 24
	private int maxp; // Upper bound of power measurement on the power axis
	private static final int numMarkings = 10; // Preferred number of tick marks on power axis
	private BufferedWriter bw = null; // Used for writing to output file
	
	/**
	 * Runs at start
	 */
	public void setup()
	{
		frame.setTitle("Bluetooth Power Monitor Realtime Graph");
		
		LocalDevice ld;
		DiscoveryAgent da = null;
		String url;
		StreamConnection conn = null;
		DataInputStream in = null;
		FileWriter fw;
		
		// Set up output file writing
		try
		{
			fw = new FileWriter("out.csv");
			bw = new BufferedWriter(fw);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		// Sets up the Bluetooth device discovery in order to connect to the monitor
		try
		{
			ld = LocalDevice.getLocalDevice();
			da = ld.getDiscoveryAgent();
		}
		catch (BluetoothStateException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		// Universally Unique Identifier for serial communication over Bluetooth
		UUID u = new UUID("0000110100001000800000805F9B34FB", false);
		
		// Start connection to power monitor
		try
		{
			// Simple, nonencrypted serial communication
			url = da.selectService(u, ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			conn = (StreamConnection) Connector.open(url);
			in = conn.openDataInputStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		// Start the power data loading thread
		Thread dloader = new Thread(new DataLoader(dataList, in, bw));
		dloader.start();
		
		try
		{
			conn.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		// Set properties for applet
		size(850, 600);
		background(255);
		stroke(0);
		// Load font files
		f12 = loadFont("SegoeUI-12.vlw");
		f24 = loadFont("SegoeUI-24.vlw");
	}
	
	/**
	 * Called for each frame of the animation
	 */
	public void draw()
	{
		int y, py = 0;
		int x, px = 0;
		background(255);
		stroke(0);
		
		printLabels();
		
		// Set maximum of power axis to 20% larger than maximum power reading
		maxp = getMaxPower()*6/5;
		
		// Draw graph subwindow
		fill(255);
		rect(gposx, -1, width - gposx, height - 1 - gposy);
		
		// Print latest power measurement if it exists
		fill(0);
		textFont(f24);
		textAlign(CENTER,CENTER);
		if (dataList.size() > 0)
		{
			// Put the text in the lower left corner
			text((int)dataList.lastElement().getPower() + "W", gposx/2, height - gposy/2);
		}
		
		// Plot data points
		for (int i=0; i<dataList.size(); i++)
		{
			// Set y based on the ratio of the power measurement to the upper bound of the power axis
			y = height - 1 - gposy - ((int)dataList.get(i).getPower())*(height - gposy)/maxp;
			x = 5*i + gposx;
			// Draw line from previous point to this one
			if (i > 0)
			{
				line(px, py, x, y - 1);
				ellipseMode(CENTER);
				ellipse(x, y - 1, 2, 2);
			}
			px = x;
			py = y;
		}
		
		// Draw time axis
		int[] markings = getPowerAxisMarkings();
		for (int i=0; i<markings.length; i++)
		{
			y = height - 2 - gposy - markings[i]*(height - gposy)/maxp;
			// Draw tick mark
			line(gposx - 5, y, gposx, y);
			// Add label
			textFont(f12);
			textAlign(RIGHT, CENTER);
			text(markings[i], gposx - 10, y - 1);
		}
	}
	
	/**
	 * Called when a key is pressed
	 */
	public void keyPressed()
	{
		// Safely exit program if escape key or q is pressed
		if ((key == ESC) || (key == 'q'))
		{
			try
			{
				// Close output file
				bw.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.exit(1);
			}
			System.exit(0);
		}

	}
	
	/**
	 * Determine what appropriate mark values for the power axis should be
	 * @return Array of integer value suggested mark values
	 */
	private int[] getPowerAxisMarkings()
	{
		// Calculate rough interval
		float interval = (float)maxp/numMarkings;
		// Convert to scientific notation
		int exponent = (int)Math.log10(interval);
		float mantissa = (float)interval / (float)Math.pow(10,exponent);
		// Change interval mantissa to 1, 2, 5, or 10 depending on which is geometrically closer
		if (mantissa < 1.41) mantissa = 1; // 1.41 is geometric mean of 1 and 2
		else if ((mantissa >= 1.41) && (mantissa < 3.16)) mantissa = 2; // 3.16 is geometric mean of 2 and 5
		else if ((mantissa >= 3.16) && (mantissa < 7.07)) mantissa = 5; // 7.07 is geometric mean of 5 and 10
		else mantissa = 10;
		interval = (int)(mantissa*Math.pow(10, exponent)); // Revised, "nice" number interval for markings
		
		// Create output array by calculating number of tick marks
		int[] rtnval = new int[(int)Math.ceil((float)maxp/interval)];
		
		// Calculate tick mark values
		for (int i=0; i<rtnval.length; i++)
		{
			rtnval[i] = i*(int)interval;
		}
		
		return rtnval;
	}
	
	/**
	 * Prints the axis labels
	 */
	private void printLabels()
	{
		int x = gposx/2;
		int y = height/2 - gposy/2;
		// Prints vertical text for power label by rotating the entire image 90 degrees, printing, then rotating back
		textAlign(CENTER, BOTTOM);
		pushMatrix();
		translate(x, y);
		rotate(-HALF_PI);
		fill(0);
		textFont(f24);
		text("Power (W)", 0, 0);
		popMatrix();
		
		// Print horizontal axis label
		textAlign(CENTER, CENTER);
		text("Last 150 Data Points", width/2 + gposx/2, height - gposy/2);
	}
	
	/**
	 * Searches through data for largest power reading
	 * @return Largest power reading in data
	 */
	private int getMaxPower()
	{
		// Upper bound of array should not be less than 10
		int max = 10;
		for (int i=0; i<dataList.size(); i++)
		{
			if (dataList.get(i).getPower() > max)
			{
				max = (int)dataList.get(i).getPower();
			}
		}
		return max;
	}

	/**
	 * Runs the PApplet as a Java application
	 * @param args Input arguments
	 */
	public static void main(String[] args)
	{
		PApplet.main(new String[] { "--bgcolor=D4D0C8", "JavaClient" });
	}
	
}
