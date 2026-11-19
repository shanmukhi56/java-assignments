import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class AppointmentDesk extends JFrame {
    private final Map<String,String[]> doctors=new LinkedHashMap<>();
    private final Set<String> bookings=new HashSet<>();
    private final JTextField patient=new JTextField(12);
    private final JComboBox<String> department=new JComboBox<>(),doctor=new JComboBox<>(),slot=new JComboBox<>(new String[]{"09:00","09:30","10:00"});
    private final JLabel status=new JLabel("Select a department and enter a patient ID.");
    private boolean dirty=false,updating=false;
    public AppointmentDesk(){
        super("Hospital Appointment Desk");doctors.put("Cardiology",new String[]{"Dr Rao","Dr Shah"});doctors.put("Orthopaedics",new String[]{"Dr Kumar","Dr Das"});
        doctors.keySet().forEach(department::addItem);
        JPanel form=new JPanel(new GridLayout(4,2,8,8));
        form.add(new JLabel("Patient ID"));form.add(patient);form.add(new JLabel("Department"));form.add(department);form.add(new JLabel("Doctor"));form.add(doctor);form.add(new JLabel("Today's time slot"));form.add(slot);
        JButton book=new JButton("Book");JPanel bottom=new JPanel(new BorderLayout());bottom.add(book,BorderLayout.NORTH);bottom.add(status);
        add(form);add(bottom,BorderLayout.SOUTH);
        department.addActionListener(e->{refreshDoctors();dirty=true;});
        doctor.addActionListener(e->{if(!updating)dirty=true;});slot.addActionListener(e->dirty=true);
        patient.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){dirty=true;}public void removeUpdate(DocumentEvent e){dirty=true;}public void changedUpdate(DocumentEvent e){dirty=true;}});
        book.addActionListener(e->book());
        addWindowListener(new WindowAdapter(){
            public void windowOpened(WindowEvent e){System.out.println("windowOpened");}
            public void windowClosing(WindowEvent e){System.out.println("windowClosing");if(!dirty||JOptionPane.showConfirmDialog(AppointmentDesk.this,"Discard the booking in progress?","Confirm close",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)dispose();}
            public void windowClosed(WindowEvent e){System.out.println("windowClosed");}
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);refreshDoctors();dirty=false;setSize(570,260);
    }
    private void refreshDoctors(){updating=true;doctor.removeAllItems();for(String d:doctors.get((String)department.getSelectedItem()))doctor.addItem(d);updating=false;}
    private void book(){String p=patient.getText().trim();if(!p.matches("[A-Za-z0-9-]{3,20}")){status.setText("Invalid patient ID (3-20 letters/digits/hyphens).");return;}
        if(doctor.getSelectedItem()==null){status.setText("Select a doctor.");return;}
        String key=doctor.getSelectedItem()+"|"+slot.getSelectedItem();
        if(!bookings.add(key)){status.setText("This doctor/time slot is already booked.");return;}
        dirty=false;status.setText("Booked: "+p+" / "+doctor.getSelectedItem()+" / "+slot.getSelectedItem());}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new AppointmentDesk().setVisible(true));}
}
