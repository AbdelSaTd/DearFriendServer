package Network;
import java.net.InetAddress;

public class UUPacket extends AbstractPacket {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2896859180187350423L;
	
	private String pseudo;
	private String userId;
	private InetAddress addr;
	private InetAddress bcAddr;
	private String mString; 
	private String uString;
	private boolean log_off;
	
	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public InetAddress getUnicastAddr() {
		return addr;
	}

	public void setUnicastAddr(InetAddress addr) {
		this.addr = addr;
	}

	public InetAddress getBroadcastAddr() {
		return bcAddr;
	}

	public void setBroadcastAddr(InetAddress bca) {
		this.bcAddr = bca;
	}

	public String getmString() {
		return mString;
	}

	public void setmString(String mString) {
		this.mString = mString;
	}

	public String getuString() {
		return uString;
	}

	public void setuString(String uString) {
		this.uString = uString;
	}

	public boolean isLog_off() {
		return log_off;
	}

	public void setLog_off(boolean log_off) {
		this.log_off = log_off;
	}

	public UUPacket(String p, String ui, boolean lo, String mString, String uString, InetAddress add, InetAddress bca) {
		super(AbstractPacket.PacketType.UserUpdate);
		pseudo = p;
		userId = ui;
		log_off = lo;
		addr = add;
		bcAddr = bca;
		this.mString=mString; 
		this.uString=uString;
	}
	/**
	 * Create an user update packet for the internet users
	 * @param p
	 * @param ui
	 * @param add
	 * @param bca
	 * @param lo
	 */
	public UUPacket(String p, String ui, InetAddress add, InetAddress bca,  boolean lo) {
		super(AbstractPacket.PacketType.UserInternetUpdate);
		pseudo = p;
		userId = ui;
		log_off = lo;
		addr = add;
		bcAddr = bca;
		this.mString=null; 
		this.uString=null;
	}

	@Override
	public String toString() {
		return "UUPacket";
	}

	
}