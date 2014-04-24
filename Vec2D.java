import javax.vecmath.*;

/**
 *
 * A 2D vector with components of double precision.
 * We also think of this as a point on a 2D plane.
 * 
 * Remark:
 * This data structure can be used both for something that
 * is conceptually a vector (e.g. a displacement vector) and
 * for something that is conceptually a point on the plane
 * (because a point can be identified with the vector from
 * the origin to the point).
 *
 */
public class Vec2D 
{
	/**
	 * The first component of the vector.
	 */
	public double x;

	/**
	 * The second component of the vector.
	 */
	public double y;

	/**
	 * @param p A vector.
	 */
	public Vec2D(Vec2D p) {
		x = p.x;
		y = p.y;
	}

	/**
	 * 
	 * @param p A Point2d object from the javax.vecmath package.
	 */
	public Vec2D(Point2d p) {
		x = p.x;
		y = p.y;
	}

	/**
	 * 
	 * @param x First component of vector.
	 * @param y Second component of vector.
	 */
	public Vec2D(double x, double y) {
		this.x = x;
		this.y = y;
	}


	public String toString() {
		return "(" + x + "," + y + ")";
	}

	/**
	 * 
	 * @param a A vector.
	 * @return The result of adding this vector to the vector a.
	 */
	public Vec2D add(Vec2D a) {
		return add(this,a);
	}

	/**
	 * 
	 * @param a A vector.
	 * @param b A vector.
	 * @return The result of adding vectors a and b.
	 */
	public static Vec2D add(Vec2D a, Vec2D b) {
		return new Vec2D(a.x+b.x,a.y+b.y);
	}

	/**
	 * @param a A vector (point).
	 * @param b A vector (point).
	 * @return The Euclidean distance between the points a and b.
	 */
	public static double dist(Vec2D a, Vec2D b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * 
	 * @param v Vector to add on to this vector.
	 */
	public void addOn(Vec2D v) {
		x += v.x;
		y += v.y;
	}

	/**
	 * 
	 * @param b A Vector.
	 * @return The result of subtracting b from this vector.
	 */
	public Vec2D minus(Vec2D b) {
		return new Vec2D(x - b.x,y - b.y);
	}

}
