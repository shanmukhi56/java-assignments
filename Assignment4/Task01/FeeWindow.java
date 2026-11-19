import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class FeeWindow extends JFrame {
    private final JTextField reg=new JTextField(15), amount=new JTextField(10);
    private final JLabel status=new JLabel("Demo payment service - no real money is charged.");
    private final FeeService service=new FeeService();
    private String lastReceipt;
    public FeeWindow(){
        super("Student Fee Payment"); setLayout(new BorderLayout(8,8));
        JPanel fields=new JPanel(new GridLayout(2,2,8,8));
        fields.add(new JLabel("Register number"));fields.add(reg);fields.add(new JLabel("Amount (INR)"));fields.add(amount);
        JPanel buttons=new JPanel(); JButton pay=new JButton("Pay"),clear=new JButton("Clear"),receipt=new JButton("Receipt");
        buttons.add(pay);buttons.add(clear);buttons.add(receipt);add(fields,BorderLayout.NORTH);add(buttons);add(status,BorderLayout.SOUTH);
        pay.addActionListener(e->{
            final String r=reg.getText().trim(),a=amount.getText();
            try{FeeService.validate(r,a);}catch(IllegalArgumentException x){status.setText(x.getMessage());return;}
            pay.setEnabled(false);clear.setEnabled(false);status.setText("Processing...");
            new SwingWorker<String,Void>(){
                protected String doInBackground(){return service.pay(r,a);}
                protected void done(){try{lastReceipt=get();status.setText(lastReceipt);}catch(Exception x){status.setText("Payment failed: "+x.getCause());}finally{pay.setEnabled(true);clear.setEnabled(true);}}
            }.execute();
        });
        clear.addActionListener(e->{reg.setText("");amount.setText("");status.setText("Fields cleared.");});
        receipt.addActionListener(e->JOptionPane.showMessageDialog(this,lastReceipt==null?"No payment completed yet.":lastReceipt));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(650,190);setLocationByPlatform(true);
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new FeeWindow().setVisible(true));}
}
