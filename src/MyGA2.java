import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class MyGA2 {
	
	private WindFarmLayoutEvaluator wfle;
	private int numOfPops = 1;
	private int generations = 0;
	private int maxTurbines = 450;
	private ArrayList<double[][]> populations; //pop#, turbine#, coords
	double minSpac;
	private Random rnd = new Random();


	public MyGA2(WindFarmLayoutEvaluator evaluator){
		wfle = evaluator;
		double minFit = Double.MAX_VALUE;
		double thisRun = 0.0;
		populations = new ArrayList<double[][]>();
		minSpac = 8.001 * wfle.getTurbineRadius();
		
		//initialise populations
		for (int i = 0; i < numOfPops;){
			//double[][] layout = initRandom();
			//double [][] layout = initGrid();
			double[][] layout = initSpaced();
			if (wfle.checkConstraint(layout)){
				populations.add(layout);
				i++;
				thisRun = wfle.evaluate(layout);
				//System.out.println("this run: " + thisRun);
				System.out.println(layout.length + " " + thisRun );
			}
			else {
				System.out.println("layout failed");
			}
		}
		
		//local search, remove/add one
		for (int i=0;i<generations;i++){
			System.out.println("generation "+i);
			
			for (int k = 0;k<populations.size();k++){
				System.out.println("k " +k);
				//greedyRemoveOne(k);
				//randomAddOne(k);
			}
//			if (!LSRemoveOne()){
//				System.out.println("no turbines removed");
//			if (!LSAddOne()){	//if no turbines removed/added from any pop
//				System.out.println("no turbines added");
//				break; //stop for 
//			}
		}
		
		int i = 0;
		for (double[][] layout : populations){
			System.out.println("Layout " + i + " size " + layout.length);
			i++;
		}
	}
	
	public void randomAddOne(int popIndex){
		double[][] layout = populations.get(popIndex);
		double[][] layoutOptConv;
		do {	
			ArrayList<double[]> layoutOpt = convertA(layout);
			double[] point = {rnd.nextDouble()*wfle.getFarmWidth(), rnd.nextDouble()*wfle.getFarmHeight()};
			layoutOpt.add(point);
			layoutOptConv = convertAL(layoutOpt);
			System.out.println("atmpt");
		}
		while (!wfle.checkConstraint(layoutOptConv));
		
		double fit = wfle.evaluate(layoutOptConv);
		System.out.println(layoutOptConv.length + " " + fit);
		
	}
	
	public void greedyRemoveOne(int popIndex){
		int bestIndex = 0;
		boolean foundBetter = false;
		ArrayList<double[]> layoutOpt;
		double[][] layout = populations.get(popIndex);
		double orig = wfle.evaluate(layout);
		
		for (int i = 0; i<populations.get(popIndex).length;i++){
			layoutOpt = convertA(layout);
			layoutOpt.remove(i);
			double[][] layoutOptA = convertAL(layoutOpt);
			
			double opt = wfle.evaluate(layoutOptA);
			System.out.println("try "+ i + " orig " + orig + " opt " + opt);
			if (opt < orig){
				bestIndex = i;
				foundBetter = true;
				System.out.println("better");
			}
		}
		
		if (foundBetter){
			ArrayList<double[]> finalOpt = convertA(layout);
			finalOpt.remove(bestIndex);
			double[][] finalOptA = convertAL(finalOpt);
			populations.set(popIndex, finalOptA);
		}
		else {
			System.out.println("no better found");
		}
	}
	
	public boolean LSAddOne(){
		boolean added = false; //check if any turbine added to a pop
		for (ListIterator<double[][]> iter = populations.listIterator(); iter.hasNext();){
			int thisInd = iter.nextIndex();
			double[][] thisLayout = iter.next();
			ArrayList<double[]> convLayout = convertA(thisLayout);
			ArrayList<double[]> convLayoutOpt = new ArrayList<double[]>();

			for (double[] point : convLayout){
				convLayoutOpt.add(point);
			}
			
			double x = 0.0;
			double y = 0.0;
			boolean viol = false; //check if any violations when adding new coord
			
			do {
				x = rnd.nextDouble() * wfle.getFarmWidth();
				y = rnd.nextDouble() * wfle.getFarmHeight();
				
				viol = false;
				for (int k=0;k<thisLayout.length;k++){
					if (
							(thisLayout[k][0] == 0.0 && thisLayout[k][1] == 0.0) ||
							(minSpac > getDist(thisLayout[k][0], thisLayout[k][1], x, y))
							){
						viol = true;
						break;
					}
				}		
			}
			while(viol); //keep cycling new coords until it can be added
			
			double[] pair = new double[] {x,y};
			convLayoutOpt.add(pair);

			double[][] thisLayoutOpt = convertAL(convLayoutOpt);

			double optFit = wfle.evaluate(thisLayoutOpt);
			double thisFit = wfle.evaluate(thisLayout);
			System.out.println("opt " + optFit + " this " + thisFit);
			if(optFit < thisFit){
				populations.set(thisInd, thisLayoutOpt);
				System.out.println("added turbine");
				added = true;
			}
			else{
				System.out.println("turbine not added");
			}
		}
		
		return added; //if any turbines added, continue passes
		
	}
	
	public boolean LSRemoveOne(){
		boolean removed = false; //check if turbine removed
		for (ListIterator<double[][]> iter = populations.listIterator(); iter.hasNext();){
			int thisInd = iter.nextIndex();
			double[][] thisLayout = iter.next();
			ArrayList<double[]> convLayout = convertA(thisLayout);
			ArrayList<double[]> convLayoutOpt = new ArrayList<double[]>();

			for (double[] point : convLayout){
				convLayoutOpt.add(point);
			}

			int toRem = rnd.nextInt(convLayoutOpt.size());
			convLayoutOpt.remove(toRem);

			double[][] thisLayoutOpt = convertAL(convLayoutOpt);

			double optFit = wfle.evaluate(thisLayoutOpt);
			double thisFit = wfle.evaluate(thisLayout);
			System.out.println("opt " + optFit + " this " + thisFit);
			if(optFit < thisFit){
				populations.set(thisInd, thisLayoutOpt);
				System.out.println("removed turbine");
				removed = true;
			}
			else{
				System.out.println("turbine not removed");
				removed = false;
			}
		}
		
		return removed; //if any turbines removed, continue passes
		
	}
	
	public double[][] initRandom(){
		//System.out.println("turbines " + maxTurbines);

		double[][] layout = new double[maxTurbines][2];
		
		double x = rnd.nextDouble() * wfle.getFarmWidth();
		double y = rnd.nextDouble() * wfle.getFarmHeight();
		layout[0][0] = x;
		layout[0][1] = y;
		
		int i = 1;
		while (i<layout.length){//wfle.checkConstraint(layout));
			x = rnd.nextDouble() * wfle.getFarmWidth();
			y = rnd.nextDouble() * wfle.getFarmHeight();
			
			boolean validCoord = true;
			for (int k=0;k<layout.length;k++){
				if (layout[k][0] == 0.0 && layout[k][1] == 0.0){
					break;
				}
				
				if (minSpac > getDist(layout[k][0], layout[k][1], x, y)){
					validCoord = false;
				}
			}
			
			if (validCoord){
				layout[i][0] = x;
				layout[i][1] = y;
				i++;
			}
		}
		return layout;
	}
	
	public double[][] initGrid(){
		double interval = 8.001 * wfle.getTurbineRadius();
		double spacerX = wfle.getFarmWidth()*0.05; //randomising spacing of grid up to 1%
		double spacerY = wfle.getFarmHeight()*0.05;
		ArrayList<double[]> layout = new ArrayList<double[]>();

		for (double x=0.0; x<wfle.getFarmWidth(); x=x+(interval+(rnd.nextDouble()*spacerX))) {
		    for (double y=0.0; y<wfle.getFarmHeight(); y=y+(interval+(rnd.nextDouble()*spacerY))) {
		    	double[] point = {x, y};
		    	layout.add(point);
	          }
	      }
		
		//System.out.println("turbines " + layout.size());
		return convertAL(layout);
	}
	
	public void initPixels(){
		int fWidth = (int)wfle.getFarmWidth();
		int fHeight = (int)wfle.getFarmHeight();
		boolean[][] pixels = new boolean[fWidth][fHeight];
		for (int i = 0;i<fWidth;i++){
			for (int k=0;k<fHeight;k++){
				pixels[i][k] = true;
			}
		}
		
		int[] point = {rnd.nextInt(fWidth), rnd.nextInt(fHeight)};
		
		for (int i = 0;i<fWidth;i++){
			for (int k=0;k<fHeight;k++){
				if (getDist((double)point[0], (double)point[1], (double)i, (double)k) < 308){
					pixels[i][k] = false;
				}
			}
		}
	}
	
	public double[][] initSpaced(){
		System.out.println("starting spaced");
		double fWidth = wfle.getFarmWidth(); //def 7000
		double fHeight = wfle.getFarmHeight(); //def 14000
		double gridSize = 400; //7000/400=17.5, 17*34=578

		double[][] layout = new double[562][2];//(int) Math.floor(((fWidth/gridSize) * (fHeight/gridSize)))][2];
		double[] point1 = new double[2];
		double gridMarkerX = 0.0;
		double gridMarkerY = 0.0;
		point1[0] = gridMarkerX + rnd.nextDouble()*gridSize;
		point1[1] = gridMarkerY + rnd.nextDouble()*gridSize;
		//System.out.println("new point " + point1[0] +" "+ point1[1]);
		gridMarkerX += gridSize;
		gridMarkerY += gridSize;
		layout[0] = point1;
		int layInd = 1;
		
		loop:
		for (;gridMarkerX<fWidth;gridMarkerX += gridSize){//-gridSize
			for (;gridMarkerY<fHeight;){
				double[] point = new double[2];
				point[0] = gridMarkerX + rnd.nextDouble()*gridSize;
				point[1] = gridMarkerY + rnd.nextDouble()*gridSize;
				//System.out.println("new point " + point[0] +" "+ point[1]);
				
				if (pointValid(point, layout)){
					System.out.println("ind " + layInd);
					layout[layInd] = point;
					layInd++;
					gridMarkerY += gridSize;
					if (layInd >= layout.length){
						break loop;
					}
				}
			}
			gridMarkerY = 0.0;
		}
		
		System.out.println("layout " +layout.length);
		
		return layout;
	}
	
	public double[][] moveOne(double[][] layout){
		double[][] holder = new double[layout.length-1][2];
		int toMoveInd = rnd.nextInt(layout.length);
		double[] toMovePoint = {layout[toMoveInd][0], layout[toMoveInd][1]};
		layout[toMoveInd] = null;
		int dir;
		for (int i = 0;i<layout.length-1;i++){
			if (layout[i] != null){
				holder[i][0] = layout[i][0];
				holder[i][1] = layout[i][1];
			}
			
		}
	}
	
	public boolean tooClose(double x1, double y1, double x2, double y2){
		if ((Math.abs((double)(x2-x1)) < 308) && (Math.abs((double)(y2-y1)) < 308)){ //check if in square
			double dist = getDist(x1,y1,x2,y2);
			//System.out.println("in sq, dist " + dist + " xy xy2 " + x1 +" "+y1+" "+x2+" "+y2);
			if (dist < 308){ //check if in radius
				return true;
			}
		}
		return false;
	}
	
	public double getDist(double x1, double y1, double x2, double y2) {
		return Math.hypot(x2-x1, y2-y1);
	}
	
	public boolean pointValid(double[] point, double[][] layout){
		for (int i=0; i<layout.length;i++){
			if (layout[i] == null){
				break;
			}
			else if (tooClose(point[0], point[1], layout[i][0], layout[i][1])){
				System.out.println("point invalid");
				return false;
			}
		}
		System.out.println("point valid");
		return true;
	}

	public double[][] convertAL(ArrayList<double[]> al){
		double[][] layout = new double[al.size()][];
		for (int i = 0; i< layout.length;i++){
			if (al.get(i) == null){
				//skip
			}
			else {
				double[] row = al.get(i);
				layout[i] = row;
			}
		}
		return layout;
	}
	
	public ArrayList<double[]> convertA(double[][] layout){
		ArrayList<double[]> nl = new ArrayList<double[]>();
		for (int i=0; i<layout.length;i++){
			nl.add(layout[i]);
		}
		return nl;
	}
}