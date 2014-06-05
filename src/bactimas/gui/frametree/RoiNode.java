package bactimas.gui.frametree;

import ij.gui.Roi;

import java.util.Hashtable;
import java.util.LinkedList;


public abstract class RoiNode implements ITooltip {
	String _measurementsTooltip;
	BacteriaNode _parent;
	Hashtable<String, BacteriaKeyValue>  _measurements ;
	public RoiNode (BacteriaNode parent) {
		_parent = parent;
	}
	
	public String getTooltip() {
		if (_measurements != null) {
			_measurementsTooltip = "<html>";
			for (BacteriaKeyValue kv : _measurements.values()) {
				_measurementsTooltip += "<b>" + kv.getKey() + "</b>:&nbsp;"
						+ kv.getValue() + "<br>";
			}
			_measurementsTooltip += "</html>";
		} else {
			_measurementsTooltip = "???";
		}
		return _measurementsTooltip;
	}
	
	public abstract String toString(); 
		//return _dbBacteria.getBactName() + ((_roi == null) ? " ---" : " ROI" );
	
	
	protected Hashtable<String, BacteriaKeyValue> getMeasurements() { 
		return _measurements;
	} 

	public abstract Roi getRoi();
	
	/**
	 * Human rois are not persisted to db. 
	 */
	protected void setMeasurements (LinkedList<BacteriaKeyValue> measurements ) {
		_measurements = new Hashtable<String, BacteriaKeyValue>(); 
		for (BacteriaKeyValue  kv : measurements) {
			_measurementsTooltip += "<b>" + kv.getKey() + "</b>:&nbsp;" + kv.getValue() + "<br>";
			_measurements.put(kv.getKey(), kv);
		}
		_measurementsTooltip += "</html>";			
	}	

	protected void setMeasurements ( Hashtable<String, BacteriaKeyValue> measurements ) {
		_measurements = measurements;			
	}		
	
}
