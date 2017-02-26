package View;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;


public class LayoutDrawer extends JPanel {
	private double[][] layout;

	public LayoutDrawer(double[][] layout) {
		this.layout = layout;
	}


	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		for (int i = 0;i<layout.length;i++){
			double[][] cross = drawX(layout[i][0], layout[i][1]);
			g2d.draw(new Line2D.Double(cross[0][0], cross[0][1], cross[1][0], cross[1][1]));
			g2d.draw(new Line2D.Double(cross[2][0], cross[2][1], cross[3][0], cross[3][1]));
			g2d.draw(new Ellipse2D.Double(layout[i][0]-154, layout[i][1]-154, 308, 308));
		}
		
	}

	private double[][] drawX(double x, double y){
		double[][] cross = new double[4][2];
		cross[0][0] = x-10;
		cross[0][1] = y;
		cross[1][0] = x+10;
		cross[1][1] = y;
		
		cross[2][0] = x;
		cross[2][1] = y-10;
		cross[3][0] = x;
		cross[3][1] = y+10;
		
		return cross;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		doDrawing(g2);
	}

}
