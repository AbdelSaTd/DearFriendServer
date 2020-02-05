package DataManagement;
import Network.AbstractPacket;

public class DatabaseSignaturePacket extends AbstractPacket {

	
	private static final long serialVersionUID = 1L;
	
	String uString; 
	String mString;
	public DatabaseSignaturePacket(PacketType t, String uString, String mString) {
		super(t);
		this.uString = uString;
		this.mString = mString;
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
	

}
