import java.sql.*;
import java.util.*;
public class JdbcRegistrationDao implements RegistrationRepository {
    private Connection connect() throws SQLException {
        String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/campus_events?connectTimeout=5000&socketTimeout=10000");
        String user=System.getenv().getOrDefault("DB_USER","campus_app");
        String password=System.getenv("DB_PASSWORD");
        if(password==null)throw new SQLException("Set DB_PASSWORD before starting the application.");
        return DriverManager.getConnection(url,user,password);
    }
    public void insert(String id,String name,String event) throws SQLException {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO registrations(student_id,student_name,event_name) VALUES(?,?,?)")){
            p.setString(1,id);p.setString(2,name);p.setString(3,event);p.executeUpdate();
        }
    }
    public List<Registration> list(String event,int limit,int offset) throws SQLException {
        boolean all=event.equals("All");
        String sql="SELECT id,student_id,student_name,event_name FROM registrations"+(all?"":" WHERE event_name=?")+" ORDER BY id LIMIT ? OFFSET ?";
        try(Connection c=connect();PreparedStatement p=c.prepareStatement(sql)){
            int i=1;if(!all)p.setString(i++,event);p.setInt(i++,limit);p.setInt(i,offset);
            List<Registration> out=new ArrayList<>();try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Registration(r.getLong(1),r.getString(2),r.getString(3),r.getString(4)));}return out;
        }
    }
}
