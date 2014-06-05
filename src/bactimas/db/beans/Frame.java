package bactimas.db.beans;


public class Frame {
	

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public double getBgBlueMean() {
		return bgBlueMean;
	}

	public void setBgBlueMean(double bgBlueMean) {
		this.bgBlueMean = bgBlueMean;
	}
	private int idFrame;
	private int idExperiment;
	private int frameNo;
	
	private String frameRedFileName;
	private String frameGreenFileName;
	private String frameBlueFileName;
	
	private String algorithm;
	
	private int transX;
	private int transY;
	
	private String ignoreFrame;
	
	private double bgGreenMean, bgRedMean, bgBlueMean;
	
	public boolean isIgnored() {
		return ignoreFrame != null && ignoreFrame.toLowerCase().equals("y");
	}
	
	public String getIgnoreFrame() {
		return ignoreFrame;
	}
	public void setIgnoreFrame(String ignoreFrame) {
		this.ignoreFrame = ignoreFrame;
	}
	
	public String getFrameRedFileName() {
		return frameRedFileName;
	}
	public void setFrameRedFileName(String frameRedFileName) {
		this.frameRedFileName = frameRedFileName;
	}
	public String getFrameGreenFileName() {
		return frameGreenFileName;
	}
	public void setFrameGreenFileName(String frameGreenFileName) {
		this.frameGreenFileName = frameGreenFileName;
	}
	public String getFrameBlueFileName() {
		return frameBlueFileName;
	}
	public void setFrameBlueFileName(String frameBlueFileName) {
		this.frameBlueFileName = frameBlueFileName;
	}
	public int getTransX() {
		return transX;
	}
	public void setTransX(int transX) {
		this.transX = transX;
	}
	public int getTransY() {
		return transY;
	}
	public void setTransY(int transY) {
		this.transY = transY;
	}
	public int getIdFrame() {
		return idFrame;
	}
	public void setIdFrame(int idFrame) {
		this.idFrame = idFrame;
	}
	public int getIdExperiment() {
		return idExperiment;
	}
	public void setIdExperiment(int idExperiment) {
		this.idExperiment = idExperiment;
	}
	public int getFrameNo() {
		return frameNo;
	}
	public void setFrameNo(int frameNo) {
		this.frameNo = frameNo;
	}
	public double getBgGreenMean() {
		return bgGreenMean;
	}
	public void setBgGreenMean(double bgGreenMean) {
		this.bgGreenMean = bgGreenMean;
	}
	public double getBgRedMean() {
		return bgRedMean;
	}
	public void setBgRedMean(double bgRedMean) {
		this.bgRedMean = bgRedMean;
	}

}
