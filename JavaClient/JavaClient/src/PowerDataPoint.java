import java.util.Date;

public class PowerDataPoint
{
	private double power;
	private Date date;
	
	public PowerDataPoint(double power)
	{
		this.power = power;
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
}
