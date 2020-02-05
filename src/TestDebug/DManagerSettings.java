package TestDebug;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import DataManagement.AbstractMessage;
import DataManagement.DataManager;
import DataManagement.FileMessage;
import DataManagement.ImageMessage;
import DataManagement.TextMessage;
import UserInfo.UserAccount;

public class DManagerSettings {
	public static void main(String[] args) {

		//DataManager dm = new DataManager();
		
		System.out.println(DataManager.dateToString(new Date()));
		String sDriverName = "org.sqlite.JDBC";
	    try {
			Class.forName(sDriverName);
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	
	    // now we set up a set of fairly basic string variables to use in the body of the code proper
	    String sTempDb = "dearFriend.db";
	    String sJdbc = "jdbc:sqlite";
	    String sDbUrl = sJdbc + ":" +"/home/ejigu/.dearFriend/"+ sTempDb;
	    Connection dbConnection = null;
		try {
			dbConnection = DriverManager.getConnection(sDbUrl);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    try { 
	    	
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	          /*  stmt.setQueryTimeout(10);//4 sec timeout for creating a table
	    	    String sMakeTable = "CREATE TABLE IF NOT EXISTS  messages (senderId text, receiverId text, date text,type integer, content text )";
	            stmt.executeUpdate( sMakeTable );
	    	    sMakeTable = "CREATE TABLE IF NOT EXISTS  users (userId text, hashedPassword text, registerDate text )";
	            stmt.executeUpdate( sMakeTable );
*/
	            //stmt.executeUpdate("DELETE FROM users");
	            //stmt.executeUpdate("DELETE FROM messages");
	         /*   
	            stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'Root1','root2','17-01-2020 20:09:01',0,'hi root2')");
	            stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root2','root1','17-01-2020 20:09:02',0,'hi root1')");	            
	            stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root1','root2','17-01-2020 20:09:03',0,'how r u')");
	            stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root2','root1','17-01-2020 20:09:04',0,'fine & u')");


	            stmt.executeUpdate("INSERT INTO users (userId,hashedPassword,registerDate) VALUES( 'root2','dwp','17-01-2020 20:09:01')");
	            stmt.executeUpdate("INSERT INTO users (userId,hashedPassword,registerDate) VALUES( 'root1','pwd','17-01-2020 20:09:01')");
*/
	            //displayMessages(stmt);
	            displayUsers(stmt);

	        }
	        catch (Exception e)
        	{
        		System.out.println ("Statement Error"+ e.toString());
        		e.printStackTrace();
        	}
	        finally {
	        	try { stmt.close(); } catch (Exception ignore){
	        	}
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Database error: "+ e.toString());
	    }
	    
		
	}
	
	
	private static void displayMessages(Statement stmt) throws SQLException {
		ResultSet rs = null ;
		String getMessagesStmt = "SELECT * FROM messages";
	    rs=stmt.executeQuery( getMessagesStmt );
	    try {
			ArrayList<AbstractMessage> messagesList = new ArrayList<>();
			
			
    	    while (rs.next()) {
    	    	System.out.println(rs.getString("date"));
    	    	Date dt = DataManager.stringToDate(rs.getString("date"));
    	    	switch(rs.getInt("type"))
    	    	{
    	    	case 0:// Text
    	    		messagesList.add(new TextMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));
    	    		break;
    	    	case 1:// Image
    	    		String filePath=DataManager.getDfDir().concat(DataManager.getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(DataManager.getFileSep()).concat(rs.getString("content")));
    	    		  byte[] fileContent;
					try {
			
						messagesList.add(new ImageMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	    		
    	    		break;
    	    	case 2:// File
    	    		String filePath1=DataManager.getDfDir().concat(DataManager.getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(DataManager.getFileSep()).concat(rs.getString("content")));
    	    		  byte[] fileContent1;
					try {
			
						messagesList.add(new FileMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	    		
    	    		break; 
    	    	default: 
    	    	}

    	    }
    	    
    	    System.out.println("Size:" + messagesList.size());
	    	for(AbstractMessage abs: messagesList) {
				System.out.println(abs.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void displayUsers(Statement stmt) throws SQLException {
		ResultSet rs = null ;
		String getMessagesStmt = "SELECT * FROM users";
	    rs=stmt.executeQuery( getMessagesStmt );
	    try {
			ArrayList<UserAccount> res = new ArrayList<>();
			while (rs.next())
			{		    	

				UserAccount cu = new UserAccount();
				cu.setHashedPassword(rs.getString("hashedPassword"));
				cu.setRegisterDate(rs.getString("registerDate"));
				cu.setUserid(rs.getString("userId"));
				res.add(cu);
			}
			
			
			Integer i=0;
			for(UserAccount cus: res) {
				System.out.println("User" + ++i);
				System.out.println("UID: " + cus.getUserid());
				System.out.println("Pwd: " + cus.getHashedPassword());
				System.out.println("Date: " + cus.getRegisterDate() + "\n----------");
			}


			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
