public class main {

  public static void main(String argv[]) {
      try {
          WindScenario ws = new WindScenario("Scenarios/00.xml"); //def = Scenarios/obs_00.xml
          KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
          wfle.initialize(ws);
          MyGA2 myga = new MyGA2(wfle);
          //GA ga = new GA(wfle);
          //ga.run();
          //MyFirstGA algorithm = new MyFirstGA(wfle);
          //algorithm.run(); // optional, name of method 'run' provided on submission
          // algorithm can also just use constructor
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
