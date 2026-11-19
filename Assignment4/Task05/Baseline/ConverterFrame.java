import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class ConverterFrame extends JFrame implements ActionListener {
    private final JTextField amount=new JTextField(10);
    private final JComboBox<String> from=new JComboBox<>(new String[]{"INR","USD","EUR"}),to=new JComboBox<>(new String[]{"USD","EUR","INR"});
    private final JLabel result=new JLabel("Illustrative fixed rates, not live rates.");
    public ConverterFrame(){
        super("Frame converter - Baseline");setLayout(new FlowLayout());JButton button=new JButton("Convert");
        add(amount);add(from);add(new JLabel("to"));add(to);add(button);add(result);
        ActionListener handler=this;
        button.addActionListener(handler);
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){dispose();}});pack();
    }
    private double rate(String c){return c.equals("USD")?0.012:c.equals("EUR")?0.011:1.0;}
    private void convert(){
        try{double n=Double.parseDouble(amount.getText().trim());if(!Double.isFinite(n)||n<0)throw new NumberFormatException();
            double value=n/rate((String)from.getSelectedItem())*rate((String)to.getSelectedItem());
            if(!Double.isFinite(value))throw new NumberFormatException();
            result.setText(String.format(java.util.Locale.US,"%.2f %s",value,to.getSelectedItem()));
        }catch(NumberFormatException e){result.setText("Invalid amount");}
        
        pack();
    }
    @Override public void actionPerformed(ActionEvent e){convert();}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new ConverterFrame().setVisible(true));}
}
