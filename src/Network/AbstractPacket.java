package Network;

import java.io.Serializable;


public abstract class AbstractPacket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3706837198411441616L;

	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;

	/*
	 * UDPacket: UserDataPacket : Is used to send a list of user (request=false) or to ask for a list (request=true)
	 * UUPacket: UserUpdate : Is used to notify other users about pseudo changing or logging off
	 * NPPacket: NewPassword: Used to  notify that the sender (of this packet) has change its password
	 * MsgPacket: Message: a message sent
	 */
	public enum PacketType{
		Message,UserUpdate,NewAccount,ArrayOfIds, Challenge,DatabaseSignature, UserInternetUpdate
	}

	private PacketType type;
	private String senderUID;
	private String receivUID;
	
	public String getSenderUID() {
		return senderUID;
	}

	public void setSenderUID(String senderUID) {
		this.senderUID = senderUID;
	}

	public String getReceivUID() {
		return receivUID;
	}

	public void setReceivUID(String receivUID) {
		this.receivUID = receivUID;
	}

	public AbstractPacket(PacketType type, String senderUID, String receivUID) {
		this.type = type;
		this.senderUID = senderUID;
		this.receivUID = receivUID;
	}

	public AbstractPacket(PacketType t) {
		type = t;
	}
	
	public PacketType getType() {
		return type;
	}
	
	public PacketType setType(PacketType pt) {
		return type = pt;
	}
	
	public AbstractPacket() {
	}


}
