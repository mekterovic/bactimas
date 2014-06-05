package bactimas.bintree;

public class BTreeStateChange {

	int frameNo;
    String stateTag, bName;

	
    
    
    
	public BTreeStateChange(int frameNo, String stateTag, String bName) {
		super();
		this.frameNo = frameNo;
		this.stateTag = stateTag;
		this.bName = bName;
	}
	public int getFrameNo() {
		return frameNo;
	}
	public void setFrameNo(int frameNo) {
		this.frameNo = frameNo;
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

    
    
    
    
    
}
