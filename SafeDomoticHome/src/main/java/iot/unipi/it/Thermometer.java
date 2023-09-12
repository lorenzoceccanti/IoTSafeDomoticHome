package iot.unipi.it;

public class Thermometer {
	private String sensorId;
	private String currentTemp;
	
	public Thermometer(String sensorId, String currentTemp)
	{
		this.sensorId = sensorId;
		this.currentTemp = currentTemp;
	}
	
	public String getSensorId() {
		return this.sensorId;
	}
	
	public String getCurrentTemp() {
		return this.currentTemp;
	}
	
	public int getCurrentTempRepresentation() {
		String[] arr;
		arr = currentTemp.split("\\.");
		
		String newStr = new String(arr[0]+arr[1]);
		return Integer.parseInt(newStr);
	}
	
}
