package DataManagement;
import java.util.Comparator;

public class MessageComparator implements Comparator<AbstractMessage> {
	    @Override
	    public int compare(AbstractMessage o1, AbstractMessage o2) {
	        return o1.getDate().compareTo(o2.getDate());
	    }
	
}
