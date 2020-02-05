package TestDebug;
import Base.Controller;

public class NetworkTest {
	
	public static void main(String[] args) {
		// This is the true official main of the system. Please do not do test here. Create your own main.
		
		Controller controller = new Controller();
		
		//controller.cliControl();

		String remoteIP = "10.1.5.84";
		//controller.testNewAccountClient(remoteIP);
		controller.testSyncDBClient(remoteIP);
		//String remoteIP = "10.1.5.70";
		//controller.testNewAccountServer(remoteIP);

		
		//controller.close();

	}
}
