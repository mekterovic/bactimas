package bactimas.bintree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;


public class Node
{
	String _name;
	private ArrayList<TimeIntensity> _timeIntensityList;
	private ArrayList<TimeSize> _timeSizeList;
	private int _frame;
	String _parentName;
	private Node leftChild;
	private Node rightChild;
	private int _levelIndex;
	private int _level;
	
	private LinkedList <BTreeStateChange> _stateChanges;

	public Node(String name, int frame, float intensity, String parentName, BTreeEventListener msg, int level, int levelIndex)
	{
		this._name = name.toUpperCase().trim();
		this._timeIntensityList = new ArrayList<TimeIntensity>();
		this._timeSizeList = new ArrayList<TimeSize>();
		this._frame = frame;

		this._parentName = parentName.toUpperCase().trim();
		this.leftChild = (this.rightChild = null);
		this._levelIndex = levelIndex;
		this._level = level;
	}

	public int getLevel() {
		return this._level;
	}
	public int getLevelIndex() {
		return this._levelIndex;
	}
	public int getFrame() {
		return this._frame;
	}
	public String getName() {
		return this._name;
	}
	public Node getLeftChild() {
		return this.leftChild;
	}
	public Node getRightChild() {
		return this.rightChild;
	}
	public LinkedList <BTreeStateChange> getStateChanges () {
		return _stateChanges;
	}
	
	public boolean addStateChange(BTreeStateChange bsc) {
		if (_name.equals(bsc.getbName())) {
			if (_stateChanges == null) _stateChanges = new LinkedList <BTreeStateChange>();
			_stateChanges.addLast(bsc);
			return true;
		}
		boolean rv = false;
		if (leftChild != null) {
			rv = leftChild.addStateChange(bsc);
		} 
		if (rv) return rv;
		if (rightChild != null) {
			rv = rightChild.addStateChange(bsc);
		} 
		return rv;
	}
	
	
	public int connect(Node child) {
		if (leftChild == null && _name.equals(child._parentName)) {
			leftChild = child;
			child._levelIndex = _levelIndex * 2;
			child._level = _level + 1;			
			return 1;			
		} else if (rightChild == null && _name.equals(child._parentName)) {
			rightChild = child;
			child._levelIndex = _levelIndex * 2 + 1;
			child._level = _level + 1;				
			return 1;
		}
		int rv = 0;
		if (leftChild != null) {
			rv = leftChild.connect(child);
		} 
		if (rv == 1) return 1;
		if (rightChild != null) {
			rv = rightChild.connect(child);
		} 
		return rv;
	}	
//	public int connect(Node child) {
//		if ((this.leftChild == null) && (this._name.equals(child._parentName))) {
//			this.leftChild = child;
//			this._levelIndex *= 2;
//			this._level += 1;
//			return 1;
//		}
//		if ((this.rightChild == null) && (this._name.equals(child._parentName))) {
//			this.rightChild = child;
//			child._levelIndex = (this._levelIndex * 2 + 1);
//			this._level += 1;
//			return 1;
//		}
//		int rv = 0;
//		if (this.leftChild != null) {
//			rv = this.leftChild.connect(child);
//		}
//		if (rv == 1) return 1;
//		if (this.rightChild != null) {
//			rv = this.rightChild.connect(child);
//		}
//		return rv;
//	}

	public int setIntensity(String nodeName, int time, double intensity)
	{
		if (this._name.equalsIgnoreCase(nodeName)) {
			this._timeIntensityList.add(new TimeIntensity(time, intensity));
			if (this._timeIntensityList.size() > 1) Collections.sort(this._timeIntensityList);
			return 1;
		}
		int rv = 0;
		if (this.leftChild != null) {
			rv = this.leftChild.setIntensity(nodeName, time, intensity);
		}
		if (rv == 1) return 1;
		if (this.rightChild != null) {
			rv = this.rightChild.setIntensity(nodeName, time, intensity);
		}
		return rv;
	}

	public int setSize(String nodeName, int time, double size)
	{
		if (this._name.equalsIgnoreCase(nodeName)) {
			this._timeSizeList.add(new TimeSize(time, size));
			if (this._timeSizeList.size() > 1) Collections.sort(this._timeSizeList);
			return 1;
		}
		int rv = 0;
		if (this.leftChild != null) {
			rv = this.leftChild.setSize(nodeName, time, size);
		}
		if (rv == 1) return 1;
		if (this.rightChild != null) {
			rv = this.rightChild.setSize(nodeName, time, size);
		}
		return rv;
	}

	public ArrayList<TimeIntensity> getIntensityList() {
		return this._timeIntensityList;
	}
	public ArrayList<TimeSize> getSizeList() {
		return this._timeSizeList;
	}
	public String toString() {
		return this._name + 
				", parent=" + this._parentName + 
				", f=" + this._frame + 
				", i.size=" + this._timeIntensityList.size() + 
				" left=" + (this.leftChild == null ? "null" : this.leftChild._name) + 
				" right=" + (this.rightChild == null ? "null" : this.rightChild._name) + 
				" level=" + this._level + 
				" level.idx=" + this._levelIndex;
	}

	public class TimeIntensity
	implements Comparable<Object>
	{
		public int _time;
		public double _intensity;

		public TimeIntensity(int time, double intensity)
		{
			this._time = time;
			this._intensity = intensity;
		}

		public int compareTo(Object o) {
			return new Integer(this._time).compareTo(new Integer(((TimeIntensity)o)._time));
		}
	}
	public class TimeSize implements Comparable<Object> {
		public int _time;
		public double _size;

		public TimeSize(int time, double size) { this._time = time;
		this._size = size; }

		public int compareTo(Object o)
		{
			return new Integer(this._time).compareTo(new Integer(((TimeSize)o)._time));
		}
	}
}