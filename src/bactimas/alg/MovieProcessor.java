package bactimas.alg;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.ResultsTable;
import ij.plugin.Macro_Runner;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.db.DALService;
import bactimas.db.beans.Bacteria;
import bactimas.db.beans.BacteriaMeasurement;
import bactimas.db.beans.ExperimentMeasurements;
import bactimas.gui.ControlPanel;
import bactimas.util.S;
import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.VoronoiAlgorithm;
import signalprocesser.voronoi.representation.RepresentationFactory;
import signalprocesser.voronoi.representation.triangulation.TriangulationRepresentation;

public class MovieProcessor {
	static Logger log = Logger.getLogger("bactimas.alg.BacteriaProcessor" );	
	

	
	public final static VPoint[] n24 = new VPoint[] {			
		new VPoint(0, -1) ,
		new VPoint(1, -1) ,
		new VPoint(1, 0) ,
		new VPoint(1, 1) ,
		new VPoint(0, 1) ,
		new VPoint(-1, 1) ,
		new VPoint(-1, 0) ,
		new VPoint(-1, -1),
		
		new VPoint(0, -2) ,
		new VPoint(1, -2) ,
		new VPoint(2, -2) ,
		new VPoint(2, -1) ,
		new VPoint(2,  0) ,
		new VPoint(2,  1) ,
		new VPoint(2,  2) ,		
		new VPoint( 1,  2) ,
		new VPoint( 0,  2) ,
		new VPoint(-1,  2) ,
		new VPoint(-2,  2) ,		
		new VPoint(-2,  1) ,
		new VPoint(-2,  0) ,
		new VPoint(-2, -1) ,
		new VPoint(-2, -2) ,		
		new VPoint(-1, -2) 		
		
	};		
	

	
	// must be in this order !!!
	public final static VPoint[] n8 = new VPoint[] {			
			new VPoint(0, -1) ,
			new VPoint(1, -1) ,
			new VPoint(1, 0) ,
			new VPoint(1, 1) ,
			new VPoint(0, 1) ,
			new VPoint(-1, 1) ,
			new VPoint(-1, 0) ,
			new VPoint(-1, -1) 			
		};		
	

	public static String getSelectionMaskAsString(int mask[][]) {
		StringBuffer sb = new StringBuffer("---------------------------------------------");
		for (int y = 0; y < mask.length; ++y) {
			sb.append("\n");
			for (int x = 0; x < mask.length; ++x)  {				
				if (mask[x][y] > 0)
					sb.append( " " + String.format("%02d", mask[x][y]));
				else
					sb.append ("   ");
			}
//			if (y%10 == 0) S.out("y=" + y);
		}
		return sb.toString();
	}	
	public static String getMaskAsString(ImageProcessor mask) {
		String s = "---------------------------------------------";
		for (int y = 0; y < mask.getHeight(); ++y) {
			s += "\n";
			for (int x = 0; x < mask.getWidth(); ++x)  {				
//				s += String.format("%03d", mask.getPixel(x, y));
				if (mask.getPixel(x, y) > 0)
					s+="#";
				else
					s+=" ";
			}
		}
		return s;
	}
	public static String getMaskAsString2(ImageProcessor mask) {
		String s = "---------------------------------------------";
		for (int y = 0; y < mask.getHeight(); ++y) {
			s += "\n" +  String.format("%03d", y) + " | ";
			for (int x = 0; x < mask.getWidth(); ++x)  {				
//				s += String.format("%03d", mask.getPixel(x, y));
				if (mask.getPixel(x, y) > 0)
					s+= " " + String.format("%03d", x);
				else
					s+="    ";
			}
		}
		return s;
	}

	public static String getMaskAsString3(ImageProcessor mask) {
		StringBuffer sb = new StringBuffer("---------------------------------------------");
		for (int y = 0; y < mask.getHeight(); ++y) {
			sb.append( "\n" +  String.format("%03d", y) + " | ");
			for (int x = 0; x < mask.getWidth(); ++x)  {				
//				s += String.format("%03d", mask.getPixel(x, y));
				if (mask.getPixel(x, y) > 0)
					sb.append(" " + String.format("%03d", x) + ":" + String.format("%03d", mask.getPixel(x, y)));
				else
					sb.append("        ");
			}
		}
		return sb.toString();
	}

	public static Roi createSelectionFromMask(ImageProcessor mask, VPoint translation) {
		ArrayList<VPoint> points = new ArrayList<VPoint>();
		try {
		for (int x = 0; x < mask.getWidth(); ++x){
			for (int y = 0; y < mask.getHeight(); ++y){
				
				if ( mask.getPixel(x ,y) > 0) {

					points.add(new VPoint(x + translation.x, y + translation.y));
					log.debug("createSelectionFromMask: adding point " + (new VPoint(x + translation.x, y + translation.y)));
				} 													
								
			}
		}		
	
		TriangulationRepresentation representation = new TriangulationRepresentation( 5 );
        try {
            // Convert points to the right form
            points = RepresentationFactory.convertPointsToTriangulationPoints(points);
            
            // Run the algorithm
            VoronoiAlgorithm.generateVoronoi(representation, points);
        } catch ( Error e ) {
            log.debug(e);
            e.printStackTrace();
        } catch ( RuntimeException e ) {
        	log.debug(e);
            e.printStackTrace();
        }

      
        ArrayList<VPoint> outterpoints = representation.getPointsFormingOutterBoundary();
        FloatPolygon test = new FloatPolygon();
		for (VPoint p : outterpoints) {			
			test.addPoint(p.x, p.y);		
			log.debug("Adding outer point " + p.x + ", " + p.y);
		}
		Roi myRoi = new PolygonRoi(test, Roi.POLYGON);

		
		return myRoi;
		} catch (Exception e) {			
			log.error(e);			
		}
		return null;
	}
		

//	private static boolean isSkeletonConnected(ByteProcessor skeletonRoi) {
//		int endpoints = 0;
//		for (int y = 0; y < skeletonRoi.getHeight(); y++) {
//			for (int x= 0; x < skeletonRoi.getWidth(); x++) {				
//				if (skeletonRoi.getPixel(x, y) != 0) {						
//					int i;
//					int n = 0;
//					for (i=0; i < MovieProcessor.n8.length; ++i) {
//						if (  skeletonRoi.getPixel(x + MovieProcessor.n8[i].x, y + MovieProcessor.n8[i].y) > 0) {
//							if (skeletonRoi.getPixel(x + MovieProcessor.n8[(i+1)%MovieProcessor.n8.length].x, y + MovieProcessor.n8[(i+1)%MovieProcessor.n8.length].y) == 0) {
//								++n;
//							} else {
//								log.debug("Not counting " + (x + MovieProcessor.n8[i].x) + ", "  + ( y + MovieProcessor.n8[i].y) + " bcs its adjacent:\n");
//										//+  Playground.getMaskAsString33(skeletonRoi));
//							}
//						}														
//					}
//					if (n == 0) return false;  // isolated dot!
//					if (n == 1) ++endpoints;
//					if (endpoints > 2) return false;
//				}
//			}
//		}
//		return true;
//	}
	public static VPoint[] getSkeletonEndpoints(ByteProcessor skeletonRoi) {
	
		ArrayList<VPoint> endpoints = new ArrayList<VPoint>(); // shouldn't be more than 2, when it's 3, actions will be taken via checkFork
		log.debug("getSkeletonEndpoints for " +  MovieProcessor.getMaskAsString(skeletonRoi));
		for (int y = 0; y < skeletonRoi.getHeight(); y++) {
			for (int x= 0; x < skeletonRoi.getWidth(); x++) {				
				if (skeletonRoi.getPixel(x, y) != 0) {						
					int i;
					int n = 0;
					for (i=0; i < MovieProcessor.n8.length; ++i) {
						if (  skeletonRoi.getPixel(x + MovieProcessor.n8[i].x, y + MovieProcessor.n8[i].y) > 0) {
							if (skeletonRoi.getPixel(x + MovieProcessor.n8[(i+1)%MovieProcessor.n8.length].x, y + MovieProcessor.n8[(i+1)%MovieProcessor.n8.length].y) == 0) {
								++n;
							} else {
								log.debug("Not counting " + (x + MovieProcessor.n8[i].x) + ", "  + ( y + MovieProcessor.n8[i].y) + " bcs its adjacent:\n");
										//+  Playground.getMaskAsString33(skeletonRoi));
							}
						}														
					}
					
					if (n == 1) endpoints.add(new VPoint(x, y));
					
				}
			}
		}
		return endpoints.toArray(new VPoint[endpoints.size()]);
	}	
	public static void applySelectionToImageProcessor(Roi r, ImageProcessor imp, int pixelValue) {
		for (int x = 0; x < imp.getWidth(); ++x) {
			for (int y=0; y < imp.getHeight(); ++y) {
				if (contains(r, x, y)) {
					imp.putPixel(x, y, pixelValue);
				}
			}
		}
	}
	public static void applySelectionToImageProcessorIfNone(Roi r, ImageProcessor imp, int pixelValue) {
		for (int x = 0; x < imp.getWidth(); ++x) {
			for (int y=0; y < imp.getHeight(); ++y) {
				if (imp.getPixel(x, y) == 0 && contains(r, x, y)) {
					imp.putPixel(x, y, pixelValue);
				}
			}
		}
	}	
			
	
	
	private static boolean contains (Roi roi, int x, int y) {
		
		if (roi instanceof ShapeRoi) {
			return roi.contains(x, y);
		}
		
		Rectangle tmp = roi.getBounds();		
		Rectangle r = new Rectangle(tmp.x, tmp.y, tmp.width + 2, tmp.height + 2);
		
		boolean contains = r.contains(x+1, y+1);
		
		if (contains==false)  // igor changed || -> &&   !!! i.e. deleted: roi.getCornerDiameter() == 0 ||
			return contains;
		
//		dropped this:		
//		RoundRectangle2D rr = new RoundRectangle2D.Float(tmp.x, tmp.y, tmp.width, tmp.height, roi.getCornerDiameter(), roi.getCornerDiameter());
//		contains =  rr.contains(x, y);
//		if (contains==false) return contains;
		
		// above is the bounding box check
		// now, we go for the real stuff (it is within the bounding box, but might not be within roi:
		FloatPolygon poly = roi.getFloatPolygon(); //xpf, ypf, nPoints);
		boolean inside = false;
//		x = x - tmp.x;
//		y = y - tmp.y;
		
		
		for (int i=0, j=poly.npoints-1; i<poly.npoints; j=i++) {
			if (poly.xpoints[i] == x && poly.ypoints[i]==y) { // Overriden by Igor to make sure borders are counted IN. Works for me :)
				inside = true; 
				break;
			}   
			
			if (((poly.ypoints[i]>y)!=(poly.ypoints[j]>y)) &&
			(x<(poly.xpoints[j]-poly.xpoints[i])*(y-poly.ypoints[i])/(poly.ypoints[j]-poly.ypoints[i])+poly.xpoints[i]))
			inside = !inside;
			
		}
		return inside;
				
	}
	
	
	
//	private static int SOBEL_TRANS_RANGE = 20;
	public static VPoint detectTranslationSobel (int frameNo, int range) {
		
		if (frameNo == 1) return new VPoint(0, 0);
		
		ImagePlus impOldSobel = CurrentExperiment.getBlueImagePlus(CurrentExperiment.getPrevFrameNo(frameNo), null);
		ImagePlus impNewSobel = CurrentExperiment.getBlueImagePlus(frameNo, null);
		
		ByteProcessor oldSobel = new ByteProcessor(impOldSobel.getProcessor(), true);				
		oldSobel.filter(ImageProcessor.FIND_EDGES);
		int th = oldSobel.getAutoThreshold();
		log.debug("oldSobel.th = " + th);
//		ImagePlus impOldSobel2 = new ImagePlus("Old Sobel", oldSobel);
//		impOldSobel2.show();
		impOldSobel = new ImagePlus("old sobel", oldSobel);
		IJ.setAutoThreshold(impOldSobel, "Default dark"); 
	    IJ.run(impOldSobel, "Convert to Mask", "");	
		
		
		ByteProcessor newSobel = new ByteProcessor(impNewSobel.getProcessor(), true);				
		newSobel.filter(ImageProcessor.FIND_EDGES);
		th = newSobel.getAutoThreshold();
		log.debug("newSobel.th = " + th);
//		ImagePlus impNewSobel2 = new ImagePlus("New Sobel", newSobel);
//		impNewSobel2.show();
		impNewSobel = new ImagePlus("new sobel", newSobel);
		IJ.setAutoThreshold(impNewSobel, "Default dark"); 
	    IJ.run(impNewSobel, "Convert to Mask", "");	
	    
	    
	    long sum, maxSum = Long.MIN_VALUE;
	    VPoint trans = null;
	    
	    for (int dx = -range; dx < range; ++dx) {
	    	for (int dy = -range; dy < range; ++dy) {
	    		sum = 0;
	    		
	    		for (int x = range; x < impNewSobel.getWidth() - range; ++x){
	    			for (int y = range; y < impNewSobel.getHeight() - range; ++y){
	    				sum += oldSobel.getPixel(x, y) * newSobel.getPixel(x + dx, y + dy);
	    			}
	    		}
	    		log.debug("Detect trans for frame " + frameNo + "  sum=" + sum + " maxSum="  + maxSum);
	    		if (sum > maxSum) {
	    			maxSum = sum;
	    			trans = new VPoint(dx, dy);
	    		}
	    	}
	    }
	    
	    if (ControlPanel.debug) {
		    ImageStack stack = new ImageStack(impNewSobel.getWidth(), impNewSobel.getHeight());
			stack.addSlice("old", oldSobel);
			stack.addSlice("new", newSobel);
			stack.addSlice("old", oldSobel);
			ByteProcessor newTransSobel = new ByteProcessor(newSobel.getWidth(), newSobel.getHeight());				
			for (int x = 0; x < newSobel.getWidth(); ++x){
				for (int y = 0; y < newSobel.getHeight(); ++y){
					newTransSobel.putPixel(x, y, newSobel.getPixel(x + trans.x, y + trans.y));
				}
			}		
			stack.addSlice("trans:" + trans, newTransSobel);
			ImagePlus dbg = new ImagePlus("Trans Sobel", stack);
			dbg.show();
			S.out("detectTranslationSobel returning " + trans);
	    }
	    log.debug("DetectTranslationSobel for frame " + frameNo + " returning " + trans);
	    return trans;

		
	}	
	

	
	
	public static void measure(String alternateFormat) {
		
		CurrentExperiment.deleteAllMeasurementsToBeMeasured();
		
		Hashtable<String, ExperimentMeasurements> toMeasure = CurrentExperiment.getExperimentMeasures();
		Hashtable<String, ExperimentMeasurements> toMeasureRed = new Hashtable<String, ExperimentMeasurements>();
		Hashtable<String, ExperimentMeasurements> toMeasureGreen = new Hashtable<String, ExperimentMeasurements>();
		Hashtable<String, ExperimentMeasurements> toMeasureBlue = new Hashtable<String, ExperimentMeasurements>();
		Hashtable<Integer, Integer> roiSizes = new Hashtable<Integer, Integer>();
		ImagePlus redImp = null, greenImp = null, blueImp = null;
		boolean loadRed = false, loadBlue = false, loadGreen = false;
		for (ExperimentMeasurements m : toMeasure.values()) {
			String value = m.getChannelName();
			if (value.toLowerCase().equals("red")) {
				loadRed = true;
				toMeasureRed.put(m.getMeasurementName(), new ExperimentMeasurements(m.getCollarSize(), m.getMeasurementName(), m.getChannelName() ));
			} else if (value.toLowerCase().equals("green")) {
				loadGreen = true;
				toMeasureGreen.put(m.getMeasurementName(),  new ExperimentMeasurements(m.getCollarSize(), m.getMeasurementName(), m.getChannelName() ));
			} else if (value.toLowerCase().equals("blue")) {
				loadBlue = true;
				toMeasureBlue.put(m.getMeasurementName(),  new ExperimentMeasurements(m.getCollarSize(), m.getMeasurementName(), m.getChannelName() ));
			}
			roiSizes.put(m.getCollarSize(), m.getCollarSize());
		}
		Analyzer.setMeasurements(0xFFFFFFFF);  // TODO: pick only the actual ones. THis could be done once (before all measurements) by testing for each bit (try-catch)...
		CurrentExperiment.closeResultsWindow();
		ControlPanel.addStatusMessage("Measuring channels(red=" + loadRed + ",green=" + loadGreen + ",blue=" + loadBlue + ")...");
		int total = 0;
		int f;
		for (f = 1;  f <= CurrentExperiment.getFrameCount(); ++f) {
			if (CurrentExperiment.getFrame(f).getIgnoreFrame().equals("y")) {
				ControlPanel.addStatusMessage("Skipping IGNORED frame " + f);
				continue;
			}
			
			if (loadRed) {
				redImp = CurrentExperiment.getRedImagePlus(f, alternateFormat);
			}
			if (loadGreen) {
				greenImp = CurrentExperiment.getGreenImagePlus(f, alternateFormat);
			}
//			if (loadBlue) {
				blueImp = CurrentExperiment.getBlueImagePlus(f, alternateFormat);
//			}
			LinkedList<Bacteria> bacterias = CurrentExperiment.getBacteriasForFrame(f);
			if (bacterias.size() == 0) {
				ControlPanel.addStatusMessage("No bacteria detected at frame " + f + ".");
				break;
			}
			StringBuffer out = new StringBuffer("Frame " + f);
			for (Bacteria b: bacterias) {
				int idRoiType = 1;
				Roi roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), f);
				if (roi == null) {
					roi = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), f);
					idRoiType = 2;
				}
				if (roi == null) {
					ControlPanel.addStatusMessage("Error: no Roi for bacterium:" + b + " at frame " + f);
				} else {
					out.append("\n   " + b + ":\t");
					Hashtable<Integer, Roi> rois = new Hashtable<Integer, Roi>();
					for (int rs : roiSizes.values()) {
						if (rs == 0) {
							rois.put(0, roi);
						} else {
							Macro_Runner mr = new Macro_Runner();	
							Roi clone = (Roi) roi.clone();
							blueImp.setRoi(clone);
							blueImp.show();
							mr.runMacroFile(S.getMacroFullPath("EnlargeSelectionNoDialog.txt"), "" + rs);
							rois.put(rs, blueImp.getRoi());								
						}
					}
					
					LinkedList<BacteriaMeasurement> measurements = new LinkedList<BacteriaMeasurement> ();
					if (loadRed) {
						out.append("Red: " + takeMeasureAll(roiSizes, rois, redImp, toMeasureRed, measurements, b.getIdBacteria(), f, idRoiType, DALService.getChannelId("red")));
					}
					if (loadGreen) {
						out.append("Green:" + takeMeasureAll(roiSizes, rois, greenImp, toMeasureGreen, measurements, b.getIdBacteria(), f, idRoiType, DALService.getChannelId("green")));
					}
					if (loadBlue) {
						out.append("Blue:" + takeMeasureAll(roiSizes, rois, blueImp, toMeasureBlue, measurements, b.getIdBacteria(), f, idRoiType, DALService.getChannelId("blue")));
					}
					CurrentExperiment.saveMeasurements(measurements);
					total += measurements.size();
					ControlPanel.addStatusMessage(out.toString());
				}
			}
			blueImp.close();
			
		}
		CurrentExperiment.closeResultsWindow();
		ControlPanel.addStatusMessage("Done: " + total + " measurements taken on " + (f-1) + " frames.");
	
	}
	private static String takeMeasureAll(Hashtable<Integer, Integer> roiSizes , Hashtable<Integer, Roi> rois, ImagePlus imp, 
										 Hashtable<String, ExperimentMeasurements> toMeasure, LinkedList<BacteriaMeasurement> measurements, 
										 int idBacteria, int frameNo, int idRoiType, int idChannel ) {		
		String out = "";
		for (int rs : roiSizes.values()) {
			Hashtable<String, String> toMeasurePerCollarSize = new Hashtable<String, String>();
			for (ExperimentMeasurements m: toMeasure.values()) {
				if (m.getCollarSize() == rs) {
					toMeasurePerCollarSize.put(m.getMeasurementName(), m.getMeasurementName());
				}
			}
			out +=  
					takeMeasure(
					rois.get(rs),
					imp,
					toMeasurePerCollarSize,
					measurements,
					rs, 
					idBacteria, 
					frameNo, 
					idRoiType,
					idChannel
					);

		}	
		return out;
	}
	
	private static String takeMeasure(Roi roi, ImagePlus imp, Hashtable<String, String> toMeasure, LinkedList<BacteriaMeasurement> measurements, int collarSize, int idBacteria, int frameNo, int idRoiType, int idChannel ) {
		roi.setImage(imp);
		imp.setRoi(roi);
		Analyzer.getResultsTable().reset();
		// for the time being, I'm using default ResultTable and delete it (and measurements options)
		Analyzer analyzer = new Analyzer(roi.getImage());		
		analyzer.measure();
		ResultsTable rt = Analyzer.getResultsTable();
		String dbg = "    ";
		DecimalFormat df = new DecimalFormat("0.00");
		for (int i = 0; i <= rt.getLastColumn(); ++i) {
			
			try {
//				ControlPanel.addStatusMessage("toMeasure.containsKey(" + rt.getColumnHeading(i).toLowerCase() + "))  = " + toMeasure.containsKey(rt.getColumnHeading(i).toLowerCase()));
				if (toMeasure.containsKey(rt.getColumnHeading(i).toLowerCase())) {
					BacteriaMeasurement bm = new BacteriaMeasurement (
								idBacteria, 
								frameNo,
								DALService.getMeasurementId(rt.getColumnHeading(i)),
								rt.getValueAsDouble(i, 0),
								idRoiType,
								idChannel
							);
										
					dbg += rt.getColumnHeading(i) + "(" + collarSize + ") = " + df.format(rt.getValueAsDouble(i, 0)) + "\t";					
					measurements.add(bm);
				}				
			} catch (Exception e) {
				// Dirty, I know, I haven't figured out the better way.
				// Don't like parsing rt.getColumnHeadings() any better					
			}												
														
		}		
		return dbg;		
	}
	
	
	
	
	public static void process (int frameFrom, int frameTo) {		
		String prevAlg = "";
		long startTime = System.currentTimeMillis();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();		
		ControlPanel.addStatusMessage("Started at:" + dateFormat.format(date));				
		for (int frame = frameFrom; frame <= frameTo; ++frame) {
			if (CurrentExperiment.getFrame(frame).isIgnored()) {
				ControlPanel.addStatusMessage("Skipping IGNORED frame:" + frame);				
			} else {
				VPoint translation = DALService.getFrameTranslation(frame);				
				if (translation == null) {
					ControlPanel.addStatusMessage("Translation hasn't been calculated for frame " + frame + ". Aborting.");
					return;
				} 				
				
				
				ITrackingAlgorithm alg = CurrentExperiment.getAlgorithmForFrame(frame);
				if (!prevAlg.equals(alg.getClassName())) {									
					alg.beforeBatch(frame);
					prevAlg = alg.getClassName();
				}
				
				ControlPanel.addStatusMessage("Processing frame:" + frame);
				alg.beforeStep(frame);
				alg.step(frame, translation);
				
			}
		}
		long millis = System.currentTimeMillis() - startTime;
		ControlPanel.addStatusMessage("Done. Duration:" + 
				String.format("%d min, %d sec", 
					    TimeUnit.MILLISECONDS.toMinutes(millis),
					    TimeUnit.MILLISECONDS.toSeconds(millis) - 
					    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
					)			
		);	
		
	}
	
//	public static void process (int frameNo, int ballRadius, int range, ImageStripType channel, String thMethod) {
//
//		
//		bacteriaRoisBag = new Hashtable<String, Roi>();
//		
//		ControlPanel.addStatusMessage("Processing frame:" + frameNo 
//				+ " using: BallRadius:" + ballRadius + " Range: " + range + " on Channel: " + channel + " th.method:" + thMethod);
//		
//		
//		BridgeToRoiManager.reset();
//		
//		//imp = ImageStrip.getInstance().getNewImagePlusWindow(frameNo);
//		
//		if (channel == ImageStripType.RED) { 
//			imp = CurrentExperiment.getRedImagePlus(frameNo, "PNG");
//		} else if (channel == ImageStripType.GREEN) {
//			imp = CurrentExperiment.getGreenImagePlus(frameNo, "PNG");
//		} else if (channel == ImageStripType.BLUE) {
//			imp = CurrentExperiment.getBlueImagePlus(frameNo, "PNG");
//		}
//		
//		
//		imp.setTitle("PROCESS F " + frameNo + " " + imp.getTitle());
//		
//		imp.show();
//		WindowManager.setCurrentWindow(imp.getWindow());
//		
//		selectionsMask = new int[imp.getProcessor().getWidth()][imp.getProcessor().getHeight()];
//		
//		sobel = new ByteProcessor(imp.getProcessor(), true);				
//		sobel.filter(ImageProcessor.FIND_EDGES);
//		
//
//
//		
//		LinkedList<Bacteria> bacterias;
//		
//		bacterias = CurrentExperiment.getBacteriasForFrame(CurrentExperiment.getPrevFrameNo(frameNo));
//		
//		LinkedList<Bacteria> splitBacterias = CurrentExperiment.getSplitBacteriasForFrame(frameNo);
//		for (Bacteria sb: splitBacterias) {
//			bacterias.removeFirstOccurrence(sb);
//			log.debug("Skipping bacteria " + sb + " bcs it has a split defined at frame:_" + frameNo);
//		}
//		
//		// ****************************************************************************************************************************
//		// ****************************************************  Register rois         ************************************************ 
//		// ****************************************************************************************************************************
//		log.debug( "****************************************************************************************************************************");
//		log.debug( "************************************  Register rois frame = " + frameNo + "********************************************************");
//		log.debug( "****************************************************************************************************************************");
//		
////		boolean firstPass = true;
////		VPoint translation = new VPoint(0, 0);
//		VPoint translation = DALService.getFrameTranslation(frameNo);
//		
//		if (translation == null) {
//			ControlPanel.addStatusMessage("Translation hasn't been calculated for frame " + frameNo + ". Aborting.");
//			return;
//		} 
//		
//		ControlPanel.addStatusMessage("   " + bacterias.size() + " found.");
//		
//		for (Bacteria b: bacterias) {
//			Roi roi;
//			if (frameNo == 1) {	 
//				roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), 1);			
//			} else { // ako ima human, onda human
//				roi = CurrentExperiment.getHumanRoiForBacteria(b.getIdBacteria(), CurrentExperiment.getPrevFrameNo(frameNo));
//				if (roi == null) {
//					roi = CurrentExperiment.getComputerRoiForBacteria(b.getIdBacteria(), CurrentExperiment.getPrevFrameNo(frameNo));					
//				}
//			}
//			
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
////			if (frameNo > 1 && firstPass) {
////				translation = detectTranslation (
////						ImageStrip.getInstance().getNewImagePlusWindow(frameNo-1),
////						ImageStrip.getInstance().getNewImagePlusWindow(frameNo), 
////						roi, 
////						null);
////				log.debug("Translation detected:" + translation);
////				S.out("Translation detected for frame " + frameNo + " :"  + translation);
////			}	
//			Rectangle r123 = roi.getBounds(); 
//			roi.setLocation(r123.x + translation.x, r123.y + translation.y);
//
//			WindowManager.setCurrentWindow(imp.getWindow());
//			imp.setRoi(roi);			
//			
//			registerBacteriaRoi(b, roi, frameNo);
//			//firstPass = false;
//		}
//
//		
//		WindowManager.setCurrentWindow(imp.getWindow());
//		
//		Macro_Runner mr = new Macro_Runner();
//
////		log.debug(Playground.getSelectionMaskAsString(selectionsMask));
////		log.debug("Sobel:");
////		log.debug(Playground.getMaskAsString33(sobel));
//		
//		ImagePlus impSobel = new ImagePlus("Sobel", sobel);		
//		
//
//		//
//		
////		sobel = new ByteProcessor(sobel, false);
//		//int th = sobel.getAutoThreshold();
//		
//		//S.out("th = " + th);
//		//IJ.setAutoThreshold(impSobel, "Default dark");
//		int th;
//		if (thMethod == null) {
//			impSobel.setRoi(imp.getRoi());   // MUST BE HERE, OTHERWISE THRESHOLD BECOMES MUCH SMALLER AND EVERYTHING FAILS!
//			// note that this is different from user choosing "default" (below - else brach) bcs here different histogram is used (taken from roi)
//			th = sobel.getAutoThreshold();
//			IJ.setAutoThreshold(impSobel, "Default dark");
//			IJ.run(impSobel, "Convert to Mask", "");
//		} else {
//			th = (new AutoThresholder()).getThreshold(thMethod, sobel.getHistogram()); 
//			IJ.setThreshold(impSobel, th, 255);
//		    IJ.run(impSobel, "Convert to Mask", "");			
//		}
//		
//		
//	    
//	    removeSingleDots(impSobel.getProcessor());
//		skeletonize((ByteProcessor)impSobel.getProcessor());  // to get rid of "fat" bacteria lines
//		
//	    if (ControlPanel.debug) {		
//		    impSobel.setTitle(impSobel.getTitle() + " th = " + th );
//	    	impSobel.show();	
//		    
////		    ImagePlus impSobelAll = new ImagePlus("Sobel no roi", new ByteProcessor(sobel, true));
////		    int thAll =  impSobelAll.getProcessor().getAutoThreshold();
////		    impSobelAll.setTitle(impSobelAll.getTitle() + " th = " + thAll);
////		    
////		    IJ.setAutoThreshold(impSobelAll, "Default dark"); 
////		    IJ.run(impSobelAll, "Convert to Mask", "");
////		    
////		    removeSingleDots(impSobelAll.getProcessor());
////			skeletonize((ByteProcessor)impSobelAll.getProcessor()); 
////			impSobelAll.show();	
//		}	    
//
//	    
//	    sobel = (ByteProcessor) impSobel.getProcessor();
//	    
//	    LinkedList<Roi> newRois2 = new LinkedList<Roi>(); 
//		for (Bacteria b: bacterias) {
//			
//			log.debug( "****************************************************************************************************************************");
//			log.debug( "************************************  Processing bacteria  " + b +        "*************************************************");
//			log.debug( "****************************************************************************************************************************");
//			
//			
//			imp.setRoi(bacteriaRoisBag.get(b.getBactName()));		
//
//			
//			Roi r = Victory.hitOrMissSobel(bacteriaRoisBag.get(b.getBactName()), b, frameNo, ballRadius);
//			registerBacteriaRoi(b, r, frameNo);  // update roi and selected grid to avoid overlaps
//												 // TODO:  
//												 // I am not releasing the unused pixels!? 
//												 // Would that help? Maybe not so much, they'll be grabbed next time (in the next frame) 
//												 // This makes for a conservative rutf war stategy, still might be worth a shot to let go of pixels more easily
//			
//			
//			newRois2.add(r);
//
//			
//			CurrentExperiment.saveROI(r, frameNo, b, bactimas.db.beans.Roi.ROI_TYPE_COMPUTER);
//			
//		}		
//		WindowManager.setCurrentWindow(imp.getWindow());
//		BridgeToRoiManager.addRoisToAll(newRois2);
//	
//
//		if (!ControlPanel.debug) {		
//			imp.close();
//		}
//		
//		
//		
//		
//	}
//	
	
	public static int getArea(ImageProcessor mask) {
		int origArea = 0;
		for (int x = 0; x < mask.getWidth(); ++x) {				
			for (int y = 0; y < mask.getHeight(); ++y) {					
				if (mask.getPixel(x, y) > 0) {
					++origArea;
				}
			}
		}
		return origArea;
	}
	
		

	
	public static int colToValue(Color c) {
		return  (  (c.getRed())   << 16
				| (c.getGreen()) << 8
				| (c.getBlue()) 
				);
	}
	
	private static void testPolygonRoi() {
		FloatPolygon test = new FloatPolygon();
		test.addPoint(1, 0);
		test.addPoint(2, 0);
		test.addPoint(3, 0);
		test.addPoint(3, 1);
		test.addPoint(2, 2);
		test.addPoint(1, 2);
		test.addPoint(0, 1);
		
		S.out(test.contains(1, 0));
		S.out(test.contains(2, 0));
		S.out(test.contains(3, 0));
		S.out(test.contains(3, 1));
		S.out(test.contains(2, 2));
		S.out(test.contains(1, 2));
		S.out(test.contains(0, 1));
		
		
	}
	
	
	public static ArrayList<VPoint> extractBorderPixels(Roi roi){
		ImageProcessor mask = roi.getMask();		
		ArrayList<VPoint> pixels = new ArrayList<VPoint>();
		Rectangle r = roi.getBounds();
		for (int x = 0; x < r.width; ++x){
			for (int y = 0; y < r.height; ++y){
//				int N = 0;				
				for (int i=0; mask.getPixel(x, y) > 0 && i < n8.length; i += 2) {
					if ( mask.getPixel(x + n8[i].x, y + n8[i].y) == 0) {
//						System.out.println("border = " + x + "," + y);
						pixels.add(new VPoint(x + r.x, y + r.y));
//						roi.getImage().getProcessor().putPixel(x + r.x, y + r.y, 255);	
//						roi.getImage().getProcessor().putPixel(x, y, 255);	
						break;
					} 													
				}				
			}
		}
		return pixels;
	}


	
	
	public static void dumpArrayListToLog(@SuppressWarnings("rawtypes") ArrayList al, String title) {
		String s = "";
		for (Object o: al) {
			s += o.toString();
		}
		log.debug("ArrayList dump:" + title + ":" + s);
	}
	
	
	


	/**
	 * Implementation of Zhang and Suen thinning algorithm, described here:  T. Y. Zhang and C. Y. Suen  "A fast parallel algorithm for thinning digital patterns",  Commun. ACM,  vol. 27,  pp.236 -239 1984	
	 * @param Roi
	 * @return
	 */
	public static ImageProcessor skeletonize(Roi roi) {
		ImageProcessor mask = new ByteProcessor(roi.getMask(), false);
		Rectangle r =  roi.getBounds();
		int step = 0;
		
		LinkedList<VPoint> toBeDeleted;
		
//		ImageStack stack = new ImageStack(mask.getWidth(), mask.getHeight());
		
		do {
			
//			ImageProcessor copy = new ByteProcessor(mask, false);
			
			
//			stack.addSlice(copy);
			
			// mark
			toBeDeleted =  new  LinkedList<VPoint>();
			for (int y = r.y; y < r.y + r.height; y++) {
				for (int x= r.x; x< r.x + r.width; x++) {				
					if (mask.getPixel(x - r.x, y - r.y) != 0) {						
						int i;
						for (i=0; i < n8.length; ++i) {
							if (mask.getPixel(x + n8[i].x -r.x, y + n8[i].y - r.y) == 0
								/*|| !r.contains(x + n8[i].x, y + n8[i].y)*/)
								break;
						}
						if (i < n8.length) { // it IS a contour pixel
							int N = 0, T = 0, prev = -1;
							for (i=0; i < n8.length; ++i) {
								if (   mask.getPixel(x + n8[i].x - r.x, y + n8[i].y - r.y) > 0
									/*|| !r.contains(x + n8[i].x, y + n8[i].y)*/) {
									++N;	
									if (prev == 0) ++T;
									prev = 1;
								} else {
									prev = 0;
								}														
							}
							// p9 -> p2?
							if (   mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0
									/*|| !r.contains(x + n8[0].x, y + n8[0].y)*/) {
								if (prev == 0) ++T;
							}
							int c, d;
							if (step == 0) {
								c =  ((mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[2].x - r.x, y + n8[2].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[4].x - r.x, y + n8[4].y - r.y) > 0) ? 1: 0);
								d =  ((mask.getPixel(x + n8[2].x - r.x, y + n8[2].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[4].x - r.x, y + n8[4].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[6].x - r.x, y + n8[6].y - r.y) > 0) ? 1: 0);								
							} else {
								c =  ((mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[2].x - r.x, y + n8[2].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[6].x - r.x, y + n8[6].y - r.y) > 0) ? 1 : 0);
								d =  ((mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[4].x - r.x, y + n8[4].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[6].x - r.x, y + n8[6].y - r.y) > 0) ? 1 : 0);		
							}
							
//							log.debug("(" + x + ", " + y + ") step = " + step +  "   N=" + N + " T = " + T + " c = " + c + " d = " + d );
	 						
							if (   (N >=2 && N <=6) // (a)  
								&& (T == 1)         // (b)
								&& (c == 0)
								&& (d == 0)
								) {  
								// mark for deletion
//								log.debug("Add to TBD " + x + ", " + y );
	 							toBeDeleted.add(new VPoint(x, y));
							} 							
						}
					}
				}
			}			
//			log.debug("to be deleted = " + toBeDeleted.size());
			// delete
			for (VPoint p : toBeDeleted) {
				mask.putPixel(p.x - r.x, p.y - r.y, 0);
			}
			step = 1 - step;
		} while (toBeDeleted.size() > 0);
		
//		ImagePlus impMask = new ImagePlus("skeleton", stack);			
//		impMask.show();
		
		return mask;
		
	}
	
	/**
	 * Implementation of Zhang and Suen thinning algorithm, described here:  T. Y. Zhang and C. Y. Suen  "A fast parallel algorithm for thinning digital patterns",  Commun. ACM,  vol. 27,  pp.236 -239 1984	
	 * @param mask
	 * @return
	 */
	public static ImageProcessor skeletonize(ByteProcessor mask) {
		
		Rectangle r =  new Rectangle(0, 0, mask.getWidth(), mask.getHeight());
		int step = 0;
		
		LinkedList<VPoint> toBeDeleted;
		
//		ImageStack stack = new ImageStack(mask.getWidth(), mask.getHeight());
		
		do {
			
//			ImageProcessor copy = new ByteProcessor(mask, false);
			
			
//			stack.addSlice(copy);
			
			// mark
			toBeDeleted =  new  LinkedList<VPoint>();
			for (int y = r.y; y < r.y + r.height; y++) {
				for (int x= r.x; x< r.x + r.width; x++) {				
					if (mask.getPixel(x - r.x, y - r.y) != 0) {						
						int i;
						for (i=0; i < n8.length; ++i) {
							if (mask.getPixel(x + n8[i].x -r.x, y + n8[i].y - r.y) == 0
								/*|| !r.contains(x + n8[i].x, y + n8[i].y)*/)
								break;
						}
						if (i < n8.length) { // it IS a contour pixel
							int N = 0, T = 0, prev = -1;
							for (i=0; i < n8.length; ++i) {
								if (   mask.getPixel(x + n8[i].x - r.x, y + n8[i].y - r.y) > 0
									/*|| !r.contains(x + n8[i].x, y + n8[i].y)*/) {
									++N;	
									if (prev == 0) ++T;
									prev = 1;
								} else {
									prev = 0;
								}														
							}
							// p9 -> p2?
							if (   mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0
									/*|| !r.contains(x + n8[0].x, y + n8[0].y)*/) {
								if (prev == 0) ++T;
							}
							int c, d;
							if (step == 0) {
								c =  ((mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[2].x - r.x, y + n8[2].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[4].x - r.x, y + n8[4].y - r.y) > 0) ? 1: 0);
								d =  ((mask.getPixel(x + n8[2].x - r.x, y + n8[2].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[4].x - r.x, y + n8[4].y - r.y) > 0) ? 1: 0) *
									   ((mask.getPixel(x + n8[6].x - r.x, y + n8[6].y - r.y) > 0) ? 1: 0);								
							} else {
								c =  ((mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[2].x - r.x, y + n8[2].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[6].x - r.x, y + n8[6].y - r.y) > 0) ? 1 : 0);
								d =  ((mask.getPixel(x + n8[0].x - r.x, y + n8[0].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[4].x - r.x, y + n8[4].y - r.y) > 0) ? 1 : 0) *
									   ((mask.getPixel(x + n8[6].x - r.x, y + n8[6].y - r.y) > 0) ? 1 : 0);		
							}
							
//							log.debug("(" + x + ", " + y + ") step = " + step +  "   N=" + N + " T = " + T + " c = " + c + " d = " + d );
	 						
							if (   (N >=2 && N <=6) // (a)  
								&& (T == 1)         // (b)
								&& (c == 0)
								&& (d == 0)
								) {  
								// mark for deletion
//								log.debug("Add to TBD " + x + ", " + y );
	 							toBeDeleted.add(new VPoint(x, y));
							} 							
						}
					}
				}
			}			
//			log.debug("to be deleted = " + toBeDeleted.size());
			// delete
			for (VPoint p : toBeDeleted) {
				mask.putPixel(p.x - r.x, p.y - r.y, 0);
			}
			step = 1 - step;
		} while (toBeDeleted.size() > 0);
		
//		ImagePlus impMask = new ImagePlus("skeleton", stack);			
//		impMask.show();
		
		return mask;
		
	}	
	
	
	public static void removeSingleDots(ImageProcessor sobel) {
		
		LinkedList<VPoint> toBeDeleted =  new  LinkedList<VPoint>();
		for (int y = 0; y < sobel.getHeight(); y++) {
			for (int x=0; x< sobel.getWidth(); x++) {	
				if (sobel.getPixel(x, y) != 0) {
					int N = 0;
					for (int i=0; i < n8.length; ++i) {
						if (sobel.getPixel(x + n8[i].x, y + n8[i].y) == 0)
							N++;						
					}
					if (N == 8) toBeDeleted.add(new VPoint(x, y));	
				}	
			}
		}
		
		for (VPoint p : toBeDeleted) {
			sobel.putPixel(p.x, p.y, 0);
		}			
		
	}	
	
	
	
//	private static Color arrayToColor(int[] rgbi) {
//		Color c = null;
//		if (rgbi.length == 1)			
//			c =  new Color(rgbi[0], 0, 0);
//		else if (rgbi.length == 2)			
//			c = new Color(rgbi[0], rgbi[1], 0);
//		else if (rgbi.length >= 3)			
//			c = new Color(rgbi[0], rgbi[1], rgbi[2]);
//		return c;
//	}
	
	
//	private static void getHistograms2(ImagePlus imp, Roi roi, Histograms h) throws Exception {		
//		
//		if (!roi.isArea())
//			throw new Exception ("roi not area (for histogram)");
//		
//		ImageProcessor ip = imp.getProcessor();
//		ImageProcessor mask = roi.getMask();
//		Rectangle r = roi!=null?roi.getBounds():new Rectangle(0,0,ip.getWidth(),ip.getHeight());
//		
//		for (int y = r.y; y < r.y + r.height; y++) {			
//			for (int x= r.x; x< r.x + r.width; x++) {				
//				if (mask.getPixel(x - r.x, y - r.y) != 0) {
//					int[] rgbi = imp.getPixel(x, y);
//					h.processColor(rgbi);					
//				} else {
//				}
//			}
//		}
//	}
	
	public static void main(String[] args) {
    	S.out(colToValue(Color.blue));
    	S.out(colToValue(Color.green));
    	S.out(colToValue(Color.red));
    	testPolygonRoi();
    }

	

	
}
