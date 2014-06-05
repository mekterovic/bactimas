package bactimas.db.beans;

public class BacteriaSplit {
	public BacteriaSplit(int frameNo, String parentName, String childAName,
			String childBName) {
		super();
		this.frameNo = frameNo;
		this.parentName = parentName;
		this.childAName = childAName;
		this.childBName = childBName;
	}


	private int	idBacteriaParent,
				idBacteriaChildA,
				idBacteriaChildB,
				frameNo; 
	
	
	private String // dont boil the ocean approach - that is, i'm nmot using ORM
		parentName,
		childAName,
		childBName;


	public int getIdBacteriaParent() {
		return idBacteriaParent;
	}


	public void setIdBacteriaParent(int idBacteriaParent) {
		this.idBacteriaParent = idBacteriaParent;
	}


	public int getIdBacteriaChildA() {
		return idBacteriaChildA;
	}


	public void setIdBacteriaChildA(int idBacteriaChildA) {
		this.idBacteriaChildA = idBacteriaChildA;
	}


	public int getIdBacteriaChildB() {
		return idBacteriaChildB;
	}


	public void setIdBacteriaChildB(int idBacteriaChildB) {
		this.idBacteriaChildB = idBacteriaChildB;
	}


	public int getFrameNo() {
		return frameNo;
	}


	public void setFrameNo(int frameNo) {
		this.frameNo = frameNo;
	}


	public String getParentName() {
		return parentName;
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}


	public String getChildAName() {
		return childAName;
	}


	public void setChildAName(String childAName) {
		this.childAName = childAName;
	}


	public String getChildBName() {
		return childBName;
	}


	public void setChildBName(String childBName) {
		this.childBName = childBName;
	}
	
	
	
	
			 
	
}
