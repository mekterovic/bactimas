package bactimas.gui.events;
import ij.gui.Roi;

import java.util.List;
public interface IFrameListener {
//	public void frameSelectedNoRoi(int frameNo);
//	public void frameSelectedShowRois(int frameNo, List<Roi> rois);
	public void frameSelected(Object source, int frameNo, List<Roi> rois);
}
