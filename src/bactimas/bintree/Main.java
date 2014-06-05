package bactimas.bintree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Main extends JPanel implements BTreeEventListener {
	JTextPane txtpnStatus;
	private Node root = null;
	JPanel jpTree;
	private int _maxFrame;
	private int _maxLevel;
	private double _minInt;
	private double _maxInt;
	private double _minSize;
	private double _maxSize;
	private static final long serialVersionUID = 1L;
	private JTextField txtFileName;
	private JTextField txtMinStrokeWidth;
	private JTextField txtMaxStrokeWidth;
	private JTextField txtIntensityFileName;
	private JTextField txtPaletteFileName;
	private JTextField txtBSizeFileName;
	JCheckBox drawLabel;
	JCheckBox convertToHours;
	private LinkedList<Color> palette;
	Document document;
	private String bTreeFileName;
	private String intensityFileName;
	private String paletteFileName;
	private String bSizeFileName;

	public Main() {
		initDefaults();

		this._maxFrame = (this._maxLevel = 0);
		this._minSize = (this._maxSize = -1.0D);
		setLayout(new BorderLayout(0, 0));

		JPanel commandsAndStatus = new JPanel(new GridLayout(2, 1));
		JScrollPane scrollPane = new JScrollPane();
		this.txtpnStatus = new JTextPane();
		this.txtpnStatus.setText("hELLO ŽM.");
		this.txtpnStatus.setPreferredSize(new Dimension(250, 300));

		scrollPane.setViewportView(this.txtpnStatus);

		this.jpTree = new JPanel();
		this.jpTree.setBackground(SystemColor.info);
		this.jpTree.setMinimumSize(new Dimension(100, 600));

		JSplitPane splitPane = new JSplitPane(0, commandsAndStatus, this.jpTree);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.3D);

		add(splitPane, "Center");

		JPanel panel = new JPanel();
		add(panel, "North");
		panel.setLayout(new FlowLayout(0));

		GridLayout gridLayout = new GridLayout(0, 2);
		JPanel txtBoxesPane = new JPanel(gridLayout);

		this.txtFileName = new JTextField();
		this.txtFileName.setText(this.bTreeFileName);
		this.txtFileName.setPreferredSize(new Dimension(800, 20));
		txtBoxesPane.add(this.txtFileName);
		this.txtFileName.setColumns(50);
		JButton btnParse = new JButton("Parse");
		txtBoxesPane.add(btnParse);
		btnParse.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Main.this.readTree(Main.this.txtFileName.getText(), Main.this);
			}
		});
		this.txtIntensityFileName = new JTextField();
		this.txtIntensityFileName.setText(this.intensityFileName);
		this.txtIntensityFileName.setPreferredSize(new Dimension(800, 20));
		txtBoxesPane.add(this.txtIntensityFileName);
		this.txtIntensityFileName.setColumns(50);
		JButton btnIntensity = new JButton("Load intensity");
		btnIntensity.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Main.this.loadIntensity(
						Main.this.txtIntensityFileName.getText(), Main.this);
			}
		});
		txtBoxesPane.add(btnIntensity);

		this.txtPaletteFileName = new JTextField();
		this.txtPaletteFileName.setText(this.paletteFileName);
		this.txtPaletteFileName.setPreferredSize(new Dimension(800, 20));
		txtBoxesPane.add(this.txtPaletteFileName);
		this.txtPaletteFileName.setColumns(50);

		JButton btnPalette = new JButton("Load palette");
		txtBoxesPane.add(btnPalette);
		btnPalette.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Main.this.loadPalette(Main.this.txtPaletteFileName.getText(),
						Main.this);
			}
		});
		this.txtBSizeFileName = new JTextField();
		this.txtBSizeFileName.setText(this.bSizeFileName);
		this.txtBSizeFileName.setPreferredSize(new Dimension(800, 20));
		txtBoxesPane.add(this.txtBSizeFileName);
		this.txtBSizeFileName.setColumns(50);

		JButton btnLoadSize = new JButton("Load size");
		txtBoxesPane.add(btnLoadSize);
		btnLoadSize.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Main.this.loadBSize(Main.this.txtBSizeFileName.getText(),
						Main.this);
			}
		});
		panel.add(txtBoxesPane);

		panel.add(Box.createHorizontalGlue());

		JPanel commandPane = new JPanel(new GridLayout(0, 1));
		JButton btnDraw = new JButton("Draw");
		commandPane.add(btnDraw);
		btnDraw.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Main.this.draw();
			}
		});
		this.drawLabel = new JCheckBox("Label");
		this.drawLabel.setSelected(true);
		commandPane.add(this.drawLabel);

		this.convertToHours = new JCheckBox("HH:MM");
		this.convertToHours.setSelected(true);
		commandPane.add(this.convertToHours);

		this.txtMinStrokeWidth = new JTextField();
		this.txtMinStrokeWidth.setText("5");
		this.txtMinStrokeWidth.setPreferredSize(new Dimension(500, 20));
		commandPane.add(this.txtMinStrokeWidth);
		this.txtMinStrokeWidth.setColumns(3);

		this.txtMaxStrokeWidth = new JTextField();
		this.txtMaxStrokeWidth.setText("50");
		this.txtMaxStrokeWidth.setPreferredSize(new Dimension(500, 20));
		commandPane.add(this.txtMaxStrokeWidth);
		this.txtMaxStrokeWidth.setColumns(3);

		panel.add(commandPane);
		commandsAndStatus.add(panel);
		commandsAndStatus.add(scrollPane);
	}
	
	
	private void draw() {
		
		setDefaultFileName("BTREE_FILENAME", bTreeFileName);
		setDefaultFileName("INT_FILENAME", intensityFileName);
		setDefaultFileName("PALETTE_FILENAME", paletteFileName);
		setDefaultFileName("BSIZE_FILENAME", bSizeFileName);
		
		saveDefaults();
		
		jpTree.removeAll();
		jpTree.setLayout(new BorderLayout(0, 0));
	    this.jpTree.add(new BTreeVisualizationPane(this.root, 
	    	      this._maxLevel, 
	    	      this._maxFrame, 
	    	      this.palette, 
	    	      this._minInt, 
	    	      this._maxInt, 
	    	      Integer.parseInt(this.txtMinStrokeWidth.getText()), 
	    	      Integer.parseInt(this.txtMaxStrokeWidth.getText()), 
	    	      this._minSize, 
	    	      this._maxSize, 
	    	      this.drawLabel.isSelected(), 
	    	      this.convertToHours.isSelected(),
	    	      null,
	    	      "onColorLablel",
	    	      "onWidthLabel", 
	    	      18), 
	    	      "Center");				
		jpTree.setBackground(Color.WHITE);		      	

		this.invalidate();
		this.revalidate();
		this.repaint();
	}
	public void readTree(String fileName, BTreeEventListener msg) {

		try {
			root = null;
			bTreeFileName = fileName;
			File aFile = new File(fileName);
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				
				while (( line = input.readLine()) != null){
					// 1;AB;AB
					// 33;A+B;A
					// 51;Aa+Ab;B
					String[] data = line.split(";");
					try {
						int f;
						String leftRight[];

						f = Integer.parseInt(data[0]); 
						if (root == null){            		           		
							root = new Node(data[2], f, 0, data[2], this, 0, 0); 
							_maxFrame = f;
							_maxLevel = 0;
						} else {
							leftRight = data[1].split("\\+");
							Node left = new Node (leftRight[0], f, 0, data[2], this, -1, -1);
							int c = root.connect(left);
							msg.addMessage("Node " + left + ((c==0) ? " NOT ": "") + " connected."); 

							Node right = new Node (leftRight[1], f, 0, data[2], this, -1, -1);
							c = root.connect(right);
							msg.addMessage("Node " + right + ((c==0) ? " NOT ": "") + " connected.");
							
							if (f > _maxFrame) _maxFrame = f;
							if (right.getLevel() > _maxLevel) _maxLevel = right.getLevel();
						} 
						

					} catch (Exception e) {
						e.printStackTrace();
						msg.addMessage(e.getMessage());
						return;
					}

				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
			addMessage(ex.getMessage());
		}


	}
	public void loadIntensity(String fileName, BTreeEventListener msg) {

		try {
			intensityFileName = fileName;
			File aFile = new File(fileName);
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				int i = 0;
				while (( line = input.readLine()) != null){
					//x;time;119
					//a;3660;242,167
					
					String[] data = line.split(";");
					try {
						double intensity;
						int time;
						intensity = Double.parseDouble(data[2].replace(',', '.'));
						time = Integer.parseInt(data[1]);
						if (i == 0) {
							_minInt = _maxInt = intensity;
						} else if (intensity < _minInt) {
							_minInt = intensity;
						} else if (intensity > _maxInt){
							_maxInt = intensity;
						}
						++i;
						
						msg.addMessage("Intensity " 
								+ intensity 
								+ " for node " + data[0]
							    + " at time:" + time
								+ ((root.setIntensity(data[0].trim().toUpperCase(), time, intensity) == 1) ? " IS set." : " IS NOT set." )
						);
						

					} catch (Exception e) {
						e.printStackTrace();
						msg.addMessage(e.getMessage());
						return;
					}

				}
				addMessage("//*-- Min intensity: " + _minInt + " Max intensity:" + _maxInt);
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
			addMessage(ex.getMessage());
		}


	}

	public void loadPalette(String fileName, BTreeEventListener msg) {

		try {
			paletteFileName = fileName;
			File aFile = new File(fileName);
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			palette = new LinkedList<Color>();
			try {
				String line = null; //not declared within while loop
				
				while (( line = input.readLine()) != null){
					//r,g,b
					
					String[] data = line.split(",");
					try {
						Color c = new Color(
								Integer.parseInt(data[0]), 
								Integer.parseInt(data[1]),
								Integer.parseInt(data[2])
								);
						
						msg.addMessage("Loaded color " 
								+ c 
						);
						palette.addLast(c);

					} catch (Exception e) {
						e.printStackTrace();
						msg.addMessage(e.getMessage());
						return;
					}

				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
			addMessage(ex.getMessage());
		}


	}	

	
	public void loadBSize(String fileName, BTreeEventListener msg) {

		try {
			bSizeFileName = fileName;
			File aFile = new File(fileName);
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				int i = 0;
				while (( line = input.readLine()) != null){
					//x;time;size
					//A;600;2.83
					//A;1200;2.83
					//A;1800;2.83
					
					String[] data = line.split(";");
					try {
						double size;
						int time;
						size = Double.parseDouble(data[2].replace(',', '.'));
						time = Integer.parseInt(data[1]);
						if (i == 0) {
							_minSize = _maxSize = size;
						} else if (size < _minSize) {
							_minSize = size;
						} else if (size > _maxSize){
							_maxSize = size;
						}
						++i;
						
						msg.addMessage("Size " 
								+ size 
								+ " for node " + data[0]
							    + " at time:" + time
								+ ((root.setIntensity(data[0].trim().toUpperCase(), time, size) == 1) ? " IS set." : " IS NOT set." )
						);
						

					} catch (Exception e) {
						e.printStackTrace();
						msg.addMessage(e.getMessage());
						return;
					}

				}
				addMessage("//*-- Min size: " + _minSize + " Max size:" + _maxSize);
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
			addMessage(ex.getMessage());
		}


	}
	
	public void setDefaultFileName(String key, String fileName){
	      
		org.w3c.dom.Node n = DOM.getFirstChildNodeWithName(document.getDocumentElement(), key);
	
	  	n.removeChild(n.getChildNodes().item(0));
	
	    n.appendChild(document.createTextNode(fileName));

	}


	
	private void initDefaults(){


	      DocumentBuilder builder;
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	      //factory.setValidating(true);
	      //factory.setNamespaceAware(true);
	      try {
	         builder = factory.newDocumentBuilder();
	      } catch(ParserConfigurationException pce){
	         pce.printStackTrace();
	         return;
	      }
	      try {
	         document = builder.parse( new File("btree.defaults.xml") );
	      } catch (IOException ioe) {
	      // this is ok, there is no default file, let's make one:
	         document = builder.newDocument();
	         Element root = document.createElement("root");
	         Element e;
	         document.appendChild(root);

//	         e = document.createElement("FONT_NAME");
//	         e.appendChild(document.createTextNode("Courier New"));
//	         root.appendChild(e);

	         e = document.createElement("BTREE_FILENAME");
	         e.appendChild(document.createTextNode(System.getProperty("user.home")+ File.separatorChar + "btree.csv"));
	         root.appendChild(e);

	         e = document.createElement("INT_FILENAME");
	         e.appendChild(document.createTextNode(System.getProperty("user.home") + File.separatorChar + "intensity.csv"));
	         root.appendChild(e);

	         e = document.createElement("PALETTE_FILENAME");
	         e.appendChild(document.createTextNode(System.getProperty("user.home") + File.separatorChar + "palette.txt"));
	         root.appendChild(e);

	         e = document.createElement("BSIZE_FILENAME");
	         e.appendChild(document.createTextNode(System.getProperty("user.home") + File.separatorChar + "bsize.csv"));
	         root.appendChild(e);

//	         S.out("creating new");
	         saveDefaults();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	     
	     
	      NodeList nl = document.getDocumentElement().getChildNodes();
	      for(int i=0; i < nl.getLength(); i++){
	         if (nl.item(i).getNodeName().equalsIgnoreCase("BTREE_FILENAME")){
	            try{
	            	bTreeFileName = new String(DOM.getTextFromDOMElement(nl.item(i)));
	            }catch(NumberFormatException ex){
	            }
	         } else if (nl.item(i).getNodeName().equalsIgnoreCase("INT_FILENAME")){
	            try{
	            	intensityFileName = new String(DOM.getTextFromDOMElement(nl.item(i)));
	            }catch(NumberFormatException ex){
	            }
	         }  else if (nl.item(i).getNodeName().equalsIgnoreCase("PALETTE_FILENAME")){
	            try{
	            	paletteFileName = new String(DOM.getTextFromDOMElement(nl.item(i)));
	            }catch(NumberFormatException ex){
	            }
	         } else if (nl.item(i).getNodeName().equalsIgnoreCase("BSIZE_FILENAME")){
				try{
					bSizeFileName = new String(DOM.getTextFromDOMElement(nl.item(i)));
				}catch(NumberFormatException ex){
				}
		      }



	      }
//	      Font newFont = new Font(fontName, fontStyle, fontSize);
//	      textArea.setFont( newFont );
	   }


	   private void saveDefaults(){
	      TransformerFactory tFactory =
	          TransformerFactory.newInstance();
	      try{
	         Transformer transformer = tFactory.newTransformer();
	         DOMSource source = new DOMSource(document);
	         StreamResult result = new StreamResult(new File("btree.defaults.xml"));	         
	         transformer.transform(source, result);
	      } catch (Exception e){
	         this.addMessage("Unable to save defaults:");
	         e.printStackTrace();
	      }
	   }
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        JFrame frame = new JFrame("Btree");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        
        frame.getContentPane().add(new Main()
                                   ,   BorderLayout.CENTER);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        
	}
	@Override
	public void addMessage(String msg) {
		// TODO Auto-generated method stub
		txtpnStatus.setText(txtpnStatus.getText() + "\n" + msg);
	}

}
