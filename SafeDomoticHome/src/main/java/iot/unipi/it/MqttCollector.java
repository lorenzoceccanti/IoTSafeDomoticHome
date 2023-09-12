package iot.unipi.it;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

public class MqttCollector implements MqttCallback{
	
	private final String brokerAddr = "tcp://127.0.0.1:1883";
	private final String clientId = "JavaApp";
	
	private MqttClient mqttClient = null;
	
	private AlarmHandler ah = null;
	private ThermometerReader tr = null;
	
	public void connectToBroker() throws MqttException{
		// Connects to a MQTT broker using default options
		mqttClient.connect();
			
		mqttClient.subscribe("zone_0");
		mqttClient.subscribe("zone_1");
		mqttClient.subscribe("temp");
		mqttClient.subscribe("voc"); // kitchen's topic
	}
	
	public MqttCollector(AlarmHandler ah, ThermometerReader tr) throws InterruptedException
	{
		do {
			try {
				mqttClient = new MqttClient(brokerAddr, clientId);
				mqttClient.setCallback(this);
				connectToBroker();
				
				// From now on we will refer to the object passed from the Collector
				this.ah = ah;
				this.tr = tr;
			}catch(MqttException me)
			{
				System.out.println("Could not connect to the MQTT Broker, retrying..\n");
			}
		} while(!this.mqttClient.isConnected());
	}
	
	// Every time the connection is lost, I try to reconnect doubling the waiting time
		public void connectionLost(Throwable cause)
		{
			System.out.println("Connection lost");
			// First time I wait 2 seconds before reconnecting to the broker
			int waitingTime = 2000;
			while(!this.mqttClient.isConnected())
			{
				try {
					System.out.println("Trying to reconnect in " + waitingTime/1000 + "second(s)");
					Thread.sleep(waitingTime);
					System.out.println("Reconnecting..");
					// In case of failure we prepare to double the waitingTime..
					waitingTime *= 2;
					connectToBroker();
				}catch(InterruptedException ie) {
					ie.printStackTrace();
				}catch(MqttException me)
				{
					System.out.print("[MQTT ERROR]");
					me.getReasonCode();
					System.out.println("");
				}
			}
		}
		
		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
		}
		
		public void messageArrived(String topic, MqttMessage message) throws Exception{
			// Here put the code needed to distinguish the topics
			
			// Alarm topics
			if(topic.equals("zone_0") || topic.equals("zone_1"))
			{
				// JSON alarm parsing
				Gson gson = new Gson();
				Reader reader = new InputStreamReader(new ByteArrayInputStream(message.getPayload()));
		        // Convert JSON File to Java Object
		        AlarmSensor jSonSer = gson.fromJson(reader, AlarmSensor.class);
				// Setting zoneStatus for the AlarmHandler
		        ah.setZoneStatus(topic, jSonSer.getIsOpened());
		        
		        // Ring the alarm if needed
		        try {
		        	ah.ringTheAlarm();
			        // Also putting data into MySQL Database
			        if(topic.equals("zone_0"))
			        	SafeDomoticHomeDB.insertMotionZone0(jSonSer.getIsOpened());
			        if(topic.equals("zone_1"))
			        	SafeDomoticHomeDB.insertMotionZone1(jSonSer.getIsOpened());
		        }catch(AddressUnreachableException ex)
		        {
		        	// Only putting data into MySQL Database
			        if(topic.equals("zone_0"))
			        	SafeDomoticHomeDB.insertMotionZone0(jSonSer.getIsOpened());
			        if(topic.equals("zone_1"))
			        	SafeDomoticHomeDB.insertMotionZone1(jSonSer.getIsOpened());
		        }
		     
			} else if(topic.equals("temp"))
			{
				Gson gson = new Gson();
				Reader reader = new InputStreamReader(new ByteArrayInputStream(message.getPayload()));
				// Convert JSON File to Java Object
				Thermometer thermometer = gson.fromJson(reader, Thermometer.class);
				tr.setSensorId(thermometer.getSensorId());
				tr.setCurrentTemp(thermometer.getCurrentTempRepresentation());
				// Insert the record into the MySQL Db
				SafeDomoticHomeDB.insertNewTempValue(thermometer.getCurrentTemp());
			} else if(topic.equals("voc"))
			{
				Gson gson = new Gson();
				Reader reader = new InputStreamReader(new ByteArrayInputStream(message.getPayload()));
				// Convert JSON File to Java Object
				Voc voc = gson.fromJson(reader, Voc.class);
				// Checking the threshold
				int smokePercentage = voc.getSmokePercentage();
				try {
					if(smokePercentage < 15) {
						FanHandler.notifyCoapResource(0);
						VocHandler.actOnSensor(0);
					}
					else if(smokePercentage >= 15 && smokePercentage < 20)
					{
						FanHandler.notifyCoapResource(1);
						VocHandler.actOnSensor(1);
					}
					else if(smokePercentage >= 20 && smokePercentage < 25)
					{
						FanHandler.notifyCoapResource(2);
						VocHandler.actOnSensor(2);
					}
					else
					{
						FanHandler.notifyCoapResource(3);
						VocHandler.actOnSensor(3);
					}
					// Putting the data into MySQL DB
					SafeDomoticHomeDB.insertNewSmokeRecord(smokePercentage);
				} catch(AddressUnreachableException aue) {
					// Putting the data into MySQL DB ONLY
					SafeDomoticHomeDB.insertNewSmokeRecord(smokePercentage);
				} 
			}
		}
}
