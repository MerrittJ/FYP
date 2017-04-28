package Model;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilities class performing simple functions used throughout the GA
 * @author JoshMerritt
 * 
 */
public class Utils {

	private WindFarmLayoutEvaluator wfle;
	private String settingsName;
	private  Properties settings;

	public Utils(WindFarmLayoutEvaluator evaluator, String settingsName){
		wfle = evaluator;
		settings = new Properties();
		this.settingsName = settingsName;
		reader();

	}

	/**Function to sort a map. Used for determining which turbines violate minimum distance the most during advanced repair
	 * @param map
	 * @return map ordered by value
	 * 
	 * 
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue, 
						(e1, e2) -> e1, 
						LinkedHashMap::new
						));
	}

	/**Sorts a specific arrayList. Used for determining the winner of competitions in the selection operator
	 * @param al
	 * @return sorted pair arrayList based on double object
	 * 
	 * 
	 */
	public ArrayList<Pair<Integer, Double>> sortPairs(ArrayList<Pair<Integer, Double>> al){
		//sort based on the double value (fitness)
		Collections.sort(al, new Comparator<Pair<Integer, Double>>(){
			public int compare(Pair<Integer, Double> arg0, Pair<Integer, Double> arg1) {
				if (arg0.y < arg1.y){
					return -1;
				}
				else if (arg0.y > arg1.y){
					return 1;
				}
				else {
					return 0;
				}		
			}	
		});
		return al;
	}

	/**Simple function to determine distance between two points. Returns true if this distance is <308
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return boolean indicating if two points are too close
	 * 
	 * 
	 */
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

	/**Simple function to determine if a point is outside of layout field. Returns true if the case
	 * @param point
	 * @return boolean indicating if point is outside of layout field
	 * 
	 * 
	 */
	public boolean outOfBounds(double[] point){
		if (point[0] < 0 || point[1] < 0 || point[0] > wfle.getFarmWidth() || point[1] > wfle.getFarmHeight()){
			return true;
		}
		else{
			return false;
		}
	}

	/** Finds the Euclidean distance between two points
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return Euclidean distance between two points
	 */
	public double getDist(double x1, double y1, double x2, double y2) {
		return Math.hypot(x2-x1, y2-y1);
	}

	/**Function to determine if a turbine can be placed at a given point in the current layout. True if the case
	 * @param point
	 * @param layout
	 * @return boolean indicating if point is not within 308px of any turbines
	 * 
	 * 
	 */
	public boolean pointValid(double[] point, double[][] layout){
		for (int i=0; i<layout.length;i++){
			if (layout[i] == null){
				break;
			}
			else if (tooClose(point[0], point[1], layout[i][0], layout[i][1]) || outOfBounds(point)){
				//System.out.println("point invalid " + point[0] + " " + point[1] + " " + layout[i][0] + " " + layout[i][1]);
				return false;
			}
		}
		//System.out.println("point valid");
		return true;
	}

	/**Function to help swap between arrayList and array objects as each have uses within project.
	 * @param al
	 * @return Layout in the form of double[][] for use with GECCO functions
	 * 
	 * 
	 */
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

	/**Function to help swap between arrayList and array objects as each have uses within project.
	 * @param layout
	 * @return Layout in the form of ArrayList<double[]> for use when a layout may be expanding/contracting
	 * 
	 * 
	 */
	public ArrayList<double[]> convertA(double[][] layout){
		ArrayList<double[]> nl = new ArrayList<double[]>();
		for (int i=0; i<layout.length;i++){
			nl.add(layout[i]);
		}
		return nl;
	}

	/**Function to print fitness values. Has toggle to indicate if all layout fitness values should be printed or just the generation's best
	 * @param all
	 * @param fitnesses
	 * @param populations
	 * 
	 * 
	 */
	public void printFits(boolean all, ArrayList<Double> fitnesses, ArrayList<double[][]> populations){
		Collections.sort(fitnesses);
		if (all){
			int i = 0;
			for (double[][] layout : populations){
				System.out.println("Layout " + i + " size " + layout.length +" "+ fitnesses.get(i));
				i++;
			}
			System.out.println("final best fit " + fitnesses.get(0));
		}
		else {
			System.out.println("gen best fit " + fitnesses.get(0));
		}

	}

	/**Getter method to assist with loading a settings file
	 * @return 
	 * 
	 * 
	 */
	public Properties getSettings(){
		return settings;
	}
	
	/**
	 * Function to read a settings file
	 */
	public void reader(){
		InputStream input = null;

		try {
			input = new FileInputStream(settingsName);

			//load a properties file from class path, inside static method
			settings.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
