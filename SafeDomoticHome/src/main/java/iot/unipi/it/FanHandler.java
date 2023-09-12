package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;

public class FanHandler {
	
	public static void notifyCoapResource(int fanSpeed) throws AddressUnreachableException
	{
		String addr = SafeDomoticHomeDB.staticCoapDiscovery("fan");
		CoapClient coapClient = new CoapClient("coap://["+addr+"]/fan");
		coapClient.setTimeout(500);
		// Sending the request
		CoapResponse respPut = coapClient.put("{\"fanSpeed\":"+fanSpeed+"}", MediaTypeRegistry.APPLICATION_JSON); 
		if(respPut == null)
			throw new AddressUnreachableException();
	}
}
