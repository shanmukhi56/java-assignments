class ParkingTicket {

    // Static variable shared by all tickets
    private static int counter = 1000;

    // Instance variables
    private int ticketNo;
    private String vehicleNo;

    // Constructor
    ParkingTicket(String vehicleNo) {

        this.vehicleNo = vehicleNo;
        this.ticketNo = ++counter;
    }

    // Static method
    public static int getTotalTickets() {
        return counter - 1000;
    }

    public void display() {

        System.out.println("Ticket Number: " + ticketNo);
        System.out.println("Vehicle Number: " + vehicleNo);
    }
}

class ParkingFacility {

    private String facilityName;

    ParkingFacility(String facilityName) {
        this.facilityName = facilityName;
    }

    // Creates and returns an object
    public ParkingTicket issueTicket(String vehicleNo) {

        System.out.println("Facility: " + facilityName);

        return new ParkingTicket(vehicleNo);
    }

    // Receives an object
    public void closeTicket(ParkingTicket ticket) {

        System.out.println("Vehicle Exited:");
        ticket.display();
    }
}

public class Task05 {

    public static void main(String[] args) {

        System.out.println("CAMPUS SMART PARKING SYSTEM");

        // Create two parking facilities
        ParkingFacility north =
            new ParkingFacility("North Gate");

        ParkingFacility south =
            new ParkingFacility("South Gate");

        // Generate parking tickets
        ParkingTicket t1 =
            north.issueTicket("TN01AB1234");

        t1.display();

        System.out.println("--------------------");

        ParkingTicket t2 =
            south.issueTicket("TN02CD5678");

        t2.display();

        System.out.println("--------------------");

        ParkingTicket t3 =
            north.issueTicket("TN03EF9012");

        t3.display();

        System.out.println("--------------------");

        // Static method call
        System.out.println("Total Tickets Generated: "
            + ParkingTicket.getTotalTickets());

        // Close first ticket
        north.closeTicket(t1);

        // Remove reference
        t1 = null;

        System.out.println("Ticket reference removed.");

        System.out.println("Object is eligible "
            + "for garbage collection.");
    }
}
