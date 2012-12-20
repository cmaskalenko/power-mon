import java.util.Date;

public class PowerDataPoint
{
	private double power;
	private Date date;
	
	public PowerDataPoint(double power)
	{
		if (power >= 25)
		{
			this.power = power;
		}
		else
		{
			this.power = power;
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
