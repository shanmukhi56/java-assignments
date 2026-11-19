import java.util.InputMismatchException;
import java.util.Scanner;

public class ATMWithdrawal {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double balance = 5000.0;

        try {
            System.out.print("Account number: ");
            int account = sc.nextInt();

            System.out.print("Withdrawal amount: ");
            double amount = sc.nextDouble();

            if (account <= 0) {
                throw new IllegalArgumentException("Invalid account number");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            if (amount > balance) {
                throw new ArithmeticException("Insufficient balance");
            }

            balance -= amount;
            System.out.printf("Withdrawal successful. Balance: %.2f%n", balance);
        } catch (InputMismatchException | IllegalArgumentException e) {
            System.out.println("Input error: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Transaction error: " + e.getMessage());
        } finally {
            System.out.println("ATM session closed.");
            sc.close();
        }
    }
}
