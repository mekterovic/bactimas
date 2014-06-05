package bactimas.datamodel;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.Opener;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.plugin.filter.Analyzer;
import ij.process.ImageConverter;
import ij.text.TextWindow;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import bactimas.alg.ITrackingAlgorithm;
import bactimas.db.DALService;
import bactimas.db.beans.BTreeElement;
import bactimas.db.beans.Bacteria;
import bactimas.db.beans.BacteriaMeasurement;
import bactimas.db.beans.BacteriaSplit;
import bactimas.db.beans.BacteriaState;
import bactimas.db.beans.BacteriaStateChange;
import bactimas.db.beans.Experiment;
import bactimas.db.beans.ExperimentEvent;
import bactimas.db.beans.ExperimentMeasurements;
import bactimas.db.beans.Frame;
import bactimas.gui.ControlPanel;
import bactimas.gui.ImageStrip.ImageStripType;
import bactimas.gui.frametree.BacteriaKeyValue;
import bactimas.util.S;

public class CurrentExperiment {
	static Logger log = Logger.getLogger("bactimas.datamodel.CurrentExperiment" );
	private static Experiment _experiment;
	private static ArrayList<Frame> _frames;
	
	private static ArrayList<AlgorithmDesc> _trackingAlgorithmsDescs;
	private static HashMap<String, ITrackingAlgorithm> _trackingAlgorithmsMap;
	private static AlgorithmDesc defaultAlgorithmDesc;
	
	public static ITrackingAlgorithm getDefaultAlgorithm () {
		if (defaultAlgorithmDesc == null) {
			getAllAlgorithms();
		}
		return getAlgorithm(defaultAlgorithmDesc.getClassName());
	}
	public static ITrackingAlgorithm getAlgorithmForFrame (int frameNo) {
		if (getFrame(frameNo).getAlgorithm() == null) {
			return getDefaultAlgorithm();
		} else {
			return getAlgorithm(getFrame(frameNo).getAlgorithm());
		}		
	}
	
	public static ITrackingAlgorithm getAlgorithm (String algorithmClassName) {
		if (_trackingAlgorithmsMap == null) {
			_trackingAlgorithmsMap = new HashMap<String, ITrackingAlgorithm> ();			
		}
		if (_trackingAlgorithmsMap.get(algorithmClassName) == null) {
			Object alg;
			try {
				alg = Class.forName(algorithmClassName).newInstance();
				if (alg instanceof ITrackingAlgorithm) {
					ITrackingAlgorithm instance = (ITrackingAlgorithm) alg;
					_trackingAlgorithmsMap.put(algorithmClassName, instance);
					return instance;
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
			}
			
		} else {
			return _trackingAlgorithmsMap.get(algorithmClassName);
		}
		return null;
	}	
	public static  AlgorithmDesc[] getAllAlgorithms () {
		if (_trackingAlgorithmsDescs == null) {
			try {
				_trackingAlgorithmsDescs = new ArrayList<AlgorithmDesc>();
				FileInputStream fstream = new FileInputStream(S.getAllAlgorithmsAbsFileName());
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				boolean first = true;
				while ((strLine = br.readLine()) != null && !strLine.trim().equals("")) {
					Object alg = Class.forName(strLine.trim()).newInstance();
					if (alg instanceof ITrackingAlgorithm) {
						ITrackingAlgorithm instance = (ITrackingAlgorithm) alg; 
						if (first ) {
							defaultAlgorithmDesc = new AlgorithmDesc(strLine.trim(), instance.getName(), true);
							_trackingAlgorithmsDescs.add(defaultAlgorithmDesc);
						} else {
							_trackingAlgorithmsDescs.add(new AlgorithmDesc(strLine.trim(), instance.getName(), false));	
						}												
						first = false;	
					} else {
						log.error("WTF: " + strLine + " is not ITrackingAlgorithm.");
					}					
				}
				in.close();
			} catch (Exception e) {
				log.error(e);				
			}		
		} 
		AlgorithmDesc[] arr = new AlgorithmDesc[_trackingAlgorithmsDescs.size()];
		return (AlgorithmDesc[]) _trackingAlgorithmsDescs.toArray(arr);		
	}
	
	
	
	
	
	public static int getIdExperiment() {
		return (_experiment == null) ? -1: _experiment.getIdExperiment();
	}
	
	public static Experiment getExperiment() {
		return _experiment;
	}	
	public static boolean hasData () {
		return (_experiment != null);
	}
	
	public static void dumpCSV(File csvFile) {		
		DALService.dumpCSV(getIdExperiment(), csvFile);			  
	}
	
    public static boolean setExperimentEvent (int frameNo, String eventAbbr, String eventDesc ) {
    	return DALService.setExperimentEvent(getIdExperiment(), frameNo, eventAbbr, eventDesc);
    }
    public static BacteriaState[] getAllBacteriaStates() {
    	return DALService.getAllBacteriaStates();
    }
    public static boolean setStateChange(int idBacteria, int frameNo, int idState) {
    	return DALService.setStateChange(idBacteria, frameNo, idState);
    }
    
	/**
	 * Loads the experiment with the given name or creates a new one.
	 * @param expName
	 * @param redFolder
	 * @param greenFolder
	 * @param blueFolder
	 * @param spf
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param pictureScale
	 * @throws Exception
	 */
	public static void beginExperiment 	(String expName, String redFolder, String greenFolder, String blueFolder, int spf, BigDecimal pixelWidth, BigDecimal pixelHeight, BigDecimal pictureScale) throws Exception {
		_experiment = DALService.beginExperiment(expName, redFolder,  greenFolder,  blueFolder, spf, pixelWidth, pixelHeight, pictureScale);
		_frames = DALService.initFrames(redFolder, greenFolder, blueFolder);
	}	
	
	
	public static void updateCurrentExperiment 	(String expName, String redFolder, String greenFolder, String blueFolder, int spf, BigDecimal pixelWidth, BigDecimal pixelHeight, BigDecimal pictureScale) throws Exception {
		_experiment = DALService.updateExperiment(expName, redFolder,  greenFolder,  blueFolder, spf, pixelWidth, pixelHeight, pictureScale, getIdExperiment());
		_frames = DALService.initFrames(redFolder, greenFolder, blueFolder);
	}	
	
	
	public static boolean deleteExperiment(int idExperiment) {
		return DALService.deleteExperiment(idExperiment);
	}
	
	public static void reloadFrames	() throws Exception {		
		_frames = DALService.initFrames(
					getExperiment().getRedMovieFileName(), 
					getExperiment().getGreenMovieFileName(),
					getExperiment().getBlueMovieFileName()
				);
	}	
	public static int getFrameCount() {
		return (_frames == null) ? 0: _frames.size();
	}
	
	
	/**
	 * Returns a frame for the five frameNo (STARTING WITH 1)!!
	 * @param frameNo
	 * @return the frame
	 */
	
	public static Frame getFrame(int frameNo) {		
		return _frames.get(frameNo - 1);
	}
	
	public static LinkedList<Bacteria> getBacteriasForFrame(int frameNo) {
		// I'm not cacheing anything for the time being.
		if (getFrame(frameNo).isIgnored()) {
			return new LinkedList<Bacteria>();
		} else {
			return DALService.getBacteriasForFrame(getFrame(frameNo));	
		}						
	}
	
	public static int getPrevFrameNo(int frameNo) {
		int curr = frameNo;
		while (curr > 1) {
			--curr;
			if (!getFrame(curr).isIgnored()) return curr;
		}
		return frameNo;
	}
	
	public static int getSecondsPerFrame() {
		// I'm not cacheing anything for the time being.
		return DALService.getSecondsPerFrame(getIdExperiment());				
	}	

	public static LinkedList<Bacteria> getSplitBacteriasForFrame(int frameNo) {
		// I'm not cacheing anything for the time being.
		return DALService.getSplitBacteriasForFrame(getFrame(frameNo));				
	}	
	
	public static boolean hasHumanRoi (int frameNo, int idBacteria) {
	
		return DALService.hasHumanRoi(idBacteria, frameNo);
	}
	
	
	public static Hashtable<String, BacteriaKeyValue> getMeasurements (int idBacteria, int frameNo, int roiType) {
		LinkedList<BacteriaMeasurement> list = DALService.getBacteriaMeasurementsForFrame(idBacteria, frameNo, roiType);
		Hashtable<String, BacteriaKeyValue> rv = new Hashtable<String, BacteriaKeyValue>();
		for (BacteriaMeasurement item : list) {
			rv.put(item.getMeasurementName(), new BacteriaKeyValue(item.getMeasurementName(), item.getValue()));
		}
		return rv;
	}
	
	public static boolean saveROI(ij.gui.Roi roi, int frameNo, Bacteria b, int roiType) {
			roi.setName(b.getBactName() + "_" + frameNo + ((roiType == bactimas.db.beans.Roi.ROI_TYPE_COMPUTER) ? "c" : "h"));

			try {
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				RoiEncoder encoder = new RoiEncoder(bStream);
				encoder.write(roi);
				return  DALService.updateRoiForBacteria(
							new bactimas.db.beans.Roi(
									b.getIdBacteria(),  
									frameNo, 
									bStream.toByteArray(), 
									roi.getName(), 
									roiType)
						);				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
			
	}
	public static void closeResultsWindow() {
		Analyzer.getResultsTable().reset();
		java.awt.Frame[] arrayOfFrames = WindowManager.getNonImageWindows();
		for (int i = 0; i < arrayOfFrames.length; ++i) {
			String localName = arrayOfFrames[i].getTitle();
			if (localName.equalsIgnoreCase("Results")) {
				TextWindow tw = (TextWindow) arrayOfFrames[i];
				tw.close();
			}
		}
	}	

	public static boolean updateFrameAlgorithm(int frameFrom, int frameTo, String algorithmName) {
		if (!DALService.updateFrameAlgorithm( getIdExperiment(), frameFrom, frameTo, algorithmName)) {
			ControlPanel.addStatusMessage("Error saving algorithm for frames " + frameFrom + " - " + frameTo + ".");
			return false;
		}		
		return true;
	}	
	
	public static boolean updateFrameBackgroundGreenMean(int frameNo, double mean) {
		if (!DALService.updateFrameBackgroundGreenMean( getIdExperiment(), frameNo, mean)) {
			ControlPanel.addStatusMessage("Error:green bg mean wasn't updated for frame " + frameNo);
			return false;
		}		
		return true;
	}
	public static boolean updateFrameBackgroundRedMean(int frameNo, double mean) {
		if (!DALService.updateFrameBackgroundRedMean( getIdExperiment(), frameNo, mean)) {
			ControlPanel.addStatusMessage("Error: red bg mean wasn't updated for frame " + frameNo);
			return false;
		}		
		return true;
	}
	public static boolean updateFrameBackgroundRGBMean(int frameNo, double redMean, double greenMean, double blueMean) {
		if (!DALService.updateFrameBackgroundRGBMean( getIdExperiment(), frameNo, redMean, greenMean, blueMean)) {
			ControlPanel.addStatusMessage("Error: RGB bg mean wasn't updated for frame " + frameNo);
			return false;
		}		
		return true;
	}	
	
	public static void updateExperimentMeasures(Hashtable<String, ExperimentMeasurements> measures) {
		DALService.updateExperimentMeasures(measures, getIdExperiment());		
	}
	public static Hashtable<String, ExperimentMeasurements> getExperimentMeasures() {
		return DALService.getExperimentMeasures(getIdExperiment());		
	}	
	
	public static ExperimentEvent[] getAllEvents() {
		return DALService.getAllEvents(getIdExperiment());		
	}	
	public static BacteriaStateChange[] getAllBacteriaStateChanges() {
		return DALService.getAllBacteriaStateChanges(getIdExperiment());		
	}	
	public static void saveMeasurements(LinkedList<BacteriaMeasurement> measurements) {
		
//		log.debug("Saving Measurements for idBacteria = " + idBacteria +  " frameNo = " + frameNo + " idRoiType = " + idRoiType);
		boolean result = DALService.insertBacteriaMeasurements(measurements);
		if (!result) {
			log.error("Error saving measurements.");
			ControlPanel.addStatusMessage("Error saving measurements.");
		}				
	}
	public static String getBacteriaStateAt (Bacteria b, int frameNo) {
		return DALService.getBacteriaStateAt(b, frameNo);
	}
	public static void toggleIgnoreFrame(int frameNo) {
		

		boolean result = DALService.toggleIgnoreFrame(getIdExperiment(), frameNo);
		
		if (!result) {
			log.error("Error toggleIgnoreFrame.");
			ControlPanel.addStatusMessage("Error toggle ignore frame = " + frameNo );
		}				
	}	
	
	public static void deleteAllMeasurementsToBeMeasured() {
		
		log.debug("Deleting ALL measurements for idExperiment = " + getIdExperiment());
		boolean result = DALService.deleteAllMeasurementsToBeMeasured(getIdExperiment());
		if (!result) {
			log.error("Error deleting measurements.");
			ControlPanel.addStatusMessage("Error deleting measurements.");
		}
		
		
	}
	
	public static LinkedList<Color> getPalette(int idPalette) {
		return DALService.getPalette(idPalette);
	}

	public static LinkedList<BacteriaSplit> getBacteriaFamilySplits (Bacteria root) {
		return DALService.getBacteriaFamilySplits(getIdExperiment(), root);
	}
	public static LinkedList<BTreeElement> getBacteriaBTreeElements (Bacteria root, String onWidthSQL, String onColorSQL) {
		return DALService.getBacteriaBTreeElements(getIdExperiment(), root, onWidthSQL, onColorSQL);
	}

	
//	public static boolean materializeMeasurementsa () {
//		return DALService.materializeVMeasurements(getIdExperiment());
//	}
	private static ImagePlus getImageForPath(String absolutePathName, int frameNo) {
		try {
			absolutePathName = S.fixPath(absolutePathName);
			
			log.debug("getImageForPath: loading " + absolutePathName);
			
			ImagePlus ip = (new Opener()).openImage(absolutePathName);
		    ip.setTitle("Frame:" + frameNo);
			//return new ImagePlus(, ImageIO.read(new File(absolutePathName)));
		    return ip;
		} catch (Exception e) {
			log.error("getImageForPath absolutePathName = " + absolutePathName, e);
			e.printStackTrace();
		}
		return null;		
	}
	public static ImagePlus getRedImagePlus(int frameNo, String altFormat) {
		return getImageForPath(getRedFrameAbsFilename(frameNo, altFormat), frameNo);		
	}	
	public static ImagePlus getGreenImagePlus(int frameNo, String altFormat) {
		return getImageForPath(getGreenFrameAbsFilename(frameNo, altFormat), frameNo);		
	}
	public static ImagePlus getBlueImagePlus(int frameNo, String altFormat) {
		return getImageForPath(getBlueFrameAbsFilename(frameNo, altFormat), frameNo);		
	}	
	
	public static ImagePlus getImagePlus(ImageStripType channel, int frameNo, String altFormat) {
		return getImageForPath(getFrameAbsFilename(channel, frameNo, altFormat), frameNo);		
	}
	
	
	private static String swapExtension (String fileName, String newExtension) {
		return fileName.substring(0, fileName.lastIndexOf(".")) + "." + newExtension;
	}
	
	public static String getBlueFrameAbsFilename(int frameNo, String altFormat) {
		String s;
		if (altFormat == null) {
			s = S.getAbsFromRelFolder(getExperiment().getBlueMovieFileName() + java.io.File.separatorChar + getFrame(frameNo).getFrameBlueFileName());	
		} else {			
			s = S.getAbsFromRelFolder(getExperiment().getBlueMovieFileName() + java.io.File.separatorChar + swapExtension(getFrame(frameNo).getFrameBlueFileName(), altFormat));
		}		
		return S.fixPath(s);
	}
	public static String getGreenFrameAbsFilename(int frameNo, String altFormat) {
		String s;
		if (altFormat == null) {
			s = S.getAbsFromRelFolder(getExperiment().getGreenMovieFileName() + java.io.File.separatorChar + getFrame(frameNo).getFrameGreenFileName()); 		
		} else {			
			s = S.getAbsFromRelFolder(getExperiment().getGreenMovieFileName() + java.io.File.separatorChar + swapExtension(getFrame(frameNo).getFrameGreenFileName(), altFormat));
		}		
		return S.fixPath(s);	
	}
	public static String getRedFrameAbsFilename(int frameNo, String altFormat) {
		String s;
		if (altFormat == null) {
			s = S.getAbsFromRelFolder(getExperiment().getRedMovieFileName() + java.io.File.separatorChar + getFrame(frameNo).getFrameRedFileName());
		} else {			
			s = S.getAbsFromRelFolder(getExperiment().getRedMovieFileName() + java.io.File.separatorChar + swapExtension(getFrame(frameNo).getFrameRedFileName(), altFormat));
		}		
		return S.fixPath(s);
	}	
	
	public static String getFrameAbsFilename(ImageStripType channel, int frameNo, String altFormat) {
		if (channel == ImageStripType.RED) { 
			return getRedFrameAbsFilename(frameNo, altFormat);
		} else if (channel == ImageStripType.GREEN) {
			return getGreenFrameAbsFilename(frameNo, altFormat);
		} else if (channel == ImageStripType.BLUE) {
			return getBlueFrameAbsFilename(frameNo, altFormat);
		}		
		throw new RuntimeException("Unknown channel.");
	}	
	
	
//	public static ImagePlus getNewBlueImagePlusWindow (int frameNo) {
//		try {			
//			return new ImagePlus(
//					  "Frame:" + frameNo + " File:" + CurrentExperiment.getFrame(frameNo).getFrameBlueFileName()
//					, ImageIO.read(new File(CurrentExperiment.getFrame(frameNo).getFrameBlueFileName()))
//				);									
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			return null;
//		}					
//	}	
//	public static ImagePlus getNewGreenImagePlusWindow (int frameNo) {
//		try {			
//			return new ImagePlus(
//					  "Frame:" + frameNo + " File:" + CurrentExperiment.getFrame(frameNo).getFrameGreenFileName()
//					, ImageIO.read(new File(CurrentExperiment.getFrame(frameNo).getFrameGreenFileName()))
//				);									
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			return null;
//		}					
//	}		
//	public static ImagePlus getNewDbgColorImagePlusWindow2 (int frameNo) {
//		try {			
//			return new ImagePlus(
//					  "Frame:" + frameNo + " File:" + CurrentExperiment.getFrame(frameNo).getFrameBlueFileName().replace("blue by frame", "dbg by frame")
//					, ImageIO.read(new File(CurrentExperiment.getFrame(frameNo).getFrameBlueFileName().replace("blue by frame", "dbg by frame")))
//				);									
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			return null;
//		}					
//	}		
	public static ImagePlus getNewColorImagePlusWindow (int frameNo) {
				// return getImageForPath(_frames.get(frameNo).getFrameBlueFileName(), frameNo);
		ImagePlus imp = getBlueImagePlus(frameNo, null);
		ImageConverter ic = new ImageConverter(imp);
		ic.convertToRGB();
		imp.setTitle("Frame:" + frameNo + " File:" + CurrentExperiment.getFrame(frameNo).getFrameBlueFileName());
		return imp;								
							
	}		
	
	
	
	
	
	
	
	
	
	
	
	


	public static Roi getComputerRoiForBacteria (int idBacteria, int frameNo) {
		return decodeRoi(DALService.getRoiForBacteria(idBacteria, frameNo, bactimas.db.beans.Roi.ROI_TYPE_COMPUTER));
	}
	
	public static Roi getHumanRoiForBacteria (int idBacteria, int frameNo) {
		return decodeRoi(DALService.getRoiForBacteria(idBacteria, frameNo, bactimas.db.beans.Roi.ROI_TYPE_HUMAN));
	}	
	
    private static String bNames = "ABCDEFGHIJKLMNOPQRSTUVWYZ";
	public static Bacteria addBacteria(int frameNo) {
		int cnt =  getBacteriasForFrame(frameNo).size();		
		String newName = "" + 
        		bNames.charAt(cnt) +
        		((cnt >= bNames.length()) ? (1 + cnt / bNames.length()) : "");  // append A2, B2, ..., A3
		return DALService.getOrInsertBacteria(newName);
	}
	
	private static Roi decodeRoi(bactimas.db.beans.Roi dbRoi) {

		if (dbRoi != null && dbRoi.getRoiBlob() != null) {
			RoiDecoder decoder;
//			log.debug("DECODING ROINAME=" + dbRoi.getRoiName() + " Length=" + dbRoi.getRoiBlob().length + " Array=" + dbRoi.getRoiBlob());
//			log.debug(byteArrayToString(dbRoi.getRoiBlob()));
			decoder = new RoiDecoder(dbRoi.getRoiBlob(), "" + dbRoi.getRoiName());
			try {
				Roi r = decoder.getRoi();
				return r;
			} catch (IOException e) {
				JOptionPane.showConfirmDialog(null, "Error restoring ROI.", "Info", JOptionPane.INFORMATION_MESSAGE );
				log.error(e);
				e.printStackTrace();
			}
		}
		return null;
			
	}

	
	
}
