package bactimas.alg;



import signalprocesser.voronoi.VPoint;




public interface ITrackingAlgorithm {
	
	/**
	 * This is the main function to implement - the one that performs the tracking / segmentation.
	 * <p>Typically, from within this function you'll call:<br>
	 *   CurrentExperiment.getFrame(int frameNo)<br>
	 *   CurrentExperiment.getBacteriasForFrame(int frameNo)<br>
	 *   CurrentExperiment.saveROI(ij.gui.Roi roi, int frameNo, Bacteria b, int roiType) <br>
	 *   
	 * @param frameNo      the frame to process
	 * @param translation  translation with regards to the previous frame (not necessarily frameNo-1, if that one is ignored)
	 */	
	
	public void step (int frameNo, VPoint translation);  
	
	/**
	 * A friendly name, to be shown to the user.
	 * @return friendly name
	 */
	public String getName();
	
	/**
	 * Class name, used for instantiation.
	 * @return class name
	 */
	public String getClassName();
	
	/**
	 * Abbreviation, used for display in the frame tree
	 */
	public String getAbbrev();
	
	/**
	 * Invoked before every step()
	 * @param frameNo
	 */
	public void beforeStep(final int frameNo);
	
	/**
	 * Invoked before processing batch.<br>
	 * Eg, say a user wants to process 10 frames:
	 * <br> beforeBatch is called once before 
	 * <br> beforeStep is called 10 times
	 * <br>Typically used to show a dialog and acquire algorithm parameters. 
	 * @param firstFrameNo
	 */
	public void beforeBatch(final int firstFrameNo);
}
