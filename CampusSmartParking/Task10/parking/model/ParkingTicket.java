
package parking.model;

public class ParkingTicket {

    // Private fields
    private final String vehicleNo;
    private final int floor;
    private final int slot;
    private int duration;

    // Constructor
    public ParkingTicket(String vehicleNo,
                         int floor, int slot) {

        if (vehicleNo == null ||
                vehicleNo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "Invalid vehicle number");
        }

        if (floor < 0 || slot < 0) {
            throw new IllegalArgumentException(
                "Invalid parking position");
        }

        this.vehicleNo = vehicleNo;
        this.floor = floor;
        this.slot = slot;
        this.duration = 0;
    }

    // Getter methods
    public String getVehicleNo() {
        return vehicleNo;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlot() {
        return slot;
    }

    public int getDuration() {
        return duration;
    }

    // Setter with validation
    public void setDuration(int duration) {

        if (duration < 0) {

            throw new IllegalArgumentException(
                "Duration cannot be negative");
        }

        this.duration = duration;
    }

    // Display ticket details
    public void display() {

        System.out.println(
            "Vehicle Number: " + vehicleNo);

        System.out.println(
            "Floor Number: " + floor);

        System.out.println(
            "Slot Number: " + slot);

        System.out.println(
            "Duration: " + duration + " minutes");
    }
}
