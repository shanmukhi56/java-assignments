import java.nio.file.*;
public class ServiceTest {
    public static void main(String[] args)throws Exception{
        RegistrationService s=new RegistrationService(new MemoryRegistrationDao());
        s.register("s001","Student One","Music");
        if(s.page("All",0).size()!=1)throw new AssertionError();
        try{s.register("S001","Student One","Music");throw new AssertionError();}catch(IllegalArgumentException expected){System.out.println("PASS duplicate rejected");}
        try{s.register("","Student One","Music");throw new AssertionError();}catch(IllegalArgumentException expected){System.out.println("PASS invalid ID rejected");}
        s.register("s001","Student One","Quiz");if(s.page("Music",0).size()!=1)throw new AssertionError();
        for(int i=2;i<=55;i++)s.register("S"+String.format("%03d",i),"Student "+i,"Music");
        if(s.page("Music",0).size()!=51||s.page("Music",1).size()!=5)throw new AssertionError("Pagination");
        Path f=Files.createTempFile("events-test",".csv");if(s.export(f,"Music")!=55)throw new AssertionError();if(Files.readAllLines(f).size()!=56)throw new AssertionError();Files.delete(f);
        if(!RegistrationService.csv("=SUM(1,2)").startsWith("\"'="))throw new AssertionError();
        System.out.println("PASS normal registration, different event, filter, pagination and CSV export");
        System.out.println("Backend: memory test double. MySQL integration not tested by this test.");
    }
}
