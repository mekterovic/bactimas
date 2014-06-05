package bactimas.gui.frametree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import bactimas.datamodel.AlgorithmDesc;
import bactimas.datamodel.CurrentExperiment;
import bactimas.gui.ControlPanel;


class PickAlgorithmDialog extends JDialog implements ActionListener  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger("bactimas.gui.frametree.EventDialog" );


	JTextField txtFrom = new JTextField("1");
	JTextField txtTo = new JTextField();
	JComboBox<?> cboAlgorithm;

	private FrameTree frameTree;

	public PickAlgorithmDialog(FrameTree frameTree) {
		super(null, "Select algorithm", Dialog.ModalityType.APPLICATION_MODAL);
		
		this.frameTree = frameTree;
		
		JPanel panel = new JPanel(new GridLayout(0,2));
		
		txtTo.setText("" + CurrentExperiment.getFrameCount());
		
		
		
		panel.add( new JLabel("From frame:"));
		panel.add(txtFrom);
		panel.add( new JLabel("To frame:"));
		panel.add(txtTo);
		
		panel.add( new JLabel("Algorithm:"));
		
		
		AlgorithmDesc[] algs = CurrentExperiment.getAllAlgorithms();
		cboAlgorithm = new JComboBox<Object>(algs);
		panel.add(cboAlgorithm);
		
		for (AlgorithmDesc ad : algs) {
			if (ad.isDefault()) {
				cboAlgorithm.setSelectedItem(ad);
				break;
			}			
		}
		
		JButton btnAdd = new JButton("Set");
		
		
		btnAdd.addActionListener(this);		
		
		
		
	    Container cp = this.getContentPane();
	    cp.setLayout(new BorderLayout());
	    cp.add(panel, BorderLayout.CENTER);    	        
	    cp.add(btnAdd, BorderLayout.SOUTH);
	    cp.add(new JLabel("If left blank, no algorithm will be set"), BorderLayout.NORTH);    	        
	    
	}

	public void showDialog() {
		//JDialog dialog = new JDialog(null, "Set experiment settings", Dialog.ModalityType.APPLICATION_MODAL);
		
	    this.setSize(600, 150);
	    this.setLocationRelativeTo(null);
		this.setVisible(true); 		
	}	
	
    public void actionPerformed(ActionEvent e) {
        this.dispose();
        
        
        
        	
        ControlPanel.addStatusMessage("Setting up, please wait...") ;  
        
        try {
        	
            boolean res = CurrentExperiment.updateFrameAlgorithm(
            		Integer.parseInt(txtFrom.getText()),
            		Integer.parseInt(txtTo.getText()),
            		((AlgorithmDesc)cboAlgorithm.getSelectedItem()).getClassName()
                    );
            if (res) {
            	ControlPanel.addStatusMessage("Success. Reloading tree.");
            	for (int i = Integer.parseInt(txtFrom.getText()); i <= Integer.parseInt(txtTo.getText()); ++i) {
            		CurrentExperiment.getFrame(i).setAlgorithm(((AlgorithmDesc)cboAlgorithm.getSelectedItem()).getClassName());            		
            	}
            	frameTree.reloadTree();
            } else {
            	ControlPanel.addStatusMessage("Error setting algorithm.");
            }
        } catch (Exception exc) {
            // TODO Auto-generated catch block
            exc.printStackTrace();
            log.error(exc);
        }  
    
        
        
        //ControlPanel.getInstance().
        
    }




	public static void main(String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame();
		init(frame);
		frame.pack();
		frame.setVisible(true);
	}


	private static void init(JFrame frame) {
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final EventDialog dialog = new EventDialog(null);
		JButton button = new JButton("Show Dialog");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dialog.setSize(250, 120);
				dialog.setVisible(true);
			}
		});
		frame.getContentPane().add(button);
	}  
}