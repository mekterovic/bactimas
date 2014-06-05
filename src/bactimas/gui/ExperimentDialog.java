package bactimas.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.db.beans.Experiment;

public class ExperimentDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger("bactimas.gui.ExperimentDialog" );
	
    private JTextField txtExpName;
    private JTextField txtRedName;
    private JTextField txtGreenName;
    private JTextField txtBlueName;
    private JTextField txtSpf;
    private JTextField txtPixelWidth;
    private JTextField txtPixelHeight;
    private JTextField txtPictureScale;
	
	private JPanel panel;
	
	private int _actionType;
	
	public static int ACTION_TYPE_INSERT = 1, ACTION_TYPE_UPDATE = 2;
	
	public ExperimentDialog (int actionType) {
		super(null, "Set experiment settings", Dialog.ModalityType.APPLICATION_MODAL);
		
		_actionType = actionType;
		
		txtExpName       = new JTextField("My experiment");
		txtRedName       = new JTextField("");
		txtGreenName     = new JTextField("");
		txtBlueName      = new JTextField("");
		txtSpf           = new JTextField("600");
		txtPixelWidth    = new JTextField("0.128945");
		txtPixelHeight   = new JTextField("0.128945");
		txtPictureScale  = new JTextField("1.0");
		
		panel = new JPanel(new GridLayout(0,2));
		
		panel.add(new JLabel("Experiment name"));
		panel.add(txtExpName);
		panel.add(new JLabel("Red channel folder"));
		panel.add(txtRedName);
		panel.add(new JLabel("Green channel folder"));
		panel.add(txtGreenName);
		panel.add(new JLabel("Blue channel folder"));
		panel.add(txtBlueName);
		panel.add(new JLabel("Seconds per/between frame(s)"));  		
		panel.add(txtSpf);
		
		panel.add(new JLabel("Pixel width (microns)"));
		panel.add(txtPixelWidth);
		panel.add(new JLabel("Pixel height (microns)"));
		panel.add(txtPixelHeight);    	
		panel.add(new JLabel("Picture (up)scale (if you resized it from 256x256->512x512 then 2.0)"));
		panel.add(txtPictureScale);	
		
		JButton btnNewExp = new JButton("Save");
		
		
	    btnNewExp.addActionListener(this);		
		
		
		
	    Container cp = this.getContentPane();
	    cp.setLayout(new BorderLayout());
	    cp.add(panel, BorderLayout.CENTER);    	        
	    cp.add(btnNewExp, BorderLayout.SOUTH);
	    
	}
    public void actionPerformed(ActionEvent e) {
        this.dispose();
        
        BigDecimal pWidth, pHeight, scale;
        try {
            pWidth = new BigDecimal(txtPixelWidth.getText());
        } catch (Exception e1) {
            pWidth = new BigDecimal("33.01");
            ControlPanel.addStatusMessage("Couldn't parse Pixel width (microns), reverting to default (33.01). You can change this later.") ;
        }                       
        try {
            pHeight = new BigDecimal(txtPixelHeight.getText());
        } catch (Exception e1) {
            pHeight = new BigDecimal("33.01");
            ControlPanel.addStatusMessage("Couldn't parse Pixel height (microns), reverting to default (33.01). You can change this later.") ;
        }   
        try {
            scale = new BigDecimal(txtPictureScale.getText());
        } catch (Exception e1) {
            scale = new BigDecimal("1.0");
            ControlPanel.addStatusMessage("Couldn't parse picture (up)scale, reverting to default (1.0). You can change this later.") ;
        }                   
        int spf = 0;
        try {
            spf = Integer.parseInt(txtSpf.getText());                       
        } catch (Exception exc) {
            spf = 600;
            ControlPanel.addStatusMessage("Couldn't parse SPF, reverting to default (600). You can change this later.") ;
        }
        
        if (_actionType == ACTION_TYPE_INSERT) {
        	
            ControlPanel.addStatusMessage("Setting up, please wait...") ;  
            
            try {
                CurrentExperiment.beginExperiment(
                        txtExpName.getText(),
                        txtRedName.getText(), 
                        txtGreenName.getText(), 
                        txtBlueName.getText(), 
                        spf, 
                        pWidth, 
                        pHeight,
                        scale
                        );
                
            } catch (Exception exc) {
                // TODO Auto-generated catch block
                exc.printStackTrace();
                log.error(exc);
            }  
        
        } else {
            ControlPanel.addStatusMessage("Updating existing experiment, please wait...") ;  
            
            try {
                CurrentExperiment.updateCurrentExperiment(
                        txtExpName.getText(),
                        txtRedName.getText(), 
                        txtGreenName.getText(), 
                        txtBlueName.getText(), 
                        spf, 
                        pWidth, 
                        pHeight,
                        scale
                        );
                
            } catch (Exception exc) {
                // TODO Auto-generated catch block
                exc.printStackTrace();
                log.error(exc);
            }          	
        	
        }
        
 
        
        ControlPanel.getInstance().loadStrips();
        
    }
	private void setupFormValues (String expName, String red, String green, String blue, int spf, BigDecimal w, BigDecimal h, BigDecimal scale) {
		txtExpName.setText(expName);
		txtRedName.setText(red);
		txtGreenName.setText(green);
		txtBlueName.setText(blue);
		txtSpf.setText("" + spf);
		txtPixelWidth.setText("" + w);
		txtPixelHeight.setText("" + h);
		txtPictureScale.setText("" + scale);
	}
	
	public ExperimentDialog (Experiment experiment) {
		this(ACTION_TYPE_UPDATE);
		setupFormValues(
				experiment.getExperimentName(),
				experiment.getRedMovieFileName(),
				experiment.getGreenMovieFileName(),
				experiment.getBlueMovieFileName(),
				experiment.getMovieSpf(),
				experiment.getPixelWidthMicron(),
				experiment.getPixelHeightMicron(),
				experiment.getPictureScale()								
				);
	}
	public ExperimentDialog (String red, String green, String blue, int actionType) {
		this(actionType);
		txtRedName.setText(red);
		txtGreenName.setText(green);
		txtBlueName.setText(blue);
	}	
	
	public void showDialog() {
		//JDialog dialog = new JDialog(null, "Set experiment settings", Dialog.ModalityType.APPLICATION_MODAL);
		
	    this.setSize(800, 300);
	    this.setLocationRelativeTo(null);
		this.setVisible(true); 		
	}
	
    
	 
	
	


	
			
	
}
