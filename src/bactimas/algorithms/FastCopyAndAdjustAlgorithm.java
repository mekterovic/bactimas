package bactimas.algorithms;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;

import bactimas.alg.ForcedPoint;
import bactimas.alg.MovieProcessor;
import bactimas.alg.StelsRepository;
import bactimas.datamodel.CurrentExperiment;
import bactimas.db.beans.Bacteria;
import bactimas.gui.ControlPanel;
import bactimas.gui.ImageStrip.ImageStripType;
import signalprocesser.voronoi.VPoint;

public class FastCopyAndAdjustAlgorithm extends CopyAndAdjustAlgorithm {
	
	private static ImagePlus imp2;
	//private static ByteProcessor sobel;	
	
	@Override
	public String getName() {
		return "Fast Copy & Adjust";
	}

	
	@Override
	public String getAbbrev() {
		return "FC&A";
	}		
	
	
	@Override
	public void step(int frameNo, VPoint translation) {


		
		bacteriaRoisBag = new Hashtable<String, Roi>();

		

		
		ControlPanel.addStatusMessage("Processing frame:" + frameNo 
				+ " using: BallRadius:" + ballRadius + " Range: " + range + " on Channel: " + channel + " th.method:" + thMethod);
		
		
//		BridgeToRoiManager.reset();
		
		
		
		if (channel == ImageStripType.RED) { 
			imp2 = CurrentExperiment.getRedImagePlus(frameNo, "png");
		} else if (channel == ImageStripType.GREEN) {
			imp2 = CurrentExperiment.getGreenImagePlus(frameNo, "png");
		} else if (channel == ImageStripType.BLUE) {
			imp2 = CurrentExperiment.getBlueImagePlus(frameNo, "png");
		}
		
		
//		imp.setTitle("PROCESS F " + frameNo + " " + imp.getTitle());
//		
//		imp.show();
//		
//		WindowManager.setCurrentWindow(imp.getWindow());
		
		selectionsMask = new int[imp2.getProcessor().getWidth()][imp2.getProcessor().getHeight()];
		
		sobel = new ByteProcessor(imp2.getProcessor(), true);				
		sobel.filter(ImageProcessor.FIND_EDGES);
		
		
		LinkedList<Bacteria> bacterias;
		
		bacterias = CurrentExperiment.getBacteriasForFrame(CurrentExperiment.getPrevFrameNo(frameNo));
		
		LinkedList<Bacteria> splitBacterias = CurrentExperiment.getSplitBacteriasForFrame(frameNo);
		for (Bacteria sb: splitBacterias) {
			bacterias.removeFirstOccurrence(sb);
//			log.debug("Skipping bacteria " + sb + " bcs it has a split defined at frame:_" + frameNo);
		}
		
		
//		log.debug( "****************************************************************************************************************************");
//		log.debug( "************************************  Register rois frame = " + frameNo + "********************************************************");
//		log.debug( "****************************************************************************************************************************");
		
		ControlPanel.addStatusMessage("   " + bacterias.size() + " found.");
		Roi roi = null;
		for (Bacteria b: bacterias) {
			
			if (frameNo == 1) {	 
				roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), 1);			
			} else { // ako ima human, onda human
				roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), CurrentExperiment.getPrevFrameNo(frameNo));
				if (roi == null) {
					roi = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), CurrentExperiment.getPrevFrameNo(frameNo));					
				}
			}
			
//			imp.setRoi(roi);
//			
//			
//			
//			if (!roi.isArea()) {
//				log.debug("Converting roi to area roi.");
//				Macro_Runner mr = new Macro_Runner();
//				//URL macroFullPath = ClassLoader.getSystemClassLoader().getResource("/macros/ConvertLineSelectionToAreaAndVV.txt");				
//				mr.runMacroFile(S.getMacroFullPath("ConvertLineSelectionToAreaAndVV.txt"), null);
//				roi = imp.getRoi();
//				log.debug("Roi type is now:" + roi.getTypeAsString());
//			}
//			

			Rectangle r123 = roi.getBounds(); 
			roi.setLocation(r123.x + translation.x, r123.y + translation.y);

//			WindowManager.setCurrentWindow(imp.getWindow());
//			imp.setRoi(roi);			
			
			registerBacteriaRoi(b, roi, frameNo);

		}

		
//		WindowManager.setCurrentWindow(imp.getWindow());
		
		//Macro_Runner m2r = new Macro_Runner();
		
		ImagePlus impSobel = new ImagePlus("Sobel", sobel);		
		

		int th;
		if (thMethod == null) {
			impSobel.setRoi(roi);   // MUST BE HERE, OTHERWISE THRESHOLD BECOMES MUCH SMALLER AND EVERYTHING FAILS!
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
			
//			log.debug( "****************************************************************************************************************************");
//			log.debug( "************************************  Processing bacteria  " + b +        "*************************************************");
//			log.debug( "****************************************************************************************************************************");
			
			
			//imp.setRoi(bacteriaRoisBag.get(b.getBactName()));		

			
			Roi r = hitOrMissSobel(bacteriaRoisBag.get(b.getBactName()), b, frameNo, ballRadius);
			registerBacteriaRoi(b, r, frameNo);  // update roi and selected grid to avoid overlaps
												 // TODO:  
												 // I am not releasing the unused pixels!? 
												 // Would that help? Maybe not so much, they'll be grabbed next time (in the next frame) 
												 // This makes for a conservative rutf war stategy, still might be worth a shot to let go of pixels more easily
			
			
			newRois2.add(r);

			
			CurrentExperiment.saveROI(r, frameNo, b, bactimas.db.beans.Roi.ROI_TYPE_COMPUTER);
			
		}		
		//WindowManager.setCurrentWindow(imp.getWindow());
		//BridgeToRoiManager.addRoisToAll(newRois2);
	

//		if (!ControlPanel.debug) {		
//			imp.close();
//		}		
		
	}
	
	@Override
	protected int getBallHitOrMissValue (int x, int y, Rectangle offset, Bacteria b, int[][] stel, int frameNo) {
		boolean warnOnce = false;
		int r = stel.length/2;	
		int sum = 0;		
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
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected Roi hitOrMissSobel(Roi prevRoi, Bacteria b, int frameNo, int ballRadius) {

		
		ImageProcessor mask = prevRoi.getMask();
		
		Rectangle offset = prevRoi.getBounds();
		
		
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
		
		
//		int origArea = MovieProcessor.getArea(mask);
		
		ArrayList<ForcedPoint> endpointsToForceToSkeleton = new ArrayList<ForcedPoint>();
		ArrayList<ForcedPoint> endpointsToForceToBody = new ArrayList<ForcedPoint>();
		
		VPoint[] endpoints = MovieProcessor.getSkeletonEndpoints((ByteProcessor)moldSkeleton);
		
//		ImageStack stack = new ImageStack(mask.getWidth(), mask.getHeight());
			
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
//			int endpSum =  getBallHitOrMissValue (endpoints[i].x, endpoints[i].y, offset, b, StelsRepository.getStelForRadius(ballRadius), frameNo);
			
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
//							log.debug("(" + x1 + "," + y1 + ").getBallHitOrMissValue = " + val + " ballDist=" + ballDist + " FORCE SKELETON!");
						} else {
//							log.debug("(" + x1 + "," + y1 + ").getBallHitOrMissValue = " + val + " ballDist=" + ballDist);	
						}
						
					}					
				}
			}
			if (maxN != null && maxSum > 0) {
				slice.putPixel(maxN.x, maxN.y, 255);	
				sobelSlice.putPixel(maxN.x, maxN.y, 255);
			}
//			stack.addSlice("maxN: " + maxN + ".sum = " + maxSum + " endp.sum=" + endpSum, slice);
		}
//		stack.addSlice("Sobel ", sobelSlice);
		
		

		
		
							
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
//								log.debug(b.getBactName() + ".n1.skeleton(" + n1 + ").getBallHitOrMissValue = " + val + " FORCED");
							} else {
//								log.debug(b.getBactName() + ".n1.skeleton(" + n1 + ").getBallHitOrMissValue = " + val);
							}
						}																						
					}	
					if (currSum >= 0) {
						maxSkeleton.putPixel(x, y, 200);
					}
					if (maxSum > currSum) {
//						log.debug("Adjusting skeleton from " + currSum + " to " + maxSum);
						maxSkeleton.putPixel(maxN.x, maxN.y, 255);
					} else {
						maxSkeleton.putPixel(x, y, 200);
					}
				}
			}
		}

		
		Roi tmp = MovieProcessor.createSelectionFromMask(maxSkeleton, new VPoint(0, 0));
		
		if (tmp == null) {
			ControlPanel.addStatusMessage("Warning: bacteria " + b.getBactName() + " at frame " 
						+ frameNo + " is 'lost', could not reckognize it, reverting to previous selection (roi). Please review and fix by hand");
			return prevRoi;
		}
		
		ByteProcessor sel = new ByteProcessor(maxSkeleton.getWidth(), maxSkeleton.getHeight());
		MovieProcessor.applySelectionToImageProcessor(tmp, sel, 255);

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
		
		

		
//		ImagePlus imp3 = new ImagePlus("+Mask for " + b.getBactName(), tmp.getMask());
//		imp3.show();	
		
		
		ShapeRoi roiFromBalls = null;
//		log.debug("Constructing " + b.getBactName() + " PHASE 1: building from max skeleton (without forced points):");
		roiFromBalls = getRoiFromBalls(sel, ballRadius, offset, b, ballRadius, frameNo);
		
		// TODO TODO 11.7.2013. Remove phase 2, it's already included in Phase 1
//		log.debug("Constructing " + b.getBactName() + " PHASE 2: appending forced body points:");
		roiFromBalls = appendForcedBalls(roiFromBalls, endpointsToForceToBody, ballRadius, offset);
		
		
//		log.debug("Constructing " + b.getBactName() + " PHASE 3: appending sorted forced endpoints:");
		Collections.sort(endpointsToForceToSkeleton);		
		roiFromBalls = appendForcedBalls(roiFromBalls, endpointsToForceToSkeleton, ballRadius, offset);
		
		 
		
		//checkForSkeletonForks(roiFromBalls, endpoints);
		
//		int overallArea1 = MovieProcessor.getArea(roiFromBalls.getMask());
//		log.debug("FINAL getRoiFromBalls(skeleton) from adjusted skeleton decrease for bacteria " + b.getBactName() + " from " + origArea + " to " + overallArea1 + ", that is " + (100 - 100. * overallArea1/origArea) + "%");
//		
		return roiFromBalls;
		
		
	}	
	
	
	
	
}
