package DataManagement;

import java.io.Serializable;
import java.util.Date;

public abstract class AbstractMessage implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 6741441210679798653L;

	/**
	 * 
	 */
	//private static final long serialVersionUID = 6741441210679798653L;

	public enum MessageType{
	 Image, Text,File
 }

	private String senderid;
	private String receiverid; 
	private String date;
	private MessageType type; 

	public AbstractMessage(String senderid, String receiverid, Date date, MessageType type) {
		this.senderid = senderid;
		this.receiverid = receiverid;
		this.date = DataManager.dateToString(date);
		this.type = type;
	}
	
	public AbstractMessage(String senderid, String receiverid, String date, MessageType type) {
		this.senderid = senderid;
		this.receiverid = receiverid;
		this.date = date;
		this.type = type;
	}
	
	public static TextMessage getTextMessage(AbstractMessage a) {
		return (TextMessage) a;
	}
	
	public static ImageMessage getImageMessage(AbstractMessage a) {
		return (ImageMessage) a;
	}
	
	public static FileMessage getFileMessage(AbstractMessage a) {
		return (FileMessage) a;
	}

	public String getSenderid() {
		return senderid;
	}

	public void setSenderid(String usersenderid) {
		this.senderid = usersenderid;
	}

	public String getReceiverid() {
		return receiverid;
	}

	public void setReceiverid(String receiverid) {
		this.receiverid = receiverid;
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

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	@Override
	public abstract String toString() ;
}
