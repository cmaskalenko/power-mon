import java.util.Date;

public class PowerDataPoint
{
	private double power;
	private Date date;
	
	public PowerDataPoint(double power)
	{
		if (power >= 25)
		{
			this.power = power*1.063-6.19;
		}
		else
		{
			this.power = 0;
		}
		date = new Date();
	}
	
	public Date getDate()
	{
		return date;
	}
	
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
