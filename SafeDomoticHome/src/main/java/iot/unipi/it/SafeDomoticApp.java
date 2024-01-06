package iot.unipi.it;

import java.io.*;
import java.time.*;
import java.time.format.*;

import org.eclipse.californium.core.CaliforniumLogger;

public class SafeDomoticApp {
	// Utility methods
	
	public static String getCurrentTime()
	{
        // Get the current instant in UTC
        Instant currentInstant = Instant.now();
        
        // Convert the instant to a ZonedDateTime in UTC
        ZonedDateTime utcDateTime = currentInstant.atZone(ZoneOffset.UTC);
        
        // Format the UTC time as a string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String utcTimeString = utcDateTime.format(formatter);
        
        return utcTimeString;
	}
	
	public static void showCmd() {
		System.out.println("-------------------------------------------");
		System.out.println("Possible commands:");
		System.out.println("!exit: exits the program");
		System.out.println("!arm z0 z1: arms one or more alarm zone");
		System.out.println("Example: !arm Y N -> arms zone0 excluding zone1");
		System.out.println("!disarm: disarms all the alarm zones");
		System.out.println("!showzones: gets the current status of all zones");
		System.out.println("!temperaturecheck: gets the most updated temperature report");
		System.out.println("!clima ON|OFF [tempDegrees] [fanSpeed]");
		System.out.println("!pressswitch: invert the light status");
		System.out.println("-------------------------------------------");
	}
	
	public static void main(String[] args) throws InterruptedException{
		
		// Disabling logging at stdout by Californium
		CaliforniumLogger.disableLogging();
		
		// Connection with the database
		SafeDomoticHomeDB.connect();
		
		AlarmHandler mcc = new AlarmHandler();
		ConditionerHandler ch = new ConditionerHandler();
		ThermometerReader tr = new ThermometerReader();
		LightSwitch ls = new LightSwitch();
		
				
		MqttCollector mqttColl = new MqttCollector(mcc, tr);
		SafeDomoticHomeDB.setTimezone();
		System.out.println("***SafeDomoticHome***");
		showCmd();
		
		try {
			FanHandler.notifyCoapResource(0);
		}catch(AddressUnreachableException ex)
		{
			System.out.println("[WRN]: Probably the fan actuator is not working");
		}
		
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String command = "";
		String[] tokens;
		while(true) {
			
			// Always loop for temperature reading
			try {
				command = reader.readLine();
				tokens = command.split(" ");
				
				if(tokens[0].equals("!exit"))
				{
					// The fan will not be on anymore
					try {
						FanHandler.notifyCoapResource(0);
						VocHandler.actOnSensor(0);
						System.exit(1);
					}catch(AddressUnreachableException ex)
					{
						System.exit(1);
					}
				}
				else if(tokens[0].equals("!showzones")) {
					System.out.println("Magnet Sensors Status at " + getCurrentTime());
					System.out.println("Zone #0: " + new String((mcc.getZone0Status()==0)?"closed":"opened"));
					System.out.println("Zone #1: " + new String((mcc.getZone1Status()==0)?"closed":"opened"));
				} else if(tokens[0].equals("!arm")) // Alarm section
				{
					if(mcc.isAZoneAlreadyArmed())
					{
						System.out.println("First disarm ALL the zones!");
						showCmd();
						continue;
					}
					try {
						/* Consider tokens[1] -> zone0
						 * tokens[2] -> zone1
						 */
						if(tokens[1].equals("Y"))
						{
							System.out.print("Zone #0: armed |");
							mcc.armZone0();
						} else if(tokens[1].equals("N"))
							System.out.print("Zone #0: exclusion |");
						else
							System.out.print("Zone #0: syntax error |");
						
						if(tokens[2].equals("Y"))
						{
							System.out.print("Zone #1: armed |");
							mcc.armZone1();
						} else if(tokens[2].equals("N"))
							System.out.print("Zone #1: exclusion |");
						else
							System.out.print("Zone #1: syntax error |");
						// Retriving the exclusion statuses
						boolean cmd[] = mcc.getArming();
						SafeDomoticHomeDB.insertCmdLogRecord(cmd);
						System.out.println("");
					}catch(ArrayIndexOutOfBoundsException outEx)
					{
						System.out.println("Syntax error!");
					}
				} else if(tokens[0].equals("!disarm"))
				{
					mcc.disarm();
					boolean cmd[] = {false, false};
					SafeDomoticHomeDB.insertCmdLogRecord(cmd);
					System.out.println("All zones disarmed");
				} else if(tokens[0].equals("!clima")) // Air conditioning system section
				{
					try {
						// tokens[1] is ON/OFF
						// tokens[2] is tempDegrees
						// tokens[3] is fanSpeed
						// tokens[2] and tokens[3] aren't there when token[1] is OFF
						if(tokens[1].equals("OFF"))
						{
							// OF is stored as command and as JSON to make the lenght the same
							ConditionerRemote cr = new ConditionerRemote(tokens[1].substring(0, 2), "-1", "-1");
							ch.actOnConditioner(cr, tr.getLastTemp());
						}else if(tokens[1].equals("ON"))
						{
							ConditionerRemote cr = new ConditionerRemote(tokens[1], tokens[2], tokens[3]);
							ch.actOnConditioner(cr, tr.getLastTemp());
						}
						else
							System.out.println("[Conditioner]: syntax error");
					}catch(ArrayIndexOutOfBoundsException outEx)
					{
						System.out.println("Syntax error!");
					}
					catch(Exception ex)
					{
						System.out.println("Command unavaiable (probabily the temperature has not sensed yet). Retry..");
					}
				}
				else if(tokens[0].equals("!temperaturecheck"))
				{
					try {
						System.out.println("Current temperature: "+tr.getLastTemp()/10.0);
					}catch(Exception e)
					{
						System.out.println("Command unavailable: the temperature sensor has not been installed yet");
					}
				}
				else if(tokens[0].equals("!pressswitch"))
				{
					ls.press();
				}
				else 
					System.out.println("Command not found! Retry..");
			}catch(AddressUnreachableException ex)
			{
				System.out.println("The selected service is currently unreachable. Try later.");
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
				showCmd();
		}
	}

}
