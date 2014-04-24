import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.*;

/**
 *
 * JPanel for the image drop down menu and the buttons.
 *
 */
public class ButtonPanel extends JPanel implements ActionListener {
	JComboBox imageList;
	String[] bmpFileNames;
	
	Canvas canvas;
	JButton reset, shuffle;
	
	public ButtonPanel(Canvas c, String[] bmpFileNames) {
		this.canvas = c;
		
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		// JComboBox for available images.
		this.bmpFileNames = bmpFileNames;
		// Strip the extension from the filenames.
		String[] namesNoExt = new String[bmpFileNames.length];
		for (int i=0; i<bmpFileNames.length; i++) {
			int end = bmpFileNames[i].length();
			namesNoExt[i] = bmpFileNames[i].substring(0,end-4);
		}
		imageList = new JComboBox(namesNoExt);
		imageList.setAlignmentX(Component.CENTER_ALIGNMENT);
		imageList.addActionListener(this);
		add(imageList);

		// Button to shuffle shapes.
		shuffle = new JButton("Shuffle");
		shuffle.setAlignmentX(Component.CENTER_ALIGNMENT);
		shuffle.addActionListener(this);
		add(shuffle);
		
		// Button to reset shapes.
		reset = new JButton("Reset");
		reset.setAlignmentX(Component.CENTER_ALIGNMENT);
		reset.addActionListener(this);
		add(reset);
		
	}
	
	/**
	 * Handle the events from the drop down menu and the buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reset) {
			canvas.resetObjects();
		}
		if (e.getSource() == shuffle) {
			canvas.shuffleObjects();
		}
		else if (e.getSource() == imageList) {
			updateImage(e);
		}
	}
	
	/**
	 * 
	 * Change the current image.
	 * 
	 * @param e Event
	 */
	private void updateImage(ActionEvent e) {
		JComboBox box = (JComboBox) e.getSource();
		String fileName = (String) box.getSelectedItem() + ".bmp";
		try {
			canvas.setNewImage(fileName);
		}
		catch (IOException exc) {
			System.out.println(exc.getMessage());
			return;
		}
	}
	
}
