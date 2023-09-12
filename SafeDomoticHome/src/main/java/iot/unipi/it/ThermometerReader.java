package iot.unipi.it;

public class ThermometerReader{

	private String sensorId;
	private int currentTemp;

	
	public ThermometerReader()
	{
		this.sensorId = "";
		this.currentTemp = -1;
	}
	
	public int getLastTemp() throws Exception{
		if(this.currentTemp == -1)
			throw new Exception();
		else
			return this.currentTemp;
	}
	
	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}
	
	public void setCurrentTemp(int currentTemp) {
		this.currentTemp = currentTemp;
	}
	

}
