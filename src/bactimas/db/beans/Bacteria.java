package bactimas.db.beans;



public class Bacteria {
	private int idBacteria;
	private int idExperiment;
	private String bactName;
//	private byte[] initialRoiBlob;
	
	public Bacteria(int idBacteria, int idExperiment, String bactName) {
		super();
		this.idBacteria = idBacteria;
		this.idExperiment = idExperiment;
		this.bactName = bactName;
//		this.initialRoiBlob = initialRoiBlob;
	}
	public Bacteria() {		
	}	
	
	public int getIdBacteria() {
		return idBacteria;
	}
	public void setIdBacteria(int idBacteria) {
		this.idBacteria = idBacteria;
	}
	public int getIdExperiment() {
		return idExperiment;
	}
	public void setIdExperiment(int idExperiment) {
		this.idExperiment = idExperiment;
	}
	public String getBactName() {
		return bactName;
	}
	public void setBactName(String bactName) {
		this.bactName = bactName;
	}
//	public byte[] getInitialRoiBlob() {
//		return initialRoiBlob;
//	}
//	public void setInitialRoiBlob(byte[] initialRoiBlob) {
//		this.initialRoiBlob = initialRoiBlob;
//	}
	
	public boolean equals(Object obj) {
		return obj instanceof Bacteria 
				&& ((Bacteria) obj).idBacteria == this.idBacteria;
	}	
	public String toString() {
		return getBactName() + "(id=" + getIdBacteria() + ")";
	}
}
