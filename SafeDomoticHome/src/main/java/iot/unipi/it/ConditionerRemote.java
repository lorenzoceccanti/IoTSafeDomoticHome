// This is also used to JSONify
package iot.unipi.it;

public class ConditionerRemote {
	private String power; //OF, ON
	private int selTemp;
	private int fanSpeed;
	
	
	public ConditionerRemote(String power, int selTemp, int fanSpeed)
	{
		this.power = power;
		this.selTemp = selTemp;
		this.fanSpeed = fanSpeed;
	}
	
	public ConditionerRemote(String power, String selTemp, String fanSpeed)
	{
		this.power = power;
		
		if(selTemp.equals("-1")||fanSpeed.equals("-1"))
		{
			this.selTemp = -1;
			this.fanSpeed = -1;
		} else {
			// This will convert the temperature in the Contiki compliant temperature representation
			String[] arrivingSelTemp;
			arrivingSelTemp = selTemp.split("\\."); // The dot character has to be escaped
			
	        String newStr = new String(arrivingSelTemp[0] + arrivingSelTemp[1]); // concatenation
	        // Parsing int
	        this.selTemp = Integer.parseInt(newStr);
	        
	        this.fanSpeed = Integer.parseInt(fanSpeed);
		}
	}
	
	public String getPower() {
		return this.power;
	}
	
	public int getSelTemp() {
		return this.selTemp;
	}
	
	public int getFanSpeed() {
		return this.fanSpeed;
	}
}
