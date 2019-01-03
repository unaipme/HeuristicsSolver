package eus.unai.heuristics;

import eus.unai.heuristics.data.Instance;
import eus.unai.heuristics.data.Service;
import eus.unai.heuristics.exception.InfeasibleException;
import eus.unai.heuristics.solution.Solution;

import java.nio.file.Paths;

public class Heuristics {

    private static void printHelp() {
        System.out.println("Program needs some arguments to work!");
        System.out.println("java -jar heuristics.jar [filename] [greedy | graspConstructivePhase <alpha> <iterations>]");
    }

    public static void main(String [] args) throws Exception {
        if (args.length < 2) {
            printHelp();
            System.exit(1);
        }
        String filename = args[0];
        String mode = args[1].toLowerCase();
        Instance instance = Instance.readFromFile(Paths.get(filename));
        GreedySolver solver = new GreedySolver(instance);
        int overlaps[][] = instance.overlaps();
        for (int i = 0; i < instance.getNServices(); i++) {
            for (int j = i + 1; j < instance.getNServices(); j++) {
                if (overlaps[i][j] == 1) {
                    System.out.println(String.format("Services %d and %d overlap", i + 1, j + 1));
                }
            }
        }
        if ("grasp".equals(mode)) {
            if (args.length != 4) {
                printHelp();
                System.exit(1);
            }
            double alpha = Double.parseDouble(args[2]);
            int k = Integer.parseInt(args[3]);
            Solution bestSolution = null;
            double start = System.currentTimeMillis();
            for (int i = 0; i < k; i++) {
                try {
                    System.out.print("Iteration " + (i + 1) + ": ");
                    Solution solution = solver.graspConstructivePhase(alpha);
                    solution = solver.localSearch(solution);
                    if (bestSolution == null || solution.getCost() < bestSolution.getCost()) {
                        double oldCost = bestSolution == null ? Double.POSITIVE_INFINITY : bestSolution.getCost();
                        System.out.println("Better local solution found (OLD: " + oldCost + ", NEW: " + solution.getCost() + ").");
                        bestSolution = solution;
                    } else {
                        System.out.println("No better solution found (BEST: " + bestSolution.getCost() + ", CURRENT: " + solution.getCost() + ").");
                    }
                } catch (InfeasibleException e) {
                    System.out.println("Infeasible solution found. Ignoring.");
                }
            }
            double end = System.currentTimeMillis();
            if (bestSolution == null) {
                System.out.println("No solution could be found");
            } else {
                bestSolution.print();
            }
            System.out.println("Total duration of execution: " + (end - start) / 1000);
        } else if ("greedy".equals(mode)) {
            System.out.println("Running greedy algorithm");
            double start = System.currentTimeMillis();
            Solution solution = solver.solve();
            System.out.println("Running local search");
            solution = solver.localSearch(solution);
            double end = System.currentTimeMillis();
            solution.print();
            System.out.println("Total duration of execution: " + (end - start) / 1000);
        } else {
            System.out.println("No compatible mode selected");
            System.exit(1);
        }


    }

}
