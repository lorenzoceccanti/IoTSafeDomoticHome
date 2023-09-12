package iot.unipi.it;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseHandler {

	private Connection connect = null;
	private PreparedStatement ps = null;
	
	public DatabaseHandler() {
		try {
			connect = DriverManager.getConnection("jdbc:mysql://localhost/SafeDomoticHome?"
			+"user=root&password=PASSWORD");			
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public void truncateTable()
	{
		try {
			ps = connect.prepareStatement("TRUNCATE TABLE resources;");
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
	
	public void insertResourcesDB(String name, String address) {
		try {
			ps = connect.prepareStatement("INSERT INTO resources(name,address)"
			+ "VALUES(?, ?);");
			ps.setString(1, name);
			ps.setString(2, address);
			ps.executeUpdate();
		}catch(SQLException se)
		{
			System.err.println(se.getMessage());
		}
	}
}
