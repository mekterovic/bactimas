package bactimas.db.beans;

public class BacteriaState {
	int idState;
	String stateName, stateTag;	
	
	
	public BacteriaState(int idState, String stateName, String stateTag) {
		super();
		this.idState = idState;
		this.stateName = stateName;
		this.stateTag = stateTag;
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
	public String getStateTag() {
		return stateTag;
	}
	public void setStateTag(String stateTag) {
		this.stateTag = stateTag;
	}
	
	public String toString() {
		return stateName + "(" + stateTag + ")";
				
	}
	//	CREATE TABLE BacteriaState (
//	        idState integer PRIMARY KEY,
//	        stateName varchar(50) not null,
//	        stateTag char(1) not null
//	);

	
}
