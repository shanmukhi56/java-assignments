
package parking.service;

import parking.model.ParkingTicket;

public class ParkingFacility {

    private final ParkingTicket[][] slots;

    // Constructor
    public ParkingFacility() {

        // Two floors with different capacities
        slots = new ParkingTicket[2][];

        slots[0] = new ParkingTicket[3];
        slots[1] = new ParkingTicket[2];
    }

    // Package-private method
    boolean isValidSlot(int floor, int slot) {

        return floor >= 0
            && floor < slots.length
            && slot >= 0
            && slot < slots[floor].length;
    }

    // Public method to park a vehicle
    public ParkingTicket parkVehicle(
            String vehicleNo, int floor, int slot) {

        if (!isValidSlot(floor, slot)) {

            throw new IllegalArgumentException(
                "Invalid floor or slot number");
        }

        if (slots[floor][slot] != null) {

            throw new IllegalArgumentException(
                "Slot is already occupied");
        }

        ParkingTicket ticket =
            new ParkingTicket(
                vehicleNo, floor, slot);

        slots[floor][slot] = ticket;

        return ticket;
    }

    // Public method to calculate fee
    public double calculateFee(
            ParkingTicket ticket) {

        int minutes = ticket.getDuration();

        int hours =
            (int) ((minutes + 59L) / 60);

        return hours * 30.0;
    }
}
