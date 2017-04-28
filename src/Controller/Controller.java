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
 * @author JoshMerritt
 * 
 * Core module from which the project is run. Initialises layout evaluator and GA and writes results. Also enables the automation of experiments by taking settings files.
 *
 */
public class Controller {

	public static void main(String argv[]) {
		try {
			int runs = 5;
			ArrayList<String> settings = new ArrayList<String>();

			//Uncomment settings configuration to include it in runtime
			
			settings.add("settings00");
			//settings.add("settings11");
			//settings.add("settings12");
			//settings.add("settings13");
			//settings.add("settings21");
			//settings.add("settings22");
			//settings.add("settings23");
			//settings.add("settings31");
			//settings.add("settings32");
			//settings.add("settings33");
			//settings.add("settings41");
			//settings.add("settings42");
			//settings.add("settings43");

			for (int j = 0;j<settings.size();j++){
				ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer>>>> results = new ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer>>>>(); //run -> gen -> pop#,fit,turb#
				for (int i = 0; i<runs;i++){
					WindScenario ws = new WindScenario("Scenarios/00.xml"); //def = Scenarios/obs_00.xml
					KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
					wfle.initialize(ws);
					
					//GA ga = new GA(wfle,i); //GECCO-provided GA
					//ga.run();
					
					//View v = new View(myga.getBestLayout());
					
					FinalGA myga = new FinalGA(wfle, "Settings/"+settings.get(j)+".properties", i);
					results.add(myga.getResults());
				}

				ExcelWriter test = new ExcelWriter();

				test.setOutputFile("Results/"+settings.get(j)+"Results.xls");
				test.setResults(results);
				test.write();
				System.out.println("results written");
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
