package UserInfo;

import java.net.InetAddress;
public class OnlineUser  extends AbstractUserInfo {
	
	public enum UserType{
		JavaUser, WebUser
	}
	
	
	private boolean offlineFlag;
	private InetAddress localHostAddr;
	private InetAddress localBroadcastAddr; 
	private String uString; 
	private String mString;
	private UserType type;
	
	
	public String getuString() {
		return uString;
	}

	public void setuString(String uString) {
		this.uString = uString;
	}

	public String getmString() {
		return mString;
	}

	public void setmString(String mString) {
		this.mString = mString;
	}

	public OnlineUser(String userID, String pseudo, InetAddress ipA, InetAddress broadcastAddr, String uS, String mS) {
		super(userID, pseudo);
		type = UserType.JavaUser;
		offlineFlag = true;
		localHostAddr = ipA;
		localBroadcastAddr = broadcastAddr;
		uString = uS;
		mString = mS;
	}
	
	
	public UserType getType() {
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}

	public OnlineUser(String userID, InetAddress ipA, InetAddress broadcastAddr) {
		super(userID, null);
		type = UserType.JavaUser;
		localHostAddr = ipA;
		localBroadcastAddr = broadcastAddr;
	}
	
	public InetAddress getLocalBroadcastAddr() {
		return localBroadcastAddr;
	}
	
	public InetAddress getLocalHostAddr() {
		return localHostAddr;
	}

	
	
	public void setAddresses(InetAddress local, InetAddress bc) {
		localBroadcastAddr = local;
		localBroadcastAddr = bc;
	}

	
	public boolean getFlag() {
		return offlineFlag;
	}
	
	public void setFlag(boolean b) {
		offlineFlag = b;
	}
	
	
	public String toString()
	{
		return "Current pseudo: " + this.pseudo + " User id : " + this.userid + " BroadCast: " + localBroadcastAddr + " Addr: " + localHostAddr.toString() ;
		
	}

}