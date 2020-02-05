package DataManagement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import Network.NAPacket;
import Tools.LogSystem;
import UserInfo.UserAccount;

import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;



public class DataManager {
	//private DynamicDataManager dynamicDM;
	
	private static Connection dbConnection; //database connection
	
	private String pwdFile;
	private static String dfDir; //dearFriend Directory path (/.../dearFriend)
	private static String fileSep; //the file separator of the current os
	
	
	public static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	
	//Timers periods
	private static long updateStatus = 7000; //in ms
	private static long updateDBSync = 7000; //in ms
	private static long updateOnlineUserList = 20000; //in ms	
	
	private static long sleepTimePseudo = 3000; // (in ms) Represent the time waiting by the system before a connected user can deny the challenging pseudo



	public DataManager() {
		 
		/* Retrieve:
		 * hashedPawd of the user
		 * 
		 */
		
		
		DataManager.setUpEnv();
		
		try 
		{
			initDatabase(); 	
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
	
		
			
	}
	
	//Static methods
	
	//Date
	public static String dateToString(Date date) {
		SimpleDateFormat df = new SimpleDateFormat(DataManager.dateFormat);
		return df.format(date);
	}
	
	public static Date stringToDate(String date) throws ParseException {
		SimpleDateFormat formatter=new SimpleDateFormat(DataManager.dateFormat);  
	    Date dt = formatter.parse(date);
	    return dt;
	}
	
	//Normals methods
	
	//Connection methods
	private Connection getDbConnection() {
		return dbConnection;
	}

	private void setDbConnection(Connection dbConnection) {
		DataManager.dbConnection = dbConnection;
	}
	
	public static long getUpdateStatusPeriod() {
		return updateStatus;
	}
	
	public static long getUpdateDBSyncPeriod() {
		return updateDBSync;
	}
	
	
	//Times parameter config methods
	/**
	 * Return the time (in millisec) before the system will check the online list and remove inactive user
	 * @return long
	 */
	public static long getUpdateOnlineUserList() {
		return updateOnlineUserList;
	}
	
	public static long getSleepTimePseudo() {
		return sleepTimePseudo;
	}
	


	//Filesystem parameters methods
	protected String getPwdFile() {
		return pwdFile;
	}


	public static String getDfDir() {
		return dfDir;
	}


	public static String getFileSep() {
		return fileSep;
	}

	
	
	//Environments methods
	public static boolean envExist() throws Exception {
		if(DataManager.dfDir == null) {
			throw new Exception();
		}
		
		File dfDir = new File(DataManager.dfDir);
		return dfDir.exists();
	}
	
	
	/*
	 * Check if the folder that holds all files related to the application exist and create it if not
	 */
	public static void setUpEnv() {
		//check for a config file where many 
		String osName = System.getProperty("os.name").toLowerCase();
		String osSep = System.getProperty("file.separator");
		String userHome = System.getProperty("user.home");
		//System.out.println(userHome);
		//String userName = System.getProperty("user.name");
	
		/*
		System.out.println("sep: " + osSep);
		System.out.println("name: " + osName);
        System.out.println("home: " + userHome);
        System.out.println("username: " + userName);
        */
		
		if(osName.indexOf("win") >= 0) //windows
		{
			//TODO
		}
		else if(osName.indexOf("mac") >= 0) 
		{
			//TODO
		}
		else if(osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0)
		{
			String dfFiles = userHome + osSep + ".dearFriendServer";
			DataManager.fileSep = osSep;
			DataManager.dfDir = dfFiles;
			File dfDir = new File(dfFiles);
			
			if(!dfDir.exists()) {
				//It is the first time the soft is launched in this machine
				//Create the folder
				
				if(dfDir.mkdir()) {// Exception
					try{
						String iniPath = dfFiles + osSep + "appConfig.ini";
						File dfIniFile =  new File(iniPath);
						dfIniFile.createNewFile();
						
					
						
						
					   /* Read properties on ini file
					    * 
					    * Properties p = new Properties();
					    p.load(new FileInputStream(dfFiles+osSep+"appConfig.ini"));
					    System.out.println("user = " + p.getProperty("DBuser"));
					    System.out.println("password = " + p.getProperty("DBpassword"));
					    System.out.println("location = " + p.getProperty("DBlocation"));
					    p.list(System.out);*/
					}
					catch (Exception e) {
					      System.out.println(e);
					}
				}
			}
			else {
				//load config 
			}

		}
		else {
			//throws an exception of os incompatible that will be handle in the Controller
		 
		}

	}

	public void initDatabase() throws Exception
	{
		String sDriverName = "org.sqlite.JDBC";
	    Class.forName(sDriverName);
	
	    // now we set up a set of fairly basic string variables to use in the body of the code proper
	    String sTempDb = "dearFriend.db";
	    String sJdbc = "jdbc:sqlite";
	    String sDbUrl = sJdbc + ":" +dfDir+fileSep+ sTempDb;
	    setDbConnection(DriverManager.getConnection(sDbUrl));
	    try { 
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	            stmt.setQueryTimeout(10);//4 sec timeout for creating a table
	    	    String sMakeTable = "CREATE TABLE IF NOT EXISTS  messages (senderId text, receiverId text, date text,type integer, content text )";
	            stmt.executeUpdate( sMakeTable );
	    	    sMakeTable = "CREATE TABLE IF NOT EXISTS users (userId text, hashedPassword text, registerDate text )";
	            stmt.executeUpdate( sMakeTable );
	           
	            
	            //stmt.executeUpdate("DELETE FROM users");
	            //stmt.executeUpdate("DELETE FROM messages");
	            
	            //stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root1','root2','17-01-2020 20:09:01',0,'hi root2')");
	            //stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root2','root1','17-01-2020 20:09:02',0,'hi root1')");	            
	            //stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root1','root2','17-01-2020 20:09:03',0,'how r u')");
	            //stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'root2','root1','17-01-2020 20:09:04',0,'fine & u')");
	            
	            //stmt.executeUpdate("INSERT INTO users (userId,hashedPassword,registerDate) VALUES( 'toor1','pwd','19-01-2020 20:19:43')");

	            //stmt.executeUpdate("INSERT INTO users (userId,hashedPassword,registerDate) VALUES( 'root2','dwp','17-01-2020 20:09:01')");
	            //stmt.executeUpdate("INSERT INTO users (userId,hashedPassword,registerDate) VALUES( 'root1','pwd','17-01-2020 20:09:01')");


	           //stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES( 'toor1','toor2','17-01-2020 20:09:01',0,'hi')");
	            
	        }
	        catch (Exception e)
        	{
        		System.out.println ("Statement Error"+ e.toString());
        	}
	        finally {
	        	try { stmt.close(); } catch (Exception ignore){
	        	}
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Database error: "+ e.toString());
	    }

	}
	
	private void closeDbConnection() 
	{
    	try {
			getDbConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

	}

	
	//Database queries methods
	
	/*Message types
	 * 	0: text, 1: Image, 2, file
	 */
	public static ArrayList<AbstractMessage>  getMessages(String interlocuterId)
	{
		synchronized(dbConnection){
			ArrayList < AbstractMessage > messagesList=new ArrayList<AbstractMessage>();
			 try { 
			    	
			        Statement stmt = dbConnection.createStatement();
			        try {
			            stmt.setQueryTimeout(4);//4 sec timeout for retrieving messages
			    	    String getMessagesStmt = "SELECT * FROM messages WHERE senderId='".concat(interlocuterId).concat("' UNION ").concat("SELECT * FROM messages WHERE receiverId='").concat(interlocuterId).concat("'");
			    	    ResultSet rs =stmt.executeQuery( getMessagesStmt );
			    	    //Parsing date to correct type  	    

			    	    Date dt = DataManager.stringToDate(rs.getString("date"));
			    	    while (rs.next()) {
			    	    	
			    	    	
			    	    	switch(rs.getInt("type"))
			    	    	{
			    	    	case 0:
			    	    		messagesList.add(new TextMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));
			    	    		break;
			    	    	case 1:
			    	    		String filePath=DataManager.getDfDir().concat(getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(getFileSep()).concat(rs.getString("content")));
			    	    		messagesList.add(new ImageMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));

			    	    		break;
			    	    	case 2:
			    	    		String filePath1=DataManager.getDfDir().concat(getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(getFileSep()).concat(rs.getString("content")));
			 
			    	    		messagesList.add(new FileMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));

			    	    		break; 
			    	    	default: 
			    	    	}
			    	    	/*
			    	    	 
			                System.out.println(rs.getInt("receiverId") +  "\t" + rs.getInt("receiverId") +  "\t" + 
			                                   rs.getString("type") + "\t" +rs.getString("content") + "\t" +
			                                   rs.getString("date"));
			               
			                */
			            }
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
			    	System.out.println("Get Messages Database error: "+ e.toString());
			    }
			 return messagesList;
		}
		
		
			
	}
	public  static ArrayList<AbstractMessage>  getMessagesBetweenTwo(String uid, String interlocuterId)
	{
		synchronized(dbConnection) {
			ArrayList < AbstractMessage > messagesList=new ArrayList<AbstractMessage>();
			 try { 
			    	
			        Statement stmt = dbConnection.createStatement();
			        try {
			            stmt.setQueryTimeout(4);//4 sec timeout for retrieving messages
			    	    String getMessagesStmt = "SELECT * FROM messages WHERE senderId='".concat(interlocuterId).concat("'AND receiverId ='").concat(uid).concat("' UNION ").concat("SELECT * FROM messages WHERE receiverId='").concat(interlocuterId).concat("' AND senderId ='").concat(uid).concat("'");
			    	    ResultSet rs =stmt.executeQuery( getMessagesStmt );
			    	    //Parsing date to correct type  	    

			    	    
			    	    while (rs.next()) {
			    	    	
			    	    	Date dt = DataManager.stringToDate(rs.getString("date"));
			    	    	String filePath;
			    	    	switch(rs.getInt("type"))
			    	    	{
			    	    	case 0:
			    	    		messagesList.add(new TextMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,rs.getString("content")));
			    	    		break;
			    	    	case 1:
			    	    		filePath=DataManager.getDfDir().concat(getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(getFileSep()).concat(rs.getString("content")));
			    	    		messagesList.add(new ImageMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,filePath));

			    	    		break;
			    	    	case 2:
			    	    		filePath=DataManager.getDfDir().concat(getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(getFileSep()).concat(rs.getString("content")));
			    	    		messagesList.add(new FileMessage(rs.getString("senderId"),rs.getString("receiverId"),dt,filePath));

			    	    		break; 
			    	    	default: 
			    	    	}
			    	    	/*
			    	    	 
			                System.out.println(rs.getInt("receiverId") +  "\t" + rs.getInt("receiverId") +  "\t" + 
			                                   rs.getString("type") + "\t" +rs.getString("content") + "\t" +
			                                   rs.getString("date"));
			               
			                */
			            }
			        }
			        catch (Exception e)
		        	{
		        		System.out.println ("Statement Error"+ e.toString());
		        		e.printStackTrace();
		        	}
			        finally {
			        	try { stmt.close(); } catch (Exception ignore){
			        		ignore.printStackTrace();
			        	}
			        }
			    
			    
			    } catch (Exception e) {
			    	System.out.println("Get Messages Database error: "+ e.toString());
			    }
			
		return messagesList;
		}
			
	}
	
	public String getPassword(String myUserId) throws NoUserException
	{
		synchronized(dbConnection) {
		ResultSet rs = null ;
		try { 
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	            stmt.setQueryTimeout(10);
	    	    String getMessagesStmt = "SELECT * FROM users WHERE userId='".concat(myUserId).concat("'");
	    	    rs=stmt.executeQuery( getMessagesStmt );

	    	    
	    	    		    	    		    	    
	        }
	        catch (Exception e)
        	{
        		System.out.println ("Statement Error"+ e.toString());
        	}
	        finally {
	        	try 
	        	{ 
	        		stmt.close(); 
	        	}
	        	catch (Exception ignore)
	        	{
	        	}
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Get Password Database error: "+ e.toString());
	    }
		try {
			if (rs.next())
			{	    		
				return rs.getString("hashedPassword");
			}
			else 
			{
				throw new NoUserException();
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
		//never reaches this return
		}

	}
	
	/**
	 * Return all the users saved in the DB
	 * 
	 * @param myUserId
	 * @return
	 * @throws NoUserException
	 */
	public ArrayList<UserAccount> getSavedUsers() throws NoUserException
	{
		synchronized(dbConnection) {
			ResultSet rs = null ;
			
			try { 
		    	
		        Statement stmt = dbConnection.createStatement();
		        try {
		            stmt.setQueryTimeout(10);
		    	    String getMessagesStmt = "SELECT * FROM users";
		    	    rs=stmt.executeQuery( getMessagesStmt );
					//System.out.println("JE suis la 1");
	
		    	    
		    	    		    	    		    	    
		        }
		        catch (Exception e)
	        	{
	        		System.out.println ("Statement Error"+ e.toString());
	        	}
		        finally {
		        	try 
		        	{ 
		        		//stmt.close(); 
		        	}
		        	catch (Exception ignore)
		        	{
		        	}
		        }
		    
		    
		    } catch (Exception e) {
		    	System.out.println("Get Password Database error: "+ e.toString());
		    }
			//System.out.println("JE suis la3");
	
			try {
				ArrayList<UserAccount> res = new ArrayList<>();
				while (rs.next())
				{		    	
					//System.out.println("JE suis la");
	
					UserAccount cu = new UserAccount();
					cu.setHashedPassword(rs.getString("hashedPassword"));
					cu.setRegisterDate(rs.getString("registerDate"));
					cu.setUserid(rs.getString("userId"));
					res.add(cu);
				}
				return res;
	
				
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		//never reaches this return
		
	}
	
	
	
	public UserAccount getSavedUserFromId(String myUserId) throws NoUserException
	{
		synchronized(dbConnection) {
			ResultSet rs = null ;	
			try { 
		    	
		        Statement stmt = dbConnection.createStatement();
		        try {
		            stmt.setQueryTimeout(10);
		    	    String getMessagesStmt = "SELECT * FROM users WHERE userId='".concat(myUserId).concat("'");
		    	    rs=stmt.executeQuery( getMessagesStmt );
	
		    	    try {
		    			if (rs.next())
		    			{	
		    				UserAccount cu = new UserAccount();
		    				cu.setHashedPassword(rs.getString("hashedPassword"));
		    				cu.setRegisterDate(rs.getString("registerDate"));
		    				cu.setUserid(myUserId);
		    				return cu;
		    			}
		    			else 
		    			{
		    				System.out.println("No user found in db");
		    				throw new NoUserException();
		    			}
		    		} catch (SQLException e) {
		    			e.printStackTrace();
		    			throw new NoUserException();
		    		}
		    	    		    	    		    	    
		        }
		        catch (SQLException e)
	        	{
		        	e.printStackTrace();
	        		System.out.println ("Statement Error"+ e.toString());
	        		throw new NoUserException();
	        	}
		        finally {
		        	try 
		        	{ 
		        		stmt.close(); 
		        	}
		        	catch (SQLException ignore)
		        	{
		        		throw new NoUserException();
		        	}
		        }
		    
		    
		    } catch (SQLException e) {
		    	System.out.println("Get Password Database error: "+ e.toString());
		    	throw new NoUserException();
		    	
		    }
		}
		
		//never reaches this return
		
	}
	
	
	/* Verifier si present avant d'inserer + thread qui suprime lorsque timer expire
	 * Completer getMessages
	 * */
	 
	/*public boolean saveUserStatus(OtherUser toAdd ) throws NoUserException
	{
		return getDynamicDM().addActiveUser(toAdd);
	}*/
	

	public static boolean saveAccountDatabase(String userId,String hashedPassword, String registerDate)
	{	
		synchronized(dbConnection) {
			LogSystem.log3("NewAccount user is saving in database " + userId + " " + hashedPassword + " " + registerDate);
			try { 
		    	
		        Statement stmt = dbConnection.createStatement();
		        try {
		            stmt.setQueryTimeout(10);
		    	   String saveAccount="INSERT INTO users(userId ,hashedPassword,registerDate) VALUES( '".concat(userId).concat("','").concat(hashedPassword).concat("','").concat(registerDate).concat("')");
		    	    stmt.executeUpdate(saveAccount);
	
		    	    
		    	    		    	    		    	    
		        }
		        catch (Exception e)
	        	{
	        		System.out.println ("Statement Error"+ e.toString());
	        		return false;
	        	}
		        finally {
		        	try 
		        	{ 
		        		stmt.close(); 
		        	}
		        	catch (Exception ignore)
		        	{
		        		return false;
		        	}
		        }
		    
		    
		    } catch (Exception e) {
		    	System.out.println("Save Account error: "+ e.toString());
		    }
			
			
			return true;
		}
		
	}
	
	
	
	public boolean checkIfMessageSaved(AbstractMessage messageToSave)
	{
		synchronized(dbConnection) {
			boolean found =false; 
			DateFormat dateFormat = new SimpleDateFormat(DataManager.dateFormat);  
			String strDate = dateFormat.format(messageToSave.getDate());  
		
			 try { 
			    	
			        Statement stmt = dbConnection.createStatement();
			        try {
			            stmt.setQueryTimeout(4);//4 sec timeout for retrieving messages
			    	    String getMessagesStmt = "SELECT * FROM messages WHERE senderId='".concat(messageToSave.getSenderid()).concat("' AND ").concat("WHERE receiverId='").concat(messageToSave.getReceiverid()).concat("' AND ").concat("WHERE date='").concat(strDate).concat("')");
			    	    ResultSet rs =stmt.executeQuery( getMessagesStmt );
			    	
			    	   if (rs.next()!=false)
			    	   {
			    		   found =true;
			    	   }
			        }
			        catch (Exception e)
		        	{
		        		System.out.println ("Statement Error"+ e.toString());
		        	}
			        finally {
			        	try { stmt.close(); } catch (Exception ignore){
			        	}
			        }
			    
			    
			    } catch (Exception e) {
			    	System.out.println("Check Message Database error: "+ e.toString());
			    }
			
			return found;
		}
	}
	
	
	public  static String folderSelector() {
		 JFileChooser f = new JFileChooser();
	        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
	        f.showSaveDialog(null);

	       
	     return f.getSelectedFile().getPath();
	}
	public static String fileSelector()
	{

		 // set the label to the path of the selected file 
      String path=null;
		JFileChooser j = new JFileChooser(); 
		  
		// Open the save dialog 
		int r=j.showSaveDialog(null);	 
		 if (r == JFileChooser.APPROVE_OPTION) 
			  
           { 
				 path=j.getSelectedFile().getAbsolutePath(); 	            
	        
           }
		return path;           

	}
	
	
	public static String imageSelector()
	{

		 // set the label to the path of the selected file 
      String path=null;
		JFileChooser j = new JFileChooser(); 
		FileFilter filter = new FileNameExtensionFilter("JPEG file", new String[] {"jpg", "jpeg"});
		j.setFileFilter(filter);
		j.addChoosableFileFilter(filter);
		// Open the save dialog 
		int r=j.showSaveDialog(null);	 
		 if (r == JFileChooser.APPROVE_OPTION) 
			  
           { 
				 path=j.getSelectedFile().getAbsolutePath(); 	            
	        
           }
		return path;           

	}
	public boolean saveMessage(AbstractMessage messageToSave)
	{
		synchronized(dbConnection) {
		try {
		Statement stmt = dbConnection.createStatement();

        String strDate = messageToSave.getDate();  
		String fileDir;
		switch(messageToSave.getType())
    	{
    	case Text:
    		try {
    			
    			stmt.setQueryTimeout(4);//4 sec timeout for creating a table
	            stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES ('".concat(messageToSave.getSenderid()).concat("','").concat(messageToSave.getReceiverid()).concat("','").concat(strDate).concat("',").concat("0").concat(",'").concat(((TextMessage) messageToSave).getText_content()).concat("')")) ;
	            
    		}
    		catch(Exception e) {
    			System.out.println("Database saving error : "+ e.toString());
    			return false;
    		}
    		break;
    	case Image:
    		
    		 fileDir=DataManager.getDfDir().concat(getFileSep()).concat(messageToSave.getSenderid()).concat("-").concat(messageToSave.getReceiverid());
    		  File directory = new File(fileDir);
    		    if (! directory.exists()){
    		        directory.mkdirs();
    		     
    		    }
    		Path path = Paths.get(fileDir.concat(getFileSep()).concat(( (ImageMessage) messageToSave).getFilename()));
    		Files.write(path,((ImageMessage) messageToSave).getData());
    		
    		stmt.setQueryTimeout(4);//4 sec timeout for creating a table
            stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES ('".concat(messageToSave.getSenderid()).concat("','").concat(messageToSave.getReceiverid()).concat("','").concat(strDate).concat("',").concat("1").concat(",'").concat(((ImageMessage) messageToSave).getFilename()).concat("')")) ;
    		break;
    	case File:

    		 fileDir=DataManager.getDfDir().concat(getFileSep()).concat(messageToSave.getSenderid()).concat("-").concat(messageToSave.getReceiverid());
   		  File directory1 = new File(fileDir);
   		    if (! directory1.exists()){
   		        directory1.mkdirs();
   		     
   		    }
   		Path path1 = Paths.get(fileDir.concat(getFileSep()).concat(((FileMessage) messageToSave).getFilename()));
   		Files.write(path1,((FileMessage) messageToSave).getData());
   		
   		stmt.setQueryTimeout(4);//4 sec timeout for creating a table
        stmt.executeUpdate("INSERT INTO messages (senderId,receiverId ,date,type,content) VALUES ('".concat(messageToSave.getSenderid()).concat("','").concat(messageToSave.getReceiverid()).concat("','").concat(strDate).concat("',").concat("2").concat(",'").concat(((FileMessage) messageToSave).getFilename()).concat("')")) ;
   		break;
    	default: 
    		stmt.close();
    		break;
    	}

		}
		catch (Exception e)
		{	        

	    	System.out.println("Database connection error in save error: "+ e.toString());
	    	return false;

		}
		
		return true;
		}
		
	}
	
	
	private static  String strArraytoSHA1(ArrayList<String> convertme) {
	    MessageDigest md = null;
	    //Arrays.sort(convertme);
	    Collections.sort(convertme);
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		}
		String toCrypt="";
		for (int i=0; i<convertme.size();i++)
		{
			toCrypt=toCrypt.concat( Base64.getEncoder().encodeToString(md.digest(convertme.get(i).getBytes())));
		}
		
		return Base64.getEncoder().encodeToString(md.digest(toCrypt.getBytes()));
	}

	public String  generateMyUstring()
	{	
	
		String hr = strArraytoSHA1(generateArrayListOfmyUserId());
		LogSystem.log4("Current Uhash " + hr);
		return hr;
	}

	public String generateMyMstring ()
	{	
		String hr = strArraytoSHA1(generateArrayListOfmyMessageId());
		//LogSystem.log4("Current Mhash " + hr);
		return hr;
	}

	public static ArrayList<String> generateArrayListOfmyUserId()
	{
		synchronized(dbConnection) {
		ArrayList<String> listOfUserid=new ArrayList<String>();
		ResultSet rs = null ;
		try { 
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	            stmt.setQueryTimeout(100);
	    	    String getMessagesStmt = "SELECT userId FROM users";
	    	    rs=stmt.executeQuery( getMessagesStmt );
	    	    
	    	    try {
	    			//LogSystem.log4("List of my stored UserIDs ");

	    			//int i=1;
	    			while (rs.next())
	    			{	  		    	    
	      		
	    				String uid = rs.getString("userId");
	    				//LogSystem.log4(uid + " " + (i++));
	    				listOfUserid.add( uid);
	    			}
	    			
	    		} catch (SQLException e) {
	    			e.printStackTrace();
	    		}
	    	    		    	    		    	    
	        }
	        catch (Exception e)
	    	{
	    		System.out.println ("Statement Error"+ e.toString());
	    	}
	        finally {	    	   
	        	stmt.close(); 
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Get Userid List Database error: "+ e.toString());
	    }
		
		
		return listOfUserid;
		}
	}

	public static ArrayList<String> generateArrayListOfmyMessageId()
	{
		synchronized(dbConnection) {
		ArrayList<String> listOfMessageid=new ArrayList<String>();
		ResultSet rs = null ;
		try { 
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	            stmt.setQueryTimeout(10);
	    	    String getMessagesStmt = "SELECT senderId,receiverId,date FROM messages ";
	    	    rs=stmt.executeQuery( getMessagesStmt );
	
	    	    try {
	    			//LogSystem.log4("List of my stored Messsages UIDs ");
	    			//int i=1;
	    			while (rs.next())
	    			{	    		
	    				String m = rs.getString("senderId").concat("@").concat(rs.getString("receiverId").concat("@").concat(rs.getString("date")));
	    				//LogSystem.log4(m);
	    				listOfMessageid.add(m);

	    			}
	    			
	    		} catch (SQLException e) {
	    			e.printStackTrace();
	    		}
	    	    		    	    		    	    
	        }
	        catch (Exception e)
	    	{
	    		System.out.println ("Statement Error"+ e.toString());
	    	}
	        finally {
	        	try 
	        	{ 
	        		stmt.close(); 
	        	}
	        	catch (Exception ignore)
	        	{
	        	}
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Get MessageId List Database error: "+ e.toString());
	    }
		
		return listOfMessageid;
		}
	}
	
	public String getArrayListOfStringInStringFormat(ArrayList<String> a)
	{
		String r="(";
		
		for (String s: a)
		{
			r=r.concat(s).concat(",");
		}
		return r.concat(")"); 
	}
	
	public static MsgPacket getMessagePacketFromMid(String mid)
	{	
		synchronized(dbConnection) {
		MsgPacket msgpack = null;
		LogSystem.log5("Retrieving " + mid);
		//retrieve senderId, receiverId and date from Message id Mid
		String[] splitMid=mid.split("@");
		try { 
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	            stmt.setQueryTimeout(4);//4 sec timeout for retrieving messages
	    	    String getMessagesStmt = "SELECT * FROM messages WHERE senderId='".concat(splitMid[0]).concat("' AND receiverId='").concat(splitMid[1]).concat("' AND date='").concat(splitMid[2]).concat("'");
	    	    LogSystem.log5(getMessagesStmt);
	    	    ResultSet rs =stmt.executeQuery( getMessagesStmt );
	    	    Integer i = 0;
	    	    //Parsing date to correct type  	    
	    	    while (rs.next()) {
	    	    	i++;
	    	    	
	    	    	switch(rs.getInt("type"))
	    	    	{
	    	    	case 0:
	    	    		msgpack=new MsgPacket(new TextMessage(rs.getString("senderId"),rs.getString("receiverId"),rs.getString("date"),rs.getString("content")));
	    	    		break;
	    	    	case 1:
	    	    		  String filePath=DataManager.getDfDir().concat(getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(getFileSep()).concat(rs.getString("content")));
		    	    		msgpack=new MsgPacket(new ImageMessage(rs.getString("senderId"),rs.getString("receiverId"),rs.getString("date"),filePath));
		    	    		break;
	    	    	case 2:
	    	    		String filePath1=DataManager.getDfDir().concat(getFileSep()).concat(rs.getString("senderId")).concat("-").concat(rs.getString("receiverId").concat(getFileSep()).concat(rs.getString("content")));
	    	    		msgpack=new MsgPacket(new FileMessage(rs.getString("senderId"),rs.getString("receiverId"),rs.getString("date"),filePath1));
	
	    	    		break; 
	    	    	default: 
	    	    	}
	    	    	/*
	    	    	 
	                System.out.println(rs.getInt("receiverId") +  "\t" + rs.getInt("receiverId") +  "\t" + 
	                                   rs.getString("type") + "\t" +rs.getString("content") + "\t" +
	                                   rs.getString("date"));
	                
	                
	                
	               
	                */
	            }
	    	    
	    	    LogSystem.log5(i + " messages has been found");
	        }
	        catch (Exception e)
	    	{
	    		System.out.println ("StatemenT Error"+ e.toString());
	    		e.printStackTrace();
	    	}
	        finally {
	        	try { stmt.close(); } catch (Exception ignore){
	        		System.out.println ("StatemenT Error"+ ignore.toString());
	        	}
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Get Message  from Mid Database error: "+ e.toString());
	    }
		return msgpack;
		}
	}
	
	public boolean isUserIdExist(String uid) {
		return false;
	}
	
	
	public NAPacket getNAPacketFromUid(String uid)
	{
		synchronized(dbConnection) {
		NAPacket np=null;
		ResultSet rs = null ;
		try { 
	    	
	        Statement stmt = dbConnection.createStatement();
	        try {
	            stmt.setQueryTimeout(10);
	    	    String getMessagesStmt = "SELECT * FROM users WHERE userId='".concat(uid).concat("'");
	    	    rs=stmt.executeQuery( getMessagesStmt );
	
	    	    try {
	    			//Parsing date to correct type  	      
	
	    			np= new NAPacket(rs.getString("userId"), rs.getString("hashedPassword"), rs.getString("registerDate"));
	    		} catch (SQLException e) {
	    			
	    			e.printStackTrace();
	    			System.out.println("User Table error called from get NAPacket from uid");
	    		}
	    	    		    	    		    	    
	        }
	        catch (Exception e)
	    	{
	    		System.out.println ("Statement Error"+ e.toString());
	    	}
	        finally {
	        	try 
	        	{ 
	        		stmt.close(); 
	        	}
	        	catch (Exception ignore)
	        	{
	        	}
	        }
	    
	    
	    } catch (Exception e) {
	    	System.out.println("Get User From UID Database error: "+ e.toString());
	    }
		
		
		return np;
		}
	}
	
	public static byte[] filenameToArrayByte(String filename) {
		return null;
	}
	
	public static byte[] BytesArrayToFile(String destFilename, byte[] data) {
		return null;
	}
	
	public static String pathTofilename(String path) {
		 String[] tokens = path.split("[" + getFileSep() + "]+");
		 return tokens[tokens.length-1];
	}
	
	
	public void close() {
		closeDbConnection();
	}
	
}
	
    
	
	

