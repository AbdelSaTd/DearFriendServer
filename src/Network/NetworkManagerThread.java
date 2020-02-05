package Network;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.util.concurrent.LinkedBlockingQueue;

import Tools.LogSystem;

public class NetworkManagerThread implements Runnable {

	DatagramPacket packet;
	boolean isUdp;
	boolean continue_th;
	LinkedBlockingQueue<AbstractPacket> buffPacket;
	ObjectInputStream ois;
	
	public NetworkManagerThread(DatagramPacket p, boolean isUdp, LinkedBlockingQueue<AbstractPacket> bP)
	{
		continue_th = true;
		if(isUdp) {
			packet = p;
		}
		buffPacket = bP;
		
	}
	
	
	public void run() {
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
			AbstractPacket p = (AbstractPacket) ois.readObject();
			//sync
			//LogSystem.log3("Packet will be just sent to the controller");
			synchronized(buffPacket) {
				buffPacket.add(p);
			}
			//LogSystem.log3("Packet has just been sent to the controller");
			
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			LogSystem.log1("[ClassNotFoundException]: The object read is not of the good class");
			e.printStackTrace();
		}	
	}
}