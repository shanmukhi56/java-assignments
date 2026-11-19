public class JdbcIntegrationTest {
    public static void main(String[] args)throws Exception{
        String id="T"+Long.toString(System.currentTimeMillis(),36).toUpperCase();
        RegistrationService s=new RegistrationService(new JdbcRegistrationDao());
        s.register(id,"Integration Test","Quiz");
        try{s.register(id,"Integration Test","Quiz");throw new AssertionError("Duplicate accepted");}catch(IllegalArgumentException expected){System.out.println("PASS duplicate rejected by MySQL");}
        s.register(id,"Integration Test","Music");
        boolean found=false;
        for(int page=0;;page++){java.util.List<Registration> rows=s.page("Quiz",page);for(Registration r:rows)if(r.studentId.equals(id))found=true;if(rows.size()<51)break;}
        if(!found)throw new AssertionError("Inserted row not found");
        System.out.println("PASS MySQL insert, uniqueness and filtered read: "+id);
        System.out.println("Two test rows remain for inspection. Remove manually after recording evidence if desired.");
    }
}
