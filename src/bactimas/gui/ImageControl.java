package bactimas.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bactimas.gui.events.FrameManager;

public class ImageControl extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6739455157949916142L;
	static Logger log = Logger.getLogger("bactimas.gui.ImageStrip");
	BufferedImage image;
	private int _frameNo;
	File _file;
	final static float dash1[] = { 10.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, null);
		g.setColor (Color.YELLOW);
		g.drawString("F" + _frameNo, 20, 20);
		((Graphics2D)g).setStroke(dashed);
		if (selectedFrameNo == _frameNo) {
			//System.out.println("Rect on " + _frameNo);
			g.draw3DRect(2, 2, 296, 296, true);			
		}		
	}
	// TODO: listen for click, bubble up
	
	public int getFrameNo() {
		return _frameNo;
	}
	
	private static int selectedFrameNo = -1;
	protected void setSelected() {
		selectedFrameNo = _frameNo;
//		this.repaint();
	}
	
	public ImageControl(File f, int frameNo) {
		try {			
			_frameNo = frameNo;
			_file = f;
			log.debug("Loading " + f.getAbsoluteFile() + ". FrameNo=" + _frameNo);
			image = ImageIO.read(f);			
			image = createResizedCopy(image, 300, 300, true);			
			this.setPreferredSize(new Dimension(300, 300));
			
			MouseListener l = new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
//					log.debug("Instantiating ImagePlus for " + _file.getAbsoluteFile().toString());
//					ImagePlus ip;
//					try {
//						if (WindowManager.getCurrentWindow() != null) {
//							WindowManager.getCurrentWindow().close();
//						}
//						//WindowManager.removeWindow(WindowManager.getCurrentWindow());
//						
//						ip = new ImagePlus("Frame:" + _frame, ImageIO.read(_file));						
//						ip.show();
//						ip.getWindow().setAlwaysOnTop(true);
//						WindowManager.setCurrentWindow(ip.getWindow());
						
						FrameManager.fireFrameSelected(this, _frameNo, null);
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}	
					// Point p = e.getPoint();
					// if((panel.getBounds().contains(p))
					// picture.setIcon(getImages(myImage.get(imageCounter).get(SECOND)));
				}
			};	
			this.addMouseListener(l);
			
		} catch (IOException ie) {
			System.out.println("Error:" + ie.getMessage() + " for file: " + f.getAbsoluteFile());
			log.error(ie);
		}
	}
	/*
	protected ImagePlus getNewImagePlusWindow () {
		try {			
			return new ImagePlus("Frame:" + _frame, ImageIO.read(_file));									
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}					
	}
	*/
	BufferedImage createResizedCopy(Image originalImage, int scaledWidth,
			int scaledHeight, boolean preserveAlpha) {
//		log.debug("resizing...");
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight,
				imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
	
}
