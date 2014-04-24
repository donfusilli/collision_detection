import java.util.*;

/**
 * A 2D bounding box.
 *
 */
public class BBox 
{
	/**
	 * The corner of the bounding box with the smaller x,y coordinates.
	 */
	public Vec2D lower; // (minX,minY)
	
	/**
	 * The corner of the bounding box with the larger x,y coordinates.
	 */
	public Vec2D upper; // (maxX,maxY)

	/**
	 * 
	 * @param box A bounding box.
	 */
	public BBox(BBox box) {
		lower = new Vec2D(box.lower);
		upper = new Vec2D(box.upper);
	}

	/**
	 * 
	 * @param lower Corner with smaller coordinates.
	 * @param upper Corner with larger coordinates.
	 */
	public BBox(Vec2D lower, Vec2D upper) {
		if (upper.x < lower.x) throw new IllegalArgumentException("invalid bbox");
		if (upper.y < lower.y) throw new IllegalArgumentException("invalid bbox");

		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * Width: size along the x-dimension.
	 * 
	 * @return Width of the bounding box.
	 */
	public double getWidth() {
		return upper.x - lower.x;
	}

	/**
	 * Height: size along the y-dimension.
	 * 
	 * @return Height of the bounding box.
	 */
	public double getHeight() {
		return upper.y - lower.y;
	}

	/**
	 * 
	 * @return Returns the dimension (width or height) of maximum length.
	 */
	public double getLength() {
		if (getWidth() > getHeight()) return getWidth();
		
		return getHeight();
	}

	/**
	 * 
	 * @return The center of this bounding box.
	 */
	public Vec2D getCenter() {
		double x = (upper.x + lower.x)/2;
		double y = (upper.y + lower.y)/2;
		
		return new Vec2D(x, y);
	}

	/**
	 * 
	 * @param d A displacement vector.
	 * @return The result of displacing this bounding box by vector d.
	 */
	public BBox displaced(Vec2D d) {
		Vec2D low = Vec2D.add(lower, d);
		Vec2D up = Vec2D.add(upper, d);
		
		return new BBox(low, up);
	}

	/**
	 * 
	 * @param p A point.
	 * @return True iff this bounding box contains point p.
	 */
	public boolean contains(Vec2D p) {
		boolean inX = lower.x <= p.x && p.x <= upper.x;
		boolean inY = lower.y <= p.y && p.y <= upper.y;
		
		return inX && inY;
	}

	/**
	 * 
	 * @return The area of this bounding box.
	 */
	public double getArea() {
		return getWidth()*getHeight();
	}


	/**
	 * 
	 * @param box A bounding box.
	 * @return True iff this bounding box overlaps with box.
	 */
	public boolean overlaps(BBox box) {
		double thisLX = lower.x;
		double thisLY = lower.y;
		double thisUX = upper.x;
		double thisUY = upper.y;
		double lX = box.lower.x;
		double lY = box.lower.y;
		double uX = box.upper.x;
		double uY = box.upper.y;
		
		// Checks if the bounding boxes overlap.
		if (uX < thisLX || lX > thisUX || uY < thisLY || lY > thisUY) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @param iter An iterator of blocks.
	 * @return The bounding box of the blocks given by the iterator.
	 */
	public static BBox findBBox(Iterator<Block> iter) {
		// Do not modify the following "if" statement.
		if (!iter.hasNext())
			throw new IllegalArgumentException("empty iterator");

		BBox box = iter.next().getBBox();
		double lowX = box.lower.x;
		double lowY = box.lower.y;
		double upX = box.upper.x;
		double upY = box.upper.y;
		
		// Finds the bounding box that encloses all the blocks.
		while (iter.hasNext()) {
			box = iter.next().getBBox();
			double newLowX = box.lower.x;
			double newLowY = box.lower.y;
			double newUpX = box.upper.x;
			double newUpY = box.upper.y;
			
			if (newLowX < lowX) lowX = newLowX;
			if (newLowY < lowY) lowY = newLowY;
			if (newUpX > upX) upX = newUpX;
			if (newUpY > upY) upY = newUpY;
		}
		
		Vec2D low = new Vec2D(lowX, lowY);
		Vec2D up = new Vec2D(upX, upY);
		
		return new BBox(low, up);
	}

	public String toString() {
		return lower + " -- " + upper;
	}
}
