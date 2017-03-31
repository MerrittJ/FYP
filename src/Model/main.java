package Model;

import java.util.ArrayList;

import View.View;

public class main {

	public static void main(String argv[]) {
		try {
			int runs = 5;
			ArrayList<String> settings = new ArrayList<String>();
			settings.add("Settings/settings1.properties");
			settings.add("Settings/settings2.properties");
			settings.add("Settings/settings3.properties");
//			settings.add("Settings/settings4.properties");
//			settings.add("Settings/settings5.properties");

			ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>>> results = new ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>>>(); //run -> gen -> pop#,fit,turb#
			
			for (int j = 0;j<settings.size();j++){
				for (int i = 0; i<runs;i++){
					WindScenario ws = new WindScenario("Scenarios/00.xml"); //def = Scenarios/obs_00.xml
					KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
					wfle.initialize(ws);
					MyGA2 myga = new MyGA2(wfle, settings.get(j), i);
					//View v = new View(myga.getBestLayout());

					results.add(myga.getResults());

					//GA ga = new GA(wfle);
					//ga.run();
					//MyFirstGA algorithm = new MyFirstGA(wfle);
					//algorithm.run(); // optional, name of method 'run' provided on submission
					// algorithm can also just use constructor 
				}

				ExcelWriter test = new ExcelWriter();

				test.setOutputFile("Results/settings"+j+"Results.xls");
				test.setResults(results);
				test.write();
				System.out.println("results written");
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
