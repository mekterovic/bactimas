package ij.plugin.frame;

import ij.WindowManager;
import ij.gui.Roi;

import java.util.List;

import bactimas.util.S;

public class BridgeToRoiManager {
	
	private static RoiManager getRM() {
		RoiManager rm = RoiManager.getInstance();
		if (rm == null) {
			
			Runnable job = new Runnable() {
				@Override
				public void run() {
					new RoiManager();							
				}							
			};
			S.executeInEDTAndWait(job);	
						
			rm = RoiManager.getInstance();
		}
		return rm;
	}
	
	
	public static void showAll () {
		//RoiManager rm = RoiManager.getInstance();
		getRM().showAll(1); // LABELS is 1, dunno why it's private
		getRM().updateShowAll();
	}
	
	
	public static void reset () {
		//RoiManager rm = RoiManager.getInstance();
		getRM().runCommand("reset");
	}
	
	public static void addRoisToAll (List<Roi> rois) {
		//RoiManager rm = RoiManager.getInstance();
		RoiManager rm = getRM();
		rm.runCommand("reset");
		
//		showAll();
		int[] ids = WindowManager.getIDList();
		int i=0;
		while (ids != null && i < ids.length) {			
			WindowManager.setTempCurrentImage(WindowManager.getImage(ids[i]));
			rm.runCommand("show all");
			rm.runCommand("show all with labels");
			rm.runCommand("usenames", "true");
			for (Roi roi : rois) {
				if (roi != null) {
//					Roi r = (Roi) roi.clone();
					Roi r = roi;
//					String t = WindowManager.getImage(ids[i]).getTitle();
//					t = t.substring(0, t.indexOf("File"));
					//r.setName(++j + " - " + r.getName());
					r.setName(r.getName());
					rm.addRoi(r);
					rm.select(rm.getCount()-1);  // assign newly added Roi to curr image
				}
			}	
//			showAll();
			WindowManager.setTempCurrentImage(null);
			i++;
		}		
		
		
	}
	public static void addRoisToCurrent (List<Roi> rois) {
	
		//RoiManager rm = RoiManager.getInstance();
		RoiManager rm = getRM();
		rm.runCommand("reset");
		
		rm.runCommand("show all");
		rm.runCommand("show all with labels");
		rm.runCommand("usenames", "true");
		
		for (Roi roi : rois) {
			if (roi != null) {					
				Roi r = roi;
//				String t = WindowManager.getCurrentImage().getTitle();
				//r.setName(j + " - " + r.getName());
				r.setName(r.getName());
				rm.addRoi(r);
				rm.select(rm.getCount()-1);  // assign newly added Roi to curr image
			}
		}	

				
		
		
	}	
}
