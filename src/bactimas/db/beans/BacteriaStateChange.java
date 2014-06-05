package bactimas.db.beans;

public class BacteriaStateChange {
	@Override
	public String toString() {
		return "BacteriaStateChange [frameNo=" + frameNo + ", stateName="
				+ stateName + ", stateTag=" + stateTag + ", bName=" + bName
				+ "]";
	}

	public String getStateTag() {
		return stateTag;
	}

	public void setStateTag(String stateTag) {
		this.stateTag = stateTag;
	}

	public String getbName() {
		return bName;
	}

	public void setbName(String bName) {
		this.bName = bName;
	}
	int idBacteria, frameNo, idState;
    String stateName, stateTag, bName;
    
    
    
    

	public BacteriaStateChange(int idBacteria, int frameNo, int idState,
			String stateName, String stateTag, String bName) {
		super();
		this.idBacteria = idBacteria;
		this.frameNo = frameNo;
		this.idState = idState;
		this.stateName = stateName;
		this.stateTag = stateTag;
		this.bName = bName;
	}

	public int getIdBacteria() {
		return idBacteria;
	}
	public void setIdBacteria(int idBacteria) {
		this.idBacteria = idBacteria;
	}
	public int getFrameNo() {
		return frameNo;
	}
	public void setFrameNo(int frameNo) {
		this.frameNo = frameNo;
	}
	public int getIdState() {
		return idState;
	}
	public void setIdState(int idState) {
		this.idState = idState;
	}
	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

    
}
