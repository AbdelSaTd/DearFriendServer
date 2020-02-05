package Base;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import DataManagement.AbstractMessage;
import DataManagement.DataManager;
import DataManagement.DynamicDataManager;
import DataManagement.FileMessage;
import DataManagement.ImageMessage;
import DataManagement.MsgPacket;
import DataManagement.NoUserException;
import DataManagement.TextMessage;
import Network.*;
import Tools.*;
import UserInfo.OnlineUser;
import UserInfo.UserAccount;
import UserInfo.WebUser;
import UserInfo.LocalUser;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/Controller")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private DataManager dataManager ;
	private NetworkManager networkManager;
	private DynamicDataManager dynamicDM;

	
	private LinkedBlockingQueue<AbstractPacket> buffPacket;
	
	private boolean nmThBool;
	private Timer updateStatusT; // The timer in charge of notifying other user about the status of this user
	private TimerTask updateStatusTT;
	
	private Timer updateDBSyncT; // The timer in charge of
	
	private LinkedBlockingQueue<AbstractMessage> buffMessage; 
	private LinkedList<CPPacket> buffCPPacketReply;
	
	private LocalUser localUser;
	
	private HttpSession localSession;
	private Integer refresh = 0;
	
	//private boolean onInternet;                      Clients
	
	public enum ResponseDBType{
		UnknownUserId, BadPassword, Success
	}
	
    /**
     * Default constructor. 
     */
    public Controller() {
    	
    	try {
			//Get the current ip address
			ArrayList<InetAddress> addrs = NetworkManager.retrieveLocalAddr();
			InetAddress	localHostAddr = addrs.get(0);
			InetAddress localBroadcastAddr = addrs.get(1);
			String hh = localHostAddr.toString();
			System.out.println(hh);
			dataManager = new DataManager();
			
			buffPacket = new LinkedBlockingQueue<AbstractPacket>();
			
			networkManager = new NetworkManager(buffPacket, localHostAddr, localBroadcastAddr);
				
			nmThBool = true;
			
			localUser = new LocalUser("server", "server", "revres", "", null, null, localHostAddr, localBroadcastAddr);
			
			Thread nmThread = new Thread(new Runnable() {
				public void run() {
					
					//Object packetMutex = dynamicDM.getPacketMutex();
					LogSystem.log2("NM control thread is running");
					
					buffMessage = new LinkedBlockingQueue<>();
					buffCPPacketReply = new LinkedList<CPPacket>();
					ExecutorService exServ = Executors.newCachedThreadPool();
					
					//exServ.execute(arg0);
					
					try {
						while(nmThBool) {
							
							AbstractPacket lastPacket = buffPacket.take(); //ThreadSafe
							
							LogSystem.log2("Packet taken [controller]");
							Runnable packetHandler = new Runnable() {

								@Override
								public void run() {
									//LogSystem.log2("Packet has been received and will be treated");
									
									AbstractPacket.PacketType pt = lastPacket.getType();
									
									if(pt == AbstractPacket.PacketType.Message) {
										//displayConsoleMessage(lastPacket);
										LogSystem.log5("Message received [controller]");
										processIncomingMessage(lastPacket);
									}
									else if(pt == AbstractPacket.PacketType.Challenge) {
											
										CPPacket cpp = (CPPacket) lastPacket;
											if(cpp.getSenderAddr().toString().compareTo(NetworkManager.getLocalHostAddr().toString()) != 0)
											{
												LogSystem.log3("ChallengePacket [controller]");
												if(cpp.isRequest()) {
													LogSystem.log3("ChallengePacket [controller] request");
													processChallengeRequest(cpp);
												}
												else {
													synchronized(buffCPPacketReply) {
														LogSystem.log3("ChallengePacket [controller] reply");
														buffCPPacketReply.add(cpp);
													}
												}
											}
										
									}
									else if(pt == AbstractPacket.PacketType.NewAccount) {
										//Store
										LogSystem.log2("NewAccountPacket [controller]");
										NAPacket npp = (NAPacket) lastPacket;
										if(DataManager.saveAccountDatabase(npp.getUserId(), npp.getHashedPwd(), npp.getDate())) {
											LogSystem.log3("User saved");
											updateMyUString();updateMyMString();
										}
									}
									else if(pt == AbstractPacket.PacketType.UserUpdate) {
										//Status user update
										processUserUpdate(lastPacket);
										//OnlineUser user = new OnlineUser(uup.userid, uup.pseudo, updateStatusTimerTask, );
									}
									else if(pt == AbstractPacket.PacketType.UserInternetUpdate) {
										//Status user update
										processUserUpdateInternet(lastPacket);
										//OnlineUser user = new OnlineUser(uup.userid, uup.pseudo, updateStatusTimerTask, );
									}
									else if (pt==AbstractPacket.PacketType.ArrayOfIds) {
										
										processArraysOfIds(lastPacket);
									}
									else {
										//Logggg
										LogSystem.log1("[Warning!] An unknown type of incoming packet !");
									}
								}

								
							};
							
							exServ.execute(packetHandler);
						}
						
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					close();
				}
				
				public void close() {
					nmThBool = false;
				}
			});
			
			nmThread.start();

			
			dynamicDM = new DynamicDataManager(this);
			dynamicDM.startRemoverOfflineUserThread();
			
			launchDatabaseSyncIfMaster();
			updateUserStatus();
			
			
			//Create thread to monitor buffer
		}
		 catch (UnresolvedAddressException e1) { // if the host is not connected
			LogSystem.log2("Controller: None network has been found, exit application");
		}
		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		PrintWriter writer = response.getWriter();
		
		String url = request.getRequestURL().toString();
		String[] tokens = url.split("[/]+");
		String action = tokens[tokens.length-1];
		
		
		
		writer.append("<!DOCTYPE html>\r\n")
		  .append("<html>\r\n")
		  .append("		<head>\r\n")
		  .append("			<title>DearFriend</title>\r\n")
		  .append("		</head>\r\n")
		  .append("		<body>\r\n");
		
		writer.append("URL => " + url + "<br>");
		//writer.append("action: " + action + "<br>");
		
	/*	if(localSession.getId().compareTo(request.getSession().getId()) == 0)
		{
			
		}
		*/
		if(action.compareTo("Controller") == 0) {
			// Welcome
			
			writer.append("Welcome to DearFriend <br>");
			writer.append("<form action=\"/DearFriendServer/Controller/Login\" method=\"post\">\n" + 
					"    <div>\n" + 
					"        <label for=\"userid\">UserId :</label>\n" + 
					"        <input type=\"text\" id=\"userid\" name=\"userid\">\n" + 
					"    </div>\n" +
					"    <div>\n" + 
					"        <label for=\"pseudo\">Pseudo :</label>\n" + 
					"        <input type=\"text\" id=\"pseudo\" name=\"pseudo\">\n" + 
					"    </div>\n" + 
					"    <div>\n" + 
					"        <label for=\"password\">Password :</label>\n" + 
					"        <input type=\"password\" id=\"password\" name=\"password\">\n" + 
					"    </div>\n" +
					"	<div class=\"button\">\n" + 
					"        <button type=\"submit\">Connect</button>\n" + 
					"    </div>" + 
					"</form>");
			
		}
		else if(action.compareTo("Login") == 0) {
			
			writer.append("Welcome to DearFriend <br>");
			
			
			String userid = request.getParameter("userid");
			String pwd = request.getParameter("password");
			String pseudo = request.getParameter("pseudo");
			
			
			Controller.ResponseDBType rdt = accountValid(userid, pwd, pseudo);
			if(rdt == Controller.ResponseDBType.UnknownUserId) {
				writer.append("<div style=\"color:red;\">Unknown userid !!!</div><br>");
			}
			else if(rdt == Controller.ResponseDBType.BadPassword){
				writer.append("<div style=\"color:red;\">Wrong password !!!</div><br>");
			}
			else { //Success
				//localSession = request.getSession();
				UUPacket uup = new UUPacket(pseudo, userid, false, localUser.getmString(), localUser.getuString(), InetAddress.getByName(request.getRemoteAddr()), null);
				dynamicDM.activateInternetUserFlag(uup, request.getSession());
				
				response.sendRedirect(url+"/Connected");
			}
			
			
			
		}
		else if(action.compareTo("Connected") == 0)
		{

			WebUser wu = dynamicDM.getConnectedInternetUserFromSession(request.getSession().getId());
			if(wu != null) {
				writer.append("Welcome, <span style=\"color:green;\">" + wu.getUserid() + "</span><br>");
				writer.append("Refresh (" + refresh++ + ") <br>");
				writer.append("Connected users:  <br>");
				Integer i=1;
				for(OnlineUser ou: dynamicDM.getOnlineUsers()) {
					writer.append(i++ + ") " + ou.getPseudo() + "<br>");
				}
				for(OnlineUser ou: dynamicDM.getOnlineUsersInternet()) {
					if(ou.getUserid().compareTo(wu.getUserid()) != 0)
						writer.append(i++ + ") " + ou.getPseudo() + "<br>");
				}
				if(i==1) {
					writer.append("<div style=\"color:blue;\">none user connected (except you)</div><br>");
				}
				
				writer.append("    <script>\n" + 
						"       setTimeout(function(){\n" + 
						"           location.reload();\n" + 
						"       },3000); // 3000 milliseconds means 3 seconds.\n" + 
						"    </script>");
			}
			else {
				response.sendRedirect(url+"/Error");
			}
			
		}
		else {
			writer.append("<div style=\"color:red;\">Error 404</div> - DearFriend unknown service requested - <br>");
		}
		
		writer.append("		</body>\r\n")
		  .append("</html>\r\n");
		
		
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}
	
	// Process methods
	
	private void processIncomingMessage(AbstractPacket pack) {
		
		LogSystem.log2("This is a message packet");

		MsgPacket mp  = (MsgPacket) pack;
		
		OnlineUser ou = dynamicDM.getConnectedInternetUserFromId(mp.getMsg().getReceiverid());
		if(ou != null)
		{
			try {
				sendPacketInternet(mp, ou.getUserid());
				LogSystem.log2("Message forwarded to " + ou.getUserid() + " from " + mp.getSenderUID());
			} catch (IOException | OfflineUserException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	private void processArraysOfIds(AbstractPacket pack) {
		
		//if(LocalUser.isSetUp()) { // Process the updating list only if the user infos are setup
			ArrayOfIdsPacket aoip=(ArrayOfIdsPacket) pack;
			if (aoip.getState()==0) {

					ArrayList <String> hisUs=(ArrayList<String>)aoip.getUserIds().clone();
					ArrayList <String> hisMs=(ArrayList<String>)aoip.getMessageIds().clone();
					ArrayList <String> myUs=DataManager.generateArrayListOfmyUserId();
					ArrayList <String> myMs=DataManager.generateArrayListOfmyMessageId();
					ArrayList <String> commonUs=(ArrayList<String>) myUs.clone();
					ArrayList <String> commonMs=(ArrayList<String>) myMs.clone();
					
					
					commonUs.retainAll(hisUs);

					commonMs.retainAll(hisMs);
					
					//i send him a list of the Ids i dont have and send him the objects he doesnt have
					myUs.removeAll(commonUs);
					myMs.removeAll(commonMs);
					hisUs.removeAll(commonUs);
					hisMs.removeAll(commonMs);
					
					LogSystem.log5("ArrayOfIds received; Comparison of users");
					System.out.println("MyUsers");
					System.out.println(myUs);
					System.out.println("HisUsers");
					System.out.println(hisUs);
					
					
					
					ArrayOfIdsPacket aoip1 = new ArrayOfIdsPacket(aoip.getReceivUID(), NetworkManager.getLocalHostAddr(), aoip.getSenderUID(),hisUs,hisMs,1);
				
					for (String uid:myUs) 
					{
						LogSystem.log5("Sending of a new account of " + uid + " to " + aoip.getSenderUID());
						try {
							networkManager.sendPacket(dataManager.getNAPacketFromUid(uid), aoip.getSenderAddr());
						} catch (OfflineUserException | IOException e) {
							e.printStackTrace();
							System.out.println("Error in phase two sending list of NAPackets to synchronize databases");

						}
					
					}
					
					
					for (String mid:myMs) 
					{
						LogSystem.log5("Sending[2] of " + mid + " to " + aoip.getSenderUID() + " at " + aoip.getSenderAddr());
						try {
							MsgPacket msgPack = DataManager.getMessagePacketFromMid(mid);

							//System.out.println((TextMessage)msgPack.getMsg());
							
							networkManager.sendPacket(msgPack, aoip.getSenderAddr());
						} catch (OfflineUserException | IOException e) {
						
							e.printStackTrace();
							System.out.println("Error in phase two sending list of message packets to synchronize databases");

						}
					}
					
					//List of Account and Messages I don't have
					try {
						networkManager.sendPacket(aoip1, aoip.getSenderAddr());
					} catch (OfflineUserException | IOException e) {
						
						e.printStackTrace();
						System.out.println("Error in phase two sending array of ids to synchronize databases");

					}
					
			}
			else if (aoip.getState()==1)
			{
				for (String uid:aoip.getUserIds()) 
				{
					LogSystem.log5("Sending[3] of a new account of " + uid + " to " + aoip.getSenderUID());
					try {
						networkManager.sendPacket(dataManager.getNAPacketFromUid(uid), aoip.getSenderAddr());
					} catch (OfflineUserException | IOException e) {
						
						e.printStackTrace();
						System.out.println("Error in phase three sending list of NAPackets to synchronize databases");

					}
				
				}
				for (String mid:aoip.getMessageIds()) 
				{
					LogSystem.log5("Sending[3 ] of " + mid + " to " + aoip.getSenderUID());
					try {
						MsgPacket mp = DataManager.getMessagePacketFromMid(mid);
						LogSystem.log5(mp.toString());
						LogSystem.log5(mp.getMsg().toString());
						networkManager.sendPacket(DataManager.getMessagePacketFromMid(mid), aoip.getSenderAddr());
					} catch (OfflineUserException | IOException e) {
					
						e.printStackTrace();
						System.out.println("Error in phase three sending list of message packets to synchronize databases");

					}
				}
				
					
			}
		//}
		
	}
	
	private void processUserUpdate(AbstractPacket pack) {
		LogSystem.log2("NewAccountPacket [controller]");
		UUPacket uup = (UUPacket) pack; 
		if(uup.isLog_off()) {
			LogSystem.log3("userID<" + uup.getUserId() + ">" + " is deconnecting itself");
			dynamicDM.removeActiveUserLocal(uup.getUserId());
			
			for(String toNotify: dynamicDM.getConnectedInternetUsersId()) {
				try {
					sendPacketInternet(uup, toNotify);
				} catch (IOException | OfflineUserException e) {
					LogSystem.log1("Error when notifying in the local network that " + uup.getUserId() + " is logging off" , e);
				}
			}
			
		}
		else {
			
			//LogSystem.log1("[online] " + uup.userId + "(" + uup.pseudo + ") <IP:" + uup.addr.toString() + "> mS:" + uup.mString + " uS:" + uup.uString);
			LogSystem.log3("Online user received: " + uup.getUserId());
			//LogSystem.log1("userID " + uup.userId );
			//LogSystem.log1("pseudo " + uup.pseudo );
			//LogSystem.log1("IP: " + uup.addr.toString());
			//LogSystem.log1(" mS: " + uup.getmString());
			//LogSystem.log1(" uS:" + uup.getuString());
			
		//	OnlineUser ou = dynamicDM.getConnectedInternetUserFromId(uup.getUserId());
			//if(ou == null)//To avoid adding a represented user (user on internet) to the connected user list
			if(uup.getUserId().compareTo("server") != 0)
				dynamicDM.activateUserFlagLocal(uup);
		}
		
		
	}
	
	/**
	 * Processed user update internet packet
	 * @param pack
	 */
	private void processUserUpdateInternet(AbstractPacket pack) {
		LogSystem.log2("UserUpdateInternetPacket [controller]");
		UUPacket uup = (UUPacket) pack; 
		if(uup.isLog_off()) {
			LogSystem.log3("userID<" + uup.getUserId() + ">" + " is deconnecting itself");
			dynamicDM.removeActiveInternetUser(uup.getUserId());
			LogSystem.log3("userID<" + uup.getUserId() + ">" + " removed from the connected list");
			LogSystem.log1("Log_off" + uup.getUserId() + "(" + uup.getPseudo() + ") <IP:" + uup.getUnicastAddr().toString() + "> mS:" + uup.getmString() + " uS:" + uup.getuString());
			
			uup.setType(AbstractPacket.PacketType.UserUpdate);
			try {
				networkManager.notifyBroadcast(uup, NetworkManager.BroadcastType.Broadcast);
			} catch (IOException e) {
				LogSystem.log1("Error when sending broadcast", e);
			}
		}
		else {
			
			//LogSystem.log1("[online] " + uup.userId + "(" + uup.pseudo + ") <IP:" + uup.addr.toString() + "> mS:" + uup.mString + " uS:" + uup.uString);
			LogSystem.log3("Online internet user received: " + uup.getUserId());
			//LogSystem.log1("userID " + uup.userId );
			//LogSystem.log1("pseudo " + uup.pseudo );
			//LogSystem.log1("IP: " + uup.addr.toString());
			//LogSystem.log1(" mS: " + uup.getmString());
			//LogSystem.log1(" uS:" + uup.getuString());
			
			// Update of all messages need to be done here
			
			dynamicDM.activateInternetUserFlag(uup);
		}
		
		
	}
	

	private void processChallengeRequest(CPPacket cpp) {
			LogSystem.log2("ChallengePacket [request] localPseudo: " + localUser.getPseudo() + " vs Remot: " + cpp.getChallenger());
			if(cpp.getTypeCh() == CPPacket.ChallengeType.Pseudo && dynamicDM.pseudoInActiveInternetUser(cpp.getChallenger())) {
				
				//AbstractPacket ap = new CPPacket(localUser.getUserid(), true, cpp);
				
				cpp.setRequest(false);//reply
				InetAddress remoteAddr = cpp.getSenderAddr();
				cpp.setSenderAddr(localUser.getLocalHostAddr());
				
				try {
					LogSystem.log2("ChallengePacket [challenge] Will send to : " + remoteAddr);
					
					networkManager.sendPacket(cpp, remoteAddr);
					LogSystem.log2("ChallengePacket [challenge] Remote packet sent : " + remoteAddr);
				} catch (OfflineUserException e) {
					LogSystem.log1("Host disconnected while replying challenge", e);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(cpp.getTypeCh() == CPPacket.ChallengeType.UserID && dynamicDM.userIdInActiveInternetUser(cpp.getChallenger())) {
				cpp.setRequest(false);//reply
				InetAddress remoteAddr = cpp.getSenderAddr();
				cpp.setSenderAddr(localUser.getLocalHostAddr());
				
				try {
					LogSystem.log2("ChallengePacket [challenge] Will send to : " + remoteAddr);
					
					networkManager.sendPacket(cpp, remoteAddr);
					LogSystem.log2("ChallengePacket [challenge] Remote packet sent : " + remoteAddr);
				} catch (OfflineUserException e) {
					LogSystem.log1("Host disconnected while replying challenge", e);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	
	}
	
	//DynamicDataManager methods
	
	public synchronized void notifyOnlineInternetUsers(ArrayList<OnlineUser> aou, OnlineUser removed) {
		UUPacket uup = new UUPacket(removed.getPseudo(), removed.getUserid(), true, null, null, removed.getLocalHostAddr(), removed.getLocalBroadcastAddr());
		for(OnlineUser toNotify: aou) {
			try {
				sendPacketInternet(uup, toNotify.getUserid());
			} catch (IOException | OfflineUserException e) {
				LogSystem.log1("Error when notifying in the local network that " + removed.getUserid() + " is logging off" , e);
			}
		}
	}
	
	
	public ArrayList<OnlineUser> getOnlineUsers(){
		return dynamicDM.getOnlineUsers();
	}
	
	public boolean send(String userid, String content, AbstractMessage.MessageType mtype) {
		MsgPacket mp;
		AbstractMessage am; 
		try {
			
			if(mtype == AbstractMessage.MessageType.File)
			{
				am = new FileMessage(localUser.getUserid(), userid, new Date(), content);
				mp = new MsgPacket(am);
			}else if(mtype == AbstractMessage.MessageType.Image) {
				am = new ImageMessage(localUser.getUserid(), userid, new Date(), content);
				mp = new MsgPacket(am);
			}
			else {//Text
				am = new TextMessage(localUser.getUserid(), userid, new Date(), content);
				mp = new MsgPacket(am);
			}
				
			try {
				broadcastPacket(mp);
				dataManager.saveMessage(am);
			} catch (OfflineUserException e) {
				
				e.printStackTrace();
				return false;
			}
		}
		catch(IOException e) {
			System.out.println("error came from io ");
			return false;
		}
		
		return true;
		
	}

	
	public ArrayList<AbstractMessage> getMessages(String uid){
		return DataManager.getMessagesBetweenTwo(uid, localUser.getUserid());
	}
	
	
	
	// UIManager methods
	
		public String getUserid() {
			return localUser.getUserid();
		}
		
		public boolean pseudoValid(String pseudo) {
			boolean notvalid = dynamicDM.pseudoInActiveUser(pseudo) || dynamicDM.pseudoInActiveInternetUser(pseudo);
			LogSystem.log3(pseudo + " is in the current list of user ? " + notvalid);
			
			if(!notvalid) {
				//challengePseudo(pseudo);
				
				AbstractPacket ap = new CPPacket(localUser.getLocalHostAddr(), true, pseudo);
				
				try {
					networkManager.notifyBroadcast(ap, NetworkManager.BroadcastType.Broadcast);
				} catch (IOException e) {
					LogSystem.log1("Notifying of user status failed", e);
				}
				
				// sleepTime = load from file ini in the datamanager
				long sleepTime = DataManager.getSleepTimePseudo(); // in ms (3s)
				LogSystem.log3("Challenge");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					LogSystem.log1("Thread interrupted during sleep time in challenge pseudo", e);
				}
				
				
				synchronized(buffCPPacketReply) {
					CPPacket cpp = buffCPPacketReply.pollLast();
					if(cpp != null && cpp.getChallenger().compareTo(pseudo) == 0) {
						notvalid = true;
						LogSystem.log3("The pseudo " + cpp.getChallenger() + " is not a valid one !");
					}	
				}
			}

			LogSystem.log3(pseudo + " user found that have the same " + notvalid);
			return !notvalid;
		}
		
		public boolean userIdValid(String uid) {
			boolean valid = true;
			try {
				UserAccount ua  = dataManager.getSavedUserFromId(uid);
				valid = false;
			}
			catch(NoUserException e) {
				
				LogSystem.log3(uid + " challenged " + valid);
				AbstractPacket ap = new CPPacket(NetworkManager.getLocalHostAddr(), true, uid, CPPacket.ChallengeType.UserID);
				
				try {
					networkManager.notifyBroadcast(ap, NetworkManager.BroadcastType.Broadcast);
				} catch (IOException e3) {
					LogSystem.log1("Notifying of user status failed", e3);
				}
				
				// sleepTime = load from file ini in the datamanager
				long sleepTime = DataManager.getSleepTimePseudo(); // in ms (3s)
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e2) {
					LogSystem.log1("Thread interrupted during sleep time in challenge pseudo", e2);
				}
				
				
				synchronized(buffCPPacketReply) {
					CPPacket cpp = buffCPPacketReply.pollLast();
					
					if(cpp != null && cpp.getChallenger().compareTo(localUser.getUserid()) == 0) {
						LogSystem.log3(cpp.getChallenger());
						LogSystem.log3(uid + " user found that have the same " + valid);
						valid = false;
						//throw new NoUserException();
					}	
				}
				
		
			}
			
			 
			
			return valid;
		}

		/**
		 * This methods serves to the UIManager to verify that an user account is valid in the system and store the different information in the localUser
		 * @param userId
		 * @param hashedPassword
		 * @return
		 */
		public Controller.ResponseDBType accountValid(String userId, String hashedPassword, String pseudo) {
			try {
				UserAccount ua = dataManager.getSavedUserFromId(userId);
				if(ua.getHashedPassword().compareTo(hashedPassword)==0) {
					return Controller.ResponseDBType.Success;
				}
				else {
					return Controller.ResponseDBType.BadPassword;
				}
			} catch (NoUserException e) {
				return Controller.ResponseDBType.UnknownUserId;
			}
		}
		
		

	// Tests methods
	private void displayConsoleMessage(AbstractPacket pack) {
		LogSystem.log2("This is a message packet");
		//Store *can be threaded*
		MsgPacket mp  = (MsgPacket) pack;
		if(mp.getMsg().getReceiverid().compareTo(localUser.getUserid()) == 0)
		{
			if(mp.getMsg().getType() == AbstractMessage.MessageType.Text)
			{
				//LogSystem.log2("This is a text message");
				//UIManager.display();
				TextMessage tm = (TextMessage) mp.getMsg();
				System.out.println("Msg from < " + tm.getSenderid() + " > to < " + tm.getReceiverid() + " > :");
				System.out.println(tm.getText_content());
			}// Others 
			else {
				System.out.println("Message which is not a text type has been received !");
			}
		}
		else {
			LogSystem.log2("Update message arrived !");
			//LogSystem.log2("This is a text message");
			//UIManager.display();
			TextMessage tm = (TextMessage) mp.getMsg();
			System.out.println("Msg from < " + tm.getSenderid() + " > to < " + tm.getReceiverid() + " > :");
			System.out.println(tm.getText_content());
		}
		
		if(dataManager.saveMessage(mp.getMsg())) {
			System.out.println("Message saved");
		}
	
	}
	
	public void sendObject(int p) {
		
		if(p == 0) {
			TextMessage tm = new TextMessage("root",
					"root", new Date(), "ROOT");
			AbstractPacket ap = new MsgPacket(tm);

			try {
				networkManager.sendPacket(ap, "127.0.0.1");
				System.out.println("Text msg sent !");
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			}
			catch(IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			ImageMessage im;
			try {
				im = new ImageMessage("root", "weak_root", new Date(), "my_best_pic.gif");
				ap = new MsgPacket(im);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			
			try {
				networkManager.sendPacket(ap, "127.0.0.1");
				System.out.println("Image msg sent !");
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			}
			catch(IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			FileMessage fm;
			try {
				fm = new FileMessage("root", "weak_root", new Date(), "my_best_grades.gif");
				ap = new MsgPacket(fm);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
			try {
				networkManager.sendPacket(ap, "127.0.0.1");
				System.out.println("File msg sent !");
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			}
			catch(IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		
	}
	
	public void autoTestNetworkServer(String ip) {
		localUser.setUserid("Machine2");
		localUser.setPseudo("P:Mi");
	}
	
	public void autoTestNetworkClient(String ip) {
		
		localUser.setUserid("Machine1");
		localUser.setPseudo("P:Ab");
		String remUID = "Machine2";
		
		System.out.println("Broadcast by UDP");
		TextMessage tm = new TextMessage(localUser.getUserid(),
				"Machine2", new Date(), "The_content_of_the_UDP_Broadcast");
		AbstractPacket ap = new MsgPacket(tm);		
		try {
			networkManager.notifyBroadcast(ap, NetworkManager.BroadcastType.Broadcast);
		} catch (IOException e) {
			LogSystem.log1("Sending failed", e);
		}
		
		
		System.out.println("TCP Sending");
		tm = new TextMessage(localUser.getUserid(),
				remUID, new Date(), "The_content_of_the_TCP_Sending");
		ap = new MsgPacket(tm);

		try {
			networkManager.sendPacket((AbstractPacket) ap, ip);
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
		catch(IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
		
	}
	
	
	public void testOnlineUserClient(String ip) {
		localUser.setUserid("Machine1");
		localUser.setPseudo("P:Abdel");
		String remUID = "Machine2";
		
	}
	
	public void testOnlineUserServer(String ip) {
		localUser.setUserid("Machine2");
		localUser.setPseudo("P:Mike");
		String remUID = "Machine1";
		
		while(true){
			ArrayList<OnlineUser> aou = dynamicDM.getOnlineUsers();
			try {
				LogSystem.log3("Connected user");
				Thread.sleep(5000);
				for(OnlineUser ou: aou) {
					LogSystem.log3(ou.toString());
				}
			} catch (InterruptedException e) {
				LogSystem.log1("Thread interrupted during sleep time in challenge pseudo", e);
			}
		}
		
		
	}

	
	public void testSyncDBClient(String ip) {
		//dynamicDM.setUserAccountId("UID:Abdel");
		//localUser.setPseudo("P:Abdel");
		//String remUID = "UID:Mike";
		
		
	}
	
	public void testSyncDBServer(String ip) {
		//dynamicDM.setUserAccountId("UID:Mike");
		//localUser.setPseudo("P:Mike");
		//String remUID = "UID:Abdel";
	}
	
	public void testNewAccountClient(String ip) {
		localUser.setUserid("Machine1");
		localUser.setPseudo("P:Ab");
		String remUID = "Machine2";
		
		for(int i=0; i<2; i++)
		{
			AbstractPacket ap = new NAPacket("Machine5", "hashedPWD", new Date());
			
			try {
				networkManager.notifyBroadcast(ap, NetworkManager.BroadcastType.Broadcast);
				LogSystem.log3("Utilisateur Machine 5 envoyé");
			} catch (IOException e) {
				LogSystem.log1("Sending failed", e);
			}
		}
		
	}
	
	public void testNewAccountServer(String ip) {
		String uid = "Machine2";
		String ps = "P:Mi";
		localUser.setUserid(uid);
		localUser.setPseudo("P:Mi");
		
		while(true)
		{
			long sleepTime = 5000; // in ms (3s)
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				LogSystem.log1("Thread interrupted during sleep time in challenge pseudo", e);
			}
			
			System.out.println("Current Accounts ");
			try {
				ArrayList<UserAccount> acu = dataManager.getSavedUsers();
				for(UserAccount cu: acu) {
					System.out.println("Uid " + cu.getUserid());
					System.out.println("Pwd " + cu.getHashedPassword());
					System.out.println("Date " + cu.getRegisterDate() + "\n");
				}
			} catch (NoUserException e) {
				LogSystem.log3("Pas d'utilisateur dans la database");
			}
		}
		
	}
	
	public void testDMClient(String ip) {
		
		System.out.println("Current Messages ");
		ArrayList<String> users = DataManager.generateArrayListOfmyUserId();
		
		System.out.println(users);
		String myuid = localUser.getUserid();
		System.out.println(myuid + "with");
		for(String u: users) {
			System.out.println(u);
			ArrayList<AbstractMessage> msgs = DataManager.getMessages(u);
			
			for(AbstractMessage abs: msgs) {
				System.out.println(abs.toString());
			}
		}
		
	}
	
	public void testDMServer(String ip) {
		
		localUser.setUserid("Machine1");
		localUser.setPseudo("P:Ab");
		String remUID = "Machine2";
		
		System.out.println("Broadcast by UDP");
		TextMessage tm = new TextMessage(localUser.getUserid(),
				"Machine2", new Date(), "The_content_of_the_UDP_Broadcast");
		AbstractPacket ap = new MsgPacket(tm);		
		try {
			networkManager.notifyBroadcast(ap, NetworkManager.BroadcastType.Broadcast);
		} catch (IOException e) {
			LogSystem.log1("Sending failed", e);
		}
		
		
		System.out.println("TCP Sending");
		tm = new TextMessage(localUser.getUserid(),
				remUID, new Date(), "The_content_of_the_TCP_Sending");
		ap = new MsgPacket(tm);

		try {
			networkManager.sendPacket((AbstractPacket) ap, ip);
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
		catch(IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
		
	}
		

	private void launchDatabaseSyncIfMaster() {
		TimerTask updateDBSyncTT = new TimerTask() {
			public void run() {
				
				ArrayList<String> listOnline = dynamicDM.getConnectedUsersId();
				ArrayList<String> internetOnlineUsers = dynamicDM.getConnectedInternetUsersId(); 
				 
				//Sort the list
				Collections.sort(listOnline);
				System.out.println("[SyncDB] Connected users: " + listOnline);
				System.out.println("[SyncDB] Connected internet users: " + internetOnlineUsers);
				//check if userid first alphabetically
				if (listOnline.size()!=0 && (internetOnlineUsers.contains(listOnline.get(0)) || localUser.getUserid().compareTo(listOnline.get(0))<0))
				{
					LogSystem.log5("I'm the master");
					for (String key : listOnline) {
						OnlineUser ou = dynamicDM.getConnectedUserFromId(key);
						if (ou.getuString().equals(dataManager.generateMyUstring()) && ou.getmString().equals(dataManager.generateMyMstring()))				
						{
							LogSystem.log5("Database up to Date with " + key);
						}
						else  // Optimisation direction: differanciate when it is only userAcoount or mùessages that are not up to date
						{
							LogSystem.log5("DB synchronization launched with " + key);
							ArrayOfIdsPacket aoip = new ArrayOfIdsPacket(localUser.getUserid(), localUser.getLocalHostAddr(), key,DataManager.generateArrayListOfmyUserId(),DataManager.generateArrayListOfmyMessageId(),0);
								try {
									sendPacketLocal(aoip,key);
								} catch (OfflineUserException | IOException e) {
									e.printStackTrace();
									System.out.println("Error in sending first message to synchronize databases");
								}
							
						}
					}	
					
				}
			}
		};
		
		updateDBSyncT = new Timer();
		
		//We can put a relatively long time like several minutes and messages also to update connectivity of users
		long period = DataManager.getUpdateDBSyncPeriod(); // time in ms between task
		updateDBSyncT.schedule(updateDBSyncTT,0, period);
	}
	
	private void updateUserStatus() {
		//The timertask for updating users about connectivity
		updateStatusTT = new TimerTask() {
			public void run() {
					String uS = localUser.getuString();
					String mS = localUser.getmString();
					System.out.println("UserUpdate");
					LogSystem.log5("userS: " + uS + "\nmsgS: " + mS);
					ArrayList<OnlineUser> ouInternetList = dynamicDM.getOnlineUsersInternet();
					
					for(OnlineUser internetUser: ouInternetList) {
						System.out.println("Notifying for " + internetUser.getUserid() );
						UUPacket uup = new UUPacket(internetUser.getPseudo(), internetUser.getUserid(),  false, mS, uS, localUser.getLocalHostAddr(), NetworkManager.getLocalBroadcastAddr());
						try {
							networkManager.notifyBroadcast(uup, NetworkManager.BroadcastType.Broadcast);
						} catch (IOException e) {
							LogSystem.log1("Notifing of user status failed", e);
						}
					}
					
					UUPacket uup = new UUPacket(localUser.getPseudo(), localUser.getUserid(),  false, mS, uS, localUser.getLocalHostAddr(), NetworkManager.getLocalBroadcastAddr());
					
					try {
						networkManager.notifyBroadcast(uup, NetworkManager.BroadcastType.Broadcast);
					} catch (IOException e) {
						LogSystem.log1("Notifing of user status failed", e);
					}
					
					
				}
		};
		
		updateStatusT = new Timer();
		//We can put a relatively long time like several minutes and messages also to update connectivity of users
		launchUpdateUserStatus();
	}
	
	private void launchUpdateUserStatus() {
		long period = DataManager.getUpdateStatusPeriod(); // time in ms between task
		updateStatusT.schedule(updateStatusTT,0, period);
	}

	
	//NetworkManager wrap-up methods
	/**
	 * Send a packet to a local user
	 * @param ap
	 * @param userId
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws OfflineUserException
	 */
	private void sendPacketLocal(AbstractPacket ap, String userId) throws UnknownHostException, IOException, OfflineUserException{
		//throws offlineException if unable to find the address ip from the uid
		InetAddress rmIp = dynamicDM.getOnlineUserUnicastAddr(userId);
		LogSystem.log3("Translation (" + rmIp.toString() + "<--" +  userId + ")");
		networkManager.sendPacket(ap, rmIp);
	}
	
	/**
	 * Send a packet to a local user
	 * @param ap
	 * @param userId
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws OfflineUserException
	 */
	private void sendPacketInternet(AbstractPacket ap, String userId) throws UnknownHostException, IOException, OfflineUserException{
		//throws offlineException if unable to find the address ip from the uid
		InetAddress rmIp = dynamicDM.getOnlineInternetUserUnicastAddr(userId);
		LogSystem.log3("Translation (" + rmIp.toString() + "<--" +  userId + ")");
		networkManager.sendPacket(ap, rmIp);
	}
	
	private void broadcastPacket(AbstractPacket ap) throws UnknownHostException, IOException, OfflineUserException{
		//throws offlineException if unable to find the address ip from the uid
		networkManager.notifyBroadcast(ap, NetworkManager.BroadcastType.Broadcast);
	}
	
	public void close() {
		nmThBool = false;
		LogSystem.log2("Controller threads closed");
		updateStatusT.cancel();
		updateDBSyncT.cancel();
		networkManager.close();
		dataManager.close();
		dynamicDM.close();
	}
	
	//DataManager wrap-up method
	
	private synchronized void updateMyUString() {
		localUser.setuString(dataManager.generateMyUstring());
	}
	
	private synchronized void updateMyMString() {
		localUser.setmString(dataManager.generateMyMstring());
	}
	
	


}
