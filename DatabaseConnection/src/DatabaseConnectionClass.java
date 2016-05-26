import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;


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
		sql = "SELECT lecturer.ID,lecturer.Name "
				+ "FROM lecturer "
				+ "INNER JOIN nfclocation "
				+ "ON lecturer.RoomNumber = nfclocation.RoomNumber "
				+ "WHERE nfclocation.SerialNumber = "+NFCID;
		
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
	/*public String nextAvaliableTime(int lecturerID, int length)
	{
		String nextTime = "";
		int TimeSpecified=0;
		int Length =0;
		int followingTime=0;
		sql = "SELECT TimeSpecified, Length "
				+ "FROM appointment "
				+ "WHERE lecturerID = "+lecturerID;
		try {
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				TimeSpecified = rs.getInt("TimeSpecified");
				Length = rs.getInt("Length");
			}
			if((TimeSpecified+Length) ==1230)
			{
				followingTime = -1;
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		return nextTime;
	}*/
	
	public ArrayList<String> getFutureAppointments(String AndroidID)
	{
		ArrayList<String> futureAppo = new ArrayList<String>();
		sql = "SELECT `ID`,`LecturerID`,`TimeSpecified`,`Length` "
				+ "FROM appointment "
				+ "WHERE StudentID IN ("
				+ "SELECT ID "
				+ "FROM student "
				+ "WHERE ID IN ("
				+ "SELECT StudentID "
				+ "FROM device"
				+ "WHERE device.AndroidID = "+AndroidID+"))";
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
						+ "FROM lecturer "
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
	
	/*public static void main(String[]args)
	{
		DatabaseConnectionClass dbc = new DatabaseConnectionClass();
		
		ArrayList<String>lecturer = dbc.getLecturers("1513546644");
		Iterator itr = lecturer.iterator();
		
		while(itr.hasNext())
		{
			System.out.println(itr.next());
		}
		
	}*/

}
