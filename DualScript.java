import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class DualScript extends JPanel{
    JTextArea greek;
    JTextArea english;
    JToolBar toolbar;
    JFileChooser fileChooser;
    Translator translator;
    JToggleButton engVisible;
    private int greekCaret=0;
    private boolean syncingCaret=false;
    public DualScript(){
        this.setLayout(new GridBagLayout());            
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill=GridBagConstraints.BOTH;
        gbc.weightx=1.0;
        fileChooser = new JFileChooser();
        translator = new Translator();
        engVisible = new JToggleButton(){
            {
                setPreferredSize(new Dimension(40,20));
                setMaximumSize(new Dimension(40,20));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
            }
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(isSelected()?new Color(0,100,255):Color.GRAY);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(),25, 25);

                g2d.setColor(Color.WHITE);
                int knobSize=getHeight()-4;
                int x=isSelected()?getWidth()-knobSize-2:2;
                g2d.fillOval(x,2,knobSize,knobSize);
                g2d.dispose();
            }
        };
        engVisible.setSelected(true);

        greek = new JTextArea();
        greek.setBackground(new Color(255,255,255));
        greek.setEditable(false);
        greek.setFont(new Font("Roboto", Font.HANGING_BASELINE, 20));


        english = new JTextArea();
        english.setBackground(new Color(0,0,0));
        english.setForeground(new Color(255,255,255));
        english.setFont(new Font("Roboto",Font.ITALIC,20));

        greekCaret=greek.getText().length();

        english.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent k){
                if(
                    ((k.getKeyChar()-'a'<26)&&(k.getKeyChar()-'a'>=0))||
                    ((k.getKeyChar()-'A'<26)&&(k.getKeyChar()-'A'>=0))||
                    ((k.getKeyChar()-'0'<10)&&(k.getKeyChar()-'0'>=0))||
                    (k.getKeyChar()=='"')||(k.getKeyChar()=='?')||
                    (k.getKeyChar()==':')||(k.getKeyChar()==';')||
                    (k.getKeyChar()==',')||(k.getKeyChar()=='.')||
                    (k.getKeyChar()=='&')||(k.getKeyChar()=='!')||
                    (k.getKeyChar()==' ')||(k.getKeyChar()=='\\')||
                    (k.getKeyChar()=='\n')||(k.getKeyChar()=='\'')||
                    (k.getKeyChar()=='-')
                ){
                    greek.setText(greek.getText().substring(0,greekCaret)+translator.encodeEnToPs(k.getKeyChar())+greek.getText().substring(greekCaret));
                }
                else if(k.getKeyCode()==KeyEvent.VK_BACK_SPACE){
                    String s = greek.getText();
                    if(s.substring(s.length()-1).equals("'")){
                        greek.setText(s.substring(0,greekCaret-2)+s.substring(greekCaret));
                    }
                    else{
                        greek.setText(s.substring(0,greekCaret-1)+s.substring(greekCaret));
                    }
                }
                else if(k.getKeyCode()==KeyEvent.VK_V&&k.isControlDown()){
                    try {
                        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                        String pasted = (String) cb.getData(DataFlavor.stringFlavor);
                        String translated=translator.translateEnToPs(pasted);
                        greek.setText(greek.getText().substring(0,greekCaret)+translated+greek.getText().substring(greekCaret));
                        greekCaret+=translated.length();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        });
        english.addCaretListener(e->{
            if(syncingCaret) return;
            syncingCaret=true;
            greekCaret =
            greek.getText().length()
            - translator.translateEnToPs(
            english.getText().substring(e.getDot())
            ).length();

        greekCaret = Math.max(0, Math.min(greekCaret, greek.getText().length()));
        greek.setCaretPosition(greekCaret);
        syncingCaret=false;
        });


        toolbar = new JToolBar();
        JButton fileButton = new JButton("File");
        JPopupMenu fileMenu=new JPopupMenu();
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        fileMenu.add(open);
        fileMenu.add(save);
        toolbar.add(fileButton);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(engVisible);    
        toolbar.setPreferredSize(new Dimension(0,40));
        fileButton.addActionListener(e->{
           fileMenu.show(fileButton, 0, fileButton.getHeight()); 
        });

        open.addActionListener(e->{
            try {
                int result = fileChooser.showOpenDialog(null);
                if(result==JFileChooser.APPROVE_OPTION){
                    File file=fileChooser.getSelectedFile();
                    BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
                    StringBuilder psB=new StringBuilder();
                    StringBuilder enB=new StringBuilder();
                    String line;
                    while((line=reader.readLine())!=null){
                        psB.append(line);
                        enB.append(translator.translatePsToEn(line));
                    }
                    reader.close();
                    greek.setText(psB.toString());
                    english.setText(enB.toString());
                }
            } catch (Exception exception) {
                System.out.println(exception);
            }
        });
        save.addActionListener(e->{
            try {
                int result = fileChooser.showSaveDialog(null);
                if(result==JFileChooser.APPROVE_OPTION){
                    File curentFile=fileChooser.getSelectedFile();
                    try(FileWriter writer=new FileWriter(curentFile.getAbsoluteFile());){
                        writer.write(greek.getText());
                    } catch (Exception exception) {
                        System.out.println(exception);
                    }
                }
            } catch (Exception exception) {
                System.out.println(exception);
            }
        });
        greek.setLineWrap(true);
        greek.setWrapStyleWord(true);
        english.setLineWrap(true);
        english.setWrapStyleWord(true);
        JScrollPane grScroll = new JScrollPane(greek);
        JScrollPane enScroll = new JScrollPane(english);
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            grScroll,
            enScroll
        );
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(6);

        engVisible.addActionListener(e->{
            if(engVisible.isSelected()){
                splitPane.setEnabled(true);     
                splitPane.setResizeWeight(0.5);
                splitPane.setDividerLocation(0.5);
                splitPane.setDividerSize(6);
            }
            else{
                splitPane.setDividerLocation(1.0);
                splitPane.setEnabled(false);
                splitPane.setDividerSize(0);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        this.add(toolbar, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        this.add(splitPane, gbc);

    }
}