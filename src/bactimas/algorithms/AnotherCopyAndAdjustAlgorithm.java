package bactimas.algorithms;

import ij.gui.ShapeRoi;
import signalprocesser.voronoi.VPoint;

public class AnotherCopyAndAdjustAlgorithm extends CopyAndAdjustAlgorithm {



	@Override
	public String getName() {
		return "Another Copy & Adjust";
	}

	
	@Override
	public String getAbbrev() {
		return "A-C&A";
	}	

	
	@Override
	void checkForSkeletonForks(ShapeRoi roi, VPoint[] oldEndpoints) {}	
	

	
	
	

}
