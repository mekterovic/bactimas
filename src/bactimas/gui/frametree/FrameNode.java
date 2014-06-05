package bactimas.gui.frametree;

import java.text.DecimalFormat;

import javax.swing.ImageIcon;

import bactimas.datamodel.CurrentExperiment;
import bactimas.db.beans.ExperimentEvent;
import bactimas.db.beans.Frame;

public class FrameNode implements ITooltip, ICanRender {

	Frame _frame;
	ExperimentEvent _event;
	private static ImageIcon _icon = new javax.swing.ImageIcon(FrameNode.class.getResource("/bactimas/gui/icons/frame_node_icon.png"));
	
	FrameNode(Frame frame) {		
		_frame = frame;
	}
	public String toString() {
		DecimalFormat df = new DecimalFormat("0.0");
		return "f" + _frame.getFrameNo() + 
				" t=(" + _frame.getTransX() + ", " + _frame.getTransY() + ")" + 
				" bgRGB=(" + df.format(_frame.getBgRedMean()) + " " + df.format(_frame.getBgGreenMean()) + " " + df.format(_frame.getBgBlueMean()) + ")"  
				+ ((_event == null) ? "": "  " + _event.getEventAbbr())
				+ (_frame.getAlgorithm() != null ? " " + CurrentExperiment.getAlgorithm(_frame.getAlgorithm()).getAbbrev() : "");
	}


	public String getTooltip() {
		return _frame.getFrameBlueFileName()
				+ ((_event == null) ? "": _event.getEventDesc());
	}
	
	public int getFrameNo() {
		return _frame.getFrameNo();		
	}
	
	public String getRenderString() {
		if (_frame.isIgnored()) {
			return "<html><b>IGNORED:&nbsp;</b><font color=\"gray\"><i>" + toString() + "</i></font></html>";
		} else if (_event == null){
			return toString();
		} else {
			return "<html><b>" + toString() + "</b></html>";
		}
	} 
	
	public void setEvent (ExperimentEvent event) {
		_event = event;
	}
	
	public ImageIcon getIcon() {	
		return _icon;
	}
}
