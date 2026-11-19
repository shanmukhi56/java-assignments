import java.time.*;
import java.util.*;

interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
    public double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Invalid duration");
        return ((minutes + 59L) / 60) * ratePerHour();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 10; }
}
class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 30; }
}
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String regNo) { super(regNo); }
    @Override public double chargingFee(int kWh) {
        if (kWh < 0) throw new IllegalArgumentException("Invalid charging units");
        return kWh * 12.0;
    }
}

class ParkingTicket {
    private static int counter = 1000;
    private final int id = ++counter;
    private final Vehicle vehicle;
    private final int floor, slot;
    private final LocalDateTime entry;
    private final boolean freeParking, chargingSlot;

    ParkingTicket(Vehicle vehicle, int floor, int slot,
                  LocalDateTime entry, boolean freeParking,
                  boolean chargingSlot) {
        this.vehicle = vehicle;
        this.floor = floor;
        this.slot = slot;
        this.entry = entry;
        this.freeParking = freeParking;
        this.chargingSlot = chargingSlot;
    }
    public int getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalDateTime getEntry() { return entry; }
    public boolean hasFreeParking() { return freeParking; }
    public boolean hasChargingSlot() { return chargingSlot; }
    public void display() {
        System.out.println("Ticket: " + id + " | Vehicle: " + vehicle.getRegNo());
        System.out.println("Floor: " + floor + " | Slot: " + slot + " | Entry: " + entry);
        System.out.println("Free parking: " + freeParking);
    }
}

class ParkingFacility {
    private final ParkingTicket[][] slots;
    private final ParkingTicket[] active;
    private final Map<LocalDate, Double> daily = new HashMap<>();

    ParkingFacility(int[] capacity) {
        slots = new ParkingTicket[capacity.length][];
        int total = 0;
        for (int f = 0; f < capacity.length; f++) {
            slots[f] = new ParkingTicket[capacity[f]];
            total += capacity[f];
        }
        active = new ParkingTicket[total];
    }

    private static String normalize(String reg) {
        String value = reg.trim().toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z0-9]+"))
            throw new IllegalArgumentException("Invalid registration number");
        return value;
    }

    private static Vehicle createVehicle(String reg, String type) {
        switch (type.trim().toLowerCase(Locale.ROOT)) {
            case "bike": return new TwoWheeler(reg);
            case "car": return new Car(reg);
            case "ev": return new ElectricCar(reg);
            default: throw new IllegalArgumentException("Unknown vehicle type");
        }
    }

    public ParkingTicket park(String reg, String type, String role,
                              YearMonth pass, boolean charging,
                              LocalDateTime entry) {
        reg = normalize(reg);
        Vehicle v = createVehicle(reg, type);
        if (find(reg) != null) throw new IllegalArgumentException("Duplicate registration");
        role = role.trim().toLowerCase(Locale.ROOT);
        if (!role.equals("student") && !role.equals("staff") && !role.equals("visitor"))
            throw new IllegalArgumentException("Invalid user type");
        if (charging && !(v instanceof Chargeable))
            throw new IllegalArgumentException("Only EVs can use charging slots");
        boolean free = !role.equals("visitor") && YearMonth.from(entry).equals(pass);

        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if ((s == 0) != charging || slots[f][s] != null) continue;
                ParkingTicket t = new ParkingTicket(v, f, s, entry, free, charging);
                for (int i = 0; i < active.length; i++) {
                    if (active[i] == null) {
                        active[i] = t;
                        slots[f][s] = t;
                        return t;
                    }
                }
            }
        }
        throw new IllegalArgumentException("No suitable slot available");
    }

    static ParkingTicket findInArray(ParkingTicket[] tickets, String reg) {
        for (ParkingTicket t : tickets)
            if (t != null && t.getVehicle().getRegNo().equals(reg)) return t;
        return null;
    }
    public ParkingTicket find(String reg) {
        return findInArray(active, normalize(reg));
    }

    public double leave(String reg, int minutes, int kWh) {
        ParkingTicket t = find(reg);
        if (t == null) throw new IllegalArgumentException("Vehicle not found");
        if (minutes < 0 || kWh < 0) throw new IllegalArgumentException("Negative input");
        if (kWh > 0 && !t.hasChargingSlot())
            throw new IllegalArgumentException("Vehicle did not use a charging slot");
        Vehicle v = t.getVehicle();
        double fee = t.hasFreeParking() ? 0 : v.calculateFee(minutes);
        if (v instanceof Chargeable c) fee += c.chargingFee(kWh);
        LocalDate exitDay = t.getEntry().plusMinutes(minutes).toLocalDate();
        daily.merge(exitDay, fee, Double::sum);
        slots[t.getFloor()][t.getSlot()] = null;
        for (int i = 0; i < active.length; i++) {
            if (active[i] == t) { active[i] = null; break; }
        }
        return fee;
    }

    public boolean isFree(int floor, int slot) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length)
            throw new IllegalArgumentException("Invalid floor or slot number");
        return slots[floor][slot] == null;
    }
    public void displaySlots() {
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                String status = slots[f][s] == null ? "Free" : slots[f][s].getVehicle().getRegNo();
                System.out.println("Floor " + f + ", Slot " + s + (s == 0 ? " (EV charging): " : " (ordinary): ") + status);
            }
        }
    }
    public double collection(LocalDate day) { return daily.getOrDefault(day, 0.0); }
}

public class Task11 {
    private static String read(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("test")) { runTests(); return; }
        if (args.length > 0 && args[0].equals("benchmark")) { benchmark(); return; }
        ParkingFacility p = new ParkingFacility(new int[]{3, 2});
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== CAMPUS SMART PARKING =====");
            System.out.println("1. Park Vehicle\n2. Exit Vehicle\n3. Display Slots\n4. Daily Collection\n5. Quit");
            String choice = read(sc, "Enter choice: ");
            try {
                switch (choice) {
                    case "1": {
                        String reg = read(sc, "Vehicle number: ");
                        String type = read(sc, "Type (bike/car/ev): ");
                        String role = read(sc, "User (student/staff/visitor): ");
                        YearMonth pass = null;
                        if (role.equalsIgnoreCase("student") || role.equalsIgnoreCase("staff")) {
                            String month = read(sc, "Pass month (YYYY-MM, or blank): ");
                            if (!month.isEmpty()) pass = YearMonth.parse(month);
                        }
                        boolean charging = false;
                        if (type.equalsIgnoreCase("ev")) {
                            String answer = read(sc, "Charging slot (yes/no): ");
                            if (!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no"))
                                throw new IllegalArgumentException("Enter yes or no");
                            charging = answer.equalsIgnoreCase("yes");
                        }
                        p.park(reg, type, role, pass, charging, LocalDateTime.now().withSecond(0).withNano(0)).display();
                        break;
                    }
                    case "2": {
                        String reg = read(sc, "Vehicle number: ");
                        int minutes = Integer.parseInt(read(sc, "Parking duration (minutes): "));
                        int kWh = Integer.parseInt(read(sc, "Charging units (0 if none): "));
                        System.out.println("Total fee: Rs " + p.leave(reg, minutes, kWh));
                        break;
                    }
                    case "3": p.displaySlots(); break;
                    case "4": {
                        String date = read(sc, "Collection date (YYYY-MM-DD, or blank for today): ");
                        LocalDate day = date.isEmpty() ? LocalDate.now() : LocalDate.parse(date);
                        System.out.println("Collection: Rs " + p.collection(day));
                        break;
                    }
                    case "5": sc.close(); return;
                    default: System.out.println("Invalid choice");
                }
            } catch (IllegalArgumentException | DateTimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println(name + ": " + (ok ? "PASS" : "FAIL"));
    }
    private static void reject(String name, Runnable action) {
        try { action.run(); check(name, false); }
        catch (IllegalArgumentException e) { check(name + " (" + e.getMessage() + ")", true); }
    }

    private static void runTests() {
        ParkingFacility p = new ParkingFacility(new int[]{3, 2});
        LocalDateTime in = LocalDateTime.of(2026, 11, 13, 9, 0);
        LocalDate day = in.toLocalDate();
        ParkingTicket a = p.park("TN01A1", "bike", "visitor", null, false, in);
        ParkingTicket b = p.park("TN02B2", "car", "visitor", null, false, in);
        ParkingTicket c = p.park("TN03C3", "ev", "visitor", null, true, in);
        check("Normal entries and unique tickets", a.getId() != b.getId() && b.getId() != c.getId() && a.getId() != c.getId() && c.getSlot() == 0 && !p.isFree(a.getFloor(), a.getSlot()));
        reject("Duplicate registration", () -> p.park("tn02b2", "car", "visitor", null, false, in));
        reject("Unknown type", () -> p.park("X1", "truck", "visitor", null, false, in));
        reject("Invalid slot", () -> p.isFree(5, 0));
        double f1 = p.leave("TN01A1", 130, 0);
        double f2 = p.leave("TN02B2", 130, 0);
        double f3 = p.leave("TN03C3", 130, 10);
        check("Bike fee Rs " + f1, f1 == 30);
        check("Car fee Rs " + f2, f2 == 90);
        check("EV fee Rs " + f3, f3 == 210);
        check("Daily collection Rs " + p.collection(day), p.collection(day) == 330);
        check("Released slots", p.isFree(a.getFloor(), a.getSlot()) && p.find("TN01A1") == null);
        p.park("Z1", "car", "visitor", null, false, in);
        double zero = p.leave("Z1", 0, 0);
        check("Zero minutes Rs " + zero, zero == 0);
        p.park("Z2", "car", "visitor", null, false, in);
        double boundary = p.leave("Z2", 60, 0);
        check("60 minutes Rs " + boundary, boundary == 30);
        p.park("Z3", "car", "visitor", null, false, in);
        double extra = p.leave("Z3", 61, 0);
        check("61 minutes Rs " + extra, extra == 60);
        p.park("S1", "car", "student", YearMonth.from(in), false, in);
        check("Student monthly pass", p.leave("S1", 130, 0) == 0);
        p.park("S2", "ev", "staff", YearMonth.from(in), true, in);
        check("Staff pass with charging", p.leave("S2", 130, 10) == 120);
        p.park("S3", "car", "student", YearMonth.from(in).minusMonths(1), false, in);
        check("Expired pass", p.leave("S3", 60, 0) == 30);
        ParkingFacility q = new ParkingFacility(new int[]{2});
        q.park("E1", "ev", "visitor", null, true, in);
        ParkingTicket last = q.park("C1", "car", "visitor", null, false, in);
        check("Last free slot", last.getSlot() == 1 && !q.isFree(0, 1));
        reject("Parking full", () -> q.park("C2", "car", "visitor", null, false, in));
        reject("Negative duration", () -> q.leave("C1", -1, 0));
        reject("Unknown exit vehicle", () -> q.leave("X1", 60, 0));
        reject("Charging without EV slot", () -> q.leave("C1", 60, 2));
    }

    private static void benchmark() {
        int n = 5000, repeats = 2000;
        ParkingTicket[] array = new ParkingTicket[n];
        ArrayList<ParkingTicket> list = new ArrayList<>();
        HashMap<String, ParkingTicket> map = new HashMap<>();
        LocalDateTime in = LocalDateTime.of(2026, 11, 13, 9, 0);
        for (int i = 0; i < n; i++) {
            String reg = "V" + i;
            ParkingTicket t = new ParkingTicket(new Car(reg), 0, i, in, false, false);
            array[i] = t; list.add(t); map.put(reg, t);
        }
        String target = "V" + (n - 1);
        for (int mode = 0; mode < 3; mode++) {
            long sum = 0;
            for (int j = 0; j < 1000; j++) sum += lookup(mode, array, list, map, target).getId();
            long start = System.nanoTime();
            for (int j = 0; j < repeats; j++) sum += lookup(mode, array, list, map, target).getId();
            long elapsed = System.nanoTime() - start;
            String name = mode == 0 ? "Array" : mode == 1 ? "ArrayList" : "HashMap";
            System.out.printf(Locale.ROOT, "%s: %.3f ms for %d searches (checksum %d)%n", name, elapsed / 1_000_000.0, repeats, sum);
        }
    }
    private static ParkingTicket lookup(int mode, ParkingTicket[] array, ArrayList<ParkingTicket> list, HashMap<String, ParkingTicket> map, String reg) {
        if (mode == 0) return ParkingFacility.findInArray(array, reg);
        if (mode == 1) {
            for (ParkingTicket t : list) if (t.getVehicle().getRegNo().equals(reg)) return t;
            return null;
        }
        return map.get(reg);
    }
}
