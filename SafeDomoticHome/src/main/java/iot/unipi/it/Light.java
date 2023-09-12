package iot.unipi.it;

public class Light {
	private int bulbStatus;
	
	public Light(int bulbStatus)
	{
		this.bulbStatus = bulbStatus;
	}
	
	public int getBulbStatus() {
		return this.bulbStatus;
	}
}
