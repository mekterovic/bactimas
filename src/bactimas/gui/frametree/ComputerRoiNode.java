package bactimas.gui.frametree;

import ij.gui.Roi;

import javax.swing.ImageIcon;

import bactimas.datamodel.CurrentExperiment;

public class ComputerRoiNode extends RoiNode implements ICanRender {
	
	private static ImageIcon _icon = new javax.swing.ImageIcon(FrameNode.class.getResource("/bactimas/gui/icons/computer.png"));


	public ComputerRoiNode(BacteriaNode parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Comp:";
	}

	@Override
	public Roi getRoi() {
		return CurrentExperiment.getComputerRoiForBacteria(_parent.getBacteria().getIdBacteria(), _parent.getParent().getFrameNo());	
	}

	@Override
	public String getRenderString() {
		return toString();
	}

	@Override
	public ImageIcon getIcon() {
		return _icon;
	}
	

}
