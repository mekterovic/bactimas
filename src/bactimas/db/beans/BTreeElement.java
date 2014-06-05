package bactimas.db.beans;

public class BTreeElement {
	@Override
	public String toString() {
		return "BTreeElement [idExperiment=" + idExperiment + ", frameNo="
				+ frameNo + ", bactName=" + bactName + ", onWidth=" + onWidth
				+ ", onColor=" + onColor + "]";
	}
	public float getOnWidth() {
		return onWidth;
	}
	public void setOnWidth(float onWidth) {
		this.onWidth = onWidth;
	}
	public float getOnColor() {
		return onColor;
	}
	public void setOnColor(float onColor) {
		this.onColor = onColor;
	}
	public BTreeElement(int idExperiment, int frameNo, String bactName,
			 float onWidth,  float onColor) {  // float intden,float meanBg,
		super();
		this.idExperiment = idExperiment;
		this.frameNo = frameNo;
		this.bactName = bactName;
//		this.intden = intden;
		this.onColor = onColor;
//		this.setMeanBg(meanBg);
		this.onWidth = onWidth;
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
	public String getBactName() {
		return bactName;
	}
	public void setBactName(String bactName) {
		this.bactName = bactName;
	}


//	public float getMeanBg() {
//		return meanBg;
//	}
//	public void setMeanBg(float meanBg) {
//		this.meanBg = meanBg;
//	}

	private int idExperiment,frameNo; 
	private String bactName;
	private float //intden, 
	onWidth, 
//	meanBg, 
	onColor;
	
}
