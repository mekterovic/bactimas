package bactimas.db.beans;

public class ExperimentMeasurements {
	int idExperiment		
		, idMeasurement		
		, idChannel		
		, collarSize;
	
	String measurementName, channelName;

	public int getIdExperiment() {
		return idExperiment;
	}

	public void setIdExperiment(int idExperiment) {
		this.idExperiment = idExperiment;
	}

	public int getIdMeasurement() {
		return idMeasurement;
	}

	public void setIdMeasurement(int idMeasurement) {
		this.idMeasurement = idMeasurement;
	}

	public int getIdChannel() {
		return idChannel;
	}

	public void setIdChannel(int idChannel) {
		this.idChannel = idChannel;
	}

	public int getCollarSize() {
		return collarSize;
	}

	public void setCollarSize(int collarSize) {
		this.collarSize = collarSize;
	}

	public String getMeasurementName() {
		return measurementName;
	}

	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public ExperimentMeasurements(int collarSize, String measurementName,
			String channelName) {
		super();
		this.collarSize = collarSize;
		this.measurementName = measurementName;
		this.channelName = channelName;
	}
	
}
