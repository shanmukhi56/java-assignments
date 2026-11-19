public class StringComparison {
    public static void main(String[] args) {
        int n = 10000;

        long start = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + "Ticket " + i + "\n";
        }
        long stringTime = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("Ticket ").append(i).append("\n");
        }
        long builderTime = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuffer sf = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sf.append("Ticket ").append(i).append("\n");
        }
        long bufferTime = System.nanoTime() - start;

        System.out.println("Tickets Created: " + n);
        System.out.println("String Time: " + stringTime + " ns");
        System.out.println("StringBuilder Time: " + builderTime + " ns");
        System.out.println("StringBuffer Time: " + bufferTime + " ns");
    }
}