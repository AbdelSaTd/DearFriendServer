package Network;
import java.net.InetAddress;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ArrayOfIdsPacket extends AbstractPacket {


	private ArrayList<String> userIds;
	private ArrayList<String> messageIds;
	private InetAddress senderAddr;

	public InetAddress getSenderAddr() {
		return senderAddr;
	}





	public void setSenderAddr(InetAddress senderAddr) {
		this.senderAddr = senderAddr;
	}





	public ArrayList<String> getUserIds() {
		return userIds;
	}





	public void setUserIds(ArrayList<String> userIds) {
		this.userIds = userIds;
	}





	public ArrayList<String> getMessageIds() {
		return messageIds;
	}





	public void setMessageIds(ArrayList<String> messageIds) {
		this.messageIds = messageIds;
	}





	public Integer getState() {
		return state;
	}





	public void setState(Integer state) {
		this.state = state;
	}





	Integer state;
	public ArrayOfIdsPacket(String sender, InetAddress senderADdr, String receiver,ArrayList<String> ui, ArrayList<String> mi, Integer state) {
		super(AbstractPacket.PacketType.ArrayOfIds,sender,receiver);
		userIds=ui; 
		senderAddr = senderADdr;
		messageIds=mi;
		this.state=state;
	}





@Override
public String toString() {
	return "New ArrayOfIds"+ userIds.size() + "Userids and"+ messageIds.size()+ " MessageIds" ;
} 

	
	
}
