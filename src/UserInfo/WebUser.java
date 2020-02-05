package UserInfo;

import java.net.InetAddress;

import javax.servlet.http.HttpSession;

public class WebUser extends OnlineUser {
	private HttpSession session;
	
	public WebUser(String userID, String pseudo, InetAddress ipA, InetAddress broadcastAddr, String uS,
			String mS, HttpSession session) {
		super(userID, pseudo, ipA, broadcastAddr, uS, mS);
		this.session = session;
		
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	
}
