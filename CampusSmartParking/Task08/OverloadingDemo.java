
import java.time.LocalTime;
import java.time.Duration;

class TimedCar extends Car {

    TimedCar(String regNo) {
        super(regNo);
    }

    // Overloaded method
    public double calculateFee(
            LocalTime in, LocalTime out) {

        if (out.isBefore(in)) {
            throw new IllegalArgumentException(
                "Invalid exit time");
        }

        int minutes = (int) Duration
            .between(in, out).toMinutes();

        return calculateFee(minutes);
    }
}

public class OverloadingDemo {

    public static void main(String[] args) {

        TimedCar car =
            new TimedCar("TN02B2");

        System.out.println(
            "COMPILE-TIME POLYMORPHISM");

        // Method 1: int parameter
        System.out.println(
            "Fee using minutes: Rs "
            + car.calculateFee(130));

        // Method 2: LocalTime parameters
        LocalTime entry = LocalTime.of(9, 0);

        LocalTime exit = LocalTime.of(11, 10);

        System.out.println("Entry Time: " + entry);

        System.out.println("Exit Time: " + exit);

        System.out.println(
            "Fee using entry and exit time: Rs "
            + car.calculateFee(entry, exit));
    }
}
