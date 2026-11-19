import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Task A1 Alternative: Fixed-Length Records using RandomAccessFile
 * Direct slot lookup: O(1) seek time via i * RECORD_SIZE.
 * Calculates exact internal fragmentation (space wasted by padding).
 */
public class RandomAccessPassStore {
    // Fixed field widths in characters/bytes
    public static final int PASS_ID_LEN = 10;
    public static final int REG_NO_LEN = 12;
    public static final int NAME_LEN = 30;
    public static final int ROUTE_LEN = 4;    // 4-byte int
    public static final int AMOUNT_LEN = 8;   // 8-byte double

    // Total record size in bytes: 10 + 12 + 30 + 4 + 8 = 64 bytes
    public static final int RECORD_SIZE = PASS_ID_LEN + REG_NO_LEN + NAME_LEN + ROUTE_LEN + AMOUNT_LEN;

    private final File file;

    public RandomAccessPassStore(File file) {
        this.file = file;
    }

    private static byte[] padString(String str, int length) {
        byte[] padded = new byte[length];
        byte[] raw = str.getBytes(StandardCharsets.UTF_8);
        int copyLen = Math.min(raw.length, length);
        System.arraycopy(raw, 0, padded, 0, copyLen);
        for (int i = copyLen; i < length; i++) {
            padded[i] = ' '; // Pad with space
        }
        return padded;
    }

    public void writeRecordAt(int slotIndex, String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long offset = (long) slotIndex * RECORD_SIZE;
            raf.seek(offset);
            raf.write(padString(passId, PASS_ID_LEN));
            raf.write(padString(regNo, REG_NO_LEN));
            raf.write(padString(name, NAME_LEN));
            raf.writeInt(route);
            raf.writeDouble(amount);
        }
    }

    public String[] readRecordAt(int slotIndex) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long offset = (long) slotIndex * RECORD_SIZE;
            if (offset + RECORD_SIZE > raf.length()) {
                return null;
            }
            raf.seek(offset);

            byte[] idBytes = new byte[PASS_ID_LEN];
            byte[] regBytes = new byte[REG_NO_LEN];
            byte[] nameBytes = new byte[NAME_LEN];

            raf.readFully(idBytes);
            raf.readFully(regBytes);
            raf.readFully(nameBytes);
            int route = raf.readInt();
            double amount = raf.readDouble();

            return new String[]{
                    new String(idBytes, StandardCharsets.UTF_8).trim(),
                    new String(regBytes, StandardCharsets.UTF_8).trim(),
                    new String(nameBytes, StandardCharsets.UTF_8).trim(),
                    String.valueOf(route),
                    String.valueOf(amount)
            };
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== TASK A1 ALTERNATIVE: RANDOMACCESSFILE FIXED-RECORD STORE ===");
        File temp = File.createTempFile("fixed_passes", ".bin");
        temp.deleteOnExit();

        RandomAccessPassStore store = new RandomAccessPassStore(temp);
        store.writeRecordAt(0, "P1001", "192311001", "Arun", 12, 4500.0);
        store.writeRecordAt(1, "P1002", "192311002", "Bala", 12, 4500.0);
        store.writeRecordAt(2, "P1003", "192311003", "Divya", 7, 3800.0);

        System.out.println("Direct O(1) Seek at Slot 2: " + Arrays.toString(store.readRecordAt(2)));
        System.out.println("Direct O(1) Seek at Slot 0: " + Arrays.toString(store.readRecordAt(0)));

        // Padding space analysis for 8,000 passes
        int textRecordAvg = 38; // "P1001,192311001,Arun,12,4500.0\n" is ~38 bytes
        int fixedRecordSize = RECORD_SIZE; // 64 bytes
        int wastedPerRecord = fixedRecordSize - textRecordAvg;
        long totalWastedBytes = (long) wastedPerRecord * 8000;

        System.out.printf("Record Size: %d bytes | Avg Text Size: %d bytes\n", fixedRecordSize, textRecordAvg);
        System.out.printf("Padding Wasted: %d bytes/record (%.1f%% waste)\n",
                wastedPerRecord, ((double) wastedPerRecord / fixedRecordSize) * 100);
        System.out.printf("Total Wasted Storage for 8,000 records: %d KB\n", totalWastedBytes / 1024);
    }
}
