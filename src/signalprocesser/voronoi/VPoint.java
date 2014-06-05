package signalprocesser.voronoi;

public class VPoint {
    
    /* ***************************************************** */
    // Variables
    
    public int x;
    public int y;
    
    /* ***************************************************** */
    // Constructors
    
    public VPoint() {
        this(-1, -1);
    }
    public VPoint(int _x, int _y) {
        this.x = _x;
        this.y = _y;
    }
    public VPoint(VPoint point) {
        this.x = point.x;
        this.y = point.y;
    }
    
    public double distanceTo(VPoint point) {
        return Math.sqrt((this.x-point.x)*(this.x-point.x) + (this.y-point.y)*(this.y-point.y));
    }
    
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
	/* ***************************************************** */
	//
	// 676 /**
	// 677 * Computes the hash code for this <code>Color</code>.
	// 678 * @return a hash code value for this object.
	// 679 * @since JDK1.0
	// 680 */
	public int hashCode() {
		return toString().hashCode();
	}

	// 685 /**
	// 686 * Determines whether another object is equal to this
	// 687 * <code>Color</code>.
	// 688 * <p>
	// 689 * The result is <code>true</code> if and only if the argument is not
	// 690 * <code>null</code> and is a <code>Color</code> object that has the
	// same
	// 691 * red, green, blue, and alpha values as this object.
	// 692 * @param obj the object to test for equality with this
	// 693 * <code>Color</code>
	// 694 * @return <code>true</code> if the objects are the same;
	// 695 * <code>false</code> otherwise.
	// 696 * @since JDK1.0
	// 697 */
	public boolean equals(Object obj) {
		return obj instanceof VPoint && ((VPoint) obj).x == this.x
				&& ((VPoint) obj).y == this.y;
	}
       


}
