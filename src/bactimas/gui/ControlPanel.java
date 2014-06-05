/**
 * 
 */
package bactimas.gui;
import icy.gui.frame.TitledFrame;
import ij.ImageJ;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import bactimas.bintree.BTreeEvent;
import bactimas.bintree.BTreeStateChange;
import bactimas.bintree.BTreeVisualizationPane;
import bactimas.bintree.Node;
import bactimas.datamodel.CurrentExperiment;
import bactimas.db.ConnectionManager;
import bactimas.db.DALService;
import bactimas.db.ExpMeasurement;
import bactimas.db.beans.BTreeElement;
import bactimas.db.beans.Bacteria;
import bactimas.db.beans.BacteriaSplit;
import bactimas.db.beans.BacteriaStateChange;
import bactimas.db.beans.Experiment;
import bactimas.db.beans.ExperimentEvent;
import bactimas.gui.ImageStrip.ImageStripType;
import bactimas.gui.frametree.FrameTree;
import bactimas.gui.sql.SQLQueryPane;
import bactimas.util.S;

/**
 * @author igor
 *
 */
public class ControlPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1873388061037128863L;
	private static String NAME = "BacTrack";
	private static String VERSION = "0.7.1.";
	public static Logger log = Logger.getLogger("bactimas.gui.ControlPanel" );	
	public static boolean debug = false; //true;
	
	private static JTextArea txtStatus;
    private JPanel jpStrips;
    
	private ImageStrip _originalBlueImageStrip;
	private ImageStrip _originalGreenImageStrip;
	private ImageStrip _originalRedImageStrip;
	
	private FrameTree _frameTree;
//	private SelectedFrameStrip _selFrameStrip;
	JSplitPane splitPane;
	
	private static ControlPanel _instance;
	private static JFrame _frame;
	private static TitledFrame _master;
	
	public static ControlPanel getInstance() {
//		if (_instance == null) {
//			_instance = new ControlPanel();
//		}
		return _instance;
	}
	public String readFile(String filename)
	{
	   String content = null;
	   File file = new File(filename); //for ex foo.txt
	   try {
	       FileReader reader = new FileReader(file);
	       char[] chars = new char[(int) file.length()];
	       reader.read(chars);
	       content = new String(chars);
	       reader.close();
	   } catch (IOException e) {
	       e.printStackTrace();
	   }
	   return content;
	}
	/* Only in 1.7
	static String readFile(String path, java.nio.charset.Charset encoding) {
		try {
			byte[] encoded = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
			return encoding.decode(ByteBuffer.wrap(encoded)).toString();
		} catch (IOException e) {
			return "Could not find help file:" + path;
		}
	}
	*/

	
	private ControlPanel() {				
		
		super(new BorderLayout());						
		setLayout(new BorderLayout(0, 0));
		
//		if (_instance != null ) {
//			throw new RuntimeException("There is already an instance of ControlPanel running...");
//		}		
		_instance = this;
		
		S.checkInitConfAndDataFolder();
		
		ConnectionManager.init();
		
		
		initGUI();
		
		sayHello();
		
		
		
		
		
	}	
	
	private void sayHello () {
		addStatusMessage("BacTrack v" +VERSION);
		addStatusMessage("Debug is:" + ControlPanel.debug);
		addStatusMessage("App folder is: " + S.getAppFolder());
		addStatusMessage("Config folder is: " + S.getConfigFolder());
		addStatusMessage("Database definition file is: " + S.getDbPropertiesAbsFileName());		
	}
	
	private void initGUI() {
		
		txtStatus = new JTextArea();
		txtStatus.setFont(new Font("Consolas",Font.PLAIN, 12));
		txtStatus.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(txtStatus);
		
		
		jpStrips = new JPanel();
		jpStrips.setBackground(SystemColor.info);
		jpStrips.setMinimumSize(new Dimension(100, 600));
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jpStrips, scrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(.7);
		
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	
	
	public ControlPanel(TitledFrame master) {				
		super(new BorderLayout());		
		setLayout(new BorderLayout(0, 0));
		
//		if (_instance != null ) {
//			throw new RuntimeException("There is already an instance of ControlPanel running...");
//		}		
		_instance = this;		
		_master = master;
		
		S.checkInitConfAndDataFolder();
		
		
		ConnectionManager.init();
		
		initGUI();
		
				
		master.add(getMenuBar(), BorderLayout.NORTH);
		master.add(this, BorderLayout.CENTER);
		
		sayHello();
		
	}
	
	
	public static boolean isPlugin() {
		return (_master != null);
	}
	
	protected void loadStrips () {

		
		if (isPlugin()) {
			_master.setTitle(
					  NAME 
					+ " " + VERSION  
					+ "   Experiment:" + CurrentExperiment.getExperiment().getExperimentName() 
					+ "(id=" + CurrentExperiment.getExperiment().getIdExperiment() + ")");
		} else {
			_frame.setTitle(
					  NAME 
					+ " " + VERSION  
					+ "   Experiment:" + CurrentExperiment.getExperiment().getExperimentName() 
					+ "(id=" + CurrentExperiment.getExperiment().getIdExperiment() + ")");	
		}
				
		
		addStatusMessage("Loading images. This can take a little while (10-20 secs), please wait...");
		
		Runnable job = 	new Runnable () {
			public void run() {
				JPanel strips = new JPanel();
				
				strips.setLayout(new BoxLayout(strips, BoxLayout.Y_AXIS));

				_originalRedImageStrip   = new ImageStrip(ImageStripType.RED);
				_originalGreenImageStrip = new ImageStrip(ImageStripType.GREEN);
				_originalBlueImageStrip  = new ImageStrip(ImageStripType.BLUE);
				
				
				
				strips.add(_originalBlueImageStrip.getImageStrip());
				strips.add(_originalRedImageStrip.getImageStrip());
				strips.add(_originalGreenImageStrip.getImageStrip());
				
				
				_frameTree = new FrameTree();
				
				
				splitPane.setLeftComponent(strips);
				splitPane.setDividerLocation(.8);
				
				
				
				ControlPanel.this.add(_frameTree, BorderLayout.EAST);	
				
				
				
				new SelectedFrameStrip(_originalBlueImageStrip);
				new SelectedFrameStrip(_originalRedImageStrip);
				new SelectedFrameStrip(_originalGreenImageStrip);
				
				
				ControlPanel.this.invalidate();
				ControlPanel.this.revalidate();
				ControlPanel.this.repaint();
				
				if (!isPlugin()) {
					ImageJ.main(new String[] {});
				}
			}
		};
		
		try {
	        SwingUtilities.invokeLater(job);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }		
		
		
		//executeInThread(job);
		

		
	}
    public static void __addStatusMessage(String msg) {
        
        txtStatus.append(msg + "\n");

 
        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        txtStatus.setCaretPosition(txtStatus.getDocument().getLength());
    }	
	public static void __________addStatusMessage(final String msg) {
		
			Document doc = txtStatus.getDocument();
			int origLen = doc.getLength();
			try {
				doc.insertString(origLen, "\n" + msg, null);
			} catch (BadLocationException exc) {
				// Odd APIs forces us to deal with this nonsense.
				IndexOutOfBoundsException wrapExc = new IndexOutOfBoundsException();
				wrapExc.initCause(exc);
				throw wrapExc;
			}
			txtStatus.repaint();
				
	}
	public static void addStatusMessage(final String msg) {
		java.awt.EventQueue.invokeLater(new Runnable() { public void run() {
			log.debug("addStatusMessage" + msg); 
			txtStatus.append(msg + "\n");
		     txtStatus.setCaretPosition(txtStatus.getDocument().getLength());
		}});		
	}	
	
	private static String 	MENU_ACTION_NEW_EXPERIMENT  = "new exp",
							MENU_ACTION_LOAD_EXPERIMENT  = "load exp",
							MENU_ACTION_DELETE_EXPERIMENT = "del",
							MENU_ACTION_EDIT_EXPERIMENT = "edit",
							MENU_ACTION_SHOW_BTREE = "BTREE",
							MENU_ACTION_DUMP_CSV = "dump csv",
							MENU_ACTION_SQL_PANE = "sql pane",
							MENU_ACTION_OPEN_HELP = "open help";
	
	private JComponent getMenuBar() {
		
			JMenuBar menuBar;
			JMenu menu;
			JMenuItem menuItem;
			//Create the menu bar.
			menuBar = new JMenuBar();

			//Build the first menu.
			menu = new JMenu("Experiment");
			menu.setMnemonic(KeyEvent.VK_E);
			menu.getAccessibleContext().setAccessibleDescription(
			        "Experiment menu");
			menuBar.add(menu);

			//a group of JMenuItems
			menuItem = new JMenuItem("New experiment",
			                         KeyEvent.VK_N);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
			        KeyEvent.VK_1, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription(
			        "Setup a new experiment");
			menuItem.setActionCommand(MENU_ACTION_NEW_EXPERIMENT);
			menuItem.addActionListener(this);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Load experiment",
                    KeyEvent.VK_L);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
			   KeyEvent.VK_2, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription(
			   "Loads an existing experiment from the database");
			menuItem.setActionCommand(MENU_ACTION_LOAD_EXPERIMENT);
			menuItem.addActionListener(this);
			menu.add(menuItem);			
			
			
			
			menuItem = new JMenuItem("Delete experiment",
                    KeyEvent.VK_D);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
			   KeyEvent.VK_3, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription(
			   "Deletes an existing experiment from the database");
			menuItem.setActionCommand(MENU_ACTION_DELETE_EXPERIMENT);
			menuItem.addActionListener(this);
			menu.add(menuItem);			
			
			menuItem = new JMenuItem("Edit current experiment",
                    KeyEvent.VK_D);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
			   KeyEvent.VK_4, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription(
			   "Edits the current experiment settings");
			menuItem.setActionCommand(MENU_ACTION_EDIT_EXPERIMENT);
			menuItem.addActionListener(this);
			menu.add(menuItem);				
			
		
//
			//Build second menu in the menu bar.
			menu = new JMenu("Reports");
			menu.setMnemonic(KeyEvent.VK_R);
			menu.getAccessibleContext().setAccessibleDescription("This menu shows various predefined reports");
			menuBar.add(menu);

			menuItem = new JMenuItem("BTree", KeyEvent.VK_B);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Shows the btree visualization");
			menuItem.setActionCommand(MENU_ACTION_SHOW_BTREE);
			menuItem.addActionListener(this);
			menu.add(menuItem);				

			menuItem = new JMenuItem("Dump CSV", KeyEvent.VK_D);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Dump CSV file");
			menuItem.setActionCommand(MENU_ACTION_DUMP_CSV);
			menuItem.addActionListener(this);
			menu.add(menuItem);				

			menuItem = new JMenuItem("SQL pane", KeyEvent.VK_S);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Execute user generated SQL.");
			menuItem.setActionCommand(MENU_ACTION_SQL_PANE);
			menuItem.addActionListener(this);
			menu.add(menuItem);				

			
			
			menu = new JMenu("Help");
			menu.setMnemonic(KeyEvent.VK_H);
			menu.getAccessibleContext().setAccessibleDescription("This menu shows help options");
			menuBar.add(menu);

			menuItem = new JMenuItem("Help on the web", KeyEvent.VK_H);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Start default browser with BacTrack's wiki page");
			menuItem.setActionCommand(MENU_ACTION_OPEN_HELP);
			menuItem.addActionListener(this);
			menu.add(menuItem);				
			
			
			
			return menuBar;		
	}
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		
		if (source.getActionCommand().equals(MENU_ACTION_NEW_EXPERIMENT)) {
			String[] channels = {"RED", "GREEN", "BLUE"};
			File[] channelFolders = new File[channels.length];
			for (int i = 0; i < channels.length; ++i ) {    		   
				JFileChooser fileChooser = new JFileChooser();
				if (i > 0) {
					fileChooser.setCurrentDirectory(channelFolders[i-1]);
				} else {
					fileChooser.setCurrentDirectory(new File(S.getAppFolder()));
				}
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Choose the " + channels[i]  + " channel folder.");
				int retval = fileChooser.showOpenDialog(this);
				if (retval == JFileChooser.APPROVE_OPTION) {
					channelFolders[i] = fileChooser.getSelectedFile();                          
				} else {
					S.out("new exp aborted");
					return;
				}
			}
			ExperimentDialog d;
			if (channelFolders[0].toString().startsWith(S.getAppFolder())) {
				// recommended - save relative path:
	        	d = new ExperimentDialog(
	        			channelFolders[0].toString().substring(1+S.getAppFolder().length()), 
	        			channelFolders[1].toString().substring(1+S.getAppFolder().length()), 
	        			channelFolders[2].toString().substring(1+S.getAppFolder().length()), 
	        			ExperimentDialog.ACTION_TYPE_INSERT
	        	);
			} else {
	        	d = new ExperimentDialog(
	        			channelFolders[0].toString(), 
	        			channelFolders[1].toString(), 
	        			channelFolders[2].toString(), 
	        			ExperimentDialog.ACTION_TYPE_INSERT
	        	);
			}
        	    		

        	d.showDialog();
			
			


		} else if (source.getActionCommand().equals(MENU_ACTION_LOAD_EXPERIMENT)) {
			
			Experiment[] exps = DALService.getAllExperiments();
			Experiment selected = (Experiment)JOptionPane.showInputDialog(
			                    null,
			                    "Choose the experiment:",
			                    "Experiment",
			                    JOptionPane.PLAIN_MESSAGE,
			                    null,
			                    exps,
			                    "");

			//If a string was returned, say so.
			if ((selected != null)) {
				
				try {
					CurrentExperiment.beginExperiment(
							selected.getExperimentName(),
							selected.getRedMovieFileName(), 
							selected.getGreenMovieFileName(),
							selected.getBlueMovieFileName(), 
							selected.getMovieSpf(),
							selected.getPixelWidthMicron(),
							selected.getPixelHeightMicron(), 
							selected.getPictureScale()
							);
					
					
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				loadStrips();
				
			} 			
		} else if (source.getActionCommand().equals(MENU_ACTION_DELETE_EXPERIMENT)) {
			Experiment[] exps = DALService.getAllExperiments();
			Experiment selected = (Experiment)JOptionPane.showInputDialog(
			                    null,
			                    "Choose the experiment to DELETE:",
			                    "Experiment",
			                    JOptionPane.PLAIN_MESSAGE,
			                    null,
			                    exps,
			                    "");

			//If a string was returned, say so.
			if ((selected != null)) {
				if (JOptionPane.showConfirmDialog(
					    null,
					    "Are you sure you want to delete " + selected.toString(),
					    "Delete experiment",
					    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					boolean deleted = CurrentExperiment.deleteExperiment(selected.getIdExperiment());
					ControlPanel.addStatusMessage("Experiment IS " + ((deleted) ? "" : "NOT ") + "deleted");
				}						
				
				
			} 			
		} else if (source.getActionCommand().equals(MENU_ACTION_EDIT_EXPERIMENT)) {
			if (CurrentExperiment.getExperiment() != null) {
	        	
				ExperimentDialog d = new ExperimentDialog(CurrentExperiment.getExperiment());
	        	d.showDialog();
	        	
			} else {
				JOptionPane.showConfirmDialog(
			    null,
			    "You must load the experiment first",
			    "Edit experiment",
			    JOptionPane.OK_OPTION); 
			}
																		
			 					
			
		} else if (source.getActionCommand().equals(MENU_ACTION_SHOW_BTREE)) {
			
			
			
			
        	final LinkedList<ExpMeasurement> builtIn = ExpMeasurement.getBuiltInMeasures();
        	final LinkedList<ExpMeasurement> user    = DALService.getUserMeasures(CurrentExperiment.getIdExperiment());
        	
        	
        	ArrayList<String> listOptions = new ArrayList<String>();
        	final String none = "-- none --";
        	listOptions.add(none);
        	for (ExpMeasurement m : builtIn) {
        		if (m.isPlottable()) listOptions.add(m.getName());
        	}
        	for (ExpMeasurement m : user) {
        		if (m.isPlottable()) listOptions.add(m.getName());
        	}
        	String[] options;
        	options = listOptions.toArray(new String[listOptions.size()]);
        	final JComboBox<String> cbOnWidth = new JComboBox<String>(options);
        	final JComboBox<String> cbOnColor = new JComboBox<String>(options);
        	cbOnWidth.setSelectedItem("area_square_microns");
        	cbOnColor.setSelectedItem("green_ctcf");
        	
        	final JTextField txtSpf = new JTextField("5");
        	final JTextField txtMinStrokeWidth = new JTextField("5");
        	final JTextField txtMaxStrokeWidth = new JTextField("50");
        	final JTextField txtFontSize = new JTextField("18");
        	final JCheckBox  chkDrawLabels = new JCheckBox("");
        	chkDrawLabels.setSelected(true);
        	final JCheckBox  chkConvertToHours = new JCheckBox("");
        	chkConvertToHours.setSelected(true);
        	
        	JPanel panel = new JPanel(new GridLayout(0,2));
    		panel.add(new JLabel("Map to branch width"));
    		panel.add(cbOnWidth);
    		panel.add(new JLabel("Map to branch color"));
    		panel.add(cbOnColor);
    		panel.add(new JLabel("Seconds per frame"));
			
			final int defSPF = CurrentExperiment.getSecondsPerFrame();
			txtSpf.setText("" + defSPF);    		
    		panel.add(txtSpf);
    		
    		panel.add(new JLabel("Min stroke width"));
    		panel.add(txtMinStrokeWidth);
    		panel.add(new JLabel("Max stroke width"));
    		panel.add(txtMaxStrokeWidth);    		
    		panel.add(new JLabel("Font size"));
    		panel.add(txtFontSize);
    		panel.add(new JLabel("Draw labels"));
    		panel.add(chkDrawLabels);    		
    		panel.add(new JLabel("Convert to hours"));
    		panel.add(chkConvertToHours);
    		


    		
            
    		final JDialog dialog = new JDialog(null, "Set btree settings", Dialog.ModalityType.APPLICATION_MODAL);
    		
    		JButton measureAway = new JButton("Draw");
//    		measureAway.setActionCommand(MEASURE_AWAY_COMMAND);
    		measureAway.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
//                    CurrentExperiment.materializeMeasurementsa();
        			int minStrokeWidth, maxStrokeWidth;
        			try {
        				minStrokeWidth = Integer.parseInt(txtMinStrokeWidth.getText());
        			} catch (Exception e1) {
        				minStrokeWidth = 5;
        				ControlPanel.addStatusMessage("Couldn't parse stroke width, reverting to default = 5.");
        			}		
        			try {
        				maxStrokeWidth = Integer.parseInt(txtMaxStrokeWidth.getText());
        			} catch (Exception e1) {
        				maxStrokeWidth = 50;
        				ControlPanel.addStatusMessage("Couldn't parse stroke width, reverting to default = 50.");
        			}	
        			int SPF;
        			try {
        				SPF = Integer.parseInt(txtSpf.getText());
        			} catch (Exception e1) {
        				SPF = defSPF;
        				ControlPanel.addStatusMessage("Couldn't parse SPF, reverting to " + defSPF) ;
        			}	
        			int fontSize;
        			try {
        				fontSize = Integer.parseInt(txtFontSize.getText());
        			} catch (Exception e1) {
        				fontSize = 18;
        				ControlPanel.addStatusMessage("Couldn't parse SPF, reverting to 18.") ;
        			}        			
        			boolean drawLables = chkDrawLabels.isSelected();
        			boolean convertToHours = chkConvertToHours.isSelected();
        			
        			int startingFrame = 1;
        			while (CurrentExperiment.getFrame(startingFrame).isIgnored()) startingFrame++;
        			
        			LinkedList<Bacteria> bacterias = CurrentExperiment.getBacteriasForFrame(startingFrame);
        			
        			for (Bacteria b: bacterias) {
        				LinkedList<BacteriaSplit> splits = CurrentExperiment.getBacteriaFamilySplits(b);
        				
        				if (splits.size() > 0) {
        					Node root = null;
        					int _maxFrame = Integer.MIN_VALUE, _maxLevel = Integer.MIN_VALUE;
    						
        					for (BacteriaSplit bs : splits) {
        						if (root == null) {
        							// public Node (String name, int frame, float intensity, String parentName, BTreeEventListener msg, int level, int levelIndex) {
        							root = new Node (bs.getParentName(), (startingFrame-1) * SPF, 0, bs.getParentName(), null, 0, 0);
        							_maxFrame = bs.getFrameNo();
        							_maxLevel = 0;

        						}
        						Node left = new Node (bs.getChildAName(), (bs.getFrameNo()-1) * SPF, 0, bs.getParentName(), null, -1, -1);
        						root.connect(left);
        	
        						Node right = new Node (bs.getChildBName(), (bs.getFrameNo()-1) * SPF, 0, bs.getParentName(), null, -1, -1);
        						root.connect(right);
        						
        						if ((bs.getFrameNo()-1) * SPF > _maxFrame) _maxFrame = SPF * (bs.getFrameNo()-1);
        						if (right.getLevel() > _maxLevel) _maxLevel = right.getLevel();	
        						ControlPanel.addStatusMessage("  Split at frame: " + bs.getFrameNo() 
        								+ " " + bs.getParentName() + " -> " + bs.getChildAName() + " + " + bs.getChildBName()
        						);
        					}
        					String onWidthSQL = "", onColorSQL = "";
        		        	for (ExpMeasurement m : builtIn) {
        		        		if (cbOnWidth.getSelectedItem().toString().equals(m.getName())) onWidthSQL = m.getSql();
        		        		if (cbOnColor.getSelectedItem().toString().equals(m.getName())) onColorSQL = m.getSql();
        		        	}
        		        	for (ExpMeasurement m : user) {
        		        		if (cbOnWidth.getSelectedItem().toString().equals(m.getName())) onWidthSQL = m.getSql();
        		        		if (cbOnColor.getSelectedItem().toString().equals(m.getName())) onColorSQL = m.getSql();
        		        	}
        		        	if (cbOnWidth.getSelectedItem().toString().equals(none)) onWidthSQL = "0 as none1";
    		        		if (cbOnColor.getSelectedItem().toString().equals(none)) onColorSQL = "0 as none2";        		        	
        					LinkedList<BTreeElement> items = CurrentExperiment.getBacteriaBTreeElements(b, onWidthSQL, onColorSQL);
        					double _minInt = 0, _maxInt = 0;
        					double _minSize = Double.MAX_VALUE,  _maxSize = 0;
        					boolean first = true;
        					for (BTreeElement item : items) {
        							
        						if (first) {
        							_minInt = _maxInt = item.getOnColor(); //  bi.getCtcf();
        							_minSize = _maxSize = item.getOnWidth(); // bi.getArea();
        						} else if (item.getOnColor() < _minInt) {
        							_minInt = item.getOnColor();
        						} else if (item.getOnColor() > _maxInt){
        							_maxInt = item.getOnColor();
        						}					
        						
        						if (item.getOnWidth() < _minSize) {
        							_minSize = item.getOnWidth();
        						} else if (item.getOnWidth() > _maxSize){
        							_maxSize = item.getOnWidth();
        						} 
        						
        						if ((item.getFrameNo()-1) * SPF > _maxFrame) _maxFrame = SPF * (item.getFrameNo()-1);
        						first = false;			
        						root.setIntensity(item.getBactName(), (item.getFrameNo()-1) * SPF, item.getOnColor());
        						root.setSize(item.getBactName(), (item.getFrameNo()-1) * SPF, item.getOnWidth());
        						ControlPanel.addStatusMessage("  Frame: " + item.getFrameNo() + " OnWidth(" + cbOnWidth.getSelectedItem() + ") = " + item.getOnWidth() 
        								+ " OnColor(" + cbOnColor.getSelectedItem()  + ") = " + item.getOnColor() 
        								+ "\t Node " + item.getBactName()
        							    + " \tTime:" +  (item.getFrameNo()-1) * SPF
        						);
        					}
        					ControlPanel.addStatusMessage("--Min/Max = " + _minInt + "/" + _maxInt);
        					LinkedList<Color> palette = CurrentExperiment.getPalette(1);
//        					BacteriaProcessor.dumpArrayListToLog(dbg, "btree Bacteria " + b);
        					
        					
        					BacteriaStateChange[] changes = CurrentExperiment.getAllBacteriaStateChanges();
        					if (changes != null && changes.length > 0) {
        						for (int i = 0; i < changes.length; ++i) {
        							boolean rv = root.addStateChange(
        									new BTreeStateChange(
        											(changes[i].getFrameNo()-1) * SPF,
        											changes[i].getStateTag(),
        											changes[i].getbName())
        									);
        							if (!rv) ControlPanel.addStatusMessage("Could not mark state change for " + changes[i]);
        						}
        					}
        					ExperimentEvent[] events1 = CurrentExperiment.getAllEvents();
        					LinkedList<BTreeEvent> events = new LinkedList<BTreeEvent>();
        					if (events1 != null && events1.length > 0) {
        						for (int i = 0; i < events1.length; ++i) {
        							events.addLast(new BTreeEvent((events1[i].getFrameNo()-1) * SPF, events1[i].getEventAbbr()));
        						}
        					}
        					
        					createAndShowBTree (
        							"bTree for " + b.getBactName() 
        							+ " OnColor=" + cbOnColor.getSelectedItem() + " (min,max) = (" + S.formatNumber(_minInt,2)  + ", " +  S.formatNumber(_maxInt, 2) + ")"
        							+ " OnWidth = " + cbOnWidth.getSelectedItem()+ " (min,max) = (" + S.formatNumber(_minSize, 2) + ", " + S.formatNumber(_maxSize, 2)   + ")",
        							// public BTreeVisualizationPane(Node root, int maxLevel, int maxFrame, LinkedList<Color> palette, double minInt, double maxInt, int minStrokeWidth, int maxStrokeWidth, double minSize, double maxSize, boolean drawLabel, boolean convertToHours)
        							new BTreeVisualizationPane(root, _maxLevel, _maxFrame, palette, _minInt, _maxInt, minStrokeWidth, maxStrokeWidth, _minSize, _maxSize, drawLables, convertToHours, events, cbOnColor.getSelectedItem().toString(), cbOnWidth.getSelectedItem().toString(), fontSize)
        					);
        					
        				} else {
        					ControlPanel.addStatusMessage("No splits found for " + b.getBactName() + ". Skipping, nothing to visualize.");
        				}
        				
        				
        			}                    
                    
                }
            });
    		
	        Container cp3 = dialog.getContentPane();
	        cp3.setLayout(new BorderLayout());
//	        cp3.add(new JLabel("Note that here you only define to what channel a measurement maps to." 
//	        		+ "\n In order for measure to be taken you also HAVE TO CHECK it via: ImageJ|Analyze|Set Measurements"), BorderLayout.NORTH);
	        cp3.add(panel, BorderLayout.CENTER);    	        
	        cp3.add(measureAway, BorderLayout.SOUTH);
	        
    	    dialog.setSize(400, 300);
    	    dialog.setLocationRelativeTo(null);
        	dialog.setVisible(true);  			
			
			
			
			

			

		} else if (source.getActionCommand().equals(MENU_ACTION_DUMP_CSV)) {
			JFileChooser fc = new JFileChooser();
			
			int retval = fc.showSaveDialog(null);

			if (retval == JFileChooser.APPROVE_OPTION)  {
				File csvFile = fc.getSelectedFile();
				CurrentExperiment.dumpCSV(csvFile);
			}
		} else if (source.getActionCommand().equals(MENU_ACTION_SQL_PANE)) {
		    (new SQLQueryPane()).setVisible(true);
		} else if (source.getActionCommand().equals(MENU_ACTION_OPEN_HELP)) {
			try {				 
				 Desktop.getDesktop().browse(new URI("http://homer.zpr.fer.hr/bactrack")); 
			}catch(Exception ex) {
				ex.printStackTrace();
			}

			
		}
		
		

			
		
		
		
		
		
	}
	
    private static void createAndShowBTree(String title, JPanel bTreePane) {
        //Create and set up the window.
    	_frame = new JFrame(title);
//        frame.setDefaultCloseOperation(JFrame.);

        
        _frame.setContentPane(bTreePane);
        _frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        //Display the window.
        _frame.pack();
        _frame.setVisible(true);
    }	
	


	
//    /**
//     * Create the GUI and show it.  For thread safety,
//     * this method should be invoked from the
//     * event-dispatching thread.
//     */
    private static void createAndShowGUI() {
        //Create and set up the window.
    	_frame = new JFrame(NAME + " " + VERSION);
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _instance = new ControlPanel();  
        
        
        _instance.setOpaque(true); //content panes must be opaque
        _frame.setContentPane(_instance);
        _frame.setJMenuBar((JMenuBar) _instance.getMenuBar());	
        
        _frame.setVisible(true);
        _frame.setExtendedState(Frame.MAXIMIZED_BOTH | _frame.getExtendedState());
        
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }	
	
	
}
