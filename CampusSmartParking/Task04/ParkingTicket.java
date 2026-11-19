class Vehicle {

    private String regNo;
    private String type;

    // Parameterized constructor
    Vehicle(String regNo, String type) {
        this.regNo = regNo;
        this.type = type;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getType() {
        return type;
    }
}

class ParkingSlot {

    private int floor;
    private int slot;

    // No-argument constructor
    ParkingSlot() {
        this(0, 0);
    }

    // Parameterized constructor
    ParkingSlot(int floor, int slot) {
        this.floor = floor;
        this.slot = slot;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlot() {
        return slot;
    }
}

public class ParkingTicket {

    private int ticketNo;
    private Vehicle vehicle;
    private ParkingSlot parkingSlot;

    // Parameterized constructor
    ParkingTicket(int ticketNo, Vehicle vehicle,
                  ParkingSlot parkingSlot) {

        this.ticketNo = ticketNo;
        this.vehicle = vehicle;
        this.parkingSlot = parkingSlot;
    }

    // Overloaded constructor
    ParkingTicket(int ticketNo, Vehicle vehicle) {

        this(ticketNo, vehicle, new ParkingSlot());
    }

    public void display() {

        System.out.println("Ticket Number: " + ticketNo);

        System.out.println("Vehicle Number: "
                + vehicle.getRegNo());

        System.out.println("Vehicle Type: "
                + vehicle.getType());

        System.out.println("Floor Number: "
                + parkingSlot.getFloor());

        System.out.println("Slot Number: "
                + parkingSlot.getSlot());

        System.out.println("---------------------");
    }

    public static void main(String[] args) {

        Vehicle v1 = new Vehicle("TN01AB1234", "Car");

        Vehicle v2 = new Vehicle("TN02CD5678", "TwoWheeler");

        ParkingSlot s1 = new ParkingSlot(1, 5);

        ParkingTicket t1 = new ParkingTicket(1001, v1, s1);

        ParkingTicket t2 = new ParkingTicket(1002, v2);

        System.out.println("CAMPUS SMART PARKING SYSTEM");

        t1.display();
        t2.display();
    }
}
