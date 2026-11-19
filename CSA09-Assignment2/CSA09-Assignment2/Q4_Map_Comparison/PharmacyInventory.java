import java.util.*;

public class PharmacyInventory {

    public static void main(String[] args) {
        int[] sizes = {100, 1000, 10000};
        int lookups = 100000;

        for (int size : sizes) {
            System.out.println("\n===== DATA SIZE: " + size + " =====");

            List<String> codes = new ArrayList<>();

            // Deliberately insert codes in reverse order
            for (int i = size - 1; i >= 0; i--) {
                codes.add(String.format("MED%05d", i));
            }

            Map<String, Integer> hashMap = new HashMap<>();
            Map<String, Integer> treeMap = new TreeMap<>();
            Map<String, Integer> linkedMap = new LinkedHashMap<>();

            long start = System.nanoTime();
            for (String code : codes) {
                hashMap.put(code, 100);
            }
            long hashInsert = System.nanoTime() - start;

            start = System.nanoTime();
            for (String code : codes) {
                treeMap.put(code, 100);
            }
            long treeInsert = System.nanoTime() - start;

            start = System.nanoTime();
            for (String code : codes) {
                linkedMap.put(code, 100);
            }
            long linkedInsert = System.nanoTime() - start;

            // Perform repeated lookups
            long hashLookup = performLookups(hashMap, codes, lookups);
            long treeLookup = performLookups(treeMap, codes, lookups);
            long linkedLookup = performLookups(linkedMap, codes, lookups);

            System.out.println("Insertion time (ns):");
            System.out.println("HashMap:       " + hashInsert);
            System.out.println("TreeMap:       " + treeInsert);
            System.out.println("LinkedHashMap: " + linkedInsert);

            System.out.println("\nLookup time for " + lookups + " operations (ns):");
            System.out.println("HashMap:       " + hashLookup);
            System.out.println("TreeMap:       " + treeLookup);
            System.out.println("LinkedHashMap: " + linkedLookup);

            System.out.println("\nFirst 5 keys in iteration order:");
            System.out.println("HashMap:       " + firstFive(hashMap));
            System.out.println("TreeMap:       " + firstFive(treeMap));
            System.out.println("LinkedHashMap: " + firstFive(linkedMap));
        }
    }

    private static long performLookups(
            Map<String, Integer> map, List<String> codes, int count) {

        long checksum = 0;
        long start = System.nanoTime();

        for (int i = 0; i < count; i++) {
            String key = codes.get(i % codes.size());
            checksum += map.get(key);
        }

        long elapsed = System.nanoTime() - start;

        // Use the result so the lookup work contributes to the output
        if (checksum == -1) {
            System.out.println(checksum);
        }

        return elapsed;
    }

    private static List<String> firstFive(Map<String, Integer> map) {
        List<String> result = new ArrayList<>();

        for (String key : map.keySet()) {
            result.add(key);
            if (result.size() == 5) {
                break;
            }
        }

        return result;
    }
}