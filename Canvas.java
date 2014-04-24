import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

/**
 *
 * This JPanel represents the area on the main window where images/shapes are drawn.
 * It also handles the events for clicking on shapes and moving them around.
 *
 */
public class Canvas extends JPanel implements MouseListener, MouseMotionListener 
{
	/** JFrame of main window. */
	GUI mainWindow;
	/** Width in canvas coordinates. */
	int width;
	/** Height in canvas coordinates. */
	int height;
	/** Image width in image coordinates (contained in [0,1]) */
	double imgWidth;
	/** Image height in image coordinates (contained in [0,1]) */
	double imgHeight;
	/** Scale factor to translate from image to canvas coordinates. */
	double scale;
	/** Collection of all shapes. */
	private ArrayList<Shape> shapes;
	/** Selected shape (to be dragged). */
	private Shape selectedShape;
	/** Last position at which shape was selected. */
	private Vec2D selectedAt;
	/** Random generator for shuffling shapes. */
	Random gen = new Random(System.currentTimeMillis());
	/** Default image file name */
	final String defImgFileName = "Letters.bmp";

	/**
	 * 
	 * @param mainWindow JFrame of main window.
	 * @param width Width of canvas.
	 * @param height Height of canvas.
	 */
	public Canvas(GUI mainWindow, int width, int height) 
	{
		this.mainWindow = mainWindow;
		this.width = width;
		this.height = height;
		this.scale = Math.min(width,height);

		setBackground(Color.WHITE);
		setFocusable(true);
		setLayout(null);
		setDoubleBuffered(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		
		// Load default image
		try {
			setNewImage(defImgFileName);
		}
		catch (IOException e) {
			System.out.println("Images not installed properly.");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Paints the canvas.
	 */
	public void paint(Graphics g) 
	{
		super.paint(g);		
		if (shapes == null) return;

		long timeOverlap = - System.currentTimeMillis();

		// reset overlaps
		for (Shape s: shapes) s.overlaps = false;

		// Calculate overlaps
		for (Shape s1: shapes) {
			for (Shape s2: shapes) {
				if (s1.hashCode() < s2.hashCode()) { // check distinct pairs once
					if (s1.overlaps(s2)) {
						s1.overlaps = true;
						s2.overlaps = true;
					}
				}
			}
		}

		// Calculate overlap with off-screen canvas: (Can't move objects off-screen to win)
		for (Shape s1: shapes) {
			if(s1.overlapsOffscreen(width/scale, height/scale)) // omit "pixelWidth"
				s1.overlaps = true;
		}

		timeOverlap += System.currentTimeMillis();

		long timeDisplay = - System.currentTimeMillis();
		for (Shape s: shapes) {
			s.paint(g,scale);
		}
		timeDisplay += System.currentTimeMillis();
		System.out.println("Timing Breakdown: Overlap="+timeOverlap+"ms,  Display="+timeDisplay+"ms");
	}

	/**
	 * 
	 * Changes the current image.
	 * 
	 * @param imgFileName Image file name
	 * @throws IOException
	 */
	public void setNewImage(String imgFileName) throws IOException 
	{
		System.out.println("**** New Image ****");
		ImageBlocker blocker = new ImageBlocker(imgFileName);
		System.out.println("New image loaded: " + imgFileName);

		ArrayList<HashSet<Block>> parsedShapes = blocker.getLargeConnectedComponents(0.10);
		int shapesLeft = parsedShapes.size();
		System.out.println("# shapes left = " + shapesLeft);
		shapes = new ArrayList<Shape>();
		for (HashSet<Block> s: parsedShapes) {
			shapes.add(new Shape(s));
		}

		// Default values.
		imgWidth = 1.0;
		imgHeight = 1.0;

		double minX = Double.POSITIVE_INFINITY;
		double minY = minX;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = maxX;
		for (Shape s: shapes) {
			BBox box = s.tree.getBox();
			if (box.lower.x < minX) minX = box.lower.x;
			if (box.lower.y < minY) minY = box.lower.y;
			if (box.upper.x > maxX) maxX = box.upper.x;
			if (box.upper.y > maxY) maxY = box.upper.y;
		}
		System.out.println("Area Covered: (" + minX + "," + minY + ") -- (" + maxX + "," + maxY + ")");
		imgWidth = maxX;
		imgHeight = maxY;
		
		// Calculate the scale.
		double canvasRatio = ((double) width) / ((double) height);
		double imgRatio = imgWidth / imgHeight;
		if (imgRatio <= canvasRatio) {
			// adjust according to height
			scale = ((double) height) / imgHeight;
		}
		else {
			// adjust according to width
			scale = ((double) width) / imgWidth;
		}

		repaint();
	}

	/**
	 * Shuffle the objects.
	 */
	public void shuffleObjects() 
	{
		if(shapes==null) return;

		for (Shape s: shapes) {
			// move shape randomly as long as it is inside the canvas

			s.clear(); // first clear object from displacement, etc...

			BBox box = s.tree.getBox();

			// random movement along x
			double rnd = gen.nextDouble(); // [0,1]
			double xLeft = box.lower.x;
			double xRight = imgWidth - box.upper.x;
			rnd *= xLeft + xRight; // [0,xL+xR]
			double dx = rnd - xLeft; // [-xL,xR]

			// random movement along x
			rnd = gen.nextDouble(); // [0,1]
			double yTop = box.lower.y;
			double yBtm = imgHeight - box.upper.y;
			rnd *= (yTop + yBtm); // [0,yT+yB]
			System.out.println(yTop + yBtm);
			double dy = rnd - yTop; // [-yT,yB]
			System.out.println(rnd + " " + yTop + " " + yBtm + " " + dy);

			Vec2D d = new Vec2D(dx,dy);
			s.displace(d);
			//System.out.println(s.getAbsBBox());
		}

		repaint();
	}

	/**
	 * Resets shapes to their original positions.
	 */
	public void resetObjects() 
	{
		if(shapes==null) return;

		for (Shape s: shapes)
			s.clear();

		repaint();
	}

	
	/**
	 * 
	 * @param e MouseEvent
	 * @return The location of the mouse when the event occured in image coordinates.
	 */
	private Vec2D getCoords(MouseEvent e) 
	{
		// Get position.
		int mX = e.getX();
		int mY = e.getY();

		// Transform into image coordinates. (center of pixel)
		double pixelWidth = 1.0 / ((double) scale);
		double mXImg = (mX + pixelWidth) / ((double) scale);
		double mYImg = (mY + pixelWidth) / ((double) scale);

		return new Vec2D(mXImg,mYImg);
	}

	/**
	 * Handles click event.
	 */
	public void mouseClicked(MouseEvent e) 
	{
		// Get mouse position when clicked.
		Vec2D p = getCoords(e);

		for (Shape s : shapes) {
			if (s.contains(p)) {
				s.click();
				repaint();
			}
		}
	}

	public void mouseEntered(MouseEvent e) {   }

	public void mouseExited(MouseEvent e) {   }

	/**
	 * Handles the event of left mouse button being pressed.
	 */
	public void mousePressed(MouseEvent e) 
	{
		// Left button should be pressed.
		if (e.getButton() != MouseEvent.BUTTON1) return;

		// Get mouse position when pressed.
		Vec2D p = getCoords(e);

		// reset
		selectedShape = null;
		selectedAt = null;

		for (Shape s: shapes) {
			if (s.contains(p)) {
				selectedShape = s;
				selectedAt = p;
				break;
			}
		}
	}

	public void mouseMoved(MouseEvent e) {   }

	/**
	 * Handles the event of left mouse button being released.
	 */
	public void mouseReleased(MouseEvent e) 
	{
		if (e.getButton() != MouseEvent.BUTTON1) return;

		// reset
		selectedShape = null;
		selectedAt = null;
	}

	/**
	 * Handles the event of mouse being dragged.
	 */
	public void mouseDragged(MouseEvent e) 
	{
		if (selectedShape == null || selectedAt == null) {
			// nothing selected
			return;
		}

		Vec2D droppedAt = getCoords(e);
		Vec2D d = droppedAt.minus(selectedAt); // displacement due to dragging
		selectedShape.displace(d);

		// Update the selected location to this one.
		selectedAt = droppedAt;

		repaint();
	}

}
