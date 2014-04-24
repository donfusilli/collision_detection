import java.awt.*;
import java.util.*;
import javax.vecmath.*;
import java.awt.geom.*;

/** 
 * Basic square block/pixel primitive for representing image-based
 * rigid objects and resolving contacts. Each Block is wrapped by a
 * disk and used for contact generation.
 * 
 * @author Doug James, March 2007. 
 */
public class Block 
{
	/** Image row index. */
	int i;

	/** Image column index. */
	int j;

	/** Pixel color. */
	Color c;

	/** Body-frame position --- needed for contact processing. */
	Point2d p; // Center of the block (in the image coordinate system)

	/** Halfwidth of block (Note: the block radius is sqrt(2)*h). */
	double  h;

	// Random generator for random colors.
	static Random gen = new Random(System.currentTimeMillis());

	/**
	 * Constructs a Block.
	 */
	Block(int i, int j, Color color, Point2d center, double halfwidth)
	{
		this.i  = i;
		this.j  = j;
		this.c  = color;
		this.p  = center;
		this.h  = halfwidth;
	}

	/** Image row. */
	int i() { return i; } 

	/** Image column. */
	int j() { return j; } 

	/** Center position of Block (in body coordinates). */
	public Point2d p() { return p; }

	/** Halfwidth of block. Note that the block radius is sqrt(2)*h.  */
	public double h() { return h; }

	/** Color-based mass on [0,1] with white having zero mass, and
	 * darker colors approaching one (feel free to modify). */
	public double getColorMass() 
	{
		// Scale from integer interval [0,255] to [0,1].
		double x = c.getBlue() / 255.0;
		double y = c.getGreen() / 255.0;
		double z = c.getRed() / 255.0;

		double m = 1 - ((double)(x + y + z))/3.0; // on [0,1]
		if(m < 0) m = 0;
		if(m > 1) m = 1;
		return m;
	}

	/**
	 * 
	 * @param g A Graphics2D object.
	 * @param scale Scale factor from image coordinate system to canvas coordinate system.
	 * @param d Displacement vector for the block.
	 * @param clicked Indicates whether the shape containing the block has been clicked on.
	 * @param overlaps Indicates whether the shape containing the block overlaps another shape.
	 */
	public void display(Graphics g, double scale, Vec2D d, boolean clicked, boolean overlaps) {
		Graphics2D g2d = (Graphics2D) g;

		Color prevColor = g2d.getColor();

		Color newColor;
		if (overlaps) {
			// Generate random color
			int red = gen.nextInt(256);
			int green = gen.nextInt(256);
			int blue = gen.nextInt(256);
			newColor = new Color(red,green,blue);
		}
		else {
			newColor = c;
		}

		if (clicked) {
			int red = newColor.getRed();
			int green = newColor.getGreen();
			int blue = newColor.getBlue();
			newColor = new Color(red,green,blue,64); // semi-transparent
		}
		g2d.setColor(newColor);

		// Account for displacement.
		double newX = p.x + d.x;
		double newY = p.y + d.y;

		// Draw with int precision
		/*
		int topLeftX = (int) Math.floor(scale*(newX - h));
		int topLeftY = (int) Math.floor(scale*(newY - h));
		int width = (int) Math.ceil(scale*2*h);
		int height = width;
		g2d.fill(new Rectangle(topLeftX,topLeftY,width,height));
		//g.fillRect(topLeftX, topLeftY, width, height);
		*/
		
		// Draw with double precision.
		double topLeftX = scale*(newX - h);
		double topLeftY = scale*(newY - h);
		double width = scale*2*h;
		double height = width;
		g2d.fill(new Rectangle2D.Double(topLeftX,topLeftY,width,height));

		g2d.setColor(prevColor); // restore previous color
	}

	public String toString() {
		return "x = " + j + ", y = " + i + ", c = " + c.toString() + ", pos = " + p.toString() + ", h = " + h;
	}

	/**
	 * @return The bounding box of this block.
	 */
	public BBox getBBox() {
		Vec2D tl = new Vec2D(p.x-h,p.y-h);
		Vec2D br = new Vec2D(p.x+h,p.y+h);
		return new BBox(tl,br);
	}

	/**
	 * 
	 * @param p A point.
	 * @return True iff this block contains point p.
	 */
	public boolean contains(Vec2D p) {
		return getBBox().contains(p);
	}

	/**
	 * 
	 * @param a A block.
	 * @param u Displacement vector for block a.
	 * @param b A block.
	 * @param v Displacement vector for block b.
	 * @return True iff (block a displaced by vector u) overlaps (block b displaced by vector v).
	 */
	public static boolean overlaps(Block a, Vec2D u, Block b, Vec2D v) {
		Vec2D c1 = Vec2D.add(new Vec2D(a.p),u);
		Vec2D c2 = Vec2D.add(new Vec2D(b.p),v);

		return Vec2D.dist(c1,c2) < (a.h+b.h);
	}

}
