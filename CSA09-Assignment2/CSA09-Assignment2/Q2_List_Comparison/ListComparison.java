
import java.util.*;

public class ListComparison {
    public static void main(String[] args) {
        int n = 10000;

        ArrayList<Integer> array = new ArrayList<>();
        LinkedList<Integer> linked = new LinkedList<>();
        ArrayDeque<Integer> deque = new ArrayDeque<>();

        long start;

        // Add elements
        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            array.add(i);
        System.out.println("ArrayList Add: " +
            (System.nanoTime() - start) + " ns");

        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            linked.add(i);
        System.out.println("LinkedList Add: " +
            (System.nanoTime() - start) + " ns");

        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            deque.addLast(i);
        System.out.println("ArrayDeque Add: " +
            (System.nanoTime() - start) + " ns");

        // Random access
        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            array.get(i);
        System.out.println("ArrayList Access: " +
            (System.nanoTime() - start) + " ns");

        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            linked.get(i);
        System.out.println("LinkedList Access: " +
            (System.nanoTime() - start) + " ns");

        // Remove from front
        start = System.nanoTime();
        while (!array.isEmpty())
            array.remove(0);
        System.out.println("ArrayList Front Removal: " +
            (System.nanoTime() - start) + " ns");

        start = System.nanoTime();
        while (!linked.isEmpty())
            linked.removeFirst();
        System.out.println("LinkedList Front Removal: " +
            (System.nanoTime() - start) + " ns");

        start = System.nanoTime();
        while (!deque.isEmpty())
            deque.removeFirst();
        System.out.println("ArrayDeque Front Removal: " +
            (System.nanoTime() - start) + " ns");
    }
}
