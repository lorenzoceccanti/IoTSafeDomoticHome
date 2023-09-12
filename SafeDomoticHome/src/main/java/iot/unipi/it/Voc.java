package iot.unipi.it;

public class Voc {
	private String sensorId;
	private int smokePercentage;
	
	public Voc(String sensorId, int currentSmk)
	{
		this.sensorId = sensorId;
		this.smokePercentage = currentSmk;
	}
	
	public String getSensorId() {
		return this.sensorId;
	}
	
	public int getSmokePercentage() {
		return this.smokePercentage;
	}
	
}
