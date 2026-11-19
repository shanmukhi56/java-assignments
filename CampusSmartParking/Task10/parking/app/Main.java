
package parking.app;

import parking.model.ParkingTicket;
import parking.service.ParkingFacility;

public class Main {

    public static void main(String[] args) {

        System.out.println(
            "CAMPUS SMART PARKING SYSTEM");

        ParkingFacility facility =
            new ParkingFacility();

        // Park a vehicle
        ParkingTicket ticket =
            facility.parkVehicle(
                "TN01AB1234", 0, 1);

        // Set valid duration
        ticket.setDuration(130);

        System.out.println("\nParking Details:");

        ticket.display();

        System.out.println(
            "Parking Fee: Rs "
            + facility.calculateFee(ticket));

        // Test invalid duration
        System.out.println(
            "\nValidation Tests:");

        try {

            ticket.setDuration(-20);

        } catch (IllegalArgumentException e) {

            System.out.println(
                "Error: " + e.getMessage());
        }

        // Test invalid slot
        try {

            facility.parkVehicle(
                "TN02CD5678", 5, 10);

        } catch (IllegalArgumentException e) {

            System.out.println(
                "Error: " + e.getMessage());
        }

        // Test occupied slot
        try {

            facility.parkVehicle(
                "TN03EF9012", 0, 1);

        } catch (IllegalArgumentException e) {

            System.out.println(
                "Error: " + e.getMessage());
        }

        // Wrapper class and autoboxing
        System.out.println(
            "\nWrapper Class Demonstration:");

        Integer recordedMinutes =
            ticket.getDuration();

        // Unboxing
        int minutes = recordedMinutes;

        System.out.println(
            "Boxed Duration: "
            + recordedMinutes);

        System.out.println(
            "Unboxed Duration: " + minutes);

        Integer anotherDuration = 130;

        System.out.println(
            "Equal Values: "
            + recordedMinutes.equals(
                anotherDuration));

        System.out.println(
            "\nProgram completed successfully.");
    }
}