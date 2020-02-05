package DataManagement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;


@SuppressWarnings("serial")
public class ImageMessage extends AbstractMessage{

	private String filename;
	private String path;
	private byte[] data;

	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public ImageMessage(String userid, String receiverid, Date date,String path) throws IOException
	{
		super(userid, receiverid, date, MessageType.Image);
		//String path=DataManager.getDfDir().concat(DataManager.getFileSep()).concat(userid).concat("-").concat(receiverid).concat(DataManager.getFileSep()).concat(fileName);

		this.path = path;
		filename=DataManager.pathTofilename(path);
		
		data = Files.readAllBytes(Paths.get(path));
	}
	
	public ImageMessage(String userid, String receiverid, String date, String path) throws IOException
	{
		super(userid, receiverid, date, MessageType.Image);
		this.path = path;
		filename=DataManager.pathTofilename(path);
		data = Files.readAllBytes(Paths.get(path));

	}
		public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String toString()
	{
		return "Message <from: " + getSenderid() + "> <to: " + getReceiverid() + "> <at:" + getDate().toString() + "> <Type: Image> <Filename: "+ filename + ">";
		
	}
}
