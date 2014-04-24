import java.util.*;

/**
 *
 * A non-empty collection of points organized in a hierarchical binary tree structure.
 *
 */
public class BlockTree 
{
	/**
	 * The bounding box of the blocks contained in this tree. 
	 */
	private BBox box;

	/**
	 * Number of blocks contained in this tree.
	 */
	private int numBlocks;

	/**
	 * Left child (subtree):
	 * (left  == null) iff (this is a leaf node)
	 */
	private BlockTree left;

	/**
	 * Right child (subtree):
	 * (right == null) iff (this is a leaf node)
	 */
	private BlockTree right;

	/**
	 * Block (of a leaf node):
	 * (block == null) iff (this is an intermediate node)
	 */
	private Block block; 

	// REMARK:
	// Leaf node: left, right == null && block != null
	// Intermediate node: left, right != null && block == null

	/**
	 * Construct a binary tree containing blocks.
	 * The tree has no be non-empty, i.e., it must contain at least one block.
	 * 
	 * @param vertices
	 */
	public BlockTree(ArrayList<Block> blocks) 
	{	// Leave the following two "if" statements as they are.
		if (blocks == null)
			throw new IllegalArgumentException("blocks null");
		if (blocks.size() == 0)
			throw new IllegalArgumentException("no blocks");
		
		// Checks if there is only one block and constructs a leaf node.
		if (blocks.size() == 1) {
			numBlocks = 1;
			block = blocks.get(0);
			box = this.block.getBBox();
			left = null;
			right = null;
		// Constructs an intermediate node.
		} else {
			numBlocks = blocks.size();
			block = null;
			
			// Makes a bounding box for the array of blocks.
			Iterator<Block> iter = blocks.iterator();
			box = BBox.findBBox(iter);
			
			BBox leftBBox;
			
			// Splits the bounding box in half along the width if it is greater than the height.
			if (box.getWidth() == box.getLength()) {
				double halfwidth = box.getCenter().x;
				Vec2D leftUp = new Vec2D(halfwidth, box.upper.y);
				
				leftBBox = new BBox(box.lower, leftUp);
			// Splits the bounding box in half along the height if it is greater than the width.
			} else {
				double halfheight = box.getCenter().y;
				Vec2D leftUp = new Vec2D(box.upper.x, halfheight);
				
				leftBBox = new BBox(box.lower, leftUp);
			}
			
			ArrayList<Block> leftBlocks = new ArrayList<Block>();
			ArrayList<Block> rightBlocks = new ArrayList<Block>();
			
			// Adds the list of blocks to either the left or right side.
			for (Block b : blocks) {
				if (leftBBox.overlaps(b.getBBox())) {
					leftBlocks.add(b);
				} else {
					rightBlocks.add(b);
				}
			}
			
			// Checks if either array is empty.
			if (leftBlocks.isEmpty()) {
				leftBlocks.add(rightBlocks.get(0));
				rightBlocks.remove(0);
			}
			
			if (rightBlocks.isEmpty()) {
				rightBlocks.add(leftBlocks.get(0));
				leftBlocks.remove(0);
			}
			
			// Creates the left and right BlockTrees.
			try {
				left = new BlockTree(leftBlocks);
				right = new BlockTree(rightBlocks);
			} catch (IllegalArgumentException e) {
				System.out.println("Illegal Argument Exception " + e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @return The bounding box of this collection of blocks.
	 */
	public BBox getBox() { return box; }

	/**
	 * 
	 * @return True iff this is a leaf node.
	 */
	public boolean isLeaf() {
		return (block != null);
	}

	/**
	 * 
	 * @return True iff this is an intermediate node.
	 */
	public boolean isIntermediate() {
		return !isLeaf();
	}

	/**
	 * 
	 * @return Number of blocks contained in tree.
	 */
	public int getNumBlocks() {
		return numBlocks;
	}

	/**
	 * 
	 * @param p A point.
	 * @return True iff this collection of blocks contains the point p.
	 */
	public boolean contains(Vec2D p) {
		return contains(this, p);
	}
	
	/**
	 * 
	 * @param t A BlockTree.
	 * @param p A point.
	 * @return True iff t contains the point p.
	 */
	private boolean contains(BlockTree t, Vec2D p) {
		// Checks if the BlockTree is null.
		if (t == null) {
			return false;
		// Checks if the bounding box contains the point.
		} else if (!t.box.contains(p)) {
			return false;
		// Checks if the BlockTree is a leaf node and if it contains the point.
		} else if (t.isLeaf()) {
			return t.box.contains(p);
		// Checks the BlockTree's left and right bounding boxes with the point.
		} else {
			return contains(t.left, p) || contains(t.right, p);
		}
	}

	/**
	 * 
	 * @param thisD Displacement of this tree.
	 * @param t A tree of blocks.
	 * @param d Displacement of tree t.
	 * @return True iff this tree and tree t overlap (account for displacements).
	 */
	public boolean overlaps(Vec2D thisD, BlockTree t, Vec2D d) {
		return overlaps(this, thisD, t, d);
	}
	
	/**
	 * 
	 * @param thisT A tree of blocks.
	 * @param thisD Displacement of tree thisT.
	 * @param t A tree of blocks.
	 * @param d Displacement of tree t.
	 * @return True iff tree thisT and tree t overlap (account for displacements).
	 */
	private boolean overlaps(BlockTree thisT, Vec2D thisD, BlockTree t, Vec2D d) {
		// Checks if either BlockTree is null.
		if (thisT == null || t == null) {
			return false;
		}
		
		BBox newThisT = new BBox(thisT.box).displaced(thisD);
		BBox newT = new BBox(t.box).displaced(d);
		
		// Checks if the bounding boxes overlap.
		if (!newThisT.overlaps(newT)) {
			return false;
		// Checks if both BlockTrees are leaf nodes and if they overlap.
		} else if (thisT.isLeaf() && t.isLeaf()) {
			return newThisT.overlaps(newT);
		// Checks if only one BlockTree is a leaf node and if it overlaps with the other's left and right bounding boxes.
		} else if (thisT.isLeaf()) {
			boolean thisTAndLT = overlaps(thisT, thisD, t.left, d);
			boolean thisTAndRT = overlaps(thisT, thisD, t.right, d);
			return thisTAndLT || thisTAndRT;
		// Checks if only one BlockTree is a leaf node and if it overlaps with the other's left and right bounding boxes.
		} else if (t.isLeaf()) {
			boolean tAndThisLT = overlaps(thisT.left, thisD, t, d);
			boolean tAndThisRT = overlaps(thisT.right, thisD, t, d);
			return tAndThisLT || tAndThisRT;
		// Checks each BlockTrees' left and right bounding boxes with each other.
		} else {
			boolean lThisTAndLT = overlaps(thisT.left, thisD, t.left, d);
			boolean lThisTAndRT = overlaps(thisT.left, thisD, t.right, d);
			boolean rThisTAndLT = overlaps(thisT.right, thisD, t.left, d);
			boolean rThisTAndRT = overlaps(thisT.right, thisD, t.right, d);
			return lThisTAndLT || lThisTAndRT || rThisTAndLT || rThisTAndRT;
		}
	}

	public String toString() {
		return toString(new Vec2D(0,0));
	}

	/**
	 * 
	 * @param d Displacement vector.
	 * @return String representation of this tree (displaced by d).
	 */
	public String toString(Vec2D d) {
		return toStringAux(d,"");
	}

	/**
	 * Useful for creating appropriate indentation for the toString method.
	 */
	private static final String indentation = "   ";
	/**
	 * 
	 * @param d Displacement vector.
	 * @param indent Indentation.
	 * @return String representation of this tree (displaced by d).
	 */
	private String toStringAux(Vec2D d, String indent) 
	{
		String str = indent + "Box: ";
		str += "(" + (box.lower.x + d.x) + "," + (box.lower.y + d.y) + ")";
		str += " -- ";
		str += "(" + (box.upper.x + d.x) + "," + (box.upper.y + d.y) + ")";
		str += "\n";

		if (isLeaf()) {
			String vStr = "(" + (block.p.x + d.x) + "," + (block.p.y + d.y) + ")" + block.h; 
			str += indent + "Leaf: " + vStr + "\n";
		}
		else {
			String newIndent = indent + indentation;
			str += left.toStringAux(d,newIndent);
			str += right.toStringAux(d,newIndent);
		}

		return str;
	}

}
