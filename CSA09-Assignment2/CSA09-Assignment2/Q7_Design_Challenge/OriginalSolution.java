
public class OriginalSolution {

    public static String generateTicket(
            String[] fields) {

        String ticket = "";

        for (String field : fields) {
            ticket = ticket + field + "\n";
        }

        return ticket;
    }

    public static void main(String[] args) {

        String[] fields = {
            "PNR: 1234567890",
            "Passenger: Arun",
            "Train: Chennai Express",
            "Coach: S3",
            "Berth: 45",
            "Fare: Rs.850"
        };

        System.out.println("ORIGINAL TICKET");
        System.out.println(generateTicket(fields));
    }
}