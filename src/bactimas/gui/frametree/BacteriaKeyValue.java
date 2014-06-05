package bactimas.gui.frametree;

public class BacteriaKeyValue {
	
	private String key;
	private double value;
		
	
	public BacteriaKeyValue(String key, double value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
}
