package Tools;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogSystem {

	private static boolean debug = false;
	private static boolean debug2 = false;
	private static boolean debug3 = true;
	private static boolean debug4 = false;
	private static boolean debug5 = false;
	
	/**
	 * This log is dedicated to exceptions that could occur.
	 * This methods just save in the logs a string message
	 * @param logmsg
	 */
	public static void log1(String logmsg) {
		if(debug) {
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			Date dateobj = new Date();

			System.out.println("[Exception]" + "< " + df.format(dateobj) + " > : " + logmsg);
			//Save into a file (need to be synchronized)
		}
		
	}
	
	
	/**
	 * This log is dedicated to exceptions that could occur.
	 * This methods just save in the logs a string message and more detailed information about the exception
	 * @param logmsg : the message
	 * @param e : the exception
	 */
	public static void log1(String logmsg, Exception e) {
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		Date dateobj = new Date();

		System.out.println("[\033[0;31mException\033[0m]" + "< " + df.format(dateobj) + " > : " + logmsg);
		System.out.println(e);
		e.printStackTrace();
		
		//Save into a file (need to be synchronized)

	}
	
	
	/**
	 * This log is dedicated to just print out messages about events related to the stated of the application
	 * This methods just save in the logs a string message
	 * @param logmsg
	 */
	public static void log2(String logmsg) {
		if(debug) {
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			Date dateobj = new Date();

			System.out.println("[State]" + "< " + df.format(dateobj) + " > : " + logmsg);
			//Save into a file (need to be synchronized)
		}
		
	}
	
	
	/**
	 * This log is dedicated for debug comment.
	 * This methods just print
	 * @param logmsg
	 */
	public static void log3(String logmsg) {
		if(debug3) {
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			Date dateobj = new Date();

			System.out.println("[\033[0;31mException\033[0m]" + "< " + df.format(dateobj) + " > : " + logmsg);
			//Save into a file (need to be synchronized)
		}
		
	}
	
	
	/**
	 * This log is dedicated for debug comment.
	 * This methods just print
	 * @param logmsg
	 */
	public static void log4(String logmsg) {
		if(debug4) {
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			Date dateobj = new Date();

			System.out.println("[\033[0;31mException\033[0m]" + "< " + df.format(dateobj) + " > : " + logmsg);
			//Save into a file (need to be synchronized)
		}
		
	}
	
	
	/**
	 * This log is dedicated for debug comment.
	 * This methods just print
	 * @param logmsg
	 */
	public static void log5(String logmsg) {
		if(debug5) {
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			Date dateobj = new Date();

			System.out.println("[DBSync]" + "< " + df.format(dateobj) + " > : " + logmsg);
			//Save into a file (need to be synchronized)
		}
		
	}
	
	
	
	
	
}
