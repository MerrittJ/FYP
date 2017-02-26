import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import Model.WindFarmLayoutEvaluator;

public class MyFirstGA {

	WindFarmLayoutEvaluator wfle;
	boolean[][] pops;
	Random rand;
	int num_pop;
	int tour_size;
	double mut_rate;
	double cross_rate;
	ArrayList<double[]> grid;

	public MyFirstGA(WindFarmLayoutEvaluator evaluator) {
		wfle = evaluator;
		rand = new Random();
		num_pop = 1; //def=20
		tour_size = 4;
		mut_rate = 0.05;
		cross_rate = 0.40;
		grid = new ArrayList<double[]>();
	}

	public void run() {
		// set up grid
		// centers must be > 8*R apart
		double interval = 8.001 * wfle.getTurbineRadius();

		for (double x=0.0; x<wfle.getFarmWidth(); x+=interval) {
			for (double y=0.0; y<wfle.getFarmHeight(); y+=interval) {
				double[] point = {x, y};
				grid.add(point);
			}
		}

		// initialize populations
		pops = new boolean[num_pop][grid.size()];

		for (int p=0; p<num_pop; p++) {
			for (int i=0; i<grid.size(); i++) {
				pops[p][i] = true;//rand.nextBoolean(); //random number of turbines
			}
		}	
		
		
		
//		long stop=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
//		do {
//		}
//		while (stop>System.nanoTime());
//		
//		// GA
		for (int i=0; i<10; i++) {
		evaluate();
		}
	}
	
	private void evaluate() {
		double best = Double.MAX_VALUE;
		for (int p=0; p<num_pop; p++) {
			int nturbines=0;
			for (int i=0; i<grid.size(); i++) {
				if (pops[p][i]) {
					nturbines++;
				}
			}

			double[][] layout = new double[nturbines][2];
			int l_i = 0;
			for (int i=0; i<grid.size(); i++) {
				if (pops[p][i]) {
					layout[l_i][0] = grid.get(i)[0];
					layout[l_i][1] = grid.get(i)[1];
					l_i++;
					System.out.println(layout[l_i][0] + " "+ layout[l_i][1]);
				}
			}
			
			double[][] oneLess = LSRemoveTurbine(layout);
			
			if (wfle.checkConstraint(oneLess) && wfle.checkConstraint(layout)) {
				//if (wfle.evaluate(layout) < )
			
				System.out.println(oneLess.length);
				double thisRun = wfle.evaluate(layout);
				System.out.println(thisRun);
				if (thisRun < best){
					best = thisRun;
				}
			} else {
				System.out.println("It doesn't work");
			}

		}
		System.out.println(best);
	}
	
	public double[][] LSRemoveTurbine(double[][] layout){
		Random rnd = new Random();
		int toRem = rnd.nextInt(layout.length);
		
		double[][] newLayout = new double[layout.length-1][2];
		int k = 0;
		for (int i = 0;i<layout.length;i++){
			if (i != toRem){
				newLayout[k] = layout[i];
				k++;
			}
		}

		return newLayout;
	}
}
