package iot.unipi.it;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SafeDomoticHomeDB {
	private static Connection conn = null;
	
	public static int convertToInt(boolean v)
	{
		if(v == true)
			return 1;
		else
			return 0;
	}
	public static void connect()
	{
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/SafeDomoticHome?serverTimezone=UTC&"
			+"user=root&password=PASSWORD");			
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public static String staticCoapDiscovery(String resName)
	{
		String result = null;
		try {
			PreparedStatement ps = 
			conn.prepareStatement("SELECT address FROM resources WHERE name = ?");
			ps.setString(1, resName);
			ResultSet rs = ps.executeQuery();
			// We can do this because we are sure that a resource has only 1 address in the DB
			rs.next(); // Moving the cursor by only one position
			result = rs.getString("address");
		}catch(SQLException se) {
			System.err.println(se.getMessage());
		}
		return result;
	}
	
	public static void insertNewTempValue(String degrees) {
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO thermometer(timestamp,celsiusDegree)"
			+ "VALUES(CURRENT_TIMESTAMP, ?);");
			ps.setString(1, degrees);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public static void insertMotionZone0(int sts)
	{
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO motionz0(timestamp,isMagnetOn)"
			+ "VALUES(CURRENT_TIMESTAMP, ?);");
			ps.setInt(1, sts);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public static void insertMotionZone1(int sts)
	{
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO motionz1(timestamp,isMagnetOn)"
			+ "VALUES(CURRENT_TIMESTAMP, ?);");
			ps.setInt(1, sts);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public static void insertCmdLogRecord(boolean[] cmd) {
		
		int cmd0 = convertToInt(cmd[0]);
		int cmd1 = convertToInt(cmd[1]);
		
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO alarmCmdLog(timestamp,zone0,zone1)"
			+ "VALUES(CURRENT_TIMESTAMP, ?, ?);");
			ps.setInt(1, cmd0);
			ps.setInt(2, cmd1);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public static void insertNewAlarmRecord(String zone) {
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO alarmList(timestamp,zone)"
			+ "VALUES(CURRENT_TIMESTAMP, ?);");
			ps.setString(1, zone);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public static void insertNewSmokeRecord(int percentage) {
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO vocMeter(timestamp,smokePercentage)"
			+ "VALUES(CURRENT_TIMESTAMP, ?);");
			ps.setInt(1, percentage);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}


}
