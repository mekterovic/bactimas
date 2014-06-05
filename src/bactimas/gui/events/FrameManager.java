package bactimas.gui.events;

import ij.gui.Roi;

import java.util.LinkedList;
import java.util.List;


public class FrameManager {
    private static LinkedList<IFrameListener> frameListeners = new LinkedList<IFrameListener>();
    

    public static void addFrameListener(IFrameListener fl){
    	frameListeners.add(fl);
    }    

	
	public static void fireFrameSelected(Object source, int frameNo, List<Roi> rois) {
		for (IFrameListener fl : frameListeners) {						
			fl.frameSelected(source, frameNo, rois);						
		}
	}
}
