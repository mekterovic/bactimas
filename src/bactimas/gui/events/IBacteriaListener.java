package bactimas.gui.events;

import bactimas.gui.frametree.BacteriaNode;
import ij.gui.Roi;

public interface IBacteriaListener {
	public void bacteriaSelected(BacteriaNode bn);
	public void bacteriaSetROI(BacteriaNode bn, Roi roi);
}
