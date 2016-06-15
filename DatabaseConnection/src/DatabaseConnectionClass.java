import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;


public class DatabaseConnectionClass {
	
	private Connection conn = null;
	private ResultSet rs = null;
	private Statement stmt = null;
	private String sql = "";
	
	public DatabaseConnectionClass()
	{
		try
		{
			conn = DriverManager.getConnection("jdbc:mysql://localhost/","root","");
			stmt = conn.createStatement();
			stmt.execute("USE it4b");
		}
		catch(SQLException ex)
		{
			System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public ArrayList<String> getLecturers(String NFCID)
	{
		ArrayList<String>lecturers = new ArrayList<String>();
		sql = "SELECT Lecturer.ID,Lecturer.Name "
				+ "FROM Lecturer "
				+ "INNER JOIN NFCLocation "
				+ "ON Lecturer.RoomNumber = NFCLocation.RoomNumber "
				+ "WHERE NFCLocation.SerialNumber = "+NFCID;
		
		try {
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				lecturers.add( rs.getInt("ID")+"/"+rs.getString("Name"));
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		return lecturers;
	}
	public String nextAvaliableTime(int lecturerID, int length)
	{
		String nextTime = "";
		long TimeSpecified=0;
		int Length =0;
		long followingTime=0;
		long seconds = 0;
		boolean first = false;
		Date date = null;
		Calendar c = Calendar.getInstance();
		sql = "SELECT TimeSpecified, Length "
				+ "FROM Appointment "
				+ "WHERE LecturerID = "+lecturerID;
		
		try {
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				TimeSpecified =rs.getInt("TimeSpecified");
				Length = rs.getInt("Length");
			}
			date = Date.from( Instant.ofEpochSecond( TimeSpecified+Length ) );
			c.setTime(date);
			if(c.getTimeInMillis()==0)
			{
				date = Date.from(Instant.ofEpochSecond(System.currentTimeMillis()/1000));
				c.setTime(date);
				first = true;
			}
			if(c.get(Calendar.HOUR_OF_DAY)>=13&&(c.get(Calendar.MINUTE)>=15||length>=15))
			{
				if(c.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY)
				{
					c.add(Calendar.DAY_OF_YEAR, 3);
				}
				else
				{
					c.add(Calendar.DAY_OF_YEAR, 1);
				}
				c.set(Calendar.HOUR_OF_DAY,14);
				c.set(Calendar.MINUTE, 30);
				seconds = c.getTimeInMillis()/1000;
				nextTime = seconds+"/"+(seconds+length);
			}
			else if(first==false)
			{
				followingTime = TimeSpecified+Length;
				
				nextTime = followingTime+"/"+(followingTime+length);
			}
			else if(first ==true)
			{
				c.set(Calendar.HOUR_OF_DAY, 12);
				c.set(Calendar.MINUTE, 30);
				seconds = c.getTimeInMillis()/1000;
				nextTime = seconds+"/"+(seconds+length);
				System.out.println("Last IF");
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		return nextTime;
	}
	
	public boolean makeAppointment(int LecturerID, String AndroidID, String StartEnd)
	{
		boolean check = true;
		sql = "SELECT ID FROM Device WHERE AndroidID ="+AndroidID;
		try {
			rs = stmt.executeQuery(sql);
			String[] times = StartEnd.split("/");
			int length = Integer.parseInt(times[1])-Integer.parseInt(times[0]);
			sql = "INSERT INTO Appointment "
					+ "VALUES ("+rs.getInt("ID")+","+LecturerID+","+System.currentTimeMillis()/1000+","+times[0]+","+length+")";
			try {
				rs = stmt.executeQuery(sql);
			} catch (SQLException e) {
				System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
			    check = false;
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		    check = false;
		}
		return check;
	}
	
	public ArrayList<String> getFutureAppointments(String AndroidID)
	{
		ArrayList<String> futureAppo = new ArrayList<String>();
		sql = "SELECT `ID`,`LecturerID`,`TimeSpecified`,`Length` "
				+ "FROM Appointment "
				+ "WHERE StudentID IN ("
				+ "SELECT ID "
				+ "FROM Student "
				+ "WHERE ID IN ("
				+ "SELECT StudentID "
				+ "FROM Device"
				+ "WHERE Device.AndroidID = "+AndroidID+"))";
		try {
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				ResultSet inrs = null;
				Statement instmt = null;
				String lectName = "";
				int startTime = 0;
				int endTime = 0;
				
				int appID = rs.getInt("ID");
				sql = "SELECT `Name` "
						+ "FROM Lecturer "
						+ "WHERE ID = "+appID;
				inrs = instmt.executeQuery(sql);
				
				lectName = inrs.getString("Name");
				startTime = rs.getInt("TimeSpecified");
				endTime = startTime+ rs.getInt("Length");
				
				futureAppo.add(appID +"/"+lectName+"/"+startTime+"/"+endTime);
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		return futureAppo;
	}
	
	public void NFCTagAdd(String ID, String Room)
	{
		sql = "INSERT INTO NFCLocation "
				+ "VALUES ("+ID+","+Room+")";
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
	}
	public boolean CheckStudent(String StudentName)
	{
		//True is added successfully, False is previously added
		Random rd = new Random();
		boolean check =false;
		String name = "";
		sql = "SELECT Name "
				+ "FROM Student "
				+ "WHERE Name = '"+StudentName+"'";
		try {
			rs = stmt.executeQuery(sql);
			check = false;
			
		} catch (SQLException e) {
			sql = "INSERT INTO Student "
					+ "VALUES ("+StudentName+","+rd.nextInt(1000000)+")";
			try {
				rs = stmt.executeQuery(sql);
				check = true;
			} catch (SQLException er) {
				System.out.println("SQLException: " + er.getMessage());
			    System.out.println("SQLState: " + er.getSQLState());
			    System.out.println("VendorError: " + er.getErrorCode());
			}
		}
		return check;
	}
	public boolean AddDivToStudent(String DeviceID, int ActiveCode)
	{
		boolean check = true;
		
		sql = "SELECT ID "
				+ "FROM Student "
				+ "WHERE ActivationCode ="+ActiveCode;
		try {
			rs = stmt.executeQuery(sql);
			check = true;
			int id = rs.getInt("ID");
			sql = "INSERT INTO Device"
					+ "VALUES ("+id+","+DeviceID+")";
			try{
				rs = stmt.executeQuery(sql);
				sql = "UPDATE Student "
						+ "SET ActivationCode = 0 "
						+ "WHERE ID = "+id;
				try {
					rs = stmt.executeQuery(sql);
					check = true;
				} catch (SQLException er) {
					check = false;
					System.out.println("SQLException: " + er.getMessage());
				    System.out.println("SQLState: " + er.getSQLState());
				    System.out.println("VendorError: " + er.getErrorCode());
				}
				
			} catch (SQLException e){
				check = false;
				System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
			}
		} catch (SQLException er) {
			check = false;
			System.out.println("SQLException: " + er.getMessage());
		    System.out.println("SQLState: " + er.getSQLState());
		    System.out.println("VendorError: " + er.getErrorCode());
		}
		
		return check;
	}
	public boolean cancelAppointment(int AppointmentID)
	{
		boolean check = true;
		sql = "DELETE FROM Appointment WHERE ID="+AppointmentID;
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		    check = false;
		}
		return check;
	}
	/*public static void main(String[]args)
	{
		DatabaseConnectionClass dbc = new DatabaseConnectionClass();
		
		int seconds = 15*60;
		System.out.println(dbc.nextAvaliableTime(1, seconds));
	}*/

}
