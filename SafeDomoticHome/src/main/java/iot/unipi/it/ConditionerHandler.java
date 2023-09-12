package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

public class ConditionerHandler {

	private ConditionerRemote cr;
	private int cooling = 0;
	private int warming = 0;
	
	public void determineConditionerMode(int lastTemp)
	{
		if(!(cr.getPower().equals("ON")))
			return;
		int selTemp = cr.getSelTemp();
		if(selTemp - lastTemp < 0)
		{
			cooling = 1;
			warming = 0;
		}else if(selTemp - lastTemp > 0)
		{
			cooling = 0;
			warming = 1;
		} else {
			cooling = 0;
			warming = 0;
		}
	}
	
	public void sendLedCmd()
	{
		// Retrieve the address from the DB first
		String addr = SafeDomoticHomeDB.staticCoapDiscovery("conditioner");
		CoapClient coapClient = new CoapClient("coap://["+addr+"]/clima");
		
		String payload = new String("{\"warming\":"+warming+",\"cooling\":"+cooling+"}");
		// Sending the request
		CoapResponse respPost = coapClient.post(payload, MediaTypeRegistry.APPLICATION_JSON);
	}
	
	public void actOnConditioner(ConditionerRemote cmd, int lastTemp)
	{
		cr = new ConditionerRemote(cmd.getPower(), cmd.getSelTemp(), cmd.getFanSpeed());
		determineConditionerMode(lastTemp);
		Gson gson = new Gson();
		String payload = new String(gson.toJson(cr));
		
		// Retrieve the address from the DB first
		String addr = SafeDomoticHomeDB.staticCoapDiscovery("conditioner");
		CoapClient coapClient = new CoapClient("coap://["+addr+"]/clima");
		coapClient.setTimeout(500);
		// Sending the request
		CoapResponse respPut = coapClient.put(payload, MediaTypeRegistry.APPLICATION_JSON);
		String responsePayload = respPut.getResponseText();
		
		System.out.println(responsePayload);
		// If the CoAP request is successful
		if(respPut.isSuccess())
		{
			if(cmd.getPower().equals("ON"))
				sendLedCmd();
			// Starting an MQTT interaction with the sensor emulating the thermometer
			String topic = "conditioner";
			// Sending the payload
			String brokerIP = "tcp://127.0.0.1:1883";
			String clientId = "JavaApp2";
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
	
	
}
