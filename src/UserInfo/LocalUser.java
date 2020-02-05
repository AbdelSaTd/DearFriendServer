package UserInfo;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Date;

import DataManagement.DataManager;
import Network.NetworkManager;

public class LocalUser extends AbstractUserInfo {

	private static boolean userSetUp = false;

	private String hashedPassword; 
	private String registeredDateS;
	private Date registeredDate;
	private String uString;
	private String mString;
	private InetAddress localHostAddr;
	private InetAddress localBroadcastAddr; 
	
	
	public LocalUser(String userId, String pseudo, String hashedPwd, String registeredDateS, String uString, String mString, InetAddress localHostAddr, InetAddress localBroadcastAddr) {
		super(userId, pseudo);
		this.registeredDateS = registeredDateS;
		this.localHostAddr = localHostAddr;
		this.localBroadcastAddr = localBroadcastAddr;
		this.uString = uString;
		this.hashedPassword = hashedPwd;
	}
	
	public static boolean isUserSetUp() {
		return userSetUp;
	}

	public static void setUserSetUp(boolean userSetUp) {
		LocalUser.userSetUp = userSetUp;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public LocalUser(DataManager dataManager) {
		super("none", "none");
		this.localHostAddr = NetworkManager.getLocalHostAddr();
		this.localBroadcastAddr = NetworkManager.getLocalBroadcastAddr();
		this.uString = dataManager.generateMyUstring();
		this.mString = dataManager.generateMyMstring();
	}
	
	
	public String getRegisteredDateS() {
		return registeredDateS;
	}



	public void setRegisteredDateS(String registeredDateS) {
		this.registeredDateS = registeredDateS;
		try {
			this.registeredDate = DataManager.stringToDate(registeredDateS);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}



	public Date getRegisteredDate() {
		return registeredDate;
	}



	public void setRegisteredDate(Date registeredDate) {
		this.registeredDate = registeredDate;
		this.registeredDateS = DataManager.dateToString(registeredDate);
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



	public InetAddress getLocalHostAddr() {
		return localHostAddr;
	}



	public void setLocalHostAddr(InetAddress localHostAddr) {
		this.localHostAddr = localHostAddr;
	}



	public InetAddress getLocalBroadcastAddr() {
		return localBroadcastAddr;
	}



	public void setLocalBroadcastAddr(InetAddress localBroadcastAddr) {
		this.localBroadcastAddr = localBroadcastAddr;
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
