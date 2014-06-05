package bactimas.db.beans;

public class ExperimentEvent {
/*
 * CREATE TABLE ExperimentEvent (
    idExperiment  integer NOT NULL,
    frameNo       integer NOT NULL,
    eventDesc      varchar(255) not null,
    eventAbbr      varchar(15) not null, 
    PRIMARY KEY (idExperiment, frameNo),
    FOREIGN KEY (idExperiment) REFERENCES Experiment(idExperiment)
);
 * 
 * */
	

	public ExperimentEvent(int idExperiment, int frameNo, String eventDesc, String eventAbbr) {
		super();
		this.idExperiment = idExperiment;
		this.frameNo = frameNo;
		this.eventDesc = eventDesc;
		this.eventAbbr = eventAbbr;
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
	public String getEventDesc() {
		return eventDesc;
	}
	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}
	public String getEventAbbr() {
		return eventAbbr;
	}
	public void setEventAbbr(String eventAbbr) {
		this.eventAbbr = eventAbbr;
	}
	private int idExperiment, frameNo;
	private String eventDesc, eventAbbr;
	
	
}
