package iot.unipi.it;

public class AlarmSensor {
	
	private String sensorId;
	private int isOpened;
	
	public AlarmSensor(String sensorId, int isOpened)
	{
		this.sensorId = sensorId;
		this.isOpened = isOpened;
	}
	
	public String getSensorId() {
		return this.sensorId;
	}
	
	public int getIsOpened() {
		return this.isOpened;
	}
}
