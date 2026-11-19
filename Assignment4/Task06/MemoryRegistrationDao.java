import java.util.*;
import java.sql.SQLException;
/** Explicit demonstration mode; does not establish JDBC/MySQL integration. */
public class MemoryRegistrationDao implements RegistrationRepository {
    private final List<Registration> rows=new ArrayList<>();
    public synchronized void insert(String id,String name,String event)throws SQLException{
        for(Registration r:rows)if(r.studentId.equals(id)&&r.event.equals(event))throw new SQLException("Duplicate registration","23000",1062);
        rows.add(new Registration(rows.size()+1,id,name,event));
    }
    public synchronized List<Registration> list(String event,int limit,int offset){
        List<Registration> matches=new ArrayList<>();for(Registration r:rows)if(event.equals("All")||r.event.equals(event))matches.add(r);
        return new ArrayList<>(matches.subList(Math.min(offset,matches.size()),Math.min(offset+limit,matches.size())));
    }
}
