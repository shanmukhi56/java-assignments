import java.util.List;
public interface RegistrationRepository {
    void insert(String studentId,String name,String event) throws Exception;
    List<Registration> list(String event,int limit,int offset) throws Exception;
}
