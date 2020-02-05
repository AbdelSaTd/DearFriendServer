package UserInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import DataManagement.DataManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class UserAccount extends AbstractUserInfo {
	
	private String hashedPassword; 
	private String registerDate;
	private boolean isSetUp;
	
	public UserAccount() {
		super("none", "none");
		isSetUp = false;
	}
	
	public UserAccount(String userid, String pseudo, String hashedPassword, Date registerDate) {
		super(userid, pseudo);
		this.hashedPassword = hashedPassword;
		this.registerDate = DataManager.dateToString(registerDate);
		isSetUp = true;
	}
	
	public UserAccount(String userid, String pseudo, String hashedPassword, String registerDate) {
		super(userid, pseudo);
		this.hashedPassword = hashedPassword;
		this.registerDate = registerDate;
		isSetUp = true;
	}
	
	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	public String getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(Date registerDate) {
		this.registerDate = DataManager.dateToString(registerDate);
	}
	
	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}
	public String toString()
	{
		return "Current pseudo: " + this.pseudo + " User id : " + this.userid;
		
	}
}