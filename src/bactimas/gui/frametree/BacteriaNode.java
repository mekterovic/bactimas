package bactimas.gui.frametree;

import ij.gui.Roi;

import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.db.beans.Bacteria;

public class BacteriaNode  implements ITooltip, ICanRender {
	static Logger log = Logger.getLogger("bactimas.gui.frametree.BacteriaNode" );
	Bacteria _dbBacteria;
	private static ImageIcon _icon = new javax.swing.ImageIcon(FrameNode.class.getResource("/bactimas/gui/icons/bacteria.png"));
	private static ImageIcon _disabledIcon = new javax.swing.ImageIcon(FrameNode.class.getResource("/bactimas/gui/icons/bwbacteria.png"));
//	String _name;
//	Roi _roi;
	String _tooltip;
	String _state;
	RoiNode _hRoi, _cRoi;
	FrameNode _parent;
//	TreeNode _treeNode;
	public BacteriaNode(FrameNode parent, Bacteria dbBacteria) {
		
		_parent = parent;
//		measurementsHuman = new Hashtable<String, BacteriaKeyValue>();
//		measurementsComputer = new Hashtable<String, BacteriaKeyValue>();
		_dbBacteria = dbBacteria;
	}
	
	public RoiNode addOrReplaceHumanRoi () {
		
		_hRoi = new HumanRoiNode(this);

		return _hRoi;
	}
	
	public RoiNode addOrReplaceComputerRoi () {
		
		_cRoi = new ComputerRoiNode(this);

		return _cRoi;
	}
	
	public LinkedList<Roi> getRois() {
		LinkedList<Roi> rns = new LinkedList<Roi>();
		if (_hRoi != null) rns.add(_hRoi.getRoi());
		if (_cRoi != null) rns.add(_cRoi.getRoi());
		return rns;
	} 
	
	public void setState(String state){
		_state = state;
	}
	public String toString() {
		return _dbBacteria.getBactName();
	}
	public Bacteria getBacteria() {
		return _dbBacteria;
	}	
//	public void addMeasurement(String name, double value){
//		measurements.add(new BacteriaKeyValue(name, value));
//	}
//	
	public FrameNode getParent() {
		return _parent;		
	}

	public void setHumanROI(Roi roi) {
		if (roi != null) {
//			if (_parent.getFrameNo() != 1) {
//				JOptionPane.showConfirmDialog(null, "Cannot set ROI for frame > 1 (for the time being...).", "Info", JOptionPane.INFORMATION_MESSAGE );
//				return;
//			}
			if (	CurrentExperiment.hasHumanRoi(getParent().getFrameNo(), _dbBacteria.getIdBacteria()) 
				&& JOptionPane.NO_OPTION  == JOptionPane.showConfirmDialog(null, "Overwrite ROI.", "?", JOptionPane.YES_NO_OPTION )){
				return;
			}
			
			
			if (!CurrentExperiment.saveROI(roi, _parent.getFrameNo(), _dbBacteria, bactimas.db.beans.Roi.ROI_TYPE_HUMAN)) {
				JOptionPane.showConfirmDialog(null, "Error saving roi.", "Info", JOptionPane.INFORMATION_MESSAGE );
			}	
			
//			LinkedList<BacteriaKeyValue> measurements = CurrentExperiment.measureAndSave(roi, _parent.getFrameNo(), _dbBacteria.getIdBacteria(),  bactimas.db.beans.Roi.ROI_TYPE_HUMAN);
//			
//			((HumanRoiNode)_hRoi).setMeasurements(measurements);

//			setMeasurements(measurementsHuman, measurements);

		} else {
			JOptionPane.showConfirmDialog(null, "Cannot set ROI, ROI not set (for current window).", "Info", JOptionPane.INFORMATION_MESSAGE );
		}		
	}
	
//	private void setMeasurements (Hashtable<String, BacteriaKeyValue> hash, LinkedList<BacteriaKeyValue> list) {
//		hash.clear();
//		for (BacteriaKeyValue item : list) {
//			hash.put(item.getKey(), item);
//		}
//		refreshTooltip();
//	}
	
	public String getTooltip() {
		
		try {
			if (_hRoi != null) {
				if (_cRoi != null) {

					String rv = "<html><h1>COMP vs HUMAN</h1><table border=\"1\"><tr><td></td><td>Human</td><td>Computer</td><td>Diff</td><td>%</td></tr>";
					for (BacteriaKeyValue kv : _cRoi.getMeasurements().values()) {
						rv += "<tr><td>" + kv.getKey() + "</td><td>"
								+ kv.getValue() + "</td>";
						double v2 = ((BacteriaKeyValue) _hRoi.getMeasurements()
								.get(kv.getKey())).getValue();
						rv += "<td>" + v2 + "</td><td>" + (-kv.getValue() + v2)
								+ "</td><td>" + (v2 - kv.getValue()) * 100. / v2
								+ "%" + "</td></tr>";
					}
					rv += "</table></html>";
					return rv;

				} else {
					return getSingleTooltip(_hRoi.getMeasurements(), "Human");
				}
			} else if (_cRoi != null) {
				return getSingleTooltip(_cRoi.getMeasurements(), "Computer");
			} else {
				return "No rois :(";
			}
		} catch (NullPointerException e) {
			return "NullPointerException";
		}
	}
	private String getSingleTooltip (Hashtable<String, BacteriaKeyValue> hash, String name) {
		if (hash == null) return " Null?";
		String rv = "<html><h1>" + name + "</h1><br/>" ;
		for (BacteriaKeyValue  kv : hash.values()) {
			rv += "<b>" + kv.getKey() + "</b>:&nbsp;" + kv.getValue() + "<br>";			
		}
		rv += "</html>";			
		return rv;		
	}

	
	public String getRenderString() {
		if (_state == null) {
			return toString();	
		} else {
			return "<html><i>" + toString() + " " + _state +  "</i></html>";
		}
		
	}


	public ImageIcon getIcon() {	
		if (_state == null) {
			return _icon;
		} else {
			return _disabledIcon;
		}		
		
	}
	

}
