package bactimas.gui.frametree;

import ij.gui.Roi;

import javax.swing.ImageIcon;

import bactimas.datamodel.CurrentExperiment;

public class HumanRoiNode extends RoiNode implements ICanRender {
	
	private static ImageIcon _icon = new javax.swing.ImageIcon(FrameNode.class.getResource("/bactimas/gui/icons/human.png"));

	public HumanRoiNode(BacteriaNode parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Humn:";
	}

	@Override
	public Roi getRoi() {		
		return CurrentExperiment.getHumanRoiForBacteria(_parent.getBacteria().getIdBacteria(), _parent.getParent().getFrameNo());		
	}

	@Override
	public String getRenderString() {		
		return "<html><b>" + toString() + "</b></html>";
	}

	@Override
	public ImageIcon getIcon() {		
		return _icon;
	}
	

	

}
