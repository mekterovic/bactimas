package bactimas.gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.frame.BridgeToRoiManager;

import java.util.List;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.gui.events.FrameManager;
import bactimas.gui.events.IFrameListener;

public class SelectedFrameStrip implements IFrameListener {
	private static int prevFrameNo = -1;
	private static ImagePlus activeImageCRGB; // = new ImagePlus[4];	
	static Logger log = Logger.getLogger("bactimas.gui.SelectedFrameStrip");
	public SelectedFrameStrip(ImageStrip imageStrip) {
		super();
		FrameManager.addFrameListener(this);
	}


	public void frameSelected(int frameNo) {
		
		if (prevFrameNo != frameNo) {
			
			log.debug("Switching active window(s) from " + prevFrameNo + " to " + frameNo);
			
			for (int i=0; i<4; ++i) {
				if (activeImageCRGB != null) {
					WindowManager.removeWindow(activeImageCRGB.getWindow());
					activeImageCRGB.changes = false;
					activeImageCRGB.close();
				}
			}
			
			prevFrameNo = frameNo;					
			
			ImagePlus red   = CurrentExperiment.getRedImagePlus(frameNo, null);
			ImagePlus blue  = CurrentExperiment.getBlueImagePlus(frameNo, null);
			ImagePlus green = CurrentExperiment.getGreenImagePlus(frameNo, null);
			
			ImageStack redStack = new ImageStack(red.getProcessor().getWidth(), red.getProcessor().getHeight());
			redStack.addSlice(red.getProcessor());
			
			ImageStack blueStack = new ImageStack(blue.getProcessor().getWidth(), blue.getProcessor().getHeight());
			blueStack.addSlice(blue.getProcessor());
			
			ImageStack greenStack = new ImageStack(green.getProcessor().getWidth(), green.getProcessor().getHeight());
			greenStack.addSlice(green.getProcessor());
			
			
			ImageStack allCombStack =  new ImageStack(blue.getProcessor().getWidth(), blue.getProcessor().getHeight());
			ImageStack composite;
			
			// RB:
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, redStack, null, blueStack, true);
			allCombStack.addSlice("Red+Blue", composite.getProcessor(1));
			// RGB:
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, redStack, greenStack, blueStack, true);
			allCombStack.addSlice("Red+Green+Blue", composite.getProcessor(1));
			// RG:
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, redStack, greenStack, null, true);
			allCombStack.addSlice("Red+Green", composite.getProcessor(1));
			// BG:
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, null, greenStack, blueStack, true);
			allCombStack.addSlice("Blue+Green", composite.getProcessor(1));
			
			// I'm "merging" only to convert 8-bit to RGB, I know, I know...
			// R:			
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, redStack, null, null, false);  // delete stack (keep=false)
			allCombStack.addSlice("Red", composite.getProcessor(1));
			// G:			
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, null, greenStack, null, false);  // delete stack (keep=false)
			allCombStack.addSlice("Green", composite.getProcessor(1));
			// B:			
			composite = (new ij.plugin.RGBStackMerge()).mergeStacks(red.getProcessor().getWidth(), red.getProcessor().getHeight(), 1, null, null, blueStack, false);  // delete stack (keep=false)
			allCombStack.addSlice("Blue", composite.getProcessor(1));

			
			activeImageCRGB = new ImagePlus("All comb.stack (RB, RGB, RG, BG, R, G, B)", allCombStack);			
			activeImageCRGB.show();
			activeImageCRGB.getWindow().setLocation(activeImageCRGB.getProcessor().getWidth(), 10);
			
			
			
			
			
			//firstImageCRGB[0].getWindow().setAlwaysOnTop(true);
			// It adds it self:
//			WindowManager.addWindow(activeImageCRGB[0].getWindow());
			
			
			/*
			ImageStack imstFirstImage = new ImageStack(activeImageCRGB[0].getProcessor().getWidth(), activeImageCRGB[0].getProcessor().getHeight());
			imstFirstImage.addSlice("RGB" + activeImageCRGB[0].getTitle(), activeImageCRGB[0].getProcessor());
			
			if ( activeImageCRGB[0].getProcessor() instanceof ij.process.ColorProcessor) {
				
				ImageStack firstImageStack[] = ChannelSplitter.splitRGB(imstFirstImage, true);
				String[] RGB = new String[] {"Red", "Green", "Blue"};
				for (int i=0; i<3; ++i) {
					activeImageCRGB[1+i] = new ImagePlus(RGB[i] + ":" + activeImageCRGB[0].getTitle(), firstImageStack[i].getProcessor(1));
					activeImageCRGB[1+i].show();
					activeImageCRGB[1+i].getWindow().setLocation(i * activeImageCRGB[0].getWindow().getWidth(), 20 + activeImageCRGB[0].getWindow().getHeight());
					//firstImageCRGB[1+i].getWindow().setAlwaysOnTop(false);
					// It adds it self:
	//				WindowManager.addWindow(activeImageCRGB[1+i].getWindow());
				}		
			}
			*/
		}
	}

	
	public void frameSelected(Object source, int frameNo, List<Roi> rois) {		
		frameSelected(frameNo);
		
		if (rois != null) {
			BridgeToRoiManager.addRoisToAll(rois);
			/*
			for (Roi roi : rois) {
				if (roi != null) 
					showRoiOnAllFrames(roi);
			}
			*/
		}
	}
	
//	private void showRoiOnAllFrames(Roi roi) {
//		int[] ids = WindowManager.getIDList();
//		int i=0;
//		while (ids != null && i < ids.length) {
//			WindowManager.getImage(ids[i]).setRoi(roi);
//			
//			i++;
//		}
//		
//	}	
}
