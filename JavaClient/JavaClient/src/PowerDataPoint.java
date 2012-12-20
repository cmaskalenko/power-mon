import java.util.Date;

/**
 * Class for storing a data point with a power value and a time (Date object)
 * @author Carl
 *
 */
public class PowerDataPoint
{
	private double power;
	private Date date;
	
	/**
	 * Default constructor
	 * @param power
	 */
	public PowerDataPoint(double power)
	{
		if (power >= 25)
		{
			// Adjust data based on calibration line function determined for power monitor
			this.power = power*1.063-6.19;
		}
		else
		{
			// Values less than 25 are probably erroneous values when nothing is attached to the monitor
			// Our specifications stated a minimum measurement of 50W, so this should not affect real data
			this.power = 0;
		}
		date = new Date(); // new Date() initializes with the current date and time
	}
	
	/**
	 * Allows read access to date value
	 * @return Date value
	 */
	public Date getDate()
	{
		return date;
	}
	
	/**
	 * Allows read access to power value
	 * @return Power value
	 */
	public double getPower()
	{
		return power;
	}
	
	@Override
	public String toString()
	{
		return (date.getTime() + "," + power);
	}
}
