package Network;

public class OfflineUserException extends Exception {

	/**
	 * 
	 */
	
	private String content;
	private String userId;
	private static final long serialVersionUID = 6181972631295280558L;
	
	public OfflineUserException(String c, String uid){
		content = c;
		userId = uid;
	}
	
	@Override
	public String toString() {
		return content;
	}

}
