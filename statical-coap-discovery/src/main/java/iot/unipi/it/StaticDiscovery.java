package iot.unipi.it;

import java.util.Scanner;

public class StaticDiscovery {

	public static String convertToAddress(String str)
	{
		String tokens[] = null;
		tokens = str.split(":");
		
		String result = new String("fd00::f6ce:36"+tokens[1]+":"+tokens[5]+tokens[4]+":"+tokens[3]+tokens[2]);
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("**** STATIC COAP DISCOVERY PROCEDURE ****");
		System.out.println("PUT the S/N in **lower** case");
		System.out.println("Mote S/N for CoAP-conditioner-light server:");
		Scanner sc = new Scanner(System.in);
		String snConditionerLight = new String(sc.nextLine());
		System.out.println("Mote S/N for CoAP-siren-fan server:");
		String snSiren = new String(sc.nextLine());
		sc.close();
			
		String addressConditionerLight = convertToAddress(snConditionerLight);
		String addressSiren = convertToAddress(snSiren);
		
		DatabaseHandler dh = new DatabaseHandler();
		dh.truncateTable();
		dh.insertResourcesDB("conditioner", addressConditionerLight);
		dh.insertResourcesDB("light", addressConditionerLight);
		dh.insertResourcesDB("siren", addressSiren);
		dh.insertResourcesDB("fan", addressSiren);
		System.out.println("Changes saved");
	}

}
