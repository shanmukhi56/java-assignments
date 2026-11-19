import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.nio.file.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
public class CampusEvents extends JFrame {
    private final RegistrationService service;
    private final JTextField id=new JTextField(15),name=new JTextField(20);
    private final JComboBox<String> event=new JComboBox<>(RegistrationService.EVENTS),filter=new JComboBox<>(new String[]{"All","Music","Dance","Quiz"});
    private final JLabel status=new JLabel("Ready");
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Student ID","Name","Event"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JButton register=new JButton("Register"),refresh=new JButton("Refresh"),prev=new JButton("Prev"),next=new JButton("Next"),export=new JButton("Export filtered CSV");
    private int page=0;private boolean busy=false,hasNext=false;
    public CampusEvents(boolean demo){
        super("Campus Event Registration"+(demo?" - DEMO MEMORY MODE":" - MySQL"));
        service=new RegistrationService(demo?new MemoryRegistrationDao():new JdbcRegistrationDao());
        JTabbedPane tabs=new JTabbedPane();JPanel form=new JPanel(new GridLayout(4,2,10,10));form.setBorder(BorderFactory.createEmptyBorder(30,40,80,40));
        form.add(new JLabel("Student ID"));form.add(id);form.add(new JLabel("Student name"));form.add(name);form.add(new JLabel("Event"));form.add(event);form.add(new JLabel("One registration per student/event"));form.add(register);tabs.add("Register",form);
        JPanel view=new JPanel(new BorderLayout());JPanel controls=new JPanel();controls.add(new JLabel("Event"));controls.add(filter);controls.add(refresh);controls.add(export);view.add(controls,BorderLayout.NORTH);view.add(new JScrollPane(new JTable(model)));
        JPanel paging=new JPanel();paging.add(prev);paging.add(next);view.add(paging,BorderLayout.SOUTH);tabs.add("Committee",view);add(tabs);add(status,BorderLayout.SOUTH);
        JMenuBar bar=new JMenuBar();JMenu file=new JMenu("File"),help=new JMenu("Help");JMenuItem exportMenu=new JMenuItem("Export filtered CSV"),exit=new JMenuItem("Exit"),about=new JMenuItem("About");file.add(exportMenu);file.add(exit);help.add(about);bar.add(file);bar.add(help);setJMenuBar(bar);
        register.addActionListener(e->register());refresh.addActionListener(e->load());filter.addActionListener(e->{page=0;load();});prev.addActionListener(e->{page--;load();});next.addActionListener(e->{page++;load();});export.addActionListener(e->export());exportMenu.addActionListener(e->export());exit.addActionListener(e->close());about.addActionListener(e->JOptionPane.showMessageDialog(this,"Assignment 4: layered Swing/JDBC application.\nDemo mode is temporary memory; MySQL mode persists records.\nClassroom prototype: no user authentication."));
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){close();}});setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);setSize(800,450);controlsEnabled(true);
    }
    private void close(){if(busy){JOptionPane.showMessageDialog(this,"Please wait for the current operation to finish.");return;}dispose();}
    private void controlsEnabled(boolean enabled){register.setEnabled(enabled);refresh.setEnabled(enabled);filter.setEnabled(enabled);export.setEnabled(enabled);prev.setEnabled(enabled&&page>0);next.setEnabled(enabled&&hasNext);}
    private <T> void work(Callable<T> task,Consumer<T> success){
        if(busy)return;busy=true;controlsEnabled(false);status.setText("Working...");
        new SwingWorker<T,Void>(){protected T doInBackground()throws Exception{return task.call();}protected void done(){busy=false;try{success.accept(get());}catch(Exception e){Throwable c=e.getCause()==null?e:e.getCause();status.setText("Error: "+c.getMessage());}finally{controlsEnabled(true);}}}.execute();
    }
    private void register(){final String s=id.getText(),n=name.getText(),v=(String)event.getSelectedItem();try{RegistrationService.validate(s,n,v);}catch(IllegalArgumentException e){status.setText(e.getMessage());return;}
        work(()->{service.register(s,n,v);return s;},r->status.setText("Registered "+RegistrationService.normalizeId(r)+" for "+v+". Open Committee and Refresh to view."));}
    private void load(){final String selected=(String)filter.getSelectedItem();final int requested=page;
        work(()->service.page(selected,requested),rows->{model.setRowCount(0);hasNext=rows.size()>50;for(int i=0;i<Math.min(50,rows.size());i++){Registration r=rows.get(i);model.addRow(new Object[]{r.id,r.studentId,r.name,r.event});}status.setText("Page "+(page+1)+": "+model.getRowCount()+" rows / "+selected);});}
    private void export(){if(busy)return;JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("registrations.csv"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path target=chooser.getSelectedFile().toPath();if(Files.exists(target)&&JOptionPane.showConfirmDialog(this,"Replace existing file?","Export",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        final String selected=(String)filter.getSelectedItem();work(()->service.export(target,selected),count->status.setText("Exported "+count+" registration(s): "+target));}
    public static void main(String[] args){boolean demo=args.length>0&&args[0].equals("--demo");SwingUtilities.invokeLater(()->new CampusEvents(demo).setVisible(true));}
}
