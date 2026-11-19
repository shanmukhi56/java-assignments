
// Superclass
abstract class Vehicle {

    private String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;

        System.out.println("Vehicle constructor executed");
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double calculateFee(int hours) {
        return hours * ratePerHour();
    }
}

// Subclass 1
class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo);

        System.out.println("TwoWheeler constructor executed");
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

// Subclass 2
class Car extends Vehicle {

    Car(String regNo) {
        super(regNo);

        System.out.println("Car constructor executed");
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

// Subclass of Car
class ElectricCar extends Car {

    ElectricCar(String regNo) {
        super(regNo);

        System.out.println("ElectricCar constructor executed");
    }

    @Override
    protected double ratePerHour() {
        return super.ratePerHour();
    }

    public double chargingFee(int kWh) {
        return kWh * 12;
    }
}

// Main class
public class Task06 {

    public static void main(String[] args) {

        System.out.println("CAMPUS SMART PARKING SYSTEM");

        System.out.println("\nTwo Wheeler:");

        TwoWheeler bike = new TwoWheeler("TN01A1234");

        System.out.println("Vehicle Number: " + bike.getRegNo());

        System.out.println("Parking Fee: Rs "
                + bike.calculateFee(2));

        System.out.println("\nCar:");

        Car car = new Car("TN02B5678");

        System.out.println("Vehicle Number: " + car.getRegNo());

        System.out.println("Parking Fee: Rs "
                + car.calculateFee(2));

        System.out.println("\nElectric Car:");

        ElectricCar ev = new ElectricCar("TN03C9012");

        System.out.println("Vehicle Number: " + ev.getRegNo());

        System.out.println("Parking Fee: Rs "
                + ev.calculateFee(2));

        System.out.println("Charging Fee: Rs "
                + ev.chargingFee(10));

        System.out.println("Total Fee: Rs "
                + (ev.calculateFee(2) + ev.chargingFee(10)));
    }
}
