package bactimas.bintree;

public class BTreeEvent {
	
    public BTreeEvent(int frameNo, String eventAbbr) {
		super();
		this.frameNo = frameNo;
		this.eventAbbr = eventAbbr;
	}
	public int getFrameNo() {
		return frameNo;
	}
	public void setFrameNo(int frameNo) {
		this.frameNo = frameNo;
	}
	public String getEventAbbr() {
		return eventAbbr;
	}
	public void setEventAbbr(String eventAbbr) {
		this.eventAbbr = eventAbbr;
	}
	private int frameNo;
    private String eventAbbr;
    
}
