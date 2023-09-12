package iot.unipi.it;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

import com.google.gson.Gson;

public class LightSwitch {

	public void press() throws AddressUnreachableException{
		
		// Retrieve the address from the DB first
		String addr = SafeDomoticHomeDB.staticCoapDiscovery("light");
		CoapClient coapClient = new CoapClient("coap://["+addr+"]/light");
		coapClient.setTimeout(1500); // time is expressed in milliseconds
		
		CoapResponse respGet = coapClient.get(MediaTypeRegistry.APPLICATION_JSON);
		if(respGet == null || respGet.getCode() == ResponseCode.NOT_FOUND)
			throw new AddressUnreachableException();
		
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(new ByteArrayInputStream(respGet.getPayload()));
		Light l = gson.fromJson(reader, Light.class);
		int cmd = -1;
		cmd = (l.getBulbStatus() == 0) ? 1:0;
		
		// Issuing a CoAP PUT to press the switch button
		CoapResponse resp = coapClient.put("{\"command\":"+(cmd)+"}", MediaTypeRegistry.APPLICATION_JSON);
		System.out.println(resp.getResponseText());
	}
}
