package View;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/**Swing component used to visualise layouts
 * @author JoshMerritt
 * 
 * 
 *
 */
public class LayoutDrawer extends JPanel {
	private double[][] layout;
	private double scale = 0.1;
	private double minBound = 308 * scale;

	public LayoutDrawer(double[][] layout) {
		double[][] scLayout = new double[layout.length][2];
		for (int i = 0; i<layout.length;i++){
			scLayout[i][0] = layout[i][0] * scale;
			scLayout[i][1] = layout[i][1] * scale;
		}
		this.layout = scLayout;
	}

	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		for (int i = 0;i<layout.length;i++){
			double[][] cross = drawX(layout[i][0], layout[i][1]);
			g2d.draw(new Line2D.Double(cross[0][0], cross[0][1], cross[1][0], cross[1][1]));
			g2d.draw(new Line2D.Double(cross[2][0], cross[2][1], cross[3][0], cross[3][1]));
			g2d.draw(new Ellipse2D.Double(layout[i][1]-(minBound/2), layout[i][0]-(minBound/2), minBound, minBound)); //154, 308
		}
		
	}

	private double[][] drawX(double x, double y){
		double[][] cross = new double[4][2];
		cross[0][0] = y-5;
		cross[0][1] = x;
		cross[1][0] = y+5;
		cross[1][1] = x;
		
		cross[2][0] = y;
		cross[2][1] = x-5;
		cross[3][0] = y;
		cross[3][1] = x+5;
		
		return cross;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		doDrawing(g2);
	}

}
