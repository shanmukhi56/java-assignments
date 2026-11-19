// Abstract class
abstract class Vehicle {

    private final String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    // Abstract method
    protected abstract double ratePerHour();

    // Concrete method
    public double calculateFee(int minutes) {

        if (minutes < 0) {
            throw new IllegalArgumentException(
                "Invalid duration");
        }

        int hours = (int) ((minutes + 59L) / 60);

        return hours * ratePerHour();
    }
}

// Interface for electric vehicle charging
interface Chargeable {

    double chargingFee(int kWh);

    // Static interface method
    static double ratePerKWh() {
        return 12.0;
    }
}

// Interface for payment
interface Payable {

    double amountToPay(double fee);
}

// Interface for printing receipts
interface ReceiptPrintable {

    // Default method
    default void printReceipt(double amount) {

        System.out.println(
            "Receipt Amount: Rs " + amount);
    }
}

// TwoWheeler subclass
class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10.0;
    }
}

// Car subclass
class Car extends Vehicle {

    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30.0;
    }
}

// ElectricCar subclass
class ElectricCar extends Car
        implements Chargeable {

    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double chargingFee(int kWh) {

        if (kWh < 0) {
            throw new IllegalArgumentException(
                "Invalid charging units");
        }

        return kWh * Chargeable.ratePerKWh();
    }
}

// Class implementing multiple interfaces
class Visitor implements Payable,
        ReceiptPrintable {

    @Override
    public double amountToPay(double fee) {
        return fee;
    }
}

// Main class
public class Task09 {

    public static void main(String[] args) {

        System.out.println(
            "CAMPUS SMART PARKING SYSTEM");

        Vehicle bike =
            new TwoWheeler("TN01A1234");

        Vehicle car =
            new Car("TN02B5678");

        ElectricCar ev =
            new ElectricCar("TN03C9012");

        int minutes = 130;

        System.out.println("\nVehicle Parking Fees:");

        System.out.println(
            "TwoWheeler Fee: Rs "
            + bike.calculateFee(minutes));

        System.out.println(
            "Car Fee: Rs "
            + car.calculateFee(minutes));

        System.out.println(
            "ElectricCar Parking Fee: Rs "
            + ev.calculateFee(minutes));

        double charging = ev.chargingFee(10);

        System.out.println(
            "EV Charging Fee: Rs " + charging);

        double total =
            ev.calculateFee(minutes) + charging;

        System.out.println(
            "Total EV Fee: Rs " + total);

        // Payment and receipt
        Visitor visitor = new Visitor();

        double payment =
            visitor.amountToPay(total);

        System.out.println("\nVisitor Payment:");

        System.out.println(
            "Amount to Pay: Rs " + payment);

        visitor.printReceipt(payment);
    }
}
