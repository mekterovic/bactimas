/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

//package components;

/*
 * This code is based on an example provided by Richard Stanford, 
 * a tutorial reader.
 */
package bactimas.gui.frametree;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.ResultsTable;
import ij.plugin.Macro_Runner;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import bactimas.alg.MovieProcessor;
import bactimas.datamodel.CurrentExperiment;
import bactimas.db.DALService;
import bactimas.db.beans.Bacteria;
import bactimas.db.beans.BacteriaStateChange;
import bactimas.db.beans.ExperimentEvent;
import bactimas.db.beans.ExperimentMeasurements;
import bactimas.gui.ControlPanel;
import bactimas.gui.events.FrameManager;
import bactimas.gui.events.IFrameListener;
import bactimas.util.S;
import signalprocesser.voronoi.VPoint;

public class FrameTree extends JPanel implements ActionListener, IFrameListener {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 9166734113852714762L;
	static Logger log = Logger.getLogger("bactimas.gui.FrameTree");
	protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    private static String   ADD_COMMAND = "add",
    						SPLIT_COMMAND = "split",
    						MOVE_SPLIT_COMMAND = "move split",
    						MAKE_MOVIE_COMMAND = "make movie",
    						MAKE_COLOR_MOVIE_COMMAND = "make color movie",
    						SET_ROI_COMMAND = "set roi",
    						DBG_COMMAND = "dbg",
    						TRUNC_COMMAND = "trunc",
    						GO_COMMAND = "go",
    						DETECT_TRANS_COMMAND = "detect trans",
    						COLLAPSE_COMMAND = "collapse",
    						
    						SET_ALG_COMMAND = "set algorithm", 
    						
    						EXPAND_COMMAND = "expand",
    						MEASURE_COMMAND = "MEASURE",
    						
    						SET_BG_GREEN_COMMAND = "set bg green",
    						SET_BG_RED_COMMAND = "set bg red",
    						SET_BG_BLUE_COMMAND = "set blue bg",
    						EDIT_EVENT_COMMAND = "add event",
    	    				CHANGE_STATE_COMMAND = "cg state event",
    	    				IGNORE_FRAME_COMMAND = "ignore frame";    						
    						
//    private static LinkedList<IBacteriaListener> bacteriaListeners = new LinkedList<IBacteriaListener>();
   
    
    public FrameTree() {        
    	
        super(new BorderLayout());
        
        rootNode = new DefaultMutableTreeNode(CurrentExperiment.getExperiment().getExperimentName());
        treeModel = new DefaultTreeModel(rootNode);
        
        treeModel.addTreeModelListener(new MyTreeModelListener());
        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new FrameTreeCellRenderer());
        javax.swing.ToolTipManager.sharedInstance().registerComponent(tree);

        
        // attach dbl click listener:
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {                	
                    if(e.getClickCount() == 1) {                    	
                        singleClick(selPath);
                    }
                    else if(e.getClickCount() == 2) {                    	
                        doubleClick(selPath);
                    }
                }
            }
        };
        tree.addMouseListener(ml);        
        
                
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
        
        
        JButton addButton = new JButton("Add");
        addButton.setActionCommand(ADD_COMMAND);
        addButton.addActionListener(this);
        
        JButton makeMovieButton = new JButton("Report");
        makeMovieButton.setActionCommand(MAKE_MOVIE_COMMAND);
        makeMovieButton.addActionListener(this);
        
        JButton makeColorMovieButton = new JButton("Color report");
        makeColorMovieButton.setActionCommand(MAKE_COLOR_MOVIE_COMMAND);
        makeColorMovieButton.addActionListener(this);        

        JButton setRoiButton = new JButton("Set ROI");
        setRoiButton.setActionCommand(SET_ROI_COMMAND);
        setRoiButton.addActionListener(this);        

        JButton splitButton = new JButton("Split");
        splitButton.setActionCommand(SPLIT_COMMAND);
        splitButton.addActionListener(this);        
        

        JButton moveSplitButton = new JButton("Move/del split");
        moveSplitButton.setActionCommand(MOVE_SPLIT_COMMAND);
        moveSplitButton.addActionListener(this);          
        
        JButton goButton = new JButton("Track");
        goButton.setActionCommand(GO_COMMAND);
        goButton.addActionListener(this);         
        
        JButton setAlgButton = new JButton("Set algorithm");
        setAlgButton.setActionCommand(SET_ALG_COMMAND);
        setAlgButton.addActionListener(this);    
        
        
        JButton dbgButton = new JButton("dbg");
        dbgButton.setActionCommand(DBG_COMMAND);
        dbgButton.addActionListener(this);      
        
        JButton truncButton = new JButton("Trunc (after)");
        truncButton.setActionCommand(TRUNC_COMMAND);
        truncButton.addActionListener(this);      

        
        
        JButton detectTransButton = new JButton("Detect trans");
        detectTransButton.setActionCommand(DETECT_TRANS_COMMAND);
        detectTransButton.addActionListener(this);  

        JButton collapseButton = new JButton("Collapse all");
        collapseButton.setActionCommand(COLLAPSE_COMMAND);
        collapseButton.addActionListener(this);  
        
        JButton expandButton = new JButton("Expand all");
        expandButton.setActionCommand(EXPAND_COMMAND);
        expandButton.addActionListener(this);  
        
        JButton measureButton = new JButton("Measure");
        measureButton.setActionCommand(MEASURE_COMMAND);
        measureButton.addActionListener(this);          

        JButton setBgGreenButton = new JButton("Set/msr bg(GREEN)");
        setBgGreenButton.setActionCommand(SET_BG_GREEN_COMMAND);
        setBgGreenButton.addActionListener(this);            

        JButton setBgRedButton = new JButton("Set/msr bg(RED)");
        setBgRedButton.setActionCommand(SET_BG_RED_COMMAND);
        setBgRedButton.addActionListener(this);          
        
        JButton setBgBlueButton = new JButton("Set/msr bg(R+G+B)");
        setBgBlueButton.setActionCommand(SET_BG_BLUE_COMMAND);
        setBgBlueButton.addActionListener(this);           
        

        JButton addEvent = new JButton("Edit event");
        addEvent.setActionCommand(EDIT_EVENT_COMMAND);
        addEvent.addActionListener(this);   
        
        JButton changeBacteriaState = new JButton("Change bact state");
        changeBacteriaState.setActionCommand(CHANGE_STATE_COMMAND);
        changeBacteriaState.addActionListener(this);  

        
        JButton ignoreFrameButton = new JButton("Toggle ignore frame");
        ignoreFrameButton.setActionCommand(IGNORE_FRAME_COMMAND);
        ignoreFrameButton.addActionListener(this);        
        
        JPanel panel = new JPanel(new GridLayout(0,3));
        
        panel.add(detectTransButton);
        panel.add(addButton);
        panel.add(setRoiButton);
        
        panel.add(goButton);
        panel.add(splitButton);
        panel.add(moveSplitButton);
        
//        panel.add(setBgRedButton);
//        panel.add(setBgGreenButton);
        panel.add(setAlgButton);
        panel.add(setBgBlueButton);
        panel.add(makeColorMovieButton);                       
        
        panel.add(makeMovieButton);        
        panel.add(measureButton); 
        panel.add(truncButton);
        
        
        panel.add(collapseButton);
        panel.add(expandButton);
        panel.add(dbgButton);
        
        panel.add(addEvent);
        panel.add(changeBacteriaState);        
        panel.add(ignoreFrameButton);
        
        
        
//        add(panel, BorderLayout.NORTH);
        add(panel, BorderLayout.SOUTH);                        
        
        FrameManager.addFrameListener(this); // That's right, I'm listening to myself. Shut up.
        
        initNodes();
        scrollPane.getVerticalScrollBar().setValue(0);
        
        
    }
    
 

//
//	public void bacteriaSetROI33(BacteriaNode bn, Roi roi){
//		log.debug("Bacteria set roi in FrameTree");
//		bn.addOrReplaceHumanRoi(roi);
//		bn.setHumanROI(roi);		
//	}    
//    
    
    @SuppressWarnings("unchecked")
	private void singleClick (TreePath tp) {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) (tp.getLastPathComponent());
		//System.out.println("click on " + tp);
		if (selectedNode.getUserObject() instanceof BacteriaNode) {
			
//			BacteriaNode bn = (BacteriaNode) selectedNode.getUserObject();
//			List<Roi> rois = new LinkedList<Roi>();			
//			rois.addAll(bn.getRois());
//			FrameManager.fireFrameSelected(this, bn.getParent().getFrameNo(), rois);			
			
		} else if (selectedNode.getUserObject() instanceof FrameNode) {
			
			List<Roi> rois = new LinkedList<Roi>();		
			for (DefaultMutableTreeNode n: Collections.list( (Enumeration<DefaultMutableTreeNode>) selectedNode.children())) {
				rois.addAll(((BacteriaNode) n.getUserObject()).getRois());				
			} 
			FrameManager.fireFrameSelected(this, ((FrameNode)selectedNode.getUserObject()).getFrameNo(), rois);
		}   else if (selectedNode.getUserObject() instanceof RoiNode) {
//			RoiNode rn = (RoiNode) selectedNode.getUserObject();
//			List<Roi> rois = new LinkedList<Roi>();			
//			rois.add(rn.getRoi());
//			FrameManager.fireFrameSelected(this, ((FrameNode)((DefaultMutableTreeNode)(selectedNode.getParent().getParent())).getUserObject()).getFrameNo(), rois);			
		}  	
    }
    private void doubleClick (TreePath tp) {    	
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) (tp.getLastPathComponent());
		
		if (selectedNode.getUserObject() instanceof FrameNode) {
			addBacteria();
		} else if (selectedNode.getUserObject() instanceof HumanRoiNode) {
			setHumanRoi(selectedNode);			
		}
    }
    
    
    //@SuppressWarnings("rawtypes")
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (ADD_COMMAND.equals(command)) {
            //Add button clicked
        	DefaultMutableTreeNode selectedNode =  (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
        	if (selectedNode.getUserObject() instanceof FrameNode) {
//        		CurrentExperiment.addBacteria(((FrameNode)selectedNode.getUserObject()).getFrameNo());
        		addBacteria();	        	
        	} else if (selectedNode.getUserObject() instanceof BacteriaNode) {
        		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode;
				addHumanRoiIfNotExisting(parent);
        	}
          
        } else if (MAKE_MOVIE_COMMAND.equals(command)) {
            //Remove button clicked
        	boolean color = false;
        	
        	if (JOptionPane.YES_OPTION  == JOptionPane.showConfirmDialog(null, "Do you want color stack (with green and red selections)?", "?", JOptionPane.YES_NO_OPTION )) {					
        		color = true;				
			}	
        	
        	
        	int f = 1;
        	ImageStack stack = new ImageStack(
        			CurrentExperiment.getBlueImagePlus(1, null).getProcessor().getWidth(), 
        			CurrentExperiment.getBlueImagePlus(1, null).getProcessor().getHeight()
        			);
        	
        	while (f <= CurrentExperiment.getFrameCount()) {
        		if (!CurrentExperiment.getFrame(f).isIgnored()) {  
	        		 LinkedList<Bacteria> bacterias = CurrentExperiment.getBacteriasForFrame(f);
	        		 if (bacterias == null || bacterias.size() == 0) break;
	        		 int bsize = 0;
	        		 ImageProcessor slice;
	        		 if (color) 
	        			 slice = CurrentExperiment.getBlueImagePlus(f, null).getProcessor().convertToRGB();
	        		 else
	        			 slice = CurrentExperiment.getBlueImagePlus(f, null).getProcessor();
	        		 for (Bacteria b : bacterias) {
	        			 if (color)
	        				 slice.setColor(Color.GREEN);
	        			 else 
	        				 slice.setColor(Color.WHITE);
	        			 Roi r = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), f);
	        			 if (r == null) {
	        				 r = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), f);
	        				 if (color)
		        				 slice.setColor(Color.RED);
		        			 else 
		        				 slice.setColor(Color.BLACK);	        				 	        				
	        			 }
	        			 if (r == null) break;
	        			 bsize++;
	        			 r.drawPixels(slice);
	        			 //colSlice.setRoi(r);	        			
	        			 
	        		 }
	        		 stack.addSlice("Frame:" + f + "/" +  CurrentExperiment.getFrameCount(), slice);	        		 
	
	        		 if (bsize != bacterias.size()) break;
        		}
        		 ++f;
        	} 
        	ImagePlus imp = new ImagePlus("Report", stack);        	
    		imp.show();
    		
                    
        } else if (MAKE_COLOR_MOVIE_COMMAND.equals(command)) {
            
        	int f = 1;
        	ImageStack colStack = new ImageStack(
        			CurrentExperiment.getBlueImagePlus(1, null).getProcessor().getWidth(), 
        			CurrentExperiment.getBlueImagePlus(1, null).getProcessor().getHeight()
        			);
        	while (f <= CurrentExperiment.getFrameCount()) {
        		if (!CurrentExperiment.getFrame(f).isIgnored()) {  
	        		 LinkedList<Bacteria> bacterias = CurrentExperiment.getBacteriasForFrame(f);
	        		 if (bacterias == null || bacterias.size() == 0) break;
	        		 int bsize = 0;	        		 
	        		 ImageProcessor colSlice = new ColorProcessor(colStack.getWidth(), colStack.getHeight());
	        		 for (Bacteria b : bacterias) {	        			 
	        			 colSlice.setColor(Color.WHITE);
	        			 Roi r = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), f);
	        			 if (r == null) {
	        				 r = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), f);	        				 
	        				 colSlice.setColor(getColorForBacteria(b.getBactName()));
	        			 }
	        			 if (r == null) break;
	        			 bsize++;	        			 	        			 
	        			 colSlice.fill(r);
	        			 
	        		 }	        		 
	        		 colStack.addSlice("Frame:" + f + "/" +  CurrentExperiment.getFrameCount(), colSlice);
	
	        		 if (bsize != bacterias.size()) break;
        		}
        		 ++f;
        	} 
        	
        	ImagePlus impCol = new ImagePlus("Color Report ", colStack);        	
        	impCol.show();
			
			
        	
            // removeCurrentNode();        
        } else if (SET_ROI_COMMAND.equals(command)) {
            //sET button clicked.
        	DefaultMutableTreeNode selectedNode =  (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
        	if (selectedNode.getUserObject() instanceof HumanRoiNode) {
        		
				setHumanRoi(selectedNode);
				
        	} else {
        		JOptionPane.showConfirmDialog(null,
						"Cannot set ROI, bacteria not selected.", "Info",
						JOptionPane.OK_OPTION);
        	}
        } else if (GO_COMMAND.equals(command)) {
        	
        	
      	
        	
//            //go button clicked.
        	final DefaultMutableTreeNode selectedNode =  (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
        	if (selectedNode.getUserObject() instanceof FrameNode) {
        		
        		
        		
        		
//        		final JTextField txtBallRadius = new JTextField("4");
        		final JTextField txtFramesToProcess = new JTextField("5");
//        		final JTextField txtRange = new JTextField("20");
//        		final JComboBox cboThMethod = new JComboBox( S.concat(new String[] {"don't know"}, AutoThresholder.getMethods()));
//        		
//        		
//        		JRadioButton redButton = new JRadioButton("Red");        	    
//        	    redButton.setActionCommand("Red");
//        	    JRadioButton greenButton = new JRadioButton("Green");        	    
//        	    greenButton.setActionCommand("Green");
//        	    JRadioButton blueButton = new JRadioButton("Blue");        	    
//        	    blueButton.setActionCommand("Blue");
//        	    blueButton.setSelected(true);
//        	    
//        	    //Group the radio buttons.
//        	    final ButtonGroup channelsGroup = new ButtonGroup();
//        	    channelsGroup.add(redButton);
//        	    channelsGroup.add(greenButton);
//        	    channelsGroup.add(blueButton);
//        		
        		JPanel panel = new JPanel(new GridLayout(0,2));
//        		panel.add(new JLabel("Ball radius"));
//        		panel.add(txtBallRadius);
        		
        		panel.add(new JLabel("Frame(s) to process"));
        		panel.add(txtFramesToProcess);
        		
//        		panel.add(new JLabel("Channel"));
//        		JPanel radioPanel = new JPanel(new FlowLayout());
//        		radioPanel.add(redButton);
//        		radioPanel.add(greenButton);
//        		radioPanel.add(blueButton);
//        		panel.add(radioPanel);
//        		panel.add(new JLabel("Range"));
//        		panel.add(txtRange);    		
//        		panel.add(new JLabel("Threshold method"));
//        		
//        		JButton btnHelpDecide = new JButton("Help decide");
//        		btnHelpDecide.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent e) {
//                    	ImagePlus imp = null; 
//                    	String channel = S.getSelectedButtonText(channelsGroup);
//                		if (channel.equals("Red")) { 
//                			imp = CurrentExperiment.getRedImagePlus(((FrameNode)((DefaultMutableTreeNode)selectedNode).getUserObject()).getFrameNo(), "PNG");
//                		} else if (channel.equals("Green")){
//                			imp = CurrentExperiment.getGreenImagePlus(((FrameNode)((DefaultMutableTreeNode)selectedNode).getUserObject()).getFrameNo(), "PNG");
//                		} else if (channel.equals("Blue")) {
//                			imp = CurrentExperiment.getBlueImagePlus(((FrameNode)((DefaultMutableTreeNode)selectedNode).getUserObject()).getFrameNo(), "PNG");
//                		}
//                		
//                		ImageStack stckMethods = new ImageStack(imp.getWidth(), imp.getHeight()); 			
//                		for (int i = 0; i < AutoThresholder.getMethods().length; ++i) {
//                			ByteProcessor sobel = new ByteProcessor(imp.getProcessor(), true);				
//                			sobel.filter(ImageProcessor.FIND_EDGES);
//                			ImagePlus impSobel = new ImagePlus("Channel:" + channel + " Method:" + AutoThresholder.getMethods()[i], sobel);		
//                			int th = (new AutoThresholder()).getThreshold(AutoThresholder.getMethods()[i], sobel.getHistogram()); 
//                			IJ.setThreshold(impSobel, th, 255);
//                		    IJ.run(impSobel, "Convert to Mask", "");
//                		    
//                		    MovieProcessor.removeSingleDots(impSobel.getProcessor());
//                		    MovieProcessor.skeletonize((ByteProcessor)impSobel.getProcessor());  
//                		    stckMethods.addSlice("Channel:" + channel + " Method:" + AutoThresholder.getMethods()[i], impSobel.getProcessor());
//                    	}
//                		(new ImagePlus("Find the method where cell borders are best outlined ", stckMethods)).show();                    	
//                    	
//                    }
//        		});
//        		JPanel jpMethod  = new JPanel(new FlowLayout());
//        		jpMethod.add(cboThMethod);
//        		jpMethod.add(btnHelpDecide);
//        		panel.add(jpMethod);  
                
        		final JDialog dialog = new JDialog(null, "Set processing settings", Dialog.ModalityType.MODELESS);        		
        		
        		
        		JButton btnGo = new JButton("Go");
//        		measureAway.setActionCommand(MEASURE_AWAY_COMMAND);
        		btnGo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                		Runnable job = new Runnable() {					
        					@Override
        					public void run() {
        						
        		        		int max = S.parseInt(txtFramesToProcess.getText(), "Frame(s) to process", 5);	
//        		        		int ballRadius = S.parseInt(txtBallRadius.getText(), "Ball radius", 4);
//        		        		int range = S.parseInt(txtRange.getText(), "Range", 10);
//        		        		String channel = S.getSelectedButtonText(channelsGroup);
        		        		
        		        		
        		        		MovieProcessor.process(((FrameNode)((DefaultMutableTreeNode)selectedNode).getUserObject()).getFrameNo(),
        		        				((FrameNode)((DefaultMutableTreeNode)selectedNode).getUserObject()).getFrameNo() + max - 1
        		        				);
        		        		
//        		        		for (int i = 0; i < max; ++i){
//        		    	        	MovieProcessor.process(
//        		    	        			((FrameNode)((DefaultMutableTreeNode)selectedNode).getUserObject()).getFrameNo() + i,
//        		    	        			ballRadius,
//        		    	        			range,
//        		    	        			(channel == "Blue") ? ImageStripType.BLUE : (channel == "Red") ? ImageStripType.RED : ImageStripType.GREEN,
//        		    	        			cboThMethod.getSelectedItem().toString().equals("don't know") ? null: cboThMethod.getSelectedItem().toString()
//        		    	        			);        			
//        		        		}
        		        		reloadTree(); 
        		        		ControlPanel.addStatusMessage("Done.");
        					}
        				};
                		// if executed in EDT thread (invokeLater) ImageJ's windows don't get repainted 
        				S.executeInCThread(job);                        
                    }
        		});
        		
    	        Container cp3 = dialog.getContentPane();
    	        cp3.setLayout(new BorderLayout());
    	        cp3.add(panel, BorderLayout.CENTER);    	        
    	        cp3.add(btnGo, BorderLayout.SOUTH);
    	        
        	    dialog.setSize(500, 100);
        	    dialog.setLocationRelativeTo(null);
            	dialog.setVisible(true);  			
    			
        		

	
				
        	} else {
        		JOptionPane.showConfirmDialog(null,
						"Frame not selected.", "Info",
						JOptionPane.OK_OPTION);
        	}        	
        	
        } else if (DBG_COMMAND.equals(command)) {
            
        	
        	
    		
//    		ImagePlus imp1 = CurrentExperiment.getNewDbgColorImagePlusWindow(1);
//    		ImagePlus imp2 = CurrentExperiment.getNewDbgColorImagePlusWindow2(1);
//    		
//    		ImageStack dbg = new ImageStack(imp1.getProcessor().getWidth(), imp1.getProcessor().getHeight());
//    		
//    		
//    		dbg.addSlice(imp1.getProcessor());
//    		dbg.addSlice(imp2.getProcessor());
//    		ImagePlus dbgCOlor = new ImagePlus("---dbg", dbg);
//    		dbgCOlor.show();        	
//        	
//        	imp1.show();
//        	imp2.show();
        	
        	
        	
        	//if (1<2) return;
        	
        	
        	
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				int f = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
				LinkedList<Bacteria> bacterias = CurrentExperiment
						.getBacteriasForFrame(f);
				if (bacterias == null || bacterias.size() == 0)
					return;

				ImageProcessor slice = CurrentExperiment.getBlueImagePlus(f, null).getProcessor();
				
				VPoint translation = DALService.getFrameTranslation(f);
//				BacteriaProcessor.detectTranslationSobel(f, 50);
				for (Bacteria b : bacterias) {
					slice.setColor(Color.WHITE);
					Roi r = CurrentExperiment.getHumanRoiForBacteria(
							b.getIdBacteria(), f - 1);
					if (r == null) {
						r = CurrentExperiment.getComputerRoiForBacteria(
								b.getIdBacteria(), f - 1);
					}
					if (r == null)
						break;

						
					Rectangle r123 = r.getBounds(); 
					r.setLocation(r123.x + translation.x, r123.y + translation.y);
					
					
					r.drawPixels(slice);

					ImageProcessor moldSkeleton = MovieProcessor.skeletonize(r);
					Rectangle offset = r.getBounds();
					for (int x = 0; x < moldSkeleton.getWidth(); ++x) {
						for (int y = 0; y < moldSkeleton.getHeight(); ++y) {
							if (moldSkeleton.getPixel(x, y) > 0) {
								slice.putPixel(offset.x + x, offset.y + y, 200);
							}
						}
					}
					VPoint[] endpoints = MovieProcessor.getSkeletonEndpoints((ByteProcessor) moldSkeleton);

					for (int i = 0; i < endpoints.length; ++i) {
						slice.putPixel(endpoints[i].x + offset.x, endpoints[i].y + offset.y, 255);
					}

					r = CurrentExperiment.getComputerRoiForBacteria(
							b.getIdBacteria(), f);
					slice.setColor(Color.BLACK);
					r.drawPixels(slice);
					// firstPass = false;

				}
				ImagePlus imp = new ImagePlus("#Debug frame " + f, slice);

				imp.show();

			} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected.",
						"Info", JOptionPane.OK_OPTION);
			}  
        	
			
        } else if (TRUNC_COMMAND.equals(command)) {
            //
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				final int f = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
				if (JOptionPane.YES_OPTION  == JOptionPane.showConfirmDialog(null, "Do you really want to truncate (delete) all ROIs (and splits) equall or bigger than frame " + f, "?", JOptionPane.YES_NO_OPTION )) {					
					try {
						
						Runnable job = new Runnable() {
							public void run() {
								ControlPanel.addStatusMessage("Truncate result (f >=" + f + ") = " + DALService.truncateFromFrame(f));
								ControlPanel.addStatusMessage("Reloading tree, please wait...");
								reloadTree();
								ControlPanel.addStatusMessage("Done.");
							}
						};
						S.executeInCThread(job);						
						
					} catch (Exception e1) {
						ControlPanel.addStatusMessage("Couldn't parse the assigned 'number'");
					}					
					
				}								
				  
			} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected.",
						"Info", JOptionPane.OK_OPTION);
			}  
        	
			// MOVE_SPLIT_COMMAND	       
        
        } else if (DETECT_TRANS_COMMAND.equals(command)) {
            //
        	
			final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {

				Runnable job = new Runnable() {
					public void run() {
						int f = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
						int max = Integer.parseInt(JOptionPane.showInputDialog("Last frame to process (how many frames)?", "5"));
						int range = Integer.parseInt(JOptionPane.showInputDialog("Range?", "20"));
						if (range == 0) {
							ControlPanel.addStatusMessage("Reseting ALL translations to 0.");
							VPoint trans = new VPoint(0, 0);
							for (; f <= max; ++f) {															
								ControlPanel.addStatusMessage("  Setting translation for frame " + f + ":" + trans);
								DALService.updateFrameTranslation(f, trans);
							}
						} else {
							ControlPanel.addStatusMessage("Detecting translations with range: " + range);
							if ((max - f) > 20) ControlPanel.addStatusMessage("  (Go grab a coffee...)");
							for (; f <= max; ++f) {							
								VPoint trans = MovieProcessor.detectTranslationSobel(f, range);
								ControlPanel.addStatusMessage("  Detected translation for frame " + f + ":" + trans);
								DALService.updateFrameTranslation(f, trans);
							}
						}
						ControlPanel.addStatusMessage("Done.");
						reloadTree(); 
					}
				};
				
				try {
			        SwingUtilities.invokeLater(job);
			    } catch (Exception exc) {
			        exc.printStackTrace();
			        log.error(exc);
			    }	
				//ControlPanel.executeInThread(job);
 
			} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected.",
						"Info", JOptionPane.OK_OPTION);
			}  
        	
			//
			       
        } else if (SPLIT_COMMAND.equals(command)) {
            //
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof BacteriaNode) {
				Bacteria b = ((BacteriaNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getBacteria();
				if (JOptionPane.YES_OPTION  == JOptionPane.showConfirmDialog(null, "Do you really want to split " + b.getBactName(), "?", JOptionPane.YES_NO_OPTION )) {
					Bacteria[] bactAB = DALService.splitBacteria(b, ((FrameNode)(((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject())).getFrameNo());
					DefaultMutableTreeNode parentNode =  (DefaultMutableTreeNode) selectedNode.getParent();
			        
			        BacteriaNode bnA = new BacteriaNode(
			        		(FrameNode) parentNode.getUserObject(),
			        		 bactAB[0]
			        		);
			        BacteriaNode bnB = new BacteriaNode(
			        		(FrameNode) parentNode.getUserObject(),
			        		 bactAB[1]
			        		);
			        addObject(parentNode, bnA, true);					
			        addObject(parentNode, bnB, true);
										
					removeCurrentNode();
					
					reloadTree();
				}								
				  
			} else {
				JOptionPane.showConfirmDialog(null, "Bacteria not selected.",
						"Info", JOptionPane.OK_OPTION);
			}  
        	
			//SPLIT_COMMAND
			   
        } else if (MOVE_SPLIT_COMMAND.equals(command)) {
            //
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof BacteriaNode) {
				final Bacteria b = ((BacteriaNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getBacteria();
				if (JOptionPane.YES_OPTION  == JOptionPane.showConfirmDialog(null, "Do you really want to move or delete this split ", "?", JOptionPane.YES_NO_OPTION )) {
					//Bacteria[] bactAB = DALService.splitBacteria(b, ((FrameNode)(((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject())).getFrameNo());
					//DefaultMutableTreeNode parentNode =  (DefaultMutableTreeNode) selectedNode.getParent();
					final int delta;
					try {
						
						delta = Integer.parseInt(JOptionPane.showInputDialog(null, "Please, give the 'delta' - where to move the split: " 
								+ "\n  (a) to DELETE the split: 0 "
								+ "\n  (b) to move split N frames up (e.g. from frame 10 to frame 5): give a negative number of frames (e.g. -5) "
								+ "\n  (c) to move split N frames down (e.g. from frame 10 to frame 17): give a postivie number of frames  (e.g. 7) "
								+ "\n\n So, please enter 0 or some positive or negative number (or if you wanna bail out - don't give any number)."
								+ "\n\n What will happen to your data?"
								+ "\n - (i) all bacteria (ROIs) unrelated to the split will not be affected"
								+ "\n - (ii) MOVE: affected bacteria (ROIs), that is parent and two children, will be deleted in the affected interval (e.g. from frame 10 to frame 15) and will not be affected outside the interval (that's good news :)) "
								+ "\n - (iii) DELETE: all children bacteria (ROIs) downwards (from the split frame to the end frame) will BE DELETED !!\n\n"
								,  "Where to?", 1));
						
						Runnable job = new Runnable() {
							public void run() {
								ControlPanel.addStatusMessage(DALService.moveBacteriaSplit(b, delta));
								ControlPanel.addStatusMessage("Reloading tree, please wait...");
								reloadTree();
								ControlPanel.addStatusMessage("Done. \nPlease check the split and note that you'll have to rebuild (reckognize) and re-measure bacteria in the affected interval.");
							}
						};
						S.executeInCThread(job);						
						
					} catch (Exception e1) {
						ControlPanel.addStatusMessage("Couldn't parse the assigned 'number'");
					}					
					
				}								
				  
			} else {
				JOptionPane.showConfirmDialog(null, "Bacteria not selected.",
						"Info", JOptionPane.OK_OPTION);
			}  
        	
			// MOVE_SPLIT_COMMAND
			
        } else if (COLLAPSE_COMMAND.equals(command)) {
        	expandAll(tree, false);
			tree.expandRow(0);       
        } else if (EXPAND_COMMAND.equals(command)) {
            
        	expandAll(tree, true);
        	
			       
        } else if (MEASURE_COMMAND.equals(command)) {
        	
    		ShapeRoi tmpRoi = new ShapeRoi(new OvalRoi(1, 1, 9, 9));
    		tmpRoi.setImage(CurrentExperiment.getBlueImagePlus(1, null));
    		Analyzer.getResultsTable().reset();
    		// for the time being, I'm using default ResultTable and delete it (and measurements options)
    		Analyzer analyzer = new Analyzer(tmpRoi.getImage());
    		analyzer.measure();
    		ResultsTable rt = Analyzer.getResultsTable();
    		
        	String[] channels = new String [] {"Ignore", "Red", "Green", "Blue"};
        	final JComboBox[] choices = new JComboBox[rt.getLastColumn()+1];
        	final JLabel[] measuresLabels = new JLabel[rt.getLastColumn()+1];
        	final JTextField[] collarSizes = new JTextField[rt.getLastColumn()+1];
        	
        	Hashtable<String, ExperimentMeasurements> prevMeasures = CurrentExperiment.getExperimentMeasures();
        	
        	JPanel panel = new JPanel(new GridLayout(0,7));
    		for (int i = 0; i <= rt.getLastColumn(); ++i) {
    			choices[i] = new JComboBox(channels);
    			measuresLabels[i] = new JLabel(rt.getColumnHeading(i));
    			collarSizes[i] = new JTextField("0");
    			panel.add(measuresLabels[i]);
    			panel.add(choices[i]);
    			panel.add(collarSizes[i]);
    			if (prevMeasures.containsKey(rt.getColumnHeading(i).toLowerCase())) {
    				choices[i].setSelectedItem(prevMeasures.get(rt.getColumnHeading(i).toLowerCase()).getChannelName());
    				collarSizes[i].setText(prevMeasures.get(rt.getColumnHeading(i).toLowerCase()).getCollarSize() + "");
    			}
    			if (i % 2 == 0) panel.add(Box.createRigidArea(new Dimension(100, 1)));
    			
    		}	
    		
            
    		final JDialog dialog = new JDialog(null, "Select measures (measure/channel/collarSize)", Dialog.ModalityType.APPLICATION_MODAL);
    		
    		JButton measureAway = new JButton("Measure away!");
//    		measureAway.setActionCommand(MEASURE_AWAY_COMMAND);
    		measureAway.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                    Hashtable<String, ExperimentMeasurements> measures = new Hashtable<String, ExperimentMeasurements> ();
                    String toBeMeasured = "";
                    for (int i=0; i < choices.length; ++i) {
                    	if (!choices[i].getSelectedItem().equals("Ignore")) {
                    		measures.put(measuresLabels[i].getText(),
                    				new ExperimentMeasurements(
                    						Integer.parseInt(collarSizes[i].getText()),
                    						measuresLabels[i].getText(),
                    						(String) choices[i].getSelectedItem())                    				
                    				);
                    		toBeMeasured += "\n   " + measuresLabels[i].getText() + ":" + choices[i].getSelectedItem();
                    	}
                    }
                    ControlPanel.addStatusMessage("Measuring:" + toBeMeasured);
                    CurrentExperiment.updateExperimentMeasures(measures);
                    Runnable job = new Runnable () {
                    	public void run() {
                    		MovieProcessor.measure( getAltFormat ());
                            ControlPanel.addStatusMessage("Reloading tree (refreshing bacteria tooltips with current measurements)");
                            reloadTree();
                    	}
                    };
                    S.executeInCThread(job);
                    
                }
            });
    		
	        Container cp3 = dialog.getContentPane();
	        cp3.setLayout(new BorderLayout());
//	        cp3.add(new JLabel("Note that here you only define to what channel a measurement maps to." 
//	        		+ "\n In order for measure to be taken you also HAVE TO CHECK it via: ImageJ|Analyze|Set Measurements"), BorderLayout.NORTH);
	        cp3.add(panel, BorderLayout.CENTER);    	        
	        cp3.add(measureAway, BorderLayout.SOUTH);
	        
    	    dialog.setSize(700, (rt.getLastColumn()+1)*15);
    	    dialog.setLocationRelativeTo(null);
        	dialog.setVisible(true);     
        	
        } else if (SET_BG_GREEN_COMMAND.equals(command)){
        	final ImagePlus win  = WindowManager.getImage("Mean fluorescence of background readings (GREEN)");
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				
				final int startFrame = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
        	
	        	if (win == null) {
	        		String altF =  getAltFormat ();
	        		JOptionPane.showConfirmDialog(null, "ImageJ stack will appear in a few seconds (please wait) and then set the selection and press this button again (to measure and save to db).", "Info", JOptionPane.OK_OPTION);
					
	        		ImageStack stack = new ImageStack(
	            			CurrentExperiment.getBlueImagePlus(1,  altF).getProcessor().getWidth(), 
	            			CurrentExperiment.getBlueImagePlus(1,  altF).getProcessor().getHeight()
	            			);
	            	for (int f = startFrame; f <= CurrentExperiment.getFrameCount(); ++f) {
	            		stack.addSlice(CurrentExperiment.getGreenImagePlus(f, altF).getProcessor());
	            	}
	            	ImagePlus imp = new ImagePlus("Mean fluorescence of background readings (GREEN)", stack);        	
	        		imp.show();
	        	} else {
	        		
	        		Runnable job = new Runnable() {
	        			public void run() {
	        				ControlPanel.addStatusMessage("Measuring and saving to DB, please wait...");
	        				Macro_Runner mr = new Macro_Runner();						
	        				mr.runMacroFile(S.getMacroFullPath("MeasureStack.txt"), null);
	        				ResultsTable rt = ResultsTable.getResultsTable();
	        				int updated = 0;
	        				for (int f = startFrame; f <= CurrentExperiment.getFrameCount(); ++f) {					
	        					updated += CurrentExperiment.updateFrameBackgroundGreenMean(f, rt.getValue("Mean", f - startFrame)) ? 1 : 0;
	        					if ((f)%10 == 0) ControlPanel.addStatusMessage("  1 - " + (f+1) + " saved.");
	        				}
	        				CurrentExperiment.closeResultsWindow();
	        				WindowManager.removeWindow(win.getWindow());
	        				win.changes = false;
	        				win.close();
	        				ControlPanel.addStatusMessage(updated + " values updated.");	        					        				
	        				
	        				try {
	        					CurrentExperiment.reloadFrames();
	        					reloadTree();
	        				} catch(Exception exc) {
	        					log.error(exc);
	        				}        				
	        			}        			
	        		};
	        		S.executeInCThread(job);
	        		
	        	}
        	} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected (select starting frame).",
						"Info", JOptionPane.OK_OPTION);
			}
    		
        } else if (SET_BG_RED_COMMAND.equals(command)){  // TODO : join these two red/green
        	final ImagePlus win  = WindowManager.getImage("Mean fluorescence of background readings (RED)");
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				
				final int startFrame = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
				
	        	if (win == null) {
	        		String altF =  getAltFormat ();
					JOptionPane.showConfirmDialog(null, "ImageJ stack will appear in a few seconds (please wait) and then set the selection and press this button again (to measure and save to db).", "Info", JOptionPane.OK_OPTION);
	        		ImageStack stack = new ImageStack(
	            			CurrentExperiment.getRedImagePlus(1, altF).getProcessor().getWidth(), 
	            			CurrentExperiment.getRedImagePlus(1, altF).getProcessor().getHeight()
	            			);
	            	for (int f = startFrame; f <= CurrentExperiment.getFrameCount(); ++f) {
	            		stack.addSlice(CurrentExperiment.getRedImagePlus(f, altF).getProcessor());
	            	}
	            	ImagePlus imp = new ImagePlus("Mean fluorescence of background readings (RED)", stack);        	
	        		imp.show();
	        	} else {
	        		
	        		Runnable job = new Runnable() {
	        			public void run() {
	        				ControlPanel.addStatusMessage("Measuring and saving to DB, please wait...");
	        				Macro_Runner mr = new Macro_Runner();						
	        				mr.runMacroFile(S.getMacroFullPath("MeasureStack.txt"), null);
	        				ResultsTable rt = ResultsTable.getResultsTable();
	        				int updated = 0;
	        				for (int f = startFrame; f <= CurrentExperiment.getFrameCount(); ++f) {					
	        					updated += CurrentExperiment.updateFrameBackgroundRedMean(f, rt.getValue("Mean", f - startFrame)) ? 1 : 0;
	        					if ((f)%10 == 0) ControlPanel.addStatusMessage("  1 - " + (f+1) + " saved.");
	        				}
	        				CurrentExperiment.closeResultsWindow();
	        				WindowManager.removeWindow(win.getWindow());
	        				win.changes = false;
	        				win.close();
	        				ControlPanel.addStatusMessage(updated + " values updated.");	        					        				
	        				
	        				try {
	        					CurrentExperiment.reloadFrames();
	        					reloadTree();
	        				} catch(Exception exc) {
	        					log.error(exc);
	        				}        				
	        			}        			
	        		};
	        		S.executeInCThread(job);
	        		
	        	}
        	} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected (select starting frame).",
						"Info", JOptionPane.OK_OPTION);
			}
    		
        } else if (SET_BG_BLUE_COMMAND.equals(command)){  // Measure them all at once!
        	final ImagePlus win  = WindowManager.getImage("Mean fluorescence of background readings (R,G,B)");
        	
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				
				final int startFrame = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
				
	        	if (win == null) {
	        		String altF =  getAltFormat ();
					JOptionPane.showConfirmDialog(null, "ImageJ stack will appear in a few seconds (please wait) and then set the selection and press this button again (to measure and save to db).", "Info", JOptionPane.OK_OPTION);
	        		ImageStack stack = new ImageStack(
	            			CurrentExperiment.getBlueImagePlus(1, altF).getProcessor().getWidth(), 
	            			CurrentExperiment.getBlueImagePlus(1, altF).getProcessor().getHeight()
	            			);
	            	for (int f = startFrame; f <= CurrentExperiment.getFrameCount(); ++f) {
	            		stack.addSlice(CurrentExperiment.getRedImagePlus(f, altF).getProcessor());
	            		stack.addSlice(CurrentExperiment.getGreenImagePlus(f, altF).getProcessor());
	            		stack.addSlice(CurrentExperiment.getBlueImagePlus(f, altF).getProcessor());
	            	}
	            	ImagePlus imp = new ImagePlus("Mean fluorescence of background readings (R,G,B)", stack);        	
	        		imp.show();
	        	} else {
	        		
	        		Runnable job = new Runnable() {
	        			public void run() {
	        				ControlPanel.addStatusMessage("Measuring and saving to DB, please wait...");
	        				Macro_Runner mr = new Macro_Runner();						
	        				mr.runMacroFile(S.getMacroFullPath("MeasureStack.txt"), null);
	        				ResultsTable rt = ResultsTable.getResultsTable();
	        				int updated = 0;
	        				for (int f = startFrame, sliceNo = 0; f <= CurrentExperiment.getFrameCount(); ++f, sliceNo += 3) {					
	        					updated += CurrentExperiment.updateFrameBackgroundRGBMean(
	        							  f
	        							, rt.getValue("Mean", sliceNo)
	        							, rt.getValue("Mean", sliceNo + 1)
	        							, rt.getValue("Mean", sliceNo + 2)) ? 1 : 0;
	        					
	        					if ((f)%10 == 0) ControlPanel.addStatusMessage("  1 - " + (f+1) + " saved.");
	        				}
	        				CurrentExperiment.closeResultsWindow();
	        				WindowManager.removeWindow(win.getWindow());
	        				win.changes = false;
	        				win.close();
	        				ControlPanel.addStatusMessage(updated + " values updated.");	        					        				
	        				
	        				try {
	        					CurrentExperiment.reloadFrames();
	        					reloadTree();
	        				} catch(Exception exc) {
	        					log.error(exc);
	        				}        				
	        			}        			
	        		};
	        		S.executeInCThread(job);
	        		
	        	}
        	} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected (select starting frame).",
						"Info", JOptionPane.OK_OPTION);
			}
    		
        } else if (EDIT_EVENT_COMMAND.equals(command)){
        	DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				
				
				EventDialog ed = new EventDialog(this);
	        	ed.showDialog();
        			
	        	
        	} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected (select frame to add event to).",
						"Info", JOptionPane.OK_OPTION);
			}
        }  else if (SET_ALG_COMMAND.equals(command)){
        	DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				
				
				PickAlgorithmDialog pad = new PickAlgorithmDialog(this);
	        	pad.showDialog();
        			
	        	
        	} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected (select frame to add event to).",
						"Info", JOptionPane.OK_OPTION);
			}
        } else if (CHANGE_STATE_COMMAND.equals(command)){
        	DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof BacteriaNode) {
				
				
				ChangeStateDialog csd = new ChangeStateDialog(this);
	        	csd.showDialog();
        			
	        	
        	} else {
				JOptionPane.showConfirmDialog(null, "Bacteria not selected.",
						"Info", JOptionPane.OK_OPTION);
			}
			 
        } else if (IGNORE_FRAME_COMMAND.equals(command)){
        	DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			if (selectedNode.getUserObject() instanceof FrameNode) {
				
				if (JOptionPane.YES_OPTION  == JOptionPane.showConfirmDialog(null, "Are you sure (when a frame is ignored, any existing measurements for that frame are deleted) ", "?", JOptionPane.YES_NO_OPTION )) {
					
					final int frameNo = ((FrameNode) ((DefaultMutableTreeNode) selectedNode).getUserObject()).getFrameNo();
					
	        		Runnable job = new Runnable() {
	        			public void run() {
	        				
	        				try {
	        					CurrentExperiment.toggleIgnoreFrame(frameNo);
	        					ControlPanel.addStatusMessage("Frame ignore flag toggled, reloading tree (any measurements of the ignored frame are DELETED).");
	        					CurrentExperiment.reloadFrames();
	        					reloadTree();
	        				} catch(Exception exc) {
	        					log.error(exc);
	        				}        				
	        			}        			
	        		};
	        		S.executeInCThread(job);				
					
				}	
	        	
        	} else {
				JOptionPane.showConfirmDialog(null, "Frame not selected.",
						"Info", JOptionPane.OK_OPTION);
			}
			// IGNORE_FRAME_COMMAND
        }
        
    
        
    }    
	private void setHumanRoi(DefaultMutableTreeNode selectedNode) {
		ImagePlus ip = WindowManager.getCurrentImage();
		if (ip != null) {
			Roi roi = ip.getRoi();
			if (roi != null) {	
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
				addHumanRoiIfNotExisting(parent);
				((BacteriaNode)parent.getUserObject()).setHumanROI(roi);
				((DefaultTreeModel) tree.getModel()).nodeChanged(parent);
				((DefaultTreeModel) tree.getModel()).nodeChanged(selectedNode);
			} else {
				JOptionPane
						.showConfirmDialog(
								null,
								"Cannot set ROI, ROI not set (for current window).",
								"Info", JOptionPane.OK_OPTION);
			}
		} else {
			JOptionPane.showConfirmDialog(null,
					"Cannot set ROI, no active window.", "Info",
					JOptionPane.OK_OPTION);
		}		
		
	}
    private Color getColorForBacteria (String bName) {
    	final Color[] baseColors = new Color [] {Color.RED, Color.GREEN, Color.BLUE};
    	Color c = baseColors[  (bName.charAt(0) - 'A') % baseColors.length];
    	//log.debug("init color " + c.getRed() + "," + c.getGreen() + ", " + c.getBlue() );
    	float[] rgb = c.getRGBColorComponents(null);
    	//log.debug("rgb color " + rgb[0] + "," + rgb[1] + ", " + rgb[2] );
    	for (int i=0; i < 3; ++i) {    		
    		if (rgb[i] < 1.0) {
    			rgb[i] = 50.f/255 * (bName.length() - 1);
    			c = new Color( 
    					((int) (255 * rgb[0])) % 256, 
    					((int) (255 * rgb[1])) % 256, 
    					((int) (255 * rgb[2])) % 256
    					);
    		}
    	}
    	//log.debug("returning color " + c.getRed() + "," + c.getGreen() + ", " + c.getBlue() );
    	return c;
    	
    }
    private String getAltFormat () {
    	
    	// Natively (i.e. without the need of third-party plugins) ImageJ opens the following formats: 
    	//                         TIFF[?], GIF[?], JPEG[?], PNG[?], DICOM[?], BMP[?], PGM[?] and FITS[?]. 
    	String[] formats = new String[] {"PNG", "TIF (Original)", "GIF", "JPG", "DICOM", "BMP", "PGM", "FITS", "Other"};
		String selected = (String) JOptionPane.showInputDialog(
                null,
                "Choose the picture format to measure on: ",
                "Alternative format",
                JOptionPane.PLAIN_MESSAGE,
                null,
                formats,
                "");
		if (selected.equals(formats[0])) return null;
		else if (selected.equals("Other")){			
			return (String) JOptionPane.showInputDialog(
	                null,
	                "Please, type in the custom image file extension (e.g.: jpg) ",
	                "Alternative format",
	                JOptionPane.PLAIN_MESSAGE);
		}
		else {
			if (selected.equals(formats[1])) return "tif";
			else return selected.toLowerCase();
		}
    	
    }
    
    public DefaultMutableTreeNode getSelectedNode() {
    	return (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
    }
    
    // If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public void expandAll(JTree tree, boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}    
    
    @SuppressWarnings("unchecked")
	private void addHumanRoiIfNotExisting (DefaultMutableTreeNode b) {
    	for (DefaultMutableTreeNode child: Collections.list( (Enumeration<DefaultMutableTreeNode>) b.children())) {
    		if (child.getUserObject() instanceof HumanRoiNode) {
    			return;
    		}
    	}
		
		
		RoiNode rn = ((BacteriaNode)b.getUserObject()).addOrReplaceHumanRoi();
		addObject(b, rn, true);
				
    }
    
    public void reloadTree() {
    	Runnable job = new Runnable () {
			@Override
			public void run() {
				TreePath old = tree.getSelectionPath();
		    	//clear();
		    	initNodes();
		    	try {
		    		tree.setSelectionPath(old);
		    	} catch (Exception e) { 		
		    	}			
			}    		
    	};
    	
    	S.executeInEDTThread(job);
    	
    }
    
    private void initNodes() {
		try {
			boolean loadMeasurements = true;
			boolean isEmpty = true;
			LinkedList<DefaultMutableTreeNode> bNodes = new LinkedList<DefaultMutableTreeNode>();
			
			ControlPanel.addStatusMessage("Loading " + CurrentExperiment.getFrameCount()  + " frames for exp.id = " + CurrentExperiment.getIdExperiment());
			
			clear();
			
			for (int frameNo = 1; frameNo <= CurrentExperiment.getFrameCount(); ++frameNo) {
				DefaultMutableTreeNode frameNode = addObject(rootNode,	new FrameNode(CurrentExperiment.getFrame(frameNo)));
				if (loadMeasurements) {
					LinkedList<Bacteria> bacterias = CurrentExperiment.getBacteriasForFrame(frameNo);
					loadMeasurements = (bacterias.size() > 0) ? true : false;
					if (loadMeasurements) {
						BacteriaNode bNode = null;						
						for (Bacteria b : bacterias) {							
							
							bNode = new BacteriaNode(
									(FrameNode) frameNode.getUserObject(),
									b);	
							
							
							DefaultMutableTreeNode bactNode = addObject(frameNode, bNode,	true);
							bNodes.addLast(bactNode);
							
							Roi roi = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), frameNo);
							if (roi != null) {
								RoiNode rn = bNode.addOrReplaceComputerRoi(/*roi*/);
								rn.setMeasurements(CurrentExperiment.getMeasurements(b.getIdBacteria(), frameNo, bactimas.db.beans.Roi.ROI_TYPE_COMPUTER));
								addObject(bactNode, rn,	true);
							}
							
							roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), frameNo);
							if (roi != null) {
								RoiNode rn = bNode.addOrReplaceHumanRoi(/*roi*/);
								rn.setMeasurements(CurrentExperiment.getMeasurements(b.getIdBacteria(), frameNo, bactimas.db.beans.Roi.ROI_TYPE_HUMAN));
								addObject(bactNode, rn,	true);
							}							
							
							
						}												
						isEmpty = false;
					}					
				}
				// otherwise, ignored frame will stop further loading:
				if (CurrentExperiment.getFrame(frameNo).isIgnored()) loadMeasurements = true;
			}
			ExperimentEvent[] events = CurrentExperiment.getAllEvents();			
			for (int i = 0; events !=null && i < events.length; ++i) {
				DefaultMutableTreeNode f = getFrameNode(events[i].getFrameNo()); 
				((FrameNode)f.getUserObject()).setEvent(events[i]);
				//((DefaultTreeModel) tree.getModel()).nodeChanged(f);
			}
			BacteriaStateChange[] items = CurrentExperiment.getAllBacteriaStateChanges();			
			for (int i = 0; items != null && i < items.length; ++i) {
				for (DefaultMutableTreeNode bn : bNodes) {
					if (((BacteriaNode)bn.getUserObject()).getBacteria().getIdBacteria() == items[i].getIdBacteria() 
						&& ((BacteriaNode)bn.getUserObject()).getParent().getFrameNo() >= items[i].getFrameNo()	
							) {
						((BacteriaNode)bn.getUserObject()).setState(items[i].getStateName());
						//((DefaultTreeModel) tree.getModel()).nodeChanged(bn);
					}
				}				
			}		

			((DefaultTreeModel) tree.getModel()).reload();
			tree.expandRow(0);
			
			if (isEmpty) {
				tree.setSelectionRow(1);
				addBacteria();
				tree.setSelectionRow(2);
			} else {				
				tree.setSelectionRow(1);				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

    }
    @SuppressWarnings("unchecked")
	private DefaultMutableTreeNode getFrameNode(int frameNo) {
    	for (DefaultMutableTreeNode n: Collections.list( (Enumeration<DefaultMutableTreeNode>) rootNode.children())) {
			FrameNode curr = (FrameNode) n.getUserObject();
			if (curr.getFrameNo() == frameNo) {				
				return n;				
			}				
		}     	
    	return null;
    }
    /*
    private void addBacteria(DefaultMutableTreeNode frameParent, BacteriaNode b) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();
        parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
        if (parentNode == null) return;
        if (parentNode.getUserObject() instanceof BacteriaNode) {
        	parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        } else if (!(parentNode.getUserObject() instanceof FrameNode)) {
        	JOptionPane.showConfirmDialog(null, "You must select a frame node or bacteria node.", "Info", JOptionPane.INFORMATION_MESSAGE );        	
        	return ;
        }
        BacteriaNode bn = new BacteriaNode(
        		(FrameNode) parentNode.getUserObject(),
        		CurrentExperiment.addBacteria(((FrameNode) parentNode.getUserObject()).getFrameNo())
        		);
        addObject(parentNode, bn, true);
    }    
    
*/
    //private static int bIndex = 0;
    /** Add child to the currently selected node. */
    private void addBacteria() {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();
        parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
        if (parentNode == null) return;
        if (parentNode.getUserObject() instanceof BacteriaNode) {
        	parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        } else if (!(parentNode.getUserObject() instanceof FrameNode)) {
        	JOptionPane.showConfirmDialog(null, "You must select a frame node or bacteria node.", "Info", JOptionPane.INFORMATION_MESSAGE );        	
        	return ;
        }
        BacteriaNode bn = new BacteriaNode(
        		(FrameNode) parentNode.getUserObject(),
        		CurrentExperiment.addBacteria(((FrameNode) parentNode.getUserObject()).getFrameNo())
        		);

        addObject(parentNode, bn, true);
    }
    
    
    /** Remove all nodes except the root node. */
    public void clear() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    /** Remove the currently selected node. */
    public void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        } 

        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }
    


    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, 
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }
	

        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent, 
                                 parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */

                int index = e.getChildIndices()[0];
                node = (DefaultMutableTreeNode)(node.getChildAt(index));
            
        }
        public void treeNodesInserted(TreeModelEvent e) {
        }
        public void treeNodesRemoved(TreeModelEvent e) {
        }
        public void treeStructureChanged(TreeModelEvent e) {
        }
    }



	@SuppressWarnings("unchecked")
	@Override
	public void frameSelected(Object source, int frameNo, List<Roi> rois) {
		if (source != this) {
			for (DefaultMutableTreeNode n: Collections.list( (Enumeration<DefaultMutableTreeNode>) rootNode.children())) {
				FrameNode curr = (FrameNode) n.getUserObject();
				if (curr.getFrameNo() == frameNo) {				
					tree.setSelectionPath(new TreePath(n.getPath()));
					break;
				}				
			} 
		}
	}
}
