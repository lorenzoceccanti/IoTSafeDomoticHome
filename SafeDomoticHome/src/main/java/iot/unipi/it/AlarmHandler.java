package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;

public class AlarmHandler{

	/* Alarm magnets state */
	private int zone0Status;
	private int zone1Status;
	
	/* Alarm arming */
	private boolean zone0Armed;
	private boolean zone1Armed;
		
	/* Alarm triggered */
	private boolean zone0Triggered;
	private boolean zone1Triggered;
	
	
	public void notifyCoAPSiren(String zone, String mode) throws AddressUnreachableException
	{
		// Query the database to know the address
		String addr = SafeDomoticHomeDB.staticCoapDiscovery("siren");
		// Here we suppose to deploy the siren actuator as node n.4 in Cooja
		CoapClient coapClient = new CoapClient("coap://["+addr+"]/siren");
		coapClient.setTimeout(500);
		Request req = new Request(Code.PUT);
		req.setPayload("zone="+zone+"&mode="+mode);
		
		// Sending the request
		CoapResponse resp = coapClient.advanced(req);
		if(resp == null)
			throw new AddressUnreachableException();
	}
	
	public int getZone0Status() { 
		return this.zone0Status;
	}
	public int getZone1Status() {
		return this.zone1Status;
	}
	
	public void setZoneStatus(String zone, int zoneStatus)
	{
		if(zone.equals("zone_0"))
			this.zone0Status = zoneStatus;
		if(zone.equals("zone_1"))
			this.zone1Status = zoneStatus;
	}
	
	public void ringTheAlarm() throws AddressUnreachableException{

        // Putting alarm/movements to the database
        
        // Check if the alarm is armed and the arriving message is relative to a movement
        if(zone0Armed && this.zone0Status == 1)
        {
        	// If the zone not triggered yet, publish
        	if(!zone0Triggered)
        	{
        		notifyCoAPSiren("alarm_0", "ON");
        		zone0Triggered = true;
        		SafeDomoticHomeDB.insertNewAlarmRecord("Zone 0");
        	}
        }
        if(zone1Armed && this.zone1Status == 1)
        {
        	// If the zone not triggered yet, publish
        	if(!zone1Triggered)
        	{
        		notifyCoAPSiren("alarm_1", "ON");
        		zone1Triggered = true;
        		SafeDomoticHomeDB.insertNewAlarmRecord("Zone 1");
        	}
        }
	}
	
	public boolean isAZoneAlreadyArmed() {
		if(zone0Armed || zone1Armed)
			return true;
		else
			return false;
	}
	
	public void armZone0() {
		this.zone0Armed = true;
	}
	
	public void armZone1() {
		this.zone1Armed = true;
	}
	
	public boolean[] getArming()
	{
		boolean[] res = new boolean[2];
		res[0] = this.zone0Armed;
		res[1] = this.zone1Armed;
		return res;
	}
	
	public void disarm(){
		
		// Shut up the alarm
		try {
			notifyCoAPSiren("alarm_0", "OFF");
			this.zone0Triggered = false;
			notifyCoAPSiren("alarm_1", "OFF");
			this.zone1Triggered = false;
			
			// In any case you need to disarm all the zones
			this.zone0Armed = false;
			this.zone1Armed = false;
		}catch(AddressUnreachableException ex)
		{
			System.out.println("(WRN: Siren unreachable)");
		}
		
	}
	
}
