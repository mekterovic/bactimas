package bactimas.alg;

import signalprocesser.voronoi.VPoint;


@SuppressWarnings("rawtypes")
public class PointSum implements Comparable {
	public VPoint p;
	public int sum;
	public PointSum(VPoint p, int sum) {
		super();
		this.p = p;
		this.sum = sum;
	}
	@Override
	public int compareTo(Object other) {
		// TODO Auto-generated method stub
		return -1 * (new Integer(sum)).compareTo(((PointSum)other).sum);			
	}
	public String toString() {
		return sum + "(" + p.toString() + ")"; 
	}
}