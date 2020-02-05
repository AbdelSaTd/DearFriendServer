package Network;
import java.util.Date;

import DataManagement.DataManager;

public class NAPacket extends AbstractPacket{
/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;




/**
 * Create a new instance of NAPacket (New Account Packet)
 * @param senderid
 * @param receiverid
 * @param userId
 * @param hashedPwd
 * @param date
 */
public NAPacket(String senderid, String receiverid, String userId, String hashedPwd, String date) {
	super(AbstractPacket.PacketType.NewAccount, senderid, receiverid);
	this.hashedPwd = hashedPwd;
	this.date = date;
	this.userId = userId;
}

/**
 * Create a new instance of NAPacket (New Account Packet)
 * @param userId
 * @param hashedPwd
 * @param date
 */
public NAPacket(String userId, String hashedPwd, String date) {
	super(AbstractPacket.PacketType.NewAccount);
	this.hashedPwd = hashedPwd;
	this.date = date;
	this.userId = userId;
}

/**
 * Create a new instance of NAPacket (New Account Packet)
 * @param userId
 * @param hashedPwd
 * @param date
 */
public NAPacket(String userId, String hashedPwd, Date date) {
	super(AbstractPacket.PacketType.NewAccount);
	this.hashedPwd = hashedPwd;
	this.date = DataManager.dateToString(date);
	this.userId = userId;
}



public String getHashedPwd() {
	return hashedPwd;
}



public void setHashedPwd(String hashedPwd) {
	this.hashedPwd = hashedPwd;
}




public String getDate() {
	return date;
}



public void setDate(Date date) {
	this.date = DataManager.dateToString(date);
}

public void setDate(String date) {
	this.date = date;
}

private String hashedPwd;
private String userId;
private String date;



@Override
public String toString() {
	return "New pseudo "+ this.userId + "\nPwd: " + hashedPwd ;
}

public String getUserId() {
	return userId;
} 

}
