package Network;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class CPPacket extends AbstractPacket {
	private ChallengeType typeCh;
	private boolean request;
	private String challenger;

	private InetAddress senderAddr;

	public enum ChallengeType{
		UserID, Pseudo
	}
		//Challenge Pseudo Packet
	public CPPacket(InetAddress senderAddR, boolean reQuest, String pseudoToChallenge) {
			super(PacketType.Challenge);
			
			request = reQuest;
			senderAddr = senderAddR;
			challenger = pseudoToChallenge;

		}
	
	public CPPacket(InetAddress senderAddR, boolean reQuest, String challenge, ChallengeType type) {
		super(PacketType.Challenge);
		
		this.typeCh = type;
		request = reQuest;
		senderAddr = senderAddR;
		challenger = challenge;
	}

	public ChallengeType getTypeCh() {
		return typeCh;
	}


	public boolean isRequest() {
		return request;
	}



	public void setRequest(boolean request) {
		this.request = request;
	}



	public String getChallenger() {
		return challenger;
	}



	public void setChallenger(String pseudo) {
		this.challenger = pseudo;
	}


	public InetAddress getSenderAddr() {
		return senderAddr;
	}

	public void setSenderAddr(InetAddress senderAddr) {
		this.senderAddr = senderAddr;
	}

	@Override
	public String toString() {
		return "New pseudo "+ this.senderAddr + "\nReq: " + request + "\nPse: " + challenger ;
				
	} 

}
