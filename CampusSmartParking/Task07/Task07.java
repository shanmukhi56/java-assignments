
abstract class Vehicle {

    private final String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    // Final method
    protected final int billableHours(int minutes) {

        if (minutes < 0) {
            throw new IllegalArgumentException(
                "Invalid parking duration");
        }

        return (int) ((minutes + 59L) / 60);
    }

    // Abstract method
    public abstract double calculateFee(int minutes);

    // Override toString()
    @Override
    public String toString() {

        return getClass().getSimpleName()
            + " - " + regNo;
    }

    // Override equals()
    @Override
    public final boolean equals(Object obj) {

        if (obj instanceof Vehicle) {

            Vehicle v = (Vehicle) obj;

            return regNo.equals(v.regNo);
        }

        return false;
    }

    // Override hashCode()
    @Override
    public final int hashCode() {
        return regNo.hashCode();
    }
}

// TwoWheeler subclass
class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes) {
        return billableHours(minutes) * 10.0;
    }
}

// Car subclass
class Car extends Vehicle {

    Car(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes) {
        return billableHours(minutes) * 30.0;
    }
}

// ElectricCar subclass
final class ElectricCar extends Car {

    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes) {
        return super.calculateFee(minutes);
    }

    public double chargingFee(int kWh) {
        return kWh * 12.0;
    }
}

// Main class
public class Task07 {

    public static void main(String[] args) {

        System.out.println(
            "CAMPUS SMART PARKING SYSTEM");

        TwoWheeler bike =
            new TwoWheeler("TN01A1234");

        Car car =
            new Car("TN02B5678");

        ElectricCar ev =
            new ElectricCar("TN03C9012");

        int minutes = 130;

        System.out.println("\nVehicle Details:");

        System.out.println(bike);
        System.out.println(car);
        System.out.println(ev);

        System.out.println("\nParking Fee:");

        System.out.println("Duration: "
            + minutes + " minutes");

        System.out.println("TwoWheeler Fee: Rs "
            + bike.calculateFee(minutes));

        System.out.println("Car Fee: Rs "
            + car.calculateFee(minutes));

        System.out.println("ElectricCar Fee: Rs "
            + ev.calculateFee(minutes));

        System.out.println("Charging Fee: Rs "
            + ev.chargingFee(10));

        System.out.println("Total ElectricCar Fee: Rs "
            + (ev.calculateFee(minutes)
            + ev.chargingFee(10)));

        // equals() and hashCode() demonstration

        Car car2 = new Car("TN02B5678");

        ElectricCar ev2 =
            new ElectricCar("TN02B5678");

        System.out.println("\nEquality Checking:");

        System.out.println("Car and Car2 equal: "
            + car.equals(car2));

        System.out.println("Car and EV2 equal: "
            + car.equals(ev2));

        System.out.println("Same Hash Code: "
            + (car.hashCode() == ev2.hashCode()));

        System.out.println("Car and Bike equal: "
            + car.equals(bike));
    }
}
