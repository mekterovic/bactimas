package bactimas.gui;

import ij.gui.Roi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.gui.events.FrameManager;
import bactimas.gui.events.IFrameListener;

public class ImageStrip implements IFrameListener {
	static Logger log = Logger.getLogger("bactimas.gui.ImageStrip");
	//private File _path;  // TODO not used?
	LinkedList<ImageControl> imageCollection;
	JComponent _imageStrip;
	
	public enum ImageStripType {
	    RED, GREEN, BLUE 
	}
	ImageStripType _type;
	JScrollPane masterScroller;
	private ImageStrip instance;
	private static int SKIP = 2;
	
	ImageStrip ( ImageStripType type) {
		//_path = path;	
		_type = type;
		FrameManager.addFrameListener(this); // That's right, I'm listening to myself. Shut up.
		instance = this;

	}
	
	
	// TODO
    public  ImageStrip getInstance() {
    	return instance;
    }
    
	

	public JComponent getImageStrip() {
		if (_imageStrip == null) {
			JPanel panel  = new JPanel();
			if (_type == ImageStripType.RED) {
				panel.setBackground(Color.RED);
			} else if (_type == ImageStripType.GREEN) {
				panel.setBackground(Color.GREEN);
			} else if (_type == ImageStripType.BLUE) {
				panel.setBackground(Color.BLUE);
			}			
			//panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.setLayout(new FlowLayout());
			imageCollection  = new LinkedList<ImageControl>();
			loadImages(SKIP);  


			
			for (ImageControl ic : imageCollection) {
				panel.add(ic);
				panel.add(Box.createRigidArea(new Dimension(0,5)));
			}			
			masterScroller = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);		
			masterScroller.setAlignmentX(Component.LEFT_ALIGNMENT);	
			
			_imageStrip = masterScroller;
			
			
//			ImageJ.main(new String[] {});
//			new RoiManager();
			
			
		}
		return _imageStrip;
	}

	

    	
	
	private  void loadImages(int skip) {

	
		imageCollection = new LinkedList<ImageControl>();
		for (int i = 1; i <= CurrentExperiment.getFrameCount(); ) {
			
			ImageControl ic = null;
			
			switch (_type) {
				case RED: 	ic = new ImageControl(new File(CurrentExperiment.getRedFrameAbsFilename(i, null)), i); break;
				case GREEN: ic = new ImageControl(new File(CurrentExperiment.getGreenFrameAbsFilename(i, null)), i); break;
				case BLUE: 	ic = new ImageControl(new File(CurrentExperiment.getBlueFrameAbsFilename(i, null)), i); break;
			}
			 					
			imageCollection.addLast(ic);
			i += (skip <= 0) ? 1 : skip;
		}
	}
	





	public void frameSelected(Object source, int frameNo, List<Roi> rois) {
		// TODO Auto-generated method stub
		for (ImageControl ic : imageCollection) {
			if (ic.getFrameNo() == frameNo) {
				ic.setSelected();
				break;
			}
		}			
		int m = masterScroller.getHorizontalScrollBar().getMaximum();
//		System.out.println("m=" + m + " v=" + masterScroller.getHorizontalScrollBar().getValue() + " new v=" + (int)(frameNo * m /(305.*imageCollection.size())));
		
		masterScroller.getHorizontalScrollBar().setValue((int)(1.  * ((frameNo > 3) ? frameNo/SKIP-3: 0) * m / imageCollection.size()));
		masterScroller.repaint();
		//System.out.println("TO DO!!");
	}



		
		
		
	

 	
}
