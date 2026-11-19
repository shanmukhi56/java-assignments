import java.time.*;
import java.util.*;

// Pricing is separate from slot allocation and pass eligibility.
interface PricingRule {
    double fee(LocalDateTime entry, int minutes);
}

interface Pass {
    // Return the start of the uncovered (chargeable) part of the stay.
    LocalDateTime coveredUntil(LocalDateTime entry, LocalDateTime exit);
}

class MonthlyPass implements Pass {
    private final YearMonth month;
    MonthlyPass(YearMonth month) { this.month = month; }
    public LocalDateTime coveredUntil(LocalDateTime entry, LocalDateTime exit) {
        return YearMonth.from(entry).equals(month) ? exit : entry;
    }
}

class OneDayPass implements Pass {
    static final double PRICE = 100.0;
    private final LocalDate day;
    OneDayPass(LocalDate day) { this.day = day; }
    public LocalDateTime coveredUntil(LocalDateTime entry, LocalDateTime exit) {
        if (!entry.toLocalDate().equals(day)) return entry;
        LocalDateTime midnight = day.plusDays(1).atStartOfDay();
        return exit.isBefore(midnight) ? exit : midnight;
    }
}

interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
    // Existing hourly subclasses override this method. New pricing strategies
    // do not need to define an hourly rate.
    protected double ratePerHour() {
        throw new UnsupportedOperationException("No fixed hourly rate");
    }
    protected PricingRule pricingRule() {
        return (entry, minutes) -> ((minutes + 59L) / 60) * ratePerHour();
    }
    public final double calculateFee(LocalDateTime entry, int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Invalid duration");
        Objects.requireNonNull(entry, "Entry time required");
        return pricingRule().fee(entry, minutes);
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

// New vehicle categories: existing Car, TwoWheeler and EV are unchanged.
class EBike extends Vehicle {
    EBike(String regNo) { super(regNo); }
    @Override protected PricingRule pricingRule() {
        return (entry, minutes) -> minutes == 0 ? 0 : 20.0;
    }
}

class DeliveryVan extends Vehicle {
    DeliveryVan(String regNo) { super(regNo); }
    @Override protected PricingRule pricingRule() {
        return (entry, minutes) -> {
            long hours = (minutes + 59L) / 60;
            double fee = 0;
            for (long h = 0; h < hours; h++) {
                LocalTime time = entry.plusHours(h).toLocalTime();
                boolean peak = !time.isBefore(LocalTime.of(9, 0))
                    && time.isBefore(LocalTime.of(17, 0));
                fee += peak ? 60 : 30;
            }
            return fee;
        };
    }
}

class ParkingTicket {
    private static int counter = 1000;
    private final int id = ++counter;
    private final Vehicle vehicle;
    private final int floor, slot;
    private final LocalDateTime entry;
    private final Pass pass;
    private final boolean chargingSlot;
    private final double passPurchaseFee;

    ParkingTicket(Vehicle vehicle, int floor, int slot,
                  LocalDateTime entry, Pass pass,
                  boolean chargingSlot, double passPurchaseFee) {
        this.vehicle = vehicle;
        this.floor = floor;
        this.slot = slot;
        this.entry = entry;
        this.pass = pass;
        this.passPurchaseFee = passPurchaseFee;
        this.chargingSlot = chargingSlot;
    }
    public int getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalDateTime getEntry() { return entry; }
    public Pass getPass() { return pass; }
    public double getPassPurchaseFee() { return passPurchaseFee; }
    public boolean hasChargingSlot() { return chargingSlot; }
    public void display() {
        System.out.println("Ticket: " + id + " | Vehicle: " + vehicle.getRegNo());
        System.out.println("Floor: " + floor + " | Slot: " + slot + " | Entry: " + entry);
        System.out.println("Pass: " + (pass == null ? "None" : pass.getClass().getSimpleName()));
        System.out.println("Pass purchase fee: Rs " + passPurchaseFee);
    }
}

class ParkingFacility {
    private final ParkingTicket[][] slots;
    private final ParkingTicket[] active;
    private final Map<LocalDate, Double> daily = new HashMap<>();
    private final Map<String, OneDayPass> visitorPasses = new HashMap<>();

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
            case "ebike": return new EBike(reg);
            case "van": return new DeliveryVan(reg);
            default: throw new IllegalArgumentException("Unknown vehicle type");
        }
    }

    public ParkingTicket park(String reg, String type, String role,
                              YearMonth pass, boolean charging,
                              LocalDateTime entry) {
        return park(reg, type, role, pass, false, charging, entry);
    }

    public ParkingTicket park(String reg, String type, String role,
                              YearMonth pass, boolean buyDayPass,
                              boolean charging, LocalDateTime entry) {
        reg = normalize(reg);
        Vehicle v = createVehicle(reg, type);
        if (find(reg) != null) throw new IllegalArgumentException("Duplicate registration");
        role = role.trim().toLowerCase(Locale.ROOT);
        if (!role.equals("student") && !role.equals("staff") && !role.equals("visitor"))
            throw new IllegalArgumentException("Invalid user type");
        if (charging && !(v instanceof Chargeable))
            throw new IllegalArgumentException("Only EVs can use charging slots");
        if (buyDayPass && !role.equals("visitor"))
            throw new IllegalArgumentException("One-day pass is for visitors");
        String passKey = reg + "|" + entry.toLocalDate();
        Pass parkingPass = null;
        double purchaseFee = 0;
        if (role.equals("visitor")) {
            parkingPass = visitorPasses.get(passKey);
            if (parkingPass == null && buyDayPass) {
                parkingPass = new OneDayPass(entry.toLocalDate());
                purchaseFee = OneDayPass.PRICE;
            }
        } else if (pass != null && YearMonth.from(entry).equals(pass)) {
            parkingPass = new MonthlyPass(pass);
        }

        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if ((s == 0) != charging || slots[f][s] != null) continue;
                ParkingTicket t = new ParkingTicket(v, f, s, entry, parkingPass, charging, purchaseFee);
                for (int i = 0; i < active.length; i++) {
                    if (active[i] == null) {
                        active[i] = t;
                        slots[f][s] = t;
                        // Collect the pass price only after successful allocation.
                        if (purchaseFee > 0) {
                            visitorPasses.put(passKey, (OneDayPass) parkingPass);
                            daily.merge(entry.toLocalDate(), purchaseFee, Double::sum);
                        }
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
        LocalDateTime entry = t.getEntry();
        LocalDateTime exit = entry.plusMinutes(minutes);
        LocalDateTime paidFrom = t.getPass() == null ? entry :
            t.getPass().coveredUntil(entry, exit);
        int unpaidMinutes = Math.toIntExact(Duration.between(paidFrom, exit).toMinutes());
        double fee = v.calculateFee(paidFrom, unpaidMinutes);
        if (v instanceof Chargeable c) fee += c.chargingFee(kWh);
        LocalDate exitDay = exit.toLocalDate();
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

public class Task12 {
    private static String read(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("test")) {
            System.out.println("=== TASK 11 REGRESSION TESTS ===");
            runTests();
            System.out.println("=== TASK 12 NEW REQUIREMENT TESTS ===");
            runChallengeTests();
            return;
        }
        ParkingFacility p = new ParkingFacility(new int[]{20, 30, 40, 50, 100});
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== CAMPUS SMART PARKING - TASK 12 =====");
            System.out.println("1. Park Vehicle\n2. Exit Vehicle\n3. Display Slots\n4. Daily Collection\n5. Quit");
            if (!sc.hasNextLine()) { sc.close(); return; }
            String choice = read(sc, "Enter choice: ");
            try {
                switch (choice) {
                    case "1": {
                        String reg = read(sc, "Vehicle number: ");
                        String type = read(sc, "Type (bike/car/ev/ebike/van): ");
                        String role = read(sc, "User (student/staff/visitor): ");
                        YearMonth pass = null;
                        if (role.equalsIgnoreCase("student") || role.equalsIgnoreCase("staff")) {
                            String month = read(sc, "Pass month (YYYY-MM, or blank): ");
                            if (!month.isEmpty()) pass = YearMonth.parse(month);
                        }
                        boolean dayPass = false;
                        if (role.equalsIgnoreCase("visitor")) {
                            String answer = read(sc, "Buy one-day pass Rs 100 (yes/no): ");
                            if (!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no"))
                                throw new IllegalArgumentException("Enter yes or no");
                            dayPass = answer.equalsIgnoreCase("yes");
                        }
                        boolean charging = false;
                        if (type.equalsIgnoreCase("ev")) {
                            String answer = read(sc, "Charging slot (yes/no): ");
                            if (!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no"))
                                throw new IllegalArgumentException("Enter yes or no");
                            charging = answer.equalsIgnoreCase("yes");
                        }
                        String when = read(sc, "Entry (YYYY-MM-DDTHH:MM, blank for now): ");
                        LocalDateTime entry = when.isEmpty() ? LocalDateTime.now().withSecond(0).withNano(0) : LocalDateTime.parse(when);
                        p.park(reg, type, role, pass, dayPass, charging, entry).display();
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

    private static void runChallengeTests() {
        ParkingFacility p = new ParkingFacility(new int[]{20, 30, 40, 50, 100});
        LocalDate day = LocalDate.of(2026, 11, 13);
        LocalDateTime in = day.atTime(10, 0);
        check("Five floors, unequal capacities", p.isFree(0, 19) && p.isFree(1, 29)
            && p.isFree(2, 39) && p.isFree(3, 49) && p.isFree(4, 99));
        reject("Out-of-range fifth floor slot", () -> p.isFree(4, 100));
        p.park("B1", "ebike", "visitor", null, false, in);
        check("EBike 130 minutes Rs 20", p.leave("B1", 130, 0) == 20);
        p.park("B2", "ebike", "visitor", null, false, in);
        check("EBike zero minutes Rs 0", p.leave("B2", 0, 0) == 0);
        p.park("V1", "van", "visitor", null, false, in);
        check("Van peak 120 minutes Rs 120", p.leave("V1", 120, 0) == 120);
        p.park("V2", "van", "visitor", null, false, day.atTime(18, 0));
        check("Van off-peak 120 minutes Rs 60", p.leave("V2", 120, 0) == 60);
        p.park("V3", "van", "visitor", null, false, day.atTime(16, 0));
        check("Van crosses 5 PM Rs 90", p.leave("V3", 120, 0) == 90);
        p.park("V4", "van", "visitor", null, false, day.atTime(8, 0));
        check("Van crosses 9 AM Rs 90", p.leave("V4", 120, 0) == 90);
        p.park("V5", "van", "visitor", null, false, day.atTime(17, 0));
        check("Van exactly 5 PM Rs 30", p.leave("V5", 60, 0) == 30);
        ParkingFacility q = new ParkingFacility(new int[]{3, 2});
        ParkingTicket a = q.park("P1", "car", "visitor", null, true, false, in);
        check("One-day pass purchase Rs 100", a.getPassPurchaseFee() == 100 && q.collection(day) == 100);
        check("One-day pass parking Rs 0", q.leave("P1", 130, 0) == 0);
        ParkingTicket b = q.park("P1", "car", "visitor", null, false, false, day.atTime(15, 0));
        check("Same-day pass reuse Rs 0", b.getPassPurchaseFee() == 0 && q.leave("P1", 60, 0) == 0 && q.collection(day) == 100);
        q.park("P1", "car", "visitor", null, false, in.plusDays(1));
        check("Expired day pass Rs 30", q.leave("P1", 60, 0) == 30);
        q.park("P2", "car", "visitor", null, true, false, day.atTime(23, 0));
        check("Pass crosses midnight: uncovered hour Rs 30", q.leave("P2", 120, 0) == 30);
        q.park("E1", "ev", "visitor", null, true, true, in);
        check("Day pass EV charging Rs 120", q.leave("E1", 130, 10) == 120);
        check("Pass and exit collection day 1 Rs 420", q.collection(day) == 420);
        check("Collection day 2 Rs 60", q.collection(day.plusDays(1)) == 60);
        reject("Student cannot buy visitor pass", () -> q.park("S1", "car", "student", YearMonth.from(in), true, false, in));
        ParkingFacility full = new ParkingFacility(new int[]{2});
        full.park("C1", "car", "visitor", null, false, in);
        reject("Full lot rejects pass purchase", () -> full.park("C2", "car", "visitor", null, true, false, in));
        check("No pass charge on failed allocation", full.collection(day) == 0);
        ParkingFacility large = new ParkingFacility(new int[]{20, 30, 40, 50, 100});
        ParkingTicket last = null;
        for (int i = 0; i < 235; i++) last = large.park("L" + i, "car", "visitor", null, false, in);
        check("Last ordinary slot on floor 4", last.getFloor() == 4 && last.getSlot() == 99);
        reject("All ordinary slots full", () -> large.park("OVER", "car", "visitor", null, false, in));
        for (int i = 0; i < 5; i++) last = large.park("EV" + i, "ev", "visitor", null, true, in);
        check("Last charging slot on floor 4", last.getFloor() == 4 && last.getSlot() == 0);
        reject("All charging slots full", () -> large.park("EVX", "ev", "visitor", null, true, in));
    }
}
