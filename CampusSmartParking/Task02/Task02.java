
public class Task02 {

    public static void main(String[] args) {

        // Vehicle information
        String vehicleNo = "TN01AB1234";
        String vehicleType = "Car";
        int duration = 90;
        double rate = 30.0;
        boolean occupied = true;

        // Jagged array for parking floors
        String[][] slots = new String[3][];

        slots[0] = new String[20];
        slots[1] = new String[30];
        slots[2] = new String[50];

        // Allocate a sample vehicle
        slots[0][0] = vehicleNo;

        // Integer division
        int wrongHours = duration / 60;

        // Correct billable hours
        int correctHours = (duration + 59) / 60;

        double wrongFee = wrongHours * rate;
        double correctFee = correctHours * rate;

        System.out.println("CAMPUS SMART PARKING SYSTEM");

        System.out.println("Vehicle Number: " + vehicleNo);
        System.out.println("Vehicle Type: " + vehicleType);
        System.out.println("Occupied: " + occupied);

        System.out.println("\nParking Floors:");

        for (int i = 0; i < slots.length; i++) {
            System.out.println("Floor " + (i + 1)
                + ": " + slots[i].length + " slots");
        }

        System.out.println("\nFee Calculation:");

        System.out.println("Duration: " + duration + " minutes");

        System.out.println("Wrong Billable Hours: " + wrongHours);
        System.out.println("Wrong Fee: Rs " + wrongFee);

        System.out.println("Correct Billable Hours: " + correctHours);
        System.out.println("Correct Fee: Rs " + correctFee);
    }
}
