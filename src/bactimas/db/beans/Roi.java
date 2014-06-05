package bactimas.db.beans;

public class Roi {
	private int idBacteria;
	private int frameNo;
	private byte[] roiBlob;
	private String roiName;
	private int idRoiType;
	public static int 	ROI_TYPE_HUMAN = 1,
						ROI_TYPE_COMPUTER = 2;
	
	public int getIdRoiType() {
		return idRoiType;
	}

	public void setIdRoiType(int idRoiType) {
		this.idRoiType = idRoiType;
	}

	public String getRoiName() {
		return roiName;
	}

	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}

	public Roi(int idBacteria, int frameNo, byte[] roiBlob,String roiName,int idRoiType) {
		super();
		this.idBacteria = idBacteria;
		this.frameNo = frameNo;
		this.roiBlob = roiBlob;
		this.roiName = roiName;
		//System.out.println("roi name for " + idBacteria + " is " + 	roiName);
		assert (idRoiType ==  ROI_TYPE_HUMAN ||  idRoiType == ROI_TYPE_COMPUTER):  "non existing roi type";
		this.idRoiType = idRoiType;
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
	public byte[] getRoiBlob() {
		return roiBlob;
	}
	public void setRoiBlob(byte[] roiBlob) {
		this.roiBlob = roiBlob;
	}


}
