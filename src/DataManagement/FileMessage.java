package DataManagement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;


@SuppressWarnings("serial")
public class FileMessage extends AbstractMessage{

	private String filename; // Represent the location+filename in the file system of an image
	private String path;
	private byte[] data;

public String getFilename() {
	return filename;
}
public void setFilename(String filename) {
	this.filename = filename;
}
public byte[] getData() {
	return data;
}
public void setData(byte[] data) {
	this.data = data;
}
public FileMessage(String userid, String receiverid, Date date,String path) throws IOException
{
	super(userid, receiverid, date, MessageType.File);
	this.path = path;
	filename=DataManager.pathTofilename(path);
	
	data = Files.readAllBytes(Paths.get(path));
}

public FileMessage(String userid, String receiverid, String date,String path) throws IOException
{
	super(userid, receiverid, date, MessageType.File);
	this.path = path;
	filename=DataManager.pathTofilename(path);
	
	data = Files.readAllBytes(Paths.get(path));
}
	public String toString()
	{
		return "Message <from: " + getSenderid() + "> <to: " + getReceiverid() + "> <at:" + getDate().toString() + "> <Type: Image> <Filename: "+ filename + ">";
	}
}
