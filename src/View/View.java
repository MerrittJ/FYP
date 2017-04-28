package View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;

/**Swing GUI to visualise layouts
 * @author JoshMerritt
 * 
 * 
 *
 */
public class View {

	public View(double[][] layout){
		createView(layout);
	}

	public void createView(double[][] layout){
		JFrame frame = new JFrame("WFLOP Visualisation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		LayoutDrawer ld = new LayoutDrawer(layout);
		
		ld.setBackground(new Color(248, 213, 131));
		ld.setPreferredSize(new Dimension(1800, 1000)); //TODO 7000x14000???
		
		frame.getContentPane().add(ld, BorderLayout.CENTER);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

}
