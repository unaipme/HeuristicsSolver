package eus.unai.heuristics;

import eus.unai.heuristics.data.Instance;
import eus.unai.heuristics.exception.InfeasibleException;
import eus.unai.heuristics.solution.Solution;

import java.nio.file.Paths;

public class Heuristics {

    private static void printHelp() {
        System.out.println("Program needs some arguments to work!");
        System.out.println("java -jar heuristics.jar [filename] [greedy | grasp <alpha> <iterations>]");
    }

    public static void main(String [] args) throws Exception {
        if (args.length < 2) {
            printHelp();
            System.exit(1);
        }
        String filename = args[0];
        String mode = args[1];
        Instance instance = Instance.readFromFile(Paths.get(filename));
        GreedySolver solver = new GreedySolver(instance);
        if ("grasp".equals(mode)) {
            if (args.length != 4) {
                printHelp();
                System.exit(1);
            }
            double alpha = Double.parseDouble(args[2]);
            int k = Integer.parseInt(args[3]);
            Solution bestSolution = null;
            int infeasibleSolutions = 0;
            for (int i = 0; i < k; i++) {
                try {
                    Solution solution = solver.grasp(alpha);
                    //localSearch(solution);
                    if (bestSolution == null || solution.getCost() < bestSolution.getCost()) {
                        System.out.println("Better local solution found");
                        bestSolution = solution;
                    }
                } catch (InfeasibleException e) {
                    System.out.println("Infeasible random solution approached. Ignoring.");
                    System.out.println("Infeasible count: " + ++infeasibleSolutions + "/" + (i + 1));
                }
            }
            if (bestSolution == null) {
                System.out.println("No solution could be found");
            } else {
                bestSolution.print();
            }
        } else if ("greedy".equals(mode)) {
            Solution solution = solver.solve();
            solution.print();
        } else {
            System.out.println("No compatible mode selected");
            System.exit(1);
        }


    }

}
