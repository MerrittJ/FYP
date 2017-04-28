package Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;


/**Core module for GA functionality
 * @author JoshMerritt
 * 
 * 
 *
 */
public class FinalGA {

	private WindFarmLayoutEvaluator wfle;
	
	/**
	 * Maximum additional spacing turbines can use during initialisation
	 */
	private double initGridSpacing;
	/**
	 * Number of layouts to start with and to survive to next generation
	 */
	private int popNo;
	/**
	 * Number of generations
	 */
	private int genNo;
	/**
	 * Probability to accept a layout with a better fitness value but larger size
	 */
	private double takeLargerProb;
	/**
	 * Probability a layout is permitted to use ‘addition’ mutation operator
	 */
	private double mutAddProb;
	/**
	 * Probability a turbine is permitted to use ‘move’ mutation operator
	 */
	private double mutRateTurb;
	/**
	 * Probability a layout is permitted to use ‘move’ mutation operator
	 */
	private double mutRateLayout;
	/**
	 * Maximum distance a turbine can move during mutation
	 */
	private double maxGausDist;
	/**
	 * Toggle use of advanced repair function
	 */
	private boolean useRepA;
	/**
	 * Toggle use of advanced addition variant for mutation phase
	 */
	private boolean useMutAddA;
	/**
	 * Switch to use crossover variant 
	 */
	private int crossMode;
	/**
	 * Fixed mixing ratio used in crossover variant 2
	 */
	private double c2Prob;	
	/**
	 * Percentage of first parent’s layout to use during crossover variant 3
	 */
	private int c3Perc;
	/**
	 * Toggle use of a tournament function that increases weighting of smaller layouts versus ones with higher power outputs
	 */
	private boolean altTourn;
	/**
	 * Switch to use alternative algorithms, such as ‘random’ or ‘local’ searches
	 */
	private String altMode;
	
	private ArrayList<double[][]> population;
	private ArrayList<Double> fitnesses;
	private double[][] bestLayout;
	private ArrayList<ArrayList<Result<Integer, Double, Integer>>> results; //gen -> pop#,fit,turb#
	private double minSpac;
	private int tournSize = 2;
	private Random rnd;
	private Utils utils;



	/**Initialise the GA with settings file, constraints, and random seed. Check if alternate mode is enabled.
	 * Begins run by initialising populations before running the GA
	 * @param evaluator
	 * @param settingsName
	 * @param seed
	 * 
	 * 
	 */
	public FinalGA(WindFarmLayoutEvaluator evaluator, String settingsName, int seed){
		wfle = evaluator;
		rnd = new Random(seed);
		utils = new Utils(wfle, settingsName);
		loadSettings(utils.getSettings());
		population = new ArrayList<double[][]>();
		fitnesses = new ArrayList<Double>();
		minSpac = 8.001 * wfle.getTurbineRadius();
		results = new ArrayList<ArrayList<Result<Integer, Double, Integer>>>();

		if (altMode.equals("off")){
			initPops();
			runGA();
		}
		else if (altMode.equals("random")){
			initAltRandom();
			saveResults(population, fitnesses);
		}
		
	}
	
	/**
	 * Initialise population by creating required number of layouts. Populates 'Population' and evaluates starting fitness values.
	 */
	public void initPops(){
		double thisRun = 0.0;
		for (int i = 0; i < popNo;){
			double[][] layout;
			layout = initGridRnd();

			if (wfle.checkConstraint(layout)){
				population.add(layout);
				i++;
				thisRun = wfle.evaluate(layout);
				fitnesses.add(thisRun);
				System.out.println(layout.length + " " + thisRun );
			}
			else {
				System.out.println("layout failed");
			}
		}
	}
	
	
	/**
	 * Method to run the GA. For each generation, carry out parent selection, crossover, mutation, repair, survival selection on the population.
	 */
	public void runGA(){
		for (int i=0;i<genNo;i++){
			System.out.println("generation "+i);
			int childrenSurv = 0;
			if (population.size() >= 3){	
				
				ArrayList<double[][]> parents = new ArrayList<double[][]>();
				if (!altTourn){
					parents = tournament(population, fitnesses, tournSize);
				}
				else {
					parents = tournament2(population, fitnesses, tournSize);
				}
				
				ArrayList<double[][]> children = crossover(parents);
				children = mutate(children, mutRateTurb, mutRateLayout);
				population = addChildren(population, children);
				population = repairAll(population, useRepA); 
				fitnesses = evalGen(population);
				population = selection(population, fitnesses);
	
			}
			utils.printFits(false, fitnesses, population);
			saveResults(population, fitnesses); 
		}

		utils.printFits(true, fitnesses, population); 
	}

	/**
	 * Function to initialise layouts for the alternate mode: Random search. Creates a number of layouts comparable to those produced in a GA run.
	 */
	public void initAltRandom(){
		double thisRun = 0.0;
		for (int i = 0; i < popNo*genNo;){
			double[][] layout;
			layout = initRandom();

			if (wfle.checkConstraint(layout)){
				population.add(layout);
				i++;
				thisRun = wfle.evaluate(layout);
				fitnesses.add(thisRun);
				System.out.println(layout.length + " " + thisRun );
			}
			else {
				System.out.println("layout failed");
			}
		}
	}

	/**Common computational intelligence algorithm; local search add variant. The algorithm attempts to add a turbine to a random location, checking if it improves the fitness value. If it does, it places it. Otherwise the layout is considered finished.
	 * @return true if turbine added. False otherwise.
	 * 
	 * 
	 */
	public boolean LSAddOne(){
		boolean added = false; //check if any turbine added to a pop
		for (ListIterator<double[][]> iter = population.listIterator(); iter.hasNext();){
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
				population.set(thisInd, thisLayoutOpt);
				System.out.println("added turbine");
				added = true;
			}
			else{
				System.out.println("turbine not added");
			}
		}

		return added; //if any turbines added, continue passes

	}

	/**Common computational intelligence algorithm; local search remove variant. The algorithm attempts to remove a turbine, checking if this improves the fitness value. If it does, it removes it. Otherwise the layout is considered finished.
	 * @return true if turbine removed. False otherwise.
	 * 
	 * 
	 */
	public boolean LSRemoveOne(){
		boolean removed = false; //check if turbine removed
		for (ListIterator<double[][]> iter = population.listIterator(); iter.hasNext();){
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
				population.set(thisInd, thisLayoutOpt);
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

	/**Function used to move a turbine within a layout, i.e. for the move mutate operator. Moves a turbine by finding the X and Y distance using a Gaussian distribution. Attempts to move the turbine 100 times before 'giving up'.
	 * @param layout Layout to mutate
	 * @param moveInd Index of the turbine to move
	 * @return a layout with one turbine moved
	 * 
	 * 
	 */
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
			//System.out.println("mutation succeeded");
		}
		else {
			result[moveInd][0] = rem[0];
			result[moveInd][1] = rem[1];
			//System.out.println("mutation failed. Skipping.");
		}

		return result;
	}

	/**Create a layout with turbines placed randomly within. Randomly selects a maximum number of turbines to place (up to 1000). Attempts to add a turbine 500 times before 'giving up' and considering the layout full.
	 * @return A layout
	 * 
	 * 
	 */
	public double[][] initRandom(){
		int tries = 0;
		ArrayList<double[]> layout = new ArrayList<double[]>();
		int max = rnd.nextInt(1000);
		
		do{
			double x = rnd.nextDouble() * wfle.getFarmWidth();
			double y = rnd.nextDouble() * wfle.getFarmHeight();
			double[] point = {x,y};
			
			if (utils.pointValid(point, utils.convertAL(layout))){
				layout.add(point);
			}
			else {
				tries++;
			}
		}
		while (layout.size() < max && tries <= 500);
		
		return utils.convertAL(layout);
	}

	/**Create a layout where turbines are dispersed semi-regularly with the minimum spacing requirement between them, plus an additional percentage of the field width/height. Default initialisation for GA.
	 * @return A layout
	 * 
	 * 
	 */
	public double[][] initGridRnd(){
		double spacerX = wfle.getFarmWidth()*initGridSpacing; //randomising spacing of grid up to x%
		double spacerY = wfle.getFarmHeight()*initGridSpacing;
		ArrayList<double[]> layout = new ArrayList<double[]>();

		for (double x=0.0; x<wfle.getFarmWidth(); x=x+(minSpac+(rnd.nextDouble()*spacerX))) {
			for (double y=0.0; y<wfle.getFarmHeight(); y=y+(minSpac+(rnd.nextDouble()*spacerY))) {
				double[] point = {x, y};
				layout.add(point);
			}
		}

		//System.out.println("turbines " + layout.size());
		return utils.convertAL(layout);
	}

	/**Crossover variant 1, uniform crossover. While iterating through the smaller of the two parent layouts, has 50% chance to take that turbine and place it in a child layout. If it does not, turbine with same index in larger layout is placed in child layout. 
	 * When smaller layout is complete, larger layout is iterated through, with 50% chance for each remaining turbine to be place in child.
	 * Second child is created by taking the turbines from both parents that were not used in first child.
	 * @param layout1
	 * @param layout2
	 * @return Two child layouts
	 * 
	 * 
	 */
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
			if (rnd.nextDouble() > 0.5){ //0.5 = uniform crossover
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
			if (rnd.nextDouble() < 0.5){
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
	
	/**Crossover variant 2, variable mixing ratio. While iterating through the smaller of the two parent layouts, has chance to take that turbine and place it in a child layout. If it does not, turbine with same index in larger layout is placed in child layout. Chance is derived from c2Prob parameter. 
	 * When smaller layout is complete, larger layout is iterated through, with 50% chance for each remaining turbine to be place in child.
	 * @param layout1
	 * @param layout2
	 * @return Two child layouts
	 * 
	 * 
	 */
	public ArrayList<double[][]> crossoverUniform2(double[][] layout1, double[][] layout2){
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
			if (rnd.nextDouble() > c2Prob){ 
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
			if (rnd.nextDouble() < 0.5){
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
	
	/**Crossover variant 3, percentage of parent. While iterating through the smaller of the two parent layouts, takes all turbines up to a percentage of the total layout size and places them in a child layout. When percentage is met, fill the child with the second parent's turbines. Percentage is derived from c3Perc parameter. 
	 * Second child is created by mirroring the above child, i.e. first portion of child is filled by the second parent's turbines and second portion by the first's.
	 * @param layout1
	 * @param layout2
	 * @return Two child layouts
	 * 
	 * 
	 */
	public ArrayList<double[][]> crossoverUniform3(double[][] layout1, double[][] layout2){
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

		int counter = 0;
		int proportion = c3Perc/100 * primLayout.size(); //auto-floors
		
		//iterate through smaller layout
		for (int i = 0; i<secLayout.size();i++){
			if (counter<proportion){ 
				child1.add(primLayout.get(i));
				child2.add(secLayout.get(i));
			}
			else {
				child2.add(primLayout.get(i));
				child1.add(secLayout.get(i));
			}
			counter++;
		}

		//then iterate through larger, to fill
		for (int i = secLayout.size();i<primLayout.size();i++){
			//if (rnd.nextDouble() < 0.5){
			child1.add(primLayout.get(i));
			child2.add(primLayout.get(i));
			//}
		}

		double[][] child1A = utils.convertAL(child1);
		double[][] child2A = utils.convertAL(child2);

		//child1A = repairLayout(child1A);
		//child2A = repairLayout(child2A);

		children.add(child1A);
		children.add(child2A);

		return children;
	}

	/** Simple repair function. Checks if layout is valid. If it is not, iterates through all turbines looking for those that are too close to other turbines and if they are, adds their index to a list. On completing the iterating, removes all turbines that appear in the 'offender' list.
	 * @param layout
	 * @return Repaired layout
	 * 
	 */
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
	
	/**Advanced repair function. Checks if layout is valid. If not, iterates through all turbines looking for those that are too close to other turbines and if they are, adds their index to a list. If the turbine is too close to more than one other turbine, this is also noted. 
	 * On completing the iterating, sorts the list of indices and numbers of violations and removes the most offending turbine. The layout is rechecked for validity and if it is still invalid, the next more offending turbine is removed, and so on.
	 * @param layout
	 * @return Repaired layout
	 * 
	 * 
	 */
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
	
	/**Function to repair the whole population. Iterates through individuals, repairing as necessary and according to the repair variant selected in settings.
	 * @param pops
	 * @param adv
	 * @return A fully repaired population
	 * 
	 * 
	 */
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

	/**Survival selection function. Take the top half of all layouts (elitism) and allow them to progress to next generation.
	 * @param pops
	 * @param fits
	 * @return List of layouts to survive to next generation
	 * 
	 * 
	 */
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
		
		//return holder
		return popHold;

		//no need to change fitnesses as they are re-eval'd immediately after this

	}
	
	/**Parent selection function. Creates popNo/2 competitions of size 2 and populates these randomly from the population. The winner is the layout with a better fitness value and is permitted to be a parent and is added to the 'parents' list.
	 * @param pops
	 * @param fits
	 * @param tourSize
	 * @return List of layouts that will be parents for child layouts this generation
	 * 
	 * 
	 */
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
	
	/**Alternative parent selection function. Creates popNo/2 competitions of size 2 and populates these randomly from the population. 
	 * If a layout has fewer turbines and better fitness value, it is the winner. If it has fewer turbines and worse fitness value, it has a chance to be accepted as winner.
	 * The winner is permitted to be a parent and is added to the 'parents' list.
	 * @param pops
	 * @param fits
	 * @param tourSize
	 * @return List of layouts that will be parents for child layouts this generation
	 * 
	 * 
	 */
	public ArrayList<double[][]> tournament2(ArrayList<double[][]> pops, ArrayList<Double> fits, int tourSize){
		int[] parentInds = new int[(int)Math.ceil(pops.size()/2)];
		//e.g. 5 layouts, 3 playoffs
		
		for (int i = 0;i<parentInds.length;i++){
			HashMap<Integer, ArrayList> contestants = new HashMap<Integer, ArrayList>(); //AL = double(fits), int(size)
			for (int j = 0;j<tourSize;j++){
				int p = rnd.nextInt(pops.size());
				ArrayList metrics = new ArrayList();
				metrics.add(fits.get(p));
				metrics.add(population.get(p).length);
				contestants.put(p, metrics);
			}
			
			Map.Entry<Integer, ArrayList> bestCont = null;
			for (Map.Entry<Integer, ArrayList> entry : contestants.entrySet()){
				double takeLarger = rnd.nextDouble();
			    if (bestCont == null){
			    	bestCont = entry;
			    }
			    //new fit better, new size better
			    if (((Double)entry.getValue().get(0) < (Double)bestCont.getValue().get(0)) && ((Integer)entry.getValue().get(1) < (Integer)bestCont.getValue().get(1))){
			    	bestCont = entry;
			    }
			    //new fit worse, new size better
			    else if (((Double)entry.getValue().get(0) > (Double)bestCont.getValue().get(0)) && ((Integer)entry.getValue().get(1) < (Integer)bestCont.getValue().get(1))){
			    	if (takeLarger>takeLargerProb){
			    		System.out.println("worse parent accepted");
			    		bestCont = entry;
			    	}
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
	
	/**Function to introduce children to the population.
	 * @param pops
	 * @param children
	 * @return Full population
	 * 
	 * 
	 */
	public ArrayList<double[][]> addChildren(ArrayList<double[][]> pops, ArrayList<double[][]> children){
		for (double[][] child : children){
			pops.add(child);
		}
		
		return pops;
	}
	
	/**Crossover genetic operator controller. Takes a list of parents and randomly selects from them to undergo crossover. Crossover variant depends on GA input settings.
	 * @param parents
	 * @return ArrayList of newly created child layouts 
	 * 
	 * 
	 */
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
			ArrayList<double[][]> kids;
			switch (crossMode){
				case 1: kids = crossoverUniform(parents.get(r1), parents.get(r2)); break;
				case 2: kids = crossoverUniform2(parents.get(r1), parents.get(r2)); break;
				case 3: kids = crossoverUniform3(parents.get(r1), parents.get(r2)); break;
				default: System.out.println("no crossMode specified, defaulting to 1"); kids = crossoverUniform(parents.get(r1), parents.get(r2)); break;
			}
			for(int j=0;j<kids.size();j++){
				children.add(kids.get(j));
			}
		}
		return children;
	}
	
	/**Mutation genetic operator controller. Takes a list of children created this generation and passes them to the appropriate mutation variant. Mutation variant depends on GA input settings.
	 * @param children
	 * @param mutRateT
	 * @param mutRateL
	 * @return ArrayList of all children of this generation
	 * 
	 * 
	 */
	public ArrayList<double[][]> mutate(ArrayList<double[][]> children, double mutRateT, double mutRateL){
		//only mutates children
		//mutRateT% chance for each turbine to move
		for (int i = 0;i<children.size();i++){
			if (rnd.nextDouble() < mutRateL){
				System.out.println("mutating pop " + i);
				for (int j = 0; j<children.get(i).length;j++){
					if (rnd.nextDouble() < mutRateT){
						children.set(i, moveOne(children.get(i), j));
					}
				}
			}
			
			if (rnd.nextDouble() < mutAddProb){
				if (useMutAddA){
					children.set(i, mutAddA(children.get(i)));
				}
				else {
					children.set(i, mutAddS(children.get(i)));
				}
				
			}
		}
		return children;
	}
	
	/**Simple variant of the mutation add operator. Attempts 500 times to add a turbine to a random location in the layout. If there is space, add the turbine and continue trying to add more. Otherwise, do not.
	 * @param layout
	 * @return potentially mutated child
	 * 
	 * 
	 */
	public double[][] mutAddS(double[][] layout){
		//randomly adds a turbine
		double[] point = new double[2];
		int tries = 0;
		int added = 0;
		do {	
			point[0] = rnd.nextDouble()*wfle.getFarmWidth();
			point[1] = rnd.nextDouble()*wfle.getFarmHeight();
			
			if (utils.pointValid(point, layout)){
				ArrayList<double[]> layoutAL = utils.convertA(layout);
				layoutAL.add(point);
				layout = utils.convertAL(layoutAL);
				added++;
				tries = 0;
			}
			else {
				tries++;
			}
		}
		while (tries <= 500);
		
		System.out.println(added + " turbines added in mutation");
		return layout;
	}
	
	/**Advanced variant of the mutation add operator. Iterates across the layout in a grid-like fashion, checking at specific points to see if there is space for a turbine. If there are spaces, add turbines at these points. Otherwise, do not.
	 * @param layout
	 * @return potentially mutated child
	 * 
	 * 
	 */
	public double[][] mutAddA(double[][] layout){
		//adds a turbine where there is space
		double[] point = new double[2];
		int added = 0;
		for (double i = 0;i<wfle.getFarmWidth();i=i+minSpac){
			for (double j = 0;j<wfle.getFarmHeight();j=j+minSpac){
				point[0] = i;
				point[1] = j;
				if (utils.pointValid(point, layout)){
					ArrayList<double[]> layoutAL = utils.convertA(layout);
					layoutAL.add(point);
					added++;
					layout = utils.convertAL(layoutAL);
				}
			}
		}

		System.out.println(added + " turbines added in mutation");
		return layout;
	}

	
	/**Evaluates all individuals in the current generation and updates the arraylist tracking fitness values
	 * @param pops
	 * @return
	 */
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
	
	/**Save the fitness value and layout size for each individual. Run at end of each generation
	 * @param pops
	 * @param fits
	 * 
	 * 
	 */
	public void saveResults(ArrayList<double[][]> pops, ArrayList<Double> fits){
		//called at end of each gen
		ArrayList<Result<Integer, Double, Integer>> generation = new ArrayList<Result<Integer, Double, Integer>>();
		int individualNo = 0;
		for (double[][] layout : pops){
			Result<Integer, Double, Integer> ind = new Result<Integer, Double, Integer>(individualNo,fits.get(individualNo),layout.length);
			individualNo++;
			generation.add(ind);
		}
		
		results.add(generation);
	}
	
	public double[][] getBestLayout() {
		return bestLayout;
	}
	
	public ArrayList<ArrayList<Result<Integer, Double, Integer>>> getResults() {
		return results;
	}
	
	/**Function to load all GA settings specified by user
	 * @param prop
	 * 
	 * 
	 */
	public void loadSettings(Properties prop){
		//prop.getProperty("scen");
		initGridSpacing 		= Double.parseDouble(prop.getProperty("initGridSpacing"));
		popNo 					= Integer.parseInt(prop.getProperty("popNo"));
		genNo 					= Integer.parseInt(prop.getProperty("genNo"));
		mutAddProb				= Double.parseDouble(prop.getProperty("mutAddProb"));
		mutRateTurb 			= Double.parseDouble(prop.getProperty("mutRateTurb"));
		mutRateLayout 			= Double.parseDouble(prop.getProperty("mutRateLayout"));
		maxGausDist 			= Double.parseDouble(prop.getProperty("maxGausDist"));
		useRepA 				= Boolean.parseBoolean(prop.getProperty("useRepA"));
		useMutAddA 				= Boolean.parseBoolean(prop.getProperty("useMutAddA"));
		crossMode 				= Integer.parseInt(prop.getProperty("crossMode"));
		c2Prob					= Double.parseDouble(prop.getProperty("c2Prob"));
		c3Perc					= Integer.parseInt(prop.getProperty("c3Perc"));
		altTourn				= Boolean.parseBoolean(prop.getProperty("altTourn"));		
		takeLargerProb			= Double.parseDouble(prop.getProperty("takeLargerProb"));
		altMode 				= prop.getProperty("altMode");
	}
	
}