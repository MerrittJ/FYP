package Model;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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

	public boolean outOfBounds(double[] point){
		if (point[0] < 0 || point[1] < 0 || point[0] > wfle.getFarmWidth() || point[1] > wfle.getFarmHeight()){
			return true;
		}
		else{
			return false;
		}
	}

	public double getDist(double x1, double y1, double x2, double y2) {
		return Math.hypot(x2-x1, y2-y1);
	}

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

	public Properties getSettings(){
		return settings;
	}
	
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

	public static void main(String[] args){
//		System.out.println("sort pair test");
//		ArrayList<Pair<Integer, Double>> testAL = new ArrayList<Pair<Integer, Double>>();
//
//		Pair<Integer, Double> pair1 = new Pair<Integer, Double>(1,0.8);
//		testAL.add(pair1);
//		Pair<Integer, Double> pair2 = new Pair<Integer, Double>(2,0.6);
//		testAL.add(pair2);
//		Pair<Integer, Double> pair3 = new Pair<Integer, Double>(3,0.1);
//		testAL.add(pair3);
//		//in = 0.8, 0.6, 0.1
//		//expected = 0.1, 0.6, 0.8
//
//		for (Pair<Integer, Double> pair : testAL){
//			System.out.println("x " + pair.x + " y " + pair.y);
//		}
//
//		//sortPairs(testAL);
//
//		for (Pair<Integer, Double> pair : testAL){
//			System.out.println("sorted x " + pair.x + " y " + pair.y);
//		}

//		settings = new ArrayList<String>();
//		reader();
	}
}
