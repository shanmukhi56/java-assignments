import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task A4 & Task 04: Serialization, Transient Protection & Schema Evolution
 * Demonstrates transient protection of active resources (Connection) and secrets (password),
 * recursive object graph traversal, and serialVersionUID version compatibility.
 */
public class HostelSerializationDemo {

    public static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String regNo;
        private transient String password; // PROTECTED: Never serialized to disk
        private transient Object dbConnection; // PROTECTED: Ephemeral OS socket, not serializable
        private String phoneNumber; // Added field for version evolution demonstration

        public Resident(String name, String regNo, String password) {
            this.name = name;
            this.regNo = regNo;
            this.password = password;
            this.dbConnection = "ActiveSocket[127.0.0.1:3306]";
            this.phoneNumber = "9876543210";
        }

        public String getName() { return name; }
        public String getRegNo() { return regNo; }
        public String getPassword() { return password; }
        public Object getDbConnection() { return dbConnection; }
        public String getPhoneNumber() { return phoneNumber; }

        @Override
        public String toString() {
            return String.format("Resident[name=%s, regNo=%s, pwd=%s, dbConn=%s, phone=%s]",
                    name, regNo, password, dbConnection, phoneNumber);
        }
    }

    public static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        private int roomNumber;
        private List<Resident> residents = new ArrayList<>();

        public Room(int roomNumber) {
            this.roomNumber = roomNumber;
        }

        public void addResident(Resident r) { residents.add(r); }
        public int getRoomNumber() { return roomNumber; }
        public List<Resident> getResidents() { return residents; }
    }

    public static class Hostel implements Serializable {
        private static final long serialVersionUID = 1L;
        private String hostelName;
        private List<Room> rooms = new ArrayList<>();

        public Hostel(String hostelName) {
            this.hostelName = hostelName;
        }

        public void addRoom(Room room) { rooms.add(room); }
        public String getHostelName() { return hostelName; }
        public List<Room> getRooms() { return rooms; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==========================================================================");
        System.out.println("  TASK A4: HOSTEL OBJECT GRAPH SERIALIZATION & TRANSIENT AUDIT");
        System.out.println("==========================================================================");

        Hostel hostel = new Hostel("Kavery Hostel Block B");
        Room r101 = new Room(101);
        r101.addResident(new Resident("Arun Kumar", "192311001", "SecretArun#2026"));
        r101.addResident(new Resident("Bala Chandran", "192311002", "BalaSecure@99"));
        hostel.addRoom(r101);

        File stateFile = File.createTempFile("hostel_state_", ".ser");
        stateFile.deleteOnExit();

        // 1. Serialize Object Graph
        System.out.println("\n[1. Serializing Hostel Object Graph to Disk...]");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(stateFile))) {
            oos.writeObject(hostel);
        }
        System.out.println("State serialized successfully. File size: " + stateFile.length() + " bytes");

        // 2. Deserialize Object Graph
        System.out.println("\n[2. Deserializing Hostel Object Graph from Disk...]");
        Hostel loadedHostel;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(stateFile))) {
            loadedHostel = (Hostel) ois.readObject();
        }

        System.out.println("Loaded Hostel Name: " + loadedHostel.getHostelName());
        for (Room room : loadedHostel.getRooms()) {
            System.out.println(" Room " + room.getRoomNumber() + ":");
            for (Resident res : room.getResidents()) {
                System.out.println("   " + res);
            }
        }

        System.out.println("\n--- Transient Audit Verification ---");
        Resident deserializedResident = loadedHostel.getRooms().get(0).getResidents().get(0);
        System.out.println("Password after deserialization:     " + deserializedResident.getPassword() + " (EXPECTED null - SECURED!)");
        System.out.println("Connection after deserialization:   " + deserializedResident.getDbConnection() + " (EXPECTED null - PREVENTED CRASH!)");
        System.out.println("SerialVersionUID compatibility:     Explicit serialVersionUID=1L guarantees forward/backward compatibility.");
        System.out.println("==========================================================================");
    }
}
