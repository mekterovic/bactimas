package plugins.mekterovic.bactimas;


import icy.gui.frame.TitledFrame;
import icy.plugin.abstract_.PluginActionable;

import java.awt.Dimension;

import bactimas.gui.ControlPanel;

public class BactImAs extends PluginActionable {

	private TitledFrame frame;

    @Override
    public void run()
    {
        // build frame and others controls
        frame = new TitledFrame("Bactrack", true, true, true, true);        
        new ControlPanel(frame);
        
   
//        // add a listener to frame events
//        frame.addFrameListener(new IcyFrameAdapter()
//        {
//            // called when frame is closed
//            @Override
//            public void icyFrameClosed(IcyFrameEvent e)
//            {
//                // remove the listener so there is no more reference on plugin instance
//            	// Icy.getMainInterface().removeActiveSequenceListener(activeSequenceListener);
//            }
//        });

        // set size
        frame.setSize(new Dimension(640, 480));
        // add frame to application desktop
        addIcyFrame(frame);
        // center
        frame.center();
        // and finally make it visible
        frame.setVisible(true);
        // get focus
        frame.requestFocus();
    }	

}
