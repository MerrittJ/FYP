package Model;

import java.util.ArrayList;

import View.View;

public class main {

	public static void main(String argv[]) {
		try {
			int runs = 5;
			ArrayList<String> settings = new ArrayList<String>();

			//settings.add("settings11");
//			settings.add("settings12");
			//settings.add("settings13");
//			settings.add("settings14");
			//settings.add("settings15");
			settings.add("settings21");
//			settings.add("settings22");
			settings.add("settings23");
//			settings.add("settings24");
			settings.add("settings25");
			//settings.add("settings31");
//			settings.add("settings32");
			//settings.add("settings33");
//			settings.add("settings34");
			//settings.add("settings35");
			//settings.add("settings41");
//			settings.add("settings42");
			//settings.add("settings43");
//			settings.add("settings44");
			//settings.add("settings45");

			for (int j = 0;j<settings.size();j++){
				ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>>> results = new ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>>>(); //run -> gen -> pop#,fit,turb#
				for (int i = 0; i<runs;i++){
					WindScenario ws = new WindScenario("Scenarios/00.xml"); //def = Scenarios/obs_00.xml
					KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
					wfle.initialize(ws);
					MyGA2 myga = new MyGA2(wfle, "Settings/"+settings.get(j)+".properties", i);
					//View v = new View(myga.getBestLayout());

					results.add(myga.getResults());

					//GA ga = new GA(wfle);
					//ga.run();
					//MyFirstGA algorithm = new MyFirstGA(wfle);
					//algorithm.run(); // optional, name of method 'run' provided on submission
					// algorithm can also just use constructor 
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
