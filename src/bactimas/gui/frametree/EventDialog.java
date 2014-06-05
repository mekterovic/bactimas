package bactimas.gui.frametree;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.gui.ControlPanel;

class EventDialog extends JDialog implements ActionListener  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger("bactimas.gui.frametree.EventDialog" );


	JTextField txtEventDesc = new JTextField();
	JTextField txtEventAbbr = new JTextField();


	private FrameTree frameTree;

	public EventDialog(FrameTree frameTree) {
		super(null, "Set event settings", Dialog.ModalityType.APPLICATION_MODAL);
		
		this.frameTree = frameTree;
		
		JPanel panel = new JPanel(new GridLayout(0,2));
		
		
		panel.add( new JLabel("Event abbr"));
		panel.add(txtEventAbbr);
		panel.add( new JLabel("Event desc"));
		panel.add(txtEventDesc);
		
		
		JButton btnAdd = new JButton("Set event");
		
		
		btnAdd.addActionListener(this);		
		
		
		
	    Container cp = this.getContentPane();
	    cp.setLayout(new BorderLayout());
	    cp.add(panel, BorderLayout.CENTER);    	        
	    cp.add(btnAdd, BorderLayout.SOUTH);
	    cp.add(new JLabel("If left blank, no event will be inserted (and any existing one will be deleted)"), BorderLayout.NORTH);    	        
	    
	}

	public void showDialog() {
		//JDialog dialog = new JDialog(null, "Set experiment settings", Dialog.ModalityType.APPLICATION_MODAL);
		
	    this.setSize(800, 300);
	    this.setLocationRelativeTo(null);
		this.setVisible(true); 		
	}	
	
    public void actionPerformed(ActionEvent e) {
        this.dispose();
        
        
        
        	
        ControlPanel.addStatusMessage("Setting up, please wait...") ;  
        
        try {
        	
            boolean res = CurrentExperiment.setExperimentEvent(
            		((FrameNode) (frameTree.getSelectedNode()).getUserObject()).getFrameNo(),
            		txtEventAbbr.getText(),
            		txtEventDesc.getText()
                    );
            if (res) {
            	ControlPanel.addStatusMessage("Success. Reloading tree.");
            	frameTree.reloadTree();
            } else {
            	ControlPanel.addStatusMessage("Error adding event.");
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





