
public class TriageTest {

    public static void main(String[] args) {

        Triage er = new Triage();

        er.admit("Arun", 4);
        er.admit("Priya", 2);
        er.admit("Kiran", 1);
        er.admit("Divya", 3);
        er.admit("Vijay", 1);
        er.admit("Neha", 5);
        er.admit("Rahul", 2);
        er.admit("Sneha", 3);

        System.out.println("Patient Treatment Order:");

        Patient p;
        int position = 1;

        while ((p = er.next()) != null) {
            System.out.println(position + ". " + p);
            position++;
        }
    }
}