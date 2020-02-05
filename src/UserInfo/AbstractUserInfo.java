package UserInfo;


public abstract class AbstractUserInfo implements Comparable<AbstractUserInfo>{
public AbstractUserInfo(String userid, String pseudo) {
		this.userid = userid;
		this.pseudo = pseudo;
	}
String userid;
String pseudo;

@Override
public int compareTo(AbstractUserInfo other)
{
	return  this.userid.compareTo(other.userid);
}

public String getUserid() {
	if(userid == null) {
		return "none_userid";
	}
	else {
		return userid;
	}
	
}
public void setUserid(String userid) {
	this.userid = userid;
}
public String getPseudo() {
	return pseudo;
}
public void setPseudo(String pseudo) {
	this.pseudo = pseudo;
}
@Override
public abstract String toString() ;


}
