import java.awt.Dimension;
import javax.swing.*;
import java.io.*;

/**
 *
 * The class for the main program.
 *
 */
public class GUI extends JFrame {
	// NOTE:
	// You can adjust the values of WIDTH and HEIGHT to make the canvas
	// fit well your computer screen.
	
	/**
	 * Width of the canvas.
	 */
	private static int WIDTH = 800;
	/**
	 * Height of the canvas.
	 */
	private static int HEIGHT = 650;
	
	JPanel container;
	Canvas canvas;
	private String[] bmpFileNames;

	class BmpFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".bmp");
		}
	}
	
	public GUI() {
		// Find all bmps that are in current directory.
		File currentDir = new File(".");
		bmpFileNames = currentDir.list(new BmpFilter());
		
		if (bmpFileNames.length == 0)
			throw new RuntimeException("No image files in current directory.");
		
		System.out.println("Bmp File Names:");
		for (String f: bmpFileNames)
			System.out.println(f);
		
		// Initialize JFrame settings.
		setTitle("Collision Detector");
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		container = new JPanel();

		canvas = new Canvas(this,WIDTH,HEIGHT);
		canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		container.add(canvas);
		
		ButtonPanel b = new ButtonPanel(canvas,bmpFileNames);
		container.add(b);
		
		add(container);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static void main(String[] args) {		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUI();
			}
		});
	}

}
