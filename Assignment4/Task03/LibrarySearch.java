import javax.swing.*;
import java.awt.*;
import java.util.*;
public class LibrarySearch extends JFrame {
    private final String[][] books={{"Java Fundamentals","Computing"},{"Database Systems","Computing"},{"Microcontrollers","Engineering"},{"Engineering Mathematics","Engineering"},{"Biotechnology Basics","Science"},{"Cell Biology","Science"}};
    private final JTextField query=new JTextField(16);
    private final JComboBox<String> category=new JComboBox<>(new String[]{"All","Computing","Engineering","Science"});
    private final DefaultListModel<String> model=new DefaultListModel<>();
    private final JList<String> results=new JList<>(model);
    private final JLabel status=new JLabel("Ready");private final java.util.List<String> filtered=new ArrayList<>();private int page=0;
    private final JButton prev=new JButton("Prev"),next=new JButton("Next");
    public LibrarySearch(){
        super("Campus Library Search");setLayout(new BorderLayout(10,10));
        JPanel top=new JPanel(new BorderLayout());top.add(new JLabel("CAMPUS LIBRARY",SwingConstants.CENTER),BorderLayout.NORTH);
        JPanel search=new JPanel(new FlowLayout());JButton go=new JButton("Search");search.add(query);search.add(category);search.add(go);top.add(search);add(top,BorderLayout.NORTH);
        add(new JScrollPane(results),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout());JPanel nav=new JPanel(new FlowLayout());JButton reserve=new JButton("Reserve");nav.add(prev);nav.add(next);nav.add(reserve);bottom.add(nav,BorderLayout.NORTH);bottom.add(status,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        go.addActionListener(e->search());query.addActionListener(e->search());prev.addActionListener(e->{page--;showPage();});next.addActionListener(e->{page++;showPage();});reserve.addActionListener(e->status.setText(results.getSelectedValue()==null?"Select a book first.":"Reserved (demo): "+results.getSelectedValue()));
        search();setMinimumSize(new Dimension(520,280));setSize(700,430);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    private void search(){filtered.clear();for(String[] b:books)if(b[0].toLowerCase(Locale.ROOT).contains(query.getText().trim().toLowerCase(Locale.ROOT))&&(category.getSelectedItem().equals("All")||category.getSelectedItem().equals(b[1])))filtered.add(b[0]);page=0;showPage();}
    private void showPage(){model.clear();for(int i=page*3;i<Math.min(filtered.size(),page*3+3);i++)model.addElement(filtered.get(i));prev.setEnabled(page>0);next.setEnabled((page+1)*3<filtered.size());status.setText(filtered.size()+" result(s), page "+(page+1));}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new LibrarySearch().setVisible(true));}
}
