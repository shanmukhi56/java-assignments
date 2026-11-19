import java.math.BigDecimal;
import java.util.*;
public class FeeService {
    private final List<String> payments = new ArrayList<>();
    public static BigDecimal validate(String reg, String amount) {
        if (!reg.matches("[A-Za-z0-9-]{3,20}")) throw new IllegalArgumentException("Register number: 3-20 letters/digits/hyphens required.");
        try {
            BigDecimal a = new BigDecimal(amount.trim());
            if(a.signum() <= 0 || a.scale() > 2) throw new NumberFormatException();
            return a;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("Enter a positive amount with at most 2 decimals."); }
    }
    public synchronized String pay(String reg, String amount) {
        BigDecimal a=validate(reg,amount);
        String receipt="DEMO receipt #"+(payments.size()+1)+": "+reg+" INR "+a.toPlainString();
        payments.add(receipt);
        return receipt;
    }
}
