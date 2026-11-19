
public class RedesignedSolution {

    private final StringBuilder builder =
        new StringBuilder(1024);

    public String generateTicket(String[] fields) {

        // Clear previous ticket without
        // discarding the allocated buffer
        builder.setLength(0);

        for (String field : fields) {
            builder.append(field).append('\n');
        }

        return builder.toString();
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

        RedesignedSolution generator =
            new RedesignedSolution();

        System.out.println("REDESIGNED TICKET");
        System.out.println(
            generator.generateTicket(fields)
        );
    }
}
