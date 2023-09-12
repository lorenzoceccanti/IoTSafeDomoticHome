package iot.unipi.it;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class VocHandler {
	
	public static void actOnSensor(int speed)
	{
		// Starting an MQTT interaction with the sensor emulating the thermometer
		String topic = "fan";
		// Sending the payload
		String brokerIP = "tcp://127.0.0.1:1883";
		String clientId = "JavaApp3";
		String payload = "{\"selSpeed\":"+speed+"}";
		try {
				MqttClient mqttClient = new MqttClient(brokerIP, clientId);
				mqttClient.connect();
						
				MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
				mqttClient.publish(topic, mqttMessage);
				mqttClient.disconnect();
		}catch(MqttException me)
		{
				me.printStackTrace();
		}

	}
}


