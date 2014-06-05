package bactimas.datamodel;

public class AlgorithmDesc {
	
	public AlgorithmDesc(String className, String name, boolean isDefault) {
		super();
		this.className = className;
		this.name = name;
		this.isDefault = isDefault;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	String className, name;
	boolean isDefault;
	
	
	public boolean isDefault() {
		return isDefault;
	}
	
	public String toString() {
		return name;
	}
}
