package bactimas.algorithms;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.Macro_Runner;
import ij.plugin.frame.BridgeToRoiManager;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import bactimas.alg.ForcedPoint;
import bactimas.alg.ITrackingAlgorithm;
import bactimas.alg.MovieProcessor;
import bactimas.alg.StelsRepository;
import bactimas.datamodel.CurrentExperiment;
import bactimas.db.beans.Bacteria;
import bactimas.gui.ControlPanel;
import bactimas.gui.ImageStrip.ImageStripType;
import bactimas.util.S;
import signalprocesser.voronoi.VPoint;

public class CopyAndAdjustAlgorithm implements ITrackingAlgorithm {
	
	private static Logger log = Logger.getLogger("bactimas.algorithms.CopyAndAdjustAlgorithm" );	
	
	protected Hashtable<String, Roi> bacteriaRoisBag;
	protected int[][] selectionsMask;
	
	protected ImagePlus imp;
	protected ByteProcessor sobel;
	
	int ballRadius;
	int range;
	ImageStripType channel;
	String thMethod;
	int MAX_BALL_DELTA_PERC = 25;
	
	private boolean initalized = false;
	
	
	
	
	protected void registerBacteriaRoi(Bacteria b, Roi roi, int frameNo) {
		boolean warnOnce = false;
		bacteriaRoisBag.put(b.getBactName(),  roi);
		log.debug("Roi for " + b.getBactName() + " registered. Current roi bag size = " + bacteriaRoisBag.size() 
				+ " Location is:" + roi.getBounds().x + ", " + roi.getBounds().y);
		ImageProcessor mask = roi.getMask();
		Rectangle r = roi.getBounds();
		for (int x = 0; x < mask.getWidth(); ++x) {
			for (int y = 0; y < mask.getHeight(); ++y) {
				if (mask.getPixel(x, y) > 0) {
					try {
						if (selectionsMask[x + r.x][y + r.y] != 0 && selectionsMask[x + r.x][y + r.y] != b.getIdBacteria()) {
							log.debug("***************** OVERLAP ERROR (frameNo = " + frameNo + "): " + b.getBactName() + " vs " + selectionsMask[x + r.x][y + r.y]
									+ " pixel = " + (x + r.x) + ", " +  (y + r.y)
								 );
							if (imp!= null) imp.getProcessor().putPixel(x + r.x, y + r.y, MovieProcessor.colToValue(Color.red));
						}
						selectionsMask[x + r.x][y + r.y] = b.getIdBacteria();
					} catch (ArrayIndexOutOfBoundsException e) {
						if (!warnOnce) {
							warnOnce = true;
							String state = CurrentExperiment.getBacteriaStateAt(b, frameNo);
							if (state == null || state.toLowerCase().indexOf("ignore") == -1 ){
								ControlPanel.addStatusMessage("Error registering bacteria " + b.getBactName() + ". Is it leaving the window? You should ignore it.");
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Copy And Adjust";
	}
	@Override
	public String getClassName() {
		return "bactimas.algorithms.CopyAndAdjustAlgorithm";
	}	

	
	@Override
	public String getAbbrev() {
		return "C&A";
	}	
	
	@Override
	public void step(int frameNo, VPoint translation) {


		
		bacteriaRoisBag = new Hashtable<String, Roi>();

		

		
		ControlPanel.addStatusMessage("Processing frame:" + frameNo 
				+ " using: BallRadius:" + ballRadius + ", Range: " + range + ", on Channel: " + channel + ", Threshold method:" + thMethod);
		
		
		BridgeToRoiManager.reset();
		
		imp = CurrentExperiment.getImagePlus(channel, frameNo, "png");
		
		if (imp == null) {
			ControlPanel.addStatusMessage("Error: CopyAndAdjustAlgorithm cannot open image:" + CurrentExperiment.getFrameAbsFilename(channel, frameNo, "png"));
			return;
		}
		
		imp.setTitle("PROCESS F " + frameNo + " " + imp.getTitle());
		
		imp.show();
		
		WindowManager.setCurrentWindow(imp.getWindow());
		
		selectionsMask = new int[imp.getProcessor().getWidth()][imp.getProcessor().getHeight()];
		
		sobel = new ByteProcessor(imp.getProcessor(), true);				
		sobel.filter(ImageProcessor.FIND_EDGES);
		
		
		
		LinkedList<Bacteria> bacterias;
		
		bacterias = CurrentExperiment.getBacteriasForFrame(CurrentExperiment.getPrevFrameNo(frameNo));
		
		LinkedList<Bacteria> splitBacterias = CurrentExperiment.getSplitBacteriasForFrame(frameNo);
		for (Bacteria sb: splitBacterias) {
			bacterias.removeFirstOccurrence(sb);
			log.debug("Skipping bacteria " + sb + " bcs it has a split defined at frame:_" + frameNo);
		}
		
		
		log.debug( "****************************************************************************************************************************");
		log.debug( "************************************  Register rois frame = " + frameNo + "********************************************************");
		log.debug( "****************************************************************************************************************************");
		
		ControlPanel.addStatusMessage("   " + bacterias.size() + " found.");
		
		for (Bacteria b: bacterias) {
			Roi roi;
			if (frameNo == 1) {	 
				roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), 1);			
			} else { // ako ima human, onda human
				roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), CurrentExperiment.getPrevFrameNo(frameNo));
				if (roi == null) {
					roi = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), CurrentExperiment.getPrevFrameNo(frameNo));					
				}
			}
			
			imp.setRoi(roi);
			
			
			
			if (!roi.isArea()) {
				log.debug("Converting roi to area roi.");
				Macro_Runner mr = new Macro_Runner();
				//URL macroFullPath = ClassLoader.getSystemClassLoader().getResource("/macros/ConvertLineSelectionToAreaAndVV.txt");				
				mr.runMacroFile(S.getMacroFullPath("ConvertLineSelectionToAreaAndVV.txt"), null);
				roi = imp.getRoi();
				log.debug("Roi type is now:" + roi.getTypeAsString());
			}
			

			Rectangle r123 = roi.getBounds(); 
			roi.setLocation(r123.x + translation.x, r123.y + translation.y);

			WindowManager.setCurrentWindow(imp.getWindow());
			imp.setRoi(roi);			
			
			registerBacteriaRoi(b, roi, frameNo);

		}

		
		WindowManager.setCurrentWindow(imp.getWindow());
		
		//Macro_Runner m2r = new Macro_Runner();
		
		ImagePlus impSobel = new ImagePlus("Sobel", sobel);		
		

		int th;
		if (thMethod == null) {
			impSobel.setRoi(imp.getRoi());   // MUST BE HERE, OTHERWISE THRESHOLD BECOMES MUCH SMALLER AND EVERYTHING FAILS!
			// note that this is different from user choosing "default" (below - else branch) bcs here different histogram is used (taken from roi)
			th = sobel.getAutoThreshold();
			IJ.setAutoThreshold(impSobel, "Default dark");
			IJ.run(impSobel, "Convert to Mask", "");
		} else {
			th = (new AutoThresholder()).getThreshold(thMethod, sobel.getHistogram()); 
			IJ.setThreshold(impSobel, th, 255);
		    IJ.run(impSobel, "Convert to Mask", "");			
		}
		
		
	    
	    MovieProcessor.removeSingleDots(impSobel.getProcessor());
	    MovieProcessor.skeletonize((ByteProcessor)impSobel.getProcessor());  // to get rid of "fat" bacteria lines
		
	    if (ControlPanel.debug) {		
		    impSobel.setTitle(impSobel.getTitle() + " th = " + th );
	    	impSobel.show();	
		}	    

	    
	    sobel = (ByteProcessor) impSobel.getProcessor();
	    
	    LinkedList<Roi> newRois2 = new LinkedList<Roi>(); 
		for (Bacteria b: bacterias) {
			
			log.debug( "****************************************************************************************************************************");
			log.debug( "************************************  Processing bacteria  " + b +        "*************************************************");
			log.debug( "****************************************************************************************************************************");
			
			
			imp.setRoi(bacteriaRoisBag.get(b.getBactName()));		

			
			Roi r = hitOrMissSobel(bacteriaRoisBag.get(b.getBactName()), b, frameNo, ballRadius);
			registerBacteriaRoi(b, r, frameNo);  // update roi and selected grid to avoid overlaps
												 // TODO:  
												 // I am not releasing the unused pixels!? 
												 // Would that help? Maybe not so much, they'll be grabbed next time (in the next frame) 
												 // This makes for a conservative rutf war stategy, still might be worth a shot to let go of pixels more easily
			
			
			newRois2.add(r);

			
			CurrentExperiment.saveROI(r, frameNo, b, bactimas.db.beans.Roi.ROI_TYPE_COMPUTER);
			
		}		
		WindowManager.setCurrentWindow(imp.getWindow());
		BridgeToRoiManager.addRoisToAll(newRois2);
	

		if (!ControlPanel.debug) {		
			imp.close();
		}		
		
	}
	
	

	@Override
	public void beforeStep(final int frameNo) {
		if (!initalized) beforeBatch(frameNo);
	}
	
	
	@Override
	public void beforeBatch(final int firstFrameNo) {
		Runnable job = new Runnable() {
			@Override
			public void run() {
				beforeBatchNotSafe(firstFrameNo);							
			}							
		};
		S.executeInEDTAndWait(job);		
		
	}
	
	
	public void beforeBatchNotSafe(final int firstFrameNo) {
		
		final JTextField txtBallRadius = new JTextField("4");		
		final JTextField txtRange = new JTextField("20");
		final JTextField txtMaxBallDeltraPerc = new JTextField("" + MAX_BALL_DELTA_PERC);
		final JComboBox<String> cboThMethod = new JComboBox<String>( S.concat(new String[] {"don't know"}, AutoThresholder.getMethods()));
		
		
		JRadioButton redButton = new JRadioButton("Red");        	    
	    redButton.setActionCommand("Red");
	    JRadioButton greenButton = new JRadioButton("Green");        	    
	    greenButton.setActionCommand("Green");
	    JRadioButton blueButton = new JRadioButton("Blue");        	    
	    blueButton.setActionCommand("Blue");
	    blueButton.setSelected(true);
	    
	    //Group the radio buttons.
	    final ButtonGroup channelsGroup = new ButtonGroup();
	    channelsGroup.add(redButton);
	    channelsGroup.add(greenButton);
	    channelsGroup.add(blueButton);
		
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.add(new JLabel("Ball radius"));
		panel.add(txtBallRadius);
		panel.add(new JLabel("Channel"));
		JPanel radioPanel = new JPanel(new FlowLayout());
		radioPanel.add(redButton);
		radioPanel.add(greenButton);
		radioPanel.add(blueButton);
		panel.add(radioPanel);
		panel.add(new JLabel("Range"));
		panel.add(txtRange);    		
		panel.add(new JLabel("Max ball delta perc"));
		panel.add(txtMaxBallDeltraPerc); 
		panel.add(new JLabel("Edge det. & Threshold method"));
		
		JButton btnHelpDecide = new JButton("Help decide");
		
		btnHelpDecide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	ImagePlus imp = null; 
            	String channel = S.getSelectedButtonText(channelsGroup);
        		if (channel.equals("Red")) { 
        			imp = CurrentExperiment.getRedImagePlus(firstFrameNo, "png");
        		} else if (channel.equals("Green")){
        			imp = CurrentExperiment.getGreenImagePlus(firstFrameNo, "png");
        		} else if (channel.equals("Blue")) {
        			imp = CurrentExperiment.getBlueImagePlus(firstFrameNo, "png");
        		}
        		
        		ImageStack stckMethods = new ImageStack(imp.getWidth(), imp.getHeight()); 			
        		for (int i = 0; i < AutoThresholder.getMethods().length; ++i) {
        			ByteProcessor sobel = new ByteProcessor(imp.getProcessor(), true);				
        			sobel.filter(ImageProcessor.FIND_EDGES);
        			ImagePlus impSobel = new ImagePlus("Channel:" + channel + " Method:" + AutoThresholder.getMethods()[i], sobel);		
        			int th = (new AutoThresholder()).getThreshold(AutoThresholder.getMethods()[i], sobel.getHistogram()); 
        			IJ.setThreshold(impSobel, th, 255);
        		    IJ.run(impSobel, "Convert to Mask", "");
        		    
        		    MovieProcessor.removeSingleDots(impSobel.getProcessor());
        		    MovieProcessor.skeletonize((ByteProcessor)impSobel.getProcessor());  
        		    stckMethods.addSlice("Channel:" + channel + " Method:" + AutoThresholder.getMethods()[i], impSobel.getProcessor());
            	}
        		(new ImagePlus("Find the method where cell borders are best outlined ", stckMethods)).show();                    	
            	
            }
		});
		JPanel jpMethod  = new JPanel(new FlowLayout());
		jpMethod.add(cboThMethod);
		jpMethod.add(btnHelpDecide);
		panel.add(jpMethod);  
        
        panel.setSize(500, 200);
        
        
        
//        JOptionPane pane = new JOptionPane(panel);
//        // Configure via set methods
//        JDialog dialog = pane.createDialog(null, "Set processing parameters : ");
//        // the line below is added to the example from the docs
//        dialog.setModal(false); // this says not to block background components
//        dialog.setVisible(true);
        
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,
                panel,
                "Set processing parameters : ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE)) {
        	
        	ballRadius = S.parseInt(txtBallRadius.getText(), "Ball radius", 4);
        	range    = S.parseInt(txtRange.getText(), "Range", 10);
        	String ch = S.getSelectedButtonText(channelsGroup);
        	channel  = (ch == "Blue") ? ImageStripType.BLUE : (ch == "Red") ? ImageStripType.RED : ImageStripType.GREEN;        	
        	thMethod = cboThMethod.getSelectedItem().toString().equals("don't know") ? null: cboThMethod.getSelectedItem().toString();        	
        	MAX_BALL_DELTA_PERC = S.parseInt(txtMaxBallDeltraPerc.getText(), "Max ball delta percentage", MAX_BALL_DELTA_PERC);        	        
        	
        	initalized = true;
        }                
        
        
        
		
	}
	
	
	@SuppressWarnings("unchecked")
	protected Roi hitOrMissSobel(Roi prevRoi, Bacteria b, int frameNo, int ballRadius) {
		boolean debugThisBacteria = false;
		if (ControlPanel.debug &&  JOptionPane.showConfirmDialog(
			    null,
			    "Do you want to debug bacteria " + b.getBactName(),
			    "Debug option",
			    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			debugThisBacteria = true;
		}	
		
		ImageProcessor mask = prevRoi.getMask();
		
		Rectangle offset = prevRoi.getBounds();
		
		log.debug("Offset for bacteria " + b.getBactName() + " at frame " + frameNo + " is:" + offset.x + ", " + offset.y);
		
		ArrayList<VPoint> borderPixels = MovieProcessor.extractBorderPixels(prevRoi);
		ImageProcessor mold = new ByteProcessor(mask.getWidth(), mask.getHeight());

		
		
		for (VPoint p: borderPixels) {
			mold.putPixel(p.x - offset.x, p.y - offset.y, 255);
		}
		
		ImageProcessor moldSkeleton = MovieProcessor.skeletonize(prevRoi);
		for (int x = 0; x < moldSkeleton.getWidth(); ++x) {				
			for (int y = 0; y < moldSkeleton.getHeight(); ++y) {					
				if (moldSkeleton.getPixel(x, y) > 0) { 
					mold.putPixel(x , y, 255);
				}
			}
		}
		
		
		int origArea = MovieProcessor.getArea(mask);
		
		ArrayList<ForcedPoint> endpointsToForceToSkeleton = new ArrayList<ForcedPoint>();
		ArrayList<ForcedPoint> endpointsToForceToBody = new ArrayList<ForcedPoint>();
		
		VPoint[] endpoints = MovieProcessor.getSkeletonEndpoints((ByteProcessor)moldSkeleton);
		
		ImageStack stack = new ImageStack(mask.getWidth(), mask.getHeight());
			
		ByteProcessor slice = new ByteProcessor(mold, false);
		
		
		
		ByteProcessor sobelSlice = new ByteProcessor(slice.getWidth(), slice.getHeight());
		for (int x = 0; x < sobelSlice.getWidth(); ++x) {
			for (int y = 0; y < sobelSlice.getWidth(); ++y) {
				if (sobel.getPixel(offset.x + x, offset.y + y) > 0) {
					sobelSlice.putPixel(x, y, 250);
				}
			}
		}
		
		
		VPoint currEndpoint;
		for (int i=0; i < endpoints.length; ++i) {
			int endpSum =  getBallHitOrMissValue (endpoints[i].x, endpoints[i].y, offset, b, StelsRepository.getStelForRadius(ballRadius), frameNo);
			log.debug("endpoint.getBallHitOrMissValue(" + endpoints[i] + ") = " + endpSum);
			currEndpoint = endpoints[i];
			// Find the point on the skeleton next to the endpoint, call it e1: 
			VPoint e1 = null;
			for (int n=0; n < MovieProcessor.n8.length; ++n) {
				VPoint n1 = new VPoint(currEndpoint.x + MovieProcessor.n8[n].x ,currEndpoint.y + MovieProcessor.n8[n].y);
				if (   moldSkeleton.getPixel(n1.x, n1.y) > 0 ) {
					e1 = new VPoint(n1.x, n1.y);
					break;
				}
			}
			
			int x1 = 0, y1 = 0;
			slice = new ByteProcessor(slice, false);
			
			int RANGE = 20;
			VPoint maxN = null;
			int maxSum = Integer.MIN_VALUE;
			for (x1 = currEndpoint.x - RANGE; x1 <= currEndpoint.x + RANGE; x1++) {
				for (y1 = currEndpoint.y - RANGE; y1 <= currEndpoint.y + RANGE; y1++) {
					
					int manDist =  Math.abs(currEndpoint.x  - x1) + Math.abs(currEndpoint.y  - y1);
					int manDistE =  Math.abs(e1.x  - x1) + Math.abs(e1.y  - y1);
					int ballDist = (int)(.5 + Math.sqrt( Math.pow(currEndpoint.x  - x1, 2) +  Math.pow(currEndpoint.y  - y1, 2)));
					
					if (	manDist <= manDistE 
						&& ballDist < RANGE //2 * ballRadius
						&& ((x1 >= 0) && (y1 >=0))  // careful not to get out of the frame (picture)
							) {
						
						slice.putPixel(x1, y1, 128);
						sobelSlice.putPixel(x1, y1, 128);
						int val = getBallHitOrMissValue (x1, y1, offset, b, StelsRepository.getStelForRadius(ballRadius), frameNo);
						if (val > maxSum) {
							maxSum = val;
							maxN = new VPoint(x1, y1);
						}
						if (val > 0) {
							slice.putPixel(x1, y1, 200);
							sobelSlice.putPixel(x1, y1, 200);
							endpointsToForceToSkeleton.add(new ForcedPoint(x1, y1, currEndpoint));
							log.debug("(" + x1 + "," + y1 + ").getBallHitOrMissValue = " + val + " ballDist=" + ballDist + " FORCE SKELETON!");
						} else {
							log.debug("(" + x1 + "," + y1 + ").getBallHitOrMissValue = " + val + " ballDist=" + ballDist);	
						}
						
					}					
				}
			}
			if (maxN != null && maxSum > 0) {
				slice.putPixel(maxN.x, maxN.y, 255);	
				sobelSlice.putPixel(maxN.x, maxN.y, 255);
			}
			stack.addSlice("maxN: " + maxN + ".sum = " + maxSum + " endp.sum=" + endpSum, slice);
		}
		stack.addSlice("Sobel ", sobelSlice);
		
		
		
		if (ControlPanel.debug && debugThisBacteria) {
			stack.addSlice("dummy", new ByteProcessor(mold, false));
			ImagePlus imp = new ImagePlus("+Endpoints for " + b.getBactName(), stack);
			imp.show();		
		}
		

		
		
		
							
		ByteProcessor maxSkeleton = new ByteProcessor(moldSkeleton.getWidth(), moldSkeleton.getHeight());
		
		for (int x = 0; x < moldSkeleton.getWidth(); ++x) {				
			for (int y = 0; y < moldSkeleton.getHeight(); ++y) {					
				if (moldSkeleton.getPixel(x, y) > 0) { 
					moldSkeleton.putPixel(x, y, 255);
					int maxSum = Integer.MIN_VALUE;
					VPoint maxN = null;
					int currSum = getBallHitOrMissValue (x, y, offset, b, StelsRepository.getStelForRadius(ballRadius), frameNo);
					
					for (int n=0; n < MovieProcessor.n24.length; ++n) {
						VPoint n1 = new VPoint(x + MovieProcessor.n24[n].x ,y + MovieProcessor.n24[n].y);
						if (  maxSkeleton.getPixel(n1.x, n1.y) == 0
							&& moldSkeleton.getPixel(n1.x, n1.y) == 0
								) {							
							int val = getBallHitOrMissValue (n1.x, n1.y, offset, b, StelsRepository.getStelForRadius(ballRadius), frameNo);
							if (val > maxSum) {
								maxSum = val;
								maxN = n1;
							}
							
							if (val >= 0) {
								endpointsToForceToBody.add(new ForcedPoint(n1.x, n1.y, null));
								maxSkeleton.putPixel(n1.x, n1.y, 128);  // ERR: it was x, y
								log.debug(b.getBactName() + ".n1.skeleton(" + n1 + ").getBallHitOrMissValue = " + val + " FORCED");
							} else {
								log.debug(b.getBactName() + ".n1.skeleton(" + n1 + ").getBallHitOrMissValue = " + val);
							}
						}																						
					}	
					if (currSum >= 0) {
						maxSkeleton.putPixel(x, y, 200);
					}
					if (maxSum > currSum) {
						log.debug("Adjusting skeleton from " + currSum + " to " + maxSum);
						maxSkeleton.putPixel(maxN.x, maxN.y, 255);
					} else {
						maxSkeleton.putPixel(x, y, 200);
					}
				}
			}
		}
		ImageStack skeletonStack = new ImageStack(maxSkeleton.getWidth(), maxSkeleton.getHeight());
		skeletonStack.addSlice("Max skeleton", maxSkeleton);
		
		Roi tmp = MovieProcessor.createSelectionFromMask(maxSkeleton, new VPoint(0, 0));
		
		if (tmp == null) {
			ControlPanel.addStatusMessage("Warning: bacteria " + b.getBactName() + " at frame " 
						+ frameNo + " is 'lost', could not reckognize it, reverting to previous selection (roi). Please review and fix by hand");
			return prevRoi;
		}
		
		ByteProcessor sel = new ByteProcessor(maxSkeleton.getWidth(), maxSkeleton.getHeight());
		MovieProcessor.applySelectionToImageProcessor(tmp, sel, 255);

		skeletonStack.addSlice("Connected max skeleton", sel);
//		
//		ByteProcessor sel2 = new ByteProcessor(sel,false);
		
//		ImageProcessor adjustedSkeleton = BacteriaProcessor.skeletonize(sel2);
//		skeletonStack.addSlice("Adjusted max skeleton", adjustedSkeleton);
//		
//		ImageProcessor adjustedSkeleton2 =  new ByteProcessor(adjustedSkeleton, false);
//		for (VPoint fp : endpointsToForceToSkeleton) {
//			adjustedSkeleton2.putPixel(fp.x, fp.y, 150);
//			log.debug("Adding forced endpoint " + fp + " to adjusted max skeleton2.");
//		}
//		skeletonStack.addSlice("Adjusted/forced max skeleton", adjustedSkeleton2);
		
		
		if (ControlPanel.debug && debugThisBacteria) {
			ImagePlus imp2 = new ImagePlus("+Adjust skeleton for " + b.getBactName(), skeletonStack);
			imp2.show();	
		}
		
//		ImagePlus imp3 = new ImagePlus("+Mask for " + b.getBactName(), tmp.getMask());
//		imp3.show();	
		
		
		ShapeRoi roiFromBalls = null;
		log.debug("Constructing " + b.getBactName() + " PHASE 1: building from max skeleton (without forced points):");
		roiFromBalls = getRoiFromBalls(sel, ballRadius, offset, b, ballRadius, frameNo);
		
		ImageProcessor ph1 = new ByteProcessor(512, 512);											// debug code
		ImageStack roiFromBallsStack = new ImageStack(ph1.getWidth(), ph1.getHeight()); 			// debug code
		MovieProcessor.applySelectionToImageProcessorIfNone(roiFromBalls, ph1, 200);								// debug code
		ImageProcessor ph2 = new ByteProcessor(ph1, false);											// debug code
		roiFromBallsStack.addSlice("PHASE 1: building from max skeleton (without forced points)", ph1); // debug code
		
//		log.debug("Constructing " + b.getBactName() + " PHASE 1: building from adjusted skeleton (without forced points):");
//		roiFromBalls = getRoiFromBalls(adjustedSkeleton, 4, offset, b);		
		// TODO TODO 11.7.2013. Remove phase 2, it's already included in Phase 1
		log.debug("Constructing " + b.getBactName() + " PHASE 2: appending forced body points:");
		roiFromBalls = appendForcedBalls(roiFromBalls, endpointsToForceToBody, ballRadius, offset);
		
		MovieProcessor.applySelectionToImageProcessorIfNone(roiFromBalls, ph2, 225);								// debug code
		ImageProcessor ph3 = new ByteProcessor(ph2, false);											// debug code		
		roiFromBallsStack.addSlice("PHASE 2: appending forced body points:", ph2);					// debug code
		
		log.debug("Constructing " + b.getBactName() + " PHASE 3: appending sorted forced endpoints:");
		Collections.sort(endpointsToForceToSkeleton);		
		roiFromBalls = appendForcedBalls(roiFromBalls, endpointsToForceToSkeleton, ballRadius, offset);
		
		MovieProcessor.applySelectionToImageProcessorIfNone(roiFromBalls, ph3, 255);								// debug code
		roiFromBallsStack.addSlice("PHASE 3: appending sorted forced endpoints", ph3);				// debug code
		 
		if (ControlPanel.debug && debugThisBacteria) {
			
			(new ImagePlus("+RoiFromBalls for " + b.getBactName(), roiFromBallsStack)).show();
			
		}
		
		checkForSkeletonForks(roiFromBalls, endpoints);
		
		int overallArea1 = MovieProcessor.getArea(roiFromBalls.getMask());
		log.debug("FINAL getRoiFromBalls(skeleton) from adjusted skeleton decrease for bacteria " + b.getBactName() + " from " + origArea + " to " + overallArea1 + ", that is " + (100 - 100. * overallArea1/origArea) + "%");
		
		return roiFromBalls;
		
		
	}
	
	void checkForSkeletonForks(ShapeRoi roi, VPoint[] oldEndpoints) {
		ImageProcessor skeleton = MovieProcessor.skeletonize(roi);
		VPoint[] endpoints = MovieProcessor.getSkeletonEndpoints((ByteProcessor)skeleton);
		if (endpoints.length != 2) {
			log.debug("Fork check FAILED:");
			ControlPanel.addStatusMessage("  Fork check failed - probably a 'bubble' growing out of selection - please correct the selection manually.");
			for (int i=0; i < endpoints.length; ++i) {
				log.debug("\tNew endpoint " + endpoints[i] + 
						" min distance = " + Math.min(oldEndpoints[0].distanceTo(endpoints[i]), oldEndpoints[1].distanceTo(endpoints[i]))
						);
				
			}
		} else {
			log.debug("Fork check PASSED.");
		}
	}
	
	

	protected int getBallHitOrMissValue (int x, int y, Rectangle offset, Bacteria b, int[][] stel, int frameNo) {
		boolean warnOnce = false;
		int r = stel.length/2;	
		int sum = 0;
		log.debug("Ž getBallHitOrMissValue x= " + x + " y = " + y + " offset.x = " + offset.x + " offset.y = " + offset.y + " r = " + r);
		try {
		for (int xx = -r; xx <= r; ++xx) {
			for (int yy = -r; yy <= r && sum != Integer.MIN_VALUE; ++yy) {
				if (selectionsMask[x + xx + offset.x][ y + yy + offset.y] != 0
						&& selectionsMask[x + xx + offset.x][ y + yy + offset.y] != b.getIdBacteria()){
					sum = Integer.MIN_VALUE;
					break;
				}								

				sum += stel[xx+r][yy+r] * sobel.getPixel(x + xx + offset.x, y + yy + offset.y);																			

			}
		}
		} catch (ArrayIndexOutOfBoundsException e) {
			if (!warnOnce) {
				warnOnce = true;
				String state = CurrentExperiment.getBacteriaStateAt(b, frameNo);
				if (state == null || state.toLowerCase().indexOf("ignore") == -1 ){
					ControlPanel.addStatusMessage("Error expanding bacteria " + b.getBactName() + ". Is it leaving the window? You should ignore it.");
				}
			}			
			
		}
		return sum;							
					
	}
	

	
	protected ShapeRoi getRoiFromBalls(ImageProcessor skeleton, int radius, Rectangle offset, Bacteria b, int ballRadius, int frameNo) {
		OvalRoi ball;
		ShapeRoi unionRoi = null;
		for (int y = 0; y < skeleton.getHeight(); ++y) {			
			for (int x = 0; x < skeleton.getWidth(); ++x)  {				//				
				if (skeleton.getPixel(x, y) > 0) {
					ball = new OvalRoi(
							x + offset.x - radius, 
							y + offset.y - radius,
							2 * radius + 1,
							2 * radius + 1
							);
					int val =  getBallHitOrMissValue (x, y, offset, b, StelsRepository.getStelForRadius(ballRadius), frameNo);
					
					if (val>=val) {  // TODO
						if (unionRoi == null) {
							unionRoi = new ShapeRoi(ball);
						} else {
							unionRoi = unionRoi.or(new ShapeRoi(ball));	
						}	
						log.debug("Building " + b.getBactName() + " from " + x + "," + y + " val = " +  val);
					} else {
						log.debug("Not Building " + b.getBactName() + " from " + x + "," + y + " val = " +  val);
					}
				}							
			}
		}	
		return unionRoi;
	}
	
	
	
	
	protected ShapeRoi appendForcedBalls(ShapeRoi unionRoi, ArrayList<ForcedPoint> forcedSortedEndpoints, int radius, Rectangle offset) {
		OvalRoi ball = new OvalRoi(
				100, 
				100,
				2 * radius + 1,
				2 * radius + 1
				);		
		ShapeRoi tmp;
		int ballArea = MovieProcessor.getArea(ball.getMask());
		MovieProcessor.dumpArrayListToLog(forcedSortedEndpoints, "forcedSortedEndpoints array");
		for (VPoint p: forcedSortedEndpoints) {			
			
			ball = new OvalRoi(
					p.x + offset.x - radius, 
					p.y + offset.y - radius,
					2 * radius + 1,
					2 * radius + 1
					);
			int prevArea =  MovieProcessor.getArea(unionRoi.getMask());
			tmp = (ShapeRoi) unionRoi.clone();
			tmp.or(new ShapeRoi(ball));
			int currArea = MovieProcessor.getArea(tmp.getMask());
			log.debug("Adding forced endpoint " + p + ". Area increased " + 100.*(currArea-prevArea)/ballArea + "% . " );
			if (100.*(currArea-prevArea)/ballArea > MAX_BALL_DELTA_PERC) {
				log.debug("Ignoring the outlier ball with center " +  p);				
			} else {
				unionRoi = tmp;
			}
			
		}	
		return unionRoi;
	}	
	
	

}
