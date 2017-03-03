package View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;

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
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	double[][] layout = new double[2][2];
            	layout[0][0] = 30.0;
            	layout[0][1] = 30.0;
            	layout[1][0] = 50.0;
            	layout[1][1] = 50.0;
            	View view = new View(layout);
            }
        });
    }

}
