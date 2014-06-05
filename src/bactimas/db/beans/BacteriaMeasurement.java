package bactimas.db.beans;

public class BacteriaMeasurement {
	
	private int idBacteria;
	private int frameNo;	
	private int idMeasurement;
	//----
	private double value;
	//----
	private String bactName;
	private String measurementName;
	
	private int idRoiType;
	
	private int idChannel;
	
	
	public BacteriaMeasurement(int idBacteria, int frameNo, int idMeasurement,
			double value, String bactName, String measurementName, int idRoiType, int idChannel) {
		super();
		this.idBacteria = idBacteria;
		this.frameNo = frameNo;
		this.idMeasurement = idMeasurement;
		this.value = value;
		this.bactName = bactName;
		this.measurementName = measurementName;
		this.idRoiType = idRoiType;
		this.idChannel = idChannel;
	}
	public BacteriaMeasurement(int idBacteria, int frameNo, int idMeasurement,
			double value, int idRoiType, int idChannel) {
		super();
		this.idBacteria = idBacteria;
		this.frameNo = frameNo;
		this.idMeasurement = idMeasurement;
		this.value = value;
		this.idRoiType = idRoiType;
		this.idChannel = idChannel;
	}	
	public int getIdRoiType() {
		return idRoiType;
	}

	public void setIdRoiType(int idRoiType) {
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
	public int getIdMeasurement() {
		return idMeasurement;
	}
	public void setIdMeasurement(int idMeasurement) {
		this.idMeasurement = idMeasurement;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getBactName() {
		return bactName;
	}
	public void setBactName(String bactName) {
		this.bactName = bactName;
	}
	public String getMeasurementName() {
		return measurementName;
	}
	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}

	public int getIdChannel() {
		return idChannel;
	}

	public void setIdChannel(int idChannel) {
		this.idChannel = idChannel;
	}
	
	

}
