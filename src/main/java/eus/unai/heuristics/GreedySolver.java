package eus.unai.heuristics;

import eus.unai.heuristics.data.Bus;
import eus.unai.heuristics.data.Driver;
import eus.unai.heuristics.data.Instance;
import eus.unai.heuristics.data.Service;
import eus.unai.heuristics.exception.InfeasibleException;
import eus.unai.heuristics.solution.Candidate;
import eus.unai.heuristics.solution.Solution;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GreedySolver {

    private @NonNull Instance instance;
    private int usedBuses = 0;

    /**
     * q(<s, d>)
     */
    private double evaluateDriverCost(Service s, Driver d) {
        if (d.getWt() + s.getSdt() <= instance.getBM()) {
            return s.getSdt() + instance.getCBM();
        } else {
            return instance.getCBM() * (instance.getBM() - d.getWt()) + instance.getCEM() * (d.getWt() + s.getSdt() - instance.getBM());
        }
    }

    /**
     * q(<s, b>)
     */
    private double evaluateBusCost(Service s, Bus b, Solution solution) {
        if ((usedBuses < instance.getMaxBuses() && solution.ofBus(b).isEmpty()) || !solution.ofBus(b).isEmpty()) {
            return b.getEuros_min() * s.getSdt() + b.getEuros_km() * s.getSdd();
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    public Solution solve() {
        Solution solution = new Solution(this.instance);
        this.usedBuses = 0;
        int [][] overlap = instance.overlaps();
        for (Service s : instance.getServices()) {
            Set<Bus> busCandidateSet = new HashSet<>();
            Set<Driver> driverCandidateSet = new HashSet<>();
            for (Bus b : instance.getBuses()) {
                int serviceOverlaps = solution.ofBus(b).stream().mapToInt(c -> overlap[s.getIndex()][c.getService().getIndex()]).sum();
                if (b.getCap() >= s.getDem() && serviceOverlaps == 0) {
                    busCandidateSet.add(b);
                }
            }
            for (Driver d : instance.getDrivers()) {
                int serviceOverlaps = solution.ofDriver(d).stream().mapToInt(c -> overlap[s.getIndex()][c.getService().getIndex()]).sum();
                if (d.getWt() + s.getSdt() <= d.getMaxHours() + 60 && serviceOverlaps == 0) {
                    driverCandidateSet.add(d);
                }
            }
            if (driverCandidateSet.isEmpty() || busCandidateSet.isEmpty()) throw new InfeasibleException();
            Bus b_best = busCandidateSet.stream().min(Comparator.comparing(b -> evaluateBusCost(s, b, solution))).orElseThrow(InfeasibleException::new);
            Driver d_best = driverCandidateSet.stream().min(Comparator.comparing(d -> evaluateDriverCost(s, d))).orElseThrow(InfeasibleException::new);
            if (solution.ofBus(b_best).isEmpty()) {
                usedBuses++;
            }
            d_best.addWorkedTime(s.getSdt());
            solution.add(new Candidate(s, b_best, d_best));
        }
        return solution;
    }

    public Solution grasp(double alpha) {
        Random rand = new Random();
        Solution solution = new Solution(this.instance);
        this.usedBuses = 0;
        int [][] overlap = instance.overlaps();
        for (Service s : instance.getServices()) {
            Set<Bus> busCandidateSet = new HashSet<>();
            Set<Driver> driverCandidateSet = new HashSet<>();
            for (Bus b : instance.getBuses()) {
                int serviceOverlaps = solution.ofBus(b).stream().mapToInt(c -> overlap[s.getIndex()][c.getService().getIndex()]).sum();
                if (b.getCap() >= s.getDem() && serviceOverlaps == 0) {
                    busCandidateSet.add(b);
                }
            }
            for (Driver d : instance.getDrivers()) {
                int serviceOverlaps = solution.ofDriver(d).stream().mapToInt(c -> overlap[s.getIndex()][c.getService().getIndex()]).sum();
                if (d.getWt() + s.getSdt() <= d.getMaxHours() + 60 && serviceOverlaps == 0) {
                    driverCandidateSet.add(d);
                }
            }
            if (driverCandidateSet.isEmpty() || busCandidateSet.isEmpty()) throw new InfeasibleException();


            double [] busCosts = Arrays.stream(instance.getBuses())
                        .mapToDouble(b -> evaluateBusCost(s, b, solution))
                        .sorted()
                        .toArray();
            List<Bus> busRCL_min = busCandidateSet.stream()
                    .filter(b -> evaluateBusCost(s, b, solution) <= busCosts[0] + alpha * (busCosts[busCosts.length - 1] - busCosts[0]))
                    .collect(Collectors.toList());
            Bus b_best = busRCL_min.get(rand.nextInt(busRCL_min.size()));

            double [] driverCosts = Arrays.stream(instance.getDrivers())
                    .mapToDouble(d -> evaluateDriverCost(s, d))
                    .sorted()
                    .toArray();
            List<Driver> driverRCL_min = driverCandidateSet.stream()
                    .filter(d -> evaluateDriverCost(s, d) <= driverCosts[0] + alpha * (driverCosts[driverCosts.length - 1] - driverCosts[0]))
                    .collect(Collectors.toList());
            Driver d_best = driverRCL_min.get(rand.nextInt(driverRCL_min.size()));

            if (solution.ofBus(b_best).isEmpty()) {
                usedBuses++;
            }
            d_best.addWorkedTime(s.getSdt());
            solution.add(new Candidate(s, b_best, d_best));
        }
        return solution;
    }

}
