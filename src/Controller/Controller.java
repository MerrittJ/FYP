package Controller;

import java.util.ArrayList;

import Model.ExcelWriter;
import Model.GA;
import Model.KusiakLayoutEvaluator;
import Model.FinalGA;
import Model.Result;
import Model.WindScenario;
import View.View;

/**
 * Core module from which the project is run. Initialises layout evaluator and GA and writes results. Also enables the automation of experiments by taking settings files.
 * @author JoshMerritt
 * 
 * 
 *
 */
public class Controller {

	public static void main(String argv[]) {
		try {
			int runs = 1;
			ArrayList<String> settings = new ArrayList<String>();

			//Uncomment settings configuration to include it in runtime
			
			settings.add("settings00"); //random search
			// main experiments from here on
			//settings.add("settings11");		
			//settings.add("settings12"); 
			//settings.add("settings13");
			//settings.add("settings21");
			//settings.add("settings22"); //best performing, may take several hours to run
			//settings.add("settings23");
			//settings.add("settings31");
			//settings.add("settings32");
			//settings.add("settings33");
			//settings.add("settings41");
			//settings.add("settings42");
			//settings.add("settings43");

			for (int j = 0;j<settings.size();j++){
				ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer>>>> results = new ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer>>>>(); //run -> gen -> pop#,fit,turb#
				double[][] best = new double[0][2];
				FinalGA myga;
				for (int i = 0; i<runs;i++){
					WindScenario ws = new WindScenario("Scenarios/00.xml");
					KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
					wfle.initialize(ws);
					
					//GA ga = new GA(wfle,i); //GECCO-provided GA
					//ga.run();
					
					myga = new FinalGA(wfle, "Settings/"+settings.get(j)+".properties", i);
					results.add(myga.getResults());
					System.out.println(myga.getBestLayout());
					best = myga.getBestLayout();
				}

				ExcelWriter test = new ExcelWriter();

				test.setOutputFile("Results/"+settings.get(j)+"Results.xls");
				test.setResults(results);
				test.write();
				System.out.println("results written");
				
				View v = new View(best);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
