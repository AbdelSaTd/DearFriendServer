package UserInfo;


import java.io.IOException;
import java.net.InetAddress;

public class OtherUser  extends AbstractUserInfo {
	
	private boolean offlineFlag;
	private String registeredDate;
	private InetAddress localHostAddr;
	private InetAddress localBroadcastAddr; 
	private String uString; 
	private String mString;

	public OtherUser(String userID, String pseudo, String regDate, InetAddress ipA, InetAddress broadcastAddr, String uS, String mS) {
		super(userID, pseudo);
		offlineFlag = true;
		registeredDate = regDate;
		localHostAddr = ipA;
		localBroadcastAddr = broadcastAddr;
		uString = uS;
		mString = mS;
	}
	
	public OtherUser(String userID, InetAddress ipA, InetAddress broadcastAddr) {
		super(userID, null);
		
		registeredDate = null;
		localHostAddr = ipA;
		localBroadcastAddr = broadcastAddr;
	}
	
	public InetAddress getLocalBroadcastAddr() {
		return localBroadcastAddr;
	}
	
	public InetAddress getLocalHostAddr() {
		return localHostAddr;
	}
	
	public String getRegisterDate() {
		return registeredDate;
	}
	public void setRegisterDate(String registeredDate) {
		this.registeredDate = registeredDate;
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
	
	
	public String toString()
	{
		return "Current pseudo: " + this.pseudo + " User id : " + this.userid + " BroadCast: " + localBroadcastAddr + " Addr: " + localHostAddr.toString() ;
		
	}

}
