package Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class MyGA2 {

	private WindFarmLayoutEvaluator wfle;
	private double initGridSpacing;
	private int popNo;
	private int genNo;
	private int tournSize;
	private int maxTurbines = 200; //TODO remove?
	private double crossProb; //TODO explain this?
	private double mutationRate;
	private double maxGausDist; //max dist a turbine can move during mutation
	private boolean useRepA;
	private ArrayList<double[][]> populations; //pop#, turbine#, coords
	//TODO rewrite as HashMap?
	private ArrayList<Double> fitnesses;
	private double[][] bestLayout;
	double minSpac;
	private Random rnd = new Random();
	private Utils utils;


	public MyGA2(WindFarmLayoutEvaluator evaluator){
		wfle = evaluator;
		utils = new Utils(wfle);
		loadSettings(utils.getSettings());
		double thisRun = 0.0;
		populations = new ArrayList<double[][]>();
		fitnesses = new ArrayList<Double>();
		minSpac = 8.001 * wfle.getTurbineRadius();

		//initialise populations
		for (int i = 0; i < popNo;){
			double[][] layout;
			//layout = initRandom();
			layout = initGridRnd();
			//layout = initSpaced();
			//layout = initGridEven();

			if (wfle.checkConstraint(layout)){
				populations.add(layout);
				i++;
				thisRun = wfle.evaluate(layout);
				fitnesses.add(thisRun);
				System.out.println(layout.length + " " + thisRun );
			}
			else {
				System.out.println("layout failed");
			}
		}

		for (int i=0;i<genNo;i++){
			System.out.println("generation "+i);
			//TODO fix this >3
			if (populations.size() >= 3){
				ArrayList<double[][]> parents = tournament(populations, fitnesses, tournSize);
				ArrayList<double[][]> children = crossover(parents);
				children = mutate(children, mutationRate);
				populations = addChildren(populations, children);
				populations = repairAll(populations, useRepA); 
				fitnesses = evalGen(populations);
				populations = selection(populations, fitnesses);//TODO this isn't working
			}
			utils.printFits(false, fitnesses, populations);
		}

		utils.printFits(true, fitnesses, populations); //wrong pops being kept each gen
	}

	public void randomAddOne(int popIndex){
		double[][] layout = populations.get(popIndex);
		double[][] layoutOptConv;
		do {	
			ArrayList<double[]> layoutOpt = utils.convertA(layout);
			double[] point = {rnd.nextDouble()*wfle.getFarmWidth(), rnd.nextDouble()*wfle.getFarmHeight()};
			layoutOpt.add(point);
			layoutOptConv = utils.convertAL(layoutOpt);
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
			layoutOpt = utils.convertA(layout);
			layoutOpt.remove(i);
			double[][] layoutOptA = utils.convertAL(layoutOpt);

			double opt = wfle.evaluate(layoutOptA);
			System.out.println("try "+ i + " orig " + orig + " opt " + opt);
			if (opt < orig){
				bestIndex = i;
				foundBetter = true;
				System.out.println("better");
			}
		}

		if (foundBetter){
			ArrayList<double[]> finalOpt = utils.convertA(layout);
			finalOpt.remove(bestIndex);
			double[][] finalOptA = utils.convertAL(finalOpt);
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
			ArrayList<double[]> convLayout = utils.convertA(thisLayout);
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
							(minSpac > utils.getDist(thisLayout[k][0], thisLayout[k][1], x, y))
							){
						viol = true;
						break;
					}
				}		
			}
			while(viol); //keep cycling new coords until it can be added

			double[] pair = new double[] {x,y};
			convLayoutOpt.add(pair);

			double[][] thisLayoutOpt = utils.convertAL(convLayoutOpt);

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
			ArrayList<double[]> convLayout = utils.convertA(thisLayout);
			ArrayList<double[]> convLayoutOpt = new ArrayList<double[]>();

			for (double[] point : convLayout){
				convLayoutOpt.add(point);
			}

			int toRem = rnd.nextInt(convLayoutOpt.size());
			convLayoutOpt.remove(toRem);

			double[][] thisLayoutOpt = utils.convertAL(convLayoutOpt);

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

	public double[][] moveOne(double[][] layout, int moveInd){
		//System.out.println(wfle.evaluate(layout));
		double[][] holder = new double[layout.length-1][2];
		double[][] result = new double[layout.length][2];
		double[] rem = new double[2];

		//remove the turbine to be moved. Copy the original layout
		ArrayList<double[]> removed = new ArrayList<double[]>();
		for (int i = 0; i<layout.length;i++){ 
			result[i][0] = layout[i][0];
			result[i][1] = layout[i][1];
			
			if (i != moveInd){
				removed.add(layout[i]);
			}
			else {
				rem[0] = layout[i][0];		
				rem[1] = layout[i][1];		
			}
		}
		holder = utils.convertAL(removed);

		double[] toMovePoint = new double[2];
		int tries = 0;
		do{
			double xMove = (rnd.nextGaussian()*2*maxGausDist) - maxGausDist; //between -maxDist and +maxDist
			double yMove = (rnd.nextGaussian()*2*maxGausDist) - maxGausDist;
			toMovePoint[0] = layout[moveInd][0] + xMove;
			toMovePoint[1] = layout[moveInd][1] + yMove;
			tries++;
		}
		while(!utils.pointValid(toMovePoint, holder) && tries<100);

		if (tries<100){
			result[moveInd][0] = toMovePoint[0];
			result[moveInd][1] = toMovePoint[1];
			System.out.println("mutation succeeded");
		}
		else {
			result[moveInd][0] = rem[0];
			result[moveInd][1] = rem[1];
			System.out.println("mutation failed. Skipping.");
		}

		return result;
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

				if (minSpac > utils.getDist(layout[k][0], layout[k][1], x, y)){
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

	public double[][] initGridRnd(){
		double interval = 8.001 * wfle.getTurbineRadius();
		double spacerX = wfle.getFarmWidth()*initGridSpacing; //randomising spacing of grid up to 1%
		double spacerY = wfle.getFarmHeight()*initGridSpacing;
		ArrayList<double[]> layout = new ArrayList<double[]>();

		for (double x=0.0; x<wfle.getFarmWidth(); x=x+(interval+(rnd.nextDouble()*spacerX))) {
			for (double y=0.0; y<wfle.getFarmHeight(); y=y+(interval+(rnd.nextDouble()*spacerY))) {
				double[] point = {x, y};
				layout.add(point);
			}
		}

		//System.out.println("turbines " + layout.size());
		return utils.convertAL(layout);
	}

	public double[][] initGridEven(){
		System.out.println("making grid even");
		double interval = 8.001 * wfle.getTurbineRadius();
		ArrayList<double[]> layout = new ArrayList<double[]>();

		for (double x=0.0; x<wfle.getFarmWidth(); x=x+interval) {
			for (double y=0.0; y<wfle.getFarmHeight(); y=y+interval) {
				double[] point = {x, y};
				layout.add(point);
			}
		}

		//System.out.println("turbines " + layout.size());
		return utils.convertAL(layout);
	}

	public double[][] initSpaced(){
		System.out.println("starting spaced");
		double fWidth = wfle.getFarmWidth(); //def 7000
		double fHeight = wfle.getFarmHeight(); //def 14000
		double gridSize = 400; //7000/400=17.5, 17*34=578

		// no hard coding!
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
			for (;gridMarkerX<fWidth;gridMarkerX += gridSize){
				for (;gridMarkerY<fHeight;){
					double[] point = new double[2];
					point[0] = gridMarkerX + rnd.nextDouble()*gridSize;
					point[1] = gridMarkerY + rnd.nextDouble()*gridSize;
					//System.out.println("new point " + point[0] +" "+ point[1]);

					if (utils.pointValid(point, layout)){
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

	public ArrayList<double[][]> crossoverUniform(double[][] layout1, double[][] layout2){
		ArrayList<double[][]> children = new ArrayList<double[][]>();
		ArrayList<double[]> child1 = new ArrayList<double[]>();
		ArrayList<double[]> child2 = new ArrayList<double[]>();
		ArrayList<double[]> primLayout = new ArrayList<double[]>();
		ArrayList<double[]> secLayout = new ArrayList<double[]>();

		//take larger layout
		if (layout1.length >= layout2.length){
			primLayout = utils.convertA(layout1);
			secLayout = utils.convertA(layout2);
		}
		else{
			primLayout = utils.convertA(layout2);
			secLayout = utils.convertA(layout1);
		}

		//iterate through smaller layout
		for (int i = 0; i<secLayout.size();i++){
			if (rnd.nextDouble() > crossProb){ //0.5 = uniform crossover
				child1.add(primLayout.get(i));
				child2.add(secLayout.get(i));
			}
			else {
				child2.add(primLayout.get(i));
				child1.add(secLayout.get(i));
			}
		}

		//then iterate through larger, to fill
		for (int i = secLayout.size();i<primLayout.size();i++){
			if (rnd.nextDouble() < 0.5){ //TODO should this be 50/50 chance to fill spot?
				child1.add(primLayout.get(i));
				child2.add(primLayout.get(i));
			}
		}

		double[][] child1A = utils.convertAL(child1);
		double[][] child2A = utils.convertAL(child2);

		//child1A = repairLayout(child1A);
		//child2A = repairLayout(child2A);

		children.add(child1A);
		children.add(child2A);

		return children;
	}

	public double[][] repairLayoutS(double[][] layout){
		if (!wfle.checkConstraint(layout)){
			ArrayList<double[]> repairedAL = new ArrayList<double[]>();
			for (int i = 0;i<layout.length;i++){
				repairedAL.add(layout[i]);
			}

			ArrayList<double[]> offenders = new ArrayList<double[]>();
			
			//check minDist
			for (int i=0; i<repairedAL.size(); i++) {
				for (int j = i+1; j<repairedAL.size(); j++){
					if (utils.tooClose(repairedAL.get(i)[0], repairedAL.get(i)[1], repairedAL.get(j)[0], repairedAL.get(j)[1])){
						offenders.add(repairedAL.get(j));
					}
				}
			}
			
			for (double[] offender : offenders){
				if (repairedAL.contains(offender)){
					repairedAL.remove(offender);
				}
			}
			
			System.out.println("(S) offenders removed " + offenders.size());
			return utils.convertAL(repairedAL);
		}

		return layout;
	}
	
	public double[][] repairLayoutA(double[][] layout){
		System.out.println("beginning repA");
		if (!wfle.checkConstraint(layout)){
			ArrayList<double[]> repairedAL = new ArrayList<double[]>();
			for (int i = 0;i<layout.length;i++){
				repairedAL.add(layout[i]);
			}

			HashMap<double[], Integer> offenders = new HashMap<double[], Integer>();
			
			//check minDist
			for (int i=0; i<repairedAL.size(); i++) {
				for (int j = i+1; j<repairedAL.size(); j++){
					if (utils.tooClose(repairedAL.get(i)[0], repairedAL.get(i)[1], repairedAL.get(j)[0], repairedAL.get(j)[1])){
						
						if (offenders.containsKey(repairedAL.get(j))){
							offenders.put(repairedAL.get(j), offenders.get(repairedAL.get(j))+1);
						}
						else {
							offenders.put(repairedAL.get(j), 1);
						}	
					}
				}
			}
			
			Map<double[], Integer> sortedOffenders = Utils.sortByValue(offenders);
			double[][] potLayout;
			do {
				double [] key = sortedOffenders.entrySet().iterator().next().getKey();
				repairedAL.remove(key);
				sortedOffenders.remove(key);
				potLayout = utils.convertAL(repairedAL);
			}
			while (!wfle.checkConstraint(potLayout));
			
			System.out.println("(A) offenders removed " + offenders.size());

			return utils.convertAL(repairedAL);
		}

		return layout;
	}
	
	public ArrayList<double[][]> repairAll(ArrayList<double[][]> pops, boolean adv){
		ArrayList<double[][]> newPops = new ArrayList<double[][]>();
		for (double[][] pop : pops){
			if(adv){
				newPops.add(repairLayoutA(pop));
			}
			else{
				newPops.add(repairLayoutS(pop));
			}
		}
		
//		for (double[][] pop : newPops){
//			System.out.println("rep " +pop.length);
//		}
		
		return newPops;
	}

	public ArrayList<double[][]> selection(ArrayList<double[][]> pops, ArrayList<Double> fits){
		//select best half of all layouts
		ArrayList<Integer> popsToKeep = new ArrayList<Integer>();

		//get fitnesses and indices of the layouts, then sort
		ArrayList<Pair<Integer, Double>> fitsCopy = new ArrayList<Pair<Integer, Double>>();
		for (int i = 0;i<fits.size();i++){
			fitsCopy.add(new Pair<Integer, Double>(i, fits.get(i)));
		}
		fitsCopy = utils.sortPairs(fitsCopy);
		
		//record layout indices to keep
		for (int i = 0;popsToKeep.size() < popNo;i++){
			popsToKeep.add(fitsCopy.get(i).x);
		}
		
		ArrayList<double[][]> popHold = new ArrayList<double[][]>();
		//iterate over indices to keep and add to holder
		for(int j = 0; j<popsToKeep.size();j++){ 
			popHold.add(pops.get(j));
		}
		
//		for (double[][] pop : popHold){
//			System.out.println("popHold " + pop.length);
//		}
		
		
		//return holder
		return popHold;

		//no need to change fitnesses as they are re-eval'd immediately after this

	}
	
	public ArrayList<double[][]> tournament(ArrayList<double[][]> pops, ArrayList<Double> fits, int tourSize){
		int[] parentInds = new int[(int)Math.ceil(pops.size()/2)];
		//e.g. 5 layouts, 3 playoffs
		
		for (int i = 0;i<parentInds.length;i++){
			HashMap<Integer, Double> contestants = new HashMap<Integer, Double>();
			for (int j = 0;j<tourSize;j++){
				int p = rnd.nextInt(pops.size());
				contestants.put(p, fits.get(p));
			}
			
			Map.Entry<Integer, Double> bestCont = null;
			for (Map.Entry<Integer, Double> entry : contestants.entrySet()){
			    if (bestCont == null || entry.getValue().compareTo(bestCont.getValue()) > 0){
			    	bestCont = entry;
			    }
			}
			parentInds[i] = bestCont.getKey();
		}
		
		ArrayList<double[][]> parents = new ArrayList<double[][]>();
		for (int i = 0;i<parentInds.length;i++){
			parents.add(pops.get(i));
		}
		return parents;
	}
	
	public ArrayList<double[][]> addChildren(ArrayList<double[][]> pops, ArrayList<double[][]> children){
		for (double[][] child : children){
			pops.add(child);
		}
		
		return pops;
	}
	
	public ArrayList<double[][]> crossover(ArrayList<double[][]> parents){
		ArrayList<double[][]> children = new ArrayList<double[][]>();
		
		int r1;
		int r2;
		for (int k = 0;k<parents.size();k++){
			r1 = rnd.nextInt(parents.size());
			r2 = rnd.nextInt(parents.size());
			while (r2 == r1){
				r2 = rnd.nextInt(parents.size());
			}

			ArrayList<double[][]> kids = crossoverUniform(parents.get(r1), parents.get(r2));
			for(int j=0;j<kids.size();j++){
				children.add(kids.get(j));
			}
		}
		return children;
	}
	
	public ArrayList<double[][]> mutate(ArrayList<double[][]> children, double mutRate){
		//only mutates children
		//mutRate% chance for each turbine to move
		for (int i = 0;i<children.size();i++){
			System.out.println("mutating pop " + i);
			for (int j = 0; j<children.get(i).length;j++){
				if (rnd.nextDouble() < mutRate){
					children.set(i, moveOne(children.get(i), j));
				}
			}
		}
		return children;
	}
	
	public ArrayList<Double> evalGen(ArrayList<double[][]> pops){
		fitnesses.clear();
		ArrayList<Double> newFits = new ArrayList<Double>();
		double bestFit = Double.MAX_VALUE;
		for (double[][] pop : pops){
			double fit = wfle.evaluate(pop);
			newFits.add(fit);
			if (fit < bestFit){
				bestFit = fit;
				bestLayout = pop;
			}
		}
		return newFits;
	}
	
	public double[][] getBestLayout() {
		return bestLayout;
	}
	
	public void loadSettings(Properties prop){
		//prop.getProperty("scen");
		initGridSpacing = Double.parseDouble(prop.getProperty("initGridSpacing"));
		popNo 			= Integer.parseInt(prop.getProperty("popNo"));
		genNo 			= Integer.parseInt(prop.getProperty("genNo"));
		tournSize 		= Integer.parseInt(prop.getProperty("tournSize"));
		crossProb 		= Double.parseDouble(prop.getProperty("crossProb"));
		mutationRate 	= Double.parseDouble(prop.getProperty("mutationRate"));
		maxGausDist 	= Double.parseDouble(prop.getProperty("maxGausDist"));
		useRepA 		= Boolean.parseBoolean(prop.getProperty("useRepA"));
	}
	
}