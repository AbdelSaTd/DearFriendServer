package DataManagement;
import java.lang.Comparable;

import Network.AbstractPacket;

@SuppressWarnings("serial")
public class MsgPacket extends AbstractPacket implements Comparable<MsgPacket> {
	


	public MsgPacket(AbstractMessage am) {
		super(PacketType.Message);
		msg = am;
	}

	private AbstractMessage msg;

	public AbstractMessage getMsg() {
		return msg;
	}

	public void setMsg(AbstractMessage msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "Message";
	}

	@Override
	public int compareTo(MsgPacket omp) {
		MsgPacket mp = (MsgPacket) omp;
		return this.msg.getDate().compareTo(mp.msg.getDate());
	}

}
