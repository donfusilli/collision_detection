import java.util.*;
import java.awt.*;

/**
 *
 * A shape is a collection of blocks.
 *
 */
public class Shape 
{
	/**
	 * Collection of blocks.
	 */
	private HashSet<Block> blocks;

	/**
	 * Displacement of the shape in the image coordinate system.
	 */
	private Vec2D d; 

	/**
	 * A flag indicating whether this shape has been clicked on or not.
	 */
	private boolean clickedOn; 

	/**
	 * A flag indicating whether this shape overlaps with some other shape.
	 */
	boolean overlaps; 

	/**
	 * A hierarchical tree structure for the blocks of the shape.
	 */
	BlockTree tree;

	/**
	 * 
	 * @param blocks A collection of blocks.
	 */
	public Shape(HashSet<Block> blocks) 
	{
		if (blocks == null)
			throw new IllegalArgumentException("null blocks");
		if (blocks.size() == 0)
			throw new IllegalArgumentException("empty blocks");

		this.blocks = blocks;
		d = new Vec2D(0,0); // original displacement is the zero vector
		
		Iterator<Block> iter = blocks.iterator();
		ArrayList<Block> blocksArray = new ArrayList<Block>();
		
		while (iter.hasNext()) {
			blocksArray.add(iter.next());
		}
		
		try {
			tree = new BlockTree(blocksArray);
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal Argument Exception " + e.getMessage());
		}
	}

	/**
	 * @return The center of the bounding box of the shape.
	 */
	Vec2D getCenter() {
		return tree.getBox().getCenter();
	}

	/**
	 * @return The bounding box of the image when you account for the displacement.
	 */
	BBox getAbsBBox() {
		return tree.getBox().displaced(d); // Note: add returns a new BBox
	}

	/**
	 * 
	 * @param width The width of canvas (in image coordinates)
	 * @param height The height of canvas (in image coordinates)
	 * @return True iff the object's bounding box is partially offscreen,
	 *   i.e., outside the [0,width] x [0,height] region.
	 */
	boolean overlapsOffscreen(double width, double height) { 
		BBox box = getAbsBBox();
		return (box.lower.x < 0) || (box.upper.x > width) || (box.lower.y < 0) || (box.upper.y > height);
	}

	/**
	 * 
	 * @param t A shape.
	 * @return True iff this shape overlaps with shape t.
	 */
	boolean overlaps(Shape t) {
		return betterOverlaps(t);
		//return naiveOverlaps(t);
	}

	/**
	 * 
	 * Naive implementation of overlap detection.
	 * 
	 * @param t A shape.
	 * @return True iff this shape overlaps with shape t.
	 */
	boolean naiveOverlaps(Shape t) {
		for (Block a: blocks) {
			for (Block b: t.blocks) {
				if (Block.overlaps(a,d,b,t.d)) return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * Better implementation of overlap detection.
	 * 
	 * @param t A shape.
	 * @return True iff this shape overlaps with shape t.
	 */
	boolean betterOverlaps(Shape t) {
		return tree.overlaps(d, t.tree, t.d);
	}

	/**
	 * "Clear" the shape from displacement and turned on flags.
	 */
	public void clear() {
		// bring to original position
		d.x = 0;
		d.y = 0;
		// reset clickedOn flag
		clickedOn = false;
		// reset overlaps flag
		overlaps = false;
	}

	/**
	 * 
	 * (Further) displace this shape by displacement vector v.
	 * 
	 * @param v Displacement v.
	 */
	public void displace(Vec2D v) {
		d.addOn(v);
	}

	/**
	 * 
	 * @param g A Graphics2D object.
	 * @param scale The scale from image coordinates to canvas coordinates.
	 */
	public void paint(Graphics g, double scale) {
		// Draw the blocks of the shape.
		for (Block b: blocks)
			b.display(g,scale,d,clickedOn,overlaps);

		// Draw bounding rectangle
		// EXTRA HELP: This code might give you a helpful visualization of the bounding box
		// of the shape. You can use it if you want while developing.
		/*
		BBox box = tree.getBox();
	  	if (box == null) return;
	  	BBox dBox = box.displaced(d);
	  	int x = (int) Math.floor(scale*dBox.lower.x);
	  	int y = (int) Math.floor(scale*dBox.lower.y);
	  	int width = (int) Math.ceil(scale*(dBox.upper.x-dBox.lower.x));
	  	int height = (int) Math.ceil(scale*(dBox.upper.y-dBox.lower.y));
	  	g.drawRect(x,y,width,height);
	  	*/
	  	
	}

	/**
	 * Toggle the clickedOn flag.
	 */
	public void click() {
		clickedOn = !clickedOn;
	}

	/**
	 * 
	 * @param p A point.
	 * @return True iff this shape contains the point p.
	 */
	public boolean contains(Vec2D p) {
		return betterContains(p);
		//return naiveContains(p);
	}

	/**
	 * 
	 * @param p A point.
	 * @return True iff this shape contains the point p.
	 */
	private boolean naiveContains(Vec2D p) {
		// Account for displacement of shape.
		Vec2D newP = p.minus(d);

		for (Block b: blocks) {
			if (b.contains(newP)) return true;
		}
		return false;
	}

	/**
	 * 
	 * @param p A point.
	 * @return True iff this shape contains the point p.
	 */
	private boolean betterContains(Vec2D p) {
		// NOTE: Do not forget to account for the displacement of this shape!
		Vec2D newP = p.minus(d);
		
		return tree.contains(newP);
	}
	
}
