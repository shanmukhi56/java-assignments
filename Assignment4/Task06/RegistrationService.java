import java.util.*;
import java.sql.SQLException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
public class RegistrationService {
    public static final String[] EVENTS={"Music","Dance","Quiz"};
    private final RegistrationRepository repo;
    public RegistrationService(RegistrationRepository repo){this.repo=repo;}
    public static String normalizeId(String id){return id.trim().toUpperCase(Locale.ROOT);}
    public static void validate(String id,String name,String event){
        if(!normalizeId(id).matches("[A-Z0-9-]{3,20}"))throw new IllegalArgumentException("Student ID must contain 3-20 letters/digits/hyphens.");
        if(name.trim().length()<2||name.trim().length()>80||name.trim().chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Name must contain 2-80 characters, without control characters.");
        if(!Arrays.asList(EVENTS).contains(event))throw new IllegalArgumentException("Select a valid event.");
    }
    public void register(String id,String name,String event)throws Exception{
        validate(id,name,event);
        try{repo.insert(normalizeId(id),name.trim(),event);}catch(SQLException e){if(e.getErrorCode()==1062)throw new IllegalArgumentException("Student already registered for this event.");throw e;}
    }
    public List<Registration> page(String event,int page)throws Exception{if(page<0)throw new IllegalArgumentException("Invalid page");return repo.list(event,51,Math.multiplyExact(page,50));}
    public static String csv(String value){if(!value.isEmpty()&&"=+-@\t\r".indexOf(value.charAt(0))>=0)value="'"+value;return "\""+value.replace("\"","\"\"")+"\"";}
    public int export(Path destination,String event)throws Exception{
        Path parent=destination.toAbsolutePath().getParent();Path tmp=Files.createTempFile(parent,"registrations-",".tmp");int count=0;
        try{try(BufferedWriter w=Files.newBufferedWriter(tmp,StandardCharsets.UTF_8)){
            w.write("ID,Student ID,Name,Event\r\n");
            for(int offset=0;;offset+=500){List<Registration> batch=repo.list(event,500,offset);for(Registration r:batch){w.write(r.id+","+csv(r.studentId)+","+csv(r.name)+","+csv(r.event)+"\r\n");count++;}if(batch.size()<500)break;}
        }Files.move(tmp,destination,StandardCopyOption.REPLACE_EXISTING);return count;}finally{Files.deleteIfExists(tmp);}
    }
}
