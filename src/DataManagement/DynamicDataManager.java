package DataManagement;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import Base.Controller;
import Network.OfflineUserException;
import Network.UUPacket;
import Tools.LogSystem;
import UserInfo.OnlineUser;
import UserInfo.WebUser;



public class DynamicDataManager {
	
	private Thread othUserListTh;
	private HashMap<String,OnlineUser> otherUsersInfoLocal;
	private HashMap<String,OnlineUser> otherUsersInfoInternet;
	private boolean othUserThreadBool;
	private static boolean userSetUp = false;
	private Controller controller;

	public DynamicDataManager(Controller controller) {

		otherUsersInfoLocal = new HashMap<String, OnlineUser>();
		otherUsersInfoInternet = new HashMap<String, OnlineUser>();
		othUserListTh = offlineUserRemover();
		this.controller = controller;
		
	}

	
	
	/**
	 *  This methods allows to set the pseudo, the registered date , the ip address and the port of a remote user. If the user is unknown he is added to the collection
	 * @param uid : User ID -> String
	 * @param registeredDate : Date in which the user has been registered in the system -> String
	 * @param ipAddr : Array of 4 int that represent the 4 bytes of current IP address of this user
	 * @param port
	 */
	public OnlineUser setOnlineUserInfoLocal(String uid, String pseudo, String registeredDate, InetAddress ipAddr, InetAddress bcAddr, String uString, String mString ) {
		OnlineUser remoteUser;
		synchronized(otherUsersInfoLocal) {
			remoteUser = otherUsersInfoLocal.get(uid);
		}
			if(remoteUser == null) {
				OnlineUser ou = new OnlineUser(uid, pseudo, ipAddr, bcAddr, uString, mString);
				ou.setFlag(true);
				synchronized(otherUsersInfoLocal) {
					otherUsersInfoLocal.put(uid, ou);
				}
				remoteUser=ou;
			}
			else {
				remoteUser.setPseudo(pseudo);
				remoteUser.setAddresses(ipAddr, bcAddr);
				remoteUser.setuString(uString);
				remoteUser.setmString(mString);
				remoteUser.setFlag(true);
			}
			return remoteUser;
	
		
	}
	
	
	/**
	 *  This methods allows to add a new the registered date , the ip address and the port of a remote user. If the user is unknown he is add to the collection
	 * @param uid : User ID -> String
	 * @param registeredDate : Date in which the user has been registered in the system -> String
	 * @param ipAddr : Array of 4 int that represent the 4 bytes of current IP address of this user
	 * @param port
	 */
	public OnlineUser addOnlineUserLocal(String uid, String pseudo, String registeredDate, InetAddress ipAddr, InetAddress bcAddr, String uString, String mString ) {
		OnlineUser ou = new OnlineUser(uid, pseudo, ipAddr, bcAddr, uString, mString);
		ou.setFlag(true);
			synchronized(otherUsersInfoLocal) {
				otherUsersInfoLocal.put(uid, ou);
			}
			
		return ou;
	}
	
	
	/**
	 *  This methods allows to add a new internet user to the registered date , the ip address and the port of a remote user. If the user is unknown he is add to the collection
	 * @param uid : User ID -> String
	 * @param registeredDate : Date in which the user has been registered in the system -> String
	 * @param ipAddr : Array of 4 int that represent the 4 bytes of current IP address of this user
	 * @param port
	 */
	public OnlineUser addOnlineInternetUser(String uid, String pseudo, InetAddress ipAddr, InetAddress bcAddr, String uString, String mString, HttpSession session) {
		WebUser ou = new WebUser(uid, pseudo, ipAddr, bcAddr, uString, mString, session);
		ou.setType(UserInfo.OnlineUser.UserType.WebUser);
		ou.setFlag(true);
			synchronized(otherUsersInfoInternet) {
				otherUsersInfoInternet.put(uid, ou);
			}
			
		return ou;
	}
	
	
	/**
	 *  This methods allows to add a new internet user to the registered date , the ip address and the port of a remote user. If the user is unknown he is add to the collection
	 * @param uid : User ID -> String
	 * @param registeredDate : Date in which the user has been registered in the system -> String
	 * @param ipAddr : Array of 4 int that represent the 4 bytes of current IP address of this user
	 * @param port
	 */
	public OnlineUser addOnlineInternetUser(String uid, String pseudo, InetAddress ipAddr, InetAddress bcAddr, String uString, String mString) {
		OnlineUser ou = new OnlineUser(uid, pseudo, ipAddr, bcAddr, uString, mString);
		ou.setFlag(true);
			synchronized(otherUsersInfoInternet) {
				otherUsersInfoInternet.put(uid, ou);
			}
			
		return ou;
	}
	
	
	/*
	public void setOnlineUserInfo(String uid, InetAddress ipAddr, int port ) {
		synchronized(otherUsersInfoLocal) {
			OnlineUser remoteUser = otherUsersInfoLocal.get(uid);
			if(remoteUser == null) {
				OnlineUser ou = new OnlineUser(uid, ipAddr, port);
				otherUsersInfoLocal.put(uid, ou);
			}else {
				remoteUser.setAddr(ipAddr, port);
			}
		}
		
	}*/
	
	
	/**
	 * This methods allows to set up a new OnlineUser in the list of connected user by passing a OnlineUser object. 
	 * The methods uses the id to find the user and reset its timer.
	 * @param user
	 */
	/*public boolean addActiveUser(OnlineUser user)
	{
		boolean found=false;
		for (String k :otherUsersInfoLocal.keySet()) {
		if (otherUsersInfoLocal.get(k).getUserid().equals(user.getUserid())) {// Attention 
			found=true;
		}
		}
		if (found==false)
		{
			otherUsersInfoLocal.put(user.getUserid(),user);
		}
		
		return found;
		
	}*/
	
	public void removeActiveUserLocal(String id) {
		synchronized(otherUsersInfoLocal) {
			otherUsersInfoLocal.remove(id);
		}
		
	}
	
	public void removeActiveInternetUser(String id) {
		synchronized(otherUsersInfoInternet) {
			otherUsersInfoInternet.remove(id);
		}
		
	}
	
	/**
	 * This methods allows to activate the flag of a new connected user or add him if he is not again a connected user
	 * @param uid
	 */
	public void activateUserFlagLocal(UUPacket uup) {
		OnlineUser ou;
		synchronized(otherUsersInfoLocal) {
			ou = otherUsersInfoLocal.get(uup.getUserId());
		}
			if(ou != null) {
				if(ou.getPseudo().compareTo(uup.getPseudo()) != 0) {
					ou.setPseudo(uup.getPseudo());
				}
				
				if(!ou.getFlag()) {
					ou.setFlag(true);
					ou.setmString(uup.getmString());
					ou.setuString(uup.getuString());
				}
			}
			else {
				LogSystem.log3("Adding of a new connected user ");
				addOnlineUserLocal(uup.getUserId(), uup.getPseudo(), "ERROR", uup.getUnicastAddr(), uup.getBroadcastAddr(), uup.getuString(), uup.getmString());
			}
		
	}
	
	/**
	 * This methods allows to activate the flag of a new connected user or add him if he is not again a connected user
	 * @param uup
	 */
	public void activateInternetUserFlag(UUPacket uup, HttpSession session) {
		OnlineUser ou;
		synchronized(otherUsersInfoInternet) {
			ou = otherUsersInfoInternet.get(uup.getUserId());
		}
			if(ou != null) {
				if(ou.getPseudo().compareTo(uup.getPseudo()) != 0) {
					ou.setPseudo(uup.getPseudo());
				}
				
				if(!ou.getFlag()) {
					ou.setFlag(true);
					ou.setmString(uup.getmString());
					ou.setuString(uup.getuString());
				}
			}
			else {
				LogSystem.log3("Adding of a new connected user ");
				addOnlineInternetUser(uup.getUserId(), uup.getPseudo(), uup.getUnicastAddr(), uup.getBroadcastAddr(), uup.getuString(), uup.getmString(), session);
			}
		
	}
	
	/**
	 * This methods allows to activate the flag of a new connected user or add him if he is not again a connected user
	 * @param uup
	 */
	public void activateInternetUserFlag(UUPacket uup) {
		OnlineUser ou;
		synchronized(otherUsersInfoInternet) {
			ou = otherUsersInfoInternet.get(uup.getUserId());
		}
			if(ou != null) {
				if(ou.getPseudo().compareTo(uup.getPseudo()) != 0) {
					ou.setPseudo(uup.getPseudo());
				}
				
				if(!ou.getFlag()) {
					ou.setFlag(true);
					ou.setmString(uup.getmString());
					ou.setuString(uup.getuString());
				}
			}
			else {
				LogSystem.log3("Adding of a new connected user ");
				addOnlineInternetUser(uup.getUserId(), uup.getPseudo(), uup.getUnicastAddr(), uup.getBroadcastAddr(), uup.getuString(), uup.getmString());
			}
		
	}
	
	//OnlineUser
		//Get
	
	public OnlineUser getOnlineUserLocal(String uid) throws OfflineUserException {
		OnlineUser ou = otherUsersInfoLocal.get(uid);
		if(ou == null) {
			throw new OfflineUserException("Cannot find this user", uid);
		}
		return ou;
	}
	
	public OnlineUser getOnlineInternetUser(String uid) throws OfflineUserException {
		OnlineUser ou = otherUsersInfoInternet.get(uid);
		if(ou == null) {
			throw new OfflineUserException("Cannot find this user", uid);
		}
		return ou;
	}
	
	public InetAddress getOnlineUserUnicastAddr(String uid) throws OfflineUserException {
		return getOnlineUserLocal(uid).getLocalHostAddr();
	}
	
	public InetAddress getOnlineInternetUserUnicastAddr(String uid) throws OfflineUserException {
		return getOnlineInternetUser(uid).getLocalHostAddr();
	}
	
	/**
	 * This methods allows to retrieve the list of connected users on the local network
	 * @param uid
	 */
	public ArrayList<OnlineUser> getOnlineUsers() {
		OnlineUser ou;
		ArrayList<OnlineUser> aou = new ArrayList<>();
		synchronized(otherUsersInfoLocal) {
			Set<String> ids = otherUsersInfoLocal.keySet();
			for(String id: ids) {
				ou = otherUsersInfoLocal.get(id);
				if(ou!=null) {
					aou.add(ou);
				}
			}
		}
		return aou;
		
	}
	
	/**
	 * This methods allows to retrieve the list of connected users on the internet
	 * @param uid
	 */
	public ArrayList<OnlineUser> getOnlineUsersInternet() {
		OnlineUser ou;
		ArrayList<OnlineUser> aou = new ArrayList<>();
		synchronized(otherUsersInfoInternet) {
			Set<String> ids = otherUsersInfoInternet.keySet();
			for(String id: ids) {
				ou = otherUsersInfoInternet.get(id);
				if(ou!=null) {
					aou.add(ou);
				}
			}
		}
		return aou;
		
	}
	
	public boolean pseudoInActiveUser(String ps) {
		Iterator<String> it = otherUsersInfoLocal.keySet().iterator();
		boolean notfound = true;
		
		while (it.hasNext() && notfound) {
			String nxtUserId = it.next();
			
			if (otherUsersInfoLocal.get(nxtUserId).getPseudo().compareTo(ps) == 0) {// Attention compareTo not ==
				notfound = false;
			}
		}
		
		return !notfound;
	}
	
	
	public boolean pseudoInActiveInternetUser(String ps) {
		Iterator<String> it = otherUsersInfoInternet.keySet().iterator();
		boolean notfound = true;
		
		while (it.hasNext() && notfound) {
			String nxtUserId = it.next();
			
			if (otherUsersInfoInternet.get(nxtUserId).getPseudo().compareTo(ps) == 0) {// Attention compareTo not ==
				notfound = false;
			}
		}
		
		return !notfound;
	}
	
	
	public boolean userIdInActiveInternetUser(String ps) {
		Iterator<String> it = otherUsersInfoInternet.keySet().iterator();
		boolean notfound = true;
		
		while (it.hasNext() && notfound) {
			String nxtUserId = it.next();
			
			if (otherUsersInfoInternet.get(nxtUserId).getPseudo().compareTo(ps) == 0) {// Attention compareTo not ==
				notfound = false;
			}
		}
		
		return !notfound;
	}
	
	public ArrayList<String> getConnectedUsersId(){
		ArrayList<String> res = new ArrayList<>();
		Set<String> keys = otherUsersInfoLocal.keySet();
		synchronized(otherUsersInfoLocal) {
			for(String k: keys) {
				res.add(k);
			}
		}
		return res;
	}
	
	public ArrayList<String> getConnectedInternetUsersId(){
		ArrayList<String> res = new ArrayList<>();
		Set<String> keys = otherUsersInfoInternet.keySet();
		synchronized(otherUsersInfoInternet) {
			for(String k: keys) {
				res.add(k);
			}
		}
		return res;
	}
	
	public Integer getNbConnectedUser() {
		synchronized(otherUsersInfoLocal) {
			return otherUsersInfoLocal.size();
		}
	}
	
	
	public OnlineUser getConnectedUserFromId(String key_uid) {
		//UserAccount cu = new UserAccount(userid, pseudo, hashedPassword, registerDate);
		OnlineUser ou = null;
		synchronized(otherUsersInfoLocal) {
			ou = otherUsersInfoLocal.get(key_uid);
		}
		
		return ou;
	}
	
	public OnlineUser getConnectedInternetUserFromId(String key_uid) {
		//UserAccount cu = new UserAccount(userid, pseudo, hashedPassword, registerDate);
		OnlineUser ou = null;
		synchronized(otherUsersInfoInternet) {
			ou = otherUsersInfoInternet.get(key_uid);
		}
		
		return ou;
	}
	
	public WebUser getConnectedInternetUserFromSession(String id) {
		//UserAccount cu = new UserAccount(userid, pseudo, hashedPassword, registerDate);
		Iterator<String> it = otherUsersInfoInternet.keySet().iterator();
		boolean notfound = true;
		OnlineUser ou = null;
		WebUser wu = null;
		
		while (it.hasNext() && notfound) {
			String nxtUserId = it.next();
			ou = otherUsersInfoInternet.get(nxtUserId);
			if (ou.getType() == UserInfo.OnlineUser.UserType.WebUser) {// Attention compareTo not ==
				wu = (WebUser) ou;
				if(wu.getSession().getId().compareTo(id) == 0) {
					notfound = false;
				}
			}
		}
		
		if(notfound) {
			return null;
		}
		else {
			return wu;
		}
	}
	
	public Thread offlineUserRemover() {
		return new Thread(new Runnable() {
			public void run() {
				othUserThreadBool = true;
				while(othUserThreadBool) {
					long period = DataManager.getUpdateOnlineUserList()/2; //in ms either 2min
					
					try {
						Thread.sleep(period);
						ArrayList<String> toRemove = new ArrayList<>();
						synchronized(otherUsersInfoLocal) {
							for(Map.Entry<String, OnlineUser> entry: otherUsersInfoLocal.entrySet()) {
								OnlineUser ou = entry.getValue();
								if(ou!=null) {
									if(ou.getFlag()) {
										ou.setFlag(false);
									}
									else {
										toRemove.add(entry.getKey());
									}
								}
							}
							
							for(String rem: toRemove) {
								OnlineUser ou = otherUsersInfoLocal.remove(rem);
								controller.notifyOnlineInternetUsers(getOnlineUsersInternet(), ou);
							}									
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			}
		});
	}
	
	public void startRemoverOfflineUserThread() {
		othUserListTh.start();
	}
	
	
	
	/*public UserAccount getlocalUserAccount() {
		return localUserAccount;
	}*/
	
	
	/*
	 * Dangerrrrrrrous !
	public HashMap<String, OnlineUser> getOnlineUsersInfo() {
		return otherUsersInfoLocal;
	}
	*/
	

	public boolean isUserSetUp() {
		return userSetUp;
	}



	public void setUserSetUp(boolean usersetup) {
		userSetUp = usersetup;
	}
	
	
	
	//Buffers

	

	
	public void close() {
		othUserThreadBool = false;
	}
	
}
