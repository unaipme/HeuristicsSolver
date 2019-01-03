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

    /**
     * q(<s, d>)
     */
    private double evaluateDriverCost(Service s, Driver d, Solution solution) {
        if (solution.driverWorkedTime(d) + s.getSdt() <= instance.getBM()) {
            return s.getSdt() + instance.getCBM();
        } else {
            return instance.getCBM() * (instance.getBM() - solution.driverWorkedTime(d)) + instance.getCEM() * (solution.driverWorkedTime(d) + s.getSdt() - instance.getBM());
        }
    }

    /**
     * q(<s, b>)
     */
    private double evaluateBusCost(Service s, Bus b, Solution solution) {
        if (solution.getUsedBuses() < instance.getMaxBuses() || !solution.ofBus(b).isEmpty()) {
            return b.getEuros_min() * s.getSdt() + b.getEuros_km() * s.getSdd();
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    private double candidateCost(Candidate c, Solution solution) {
        return evaluateBusCost(c.getService(), c.getBus(), solution) + evaluateDriverCost(c.getService(), c.getDriver(), solution);
    }

    public Solution solve() {
        Solution solution = new Solution(this.instance);
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
                if (solution.driverWorkedTime(d) + s.getSdt() <= d.getMaxHours() * 60 && serviceOverlaps == 0) {
                    driverCandidateSet.add(d);
                }
            }
            if (driverCandidateSet.isEmpty() || busCandidateSet.isEmpty()) {
                throw new InfeasibleException();
            }
            Bus b_best = busCandidateSet.stream().min(Comparator.comparing(b -> evaluateBusCost(s, b, solution))).orElseThrow(InfeasibleException::new);
            Driver d_best = driverCandidateSet.stream().min(Comparator.comparing(d -> evaluateDriverCost(s, d, solution))).orElseThrow(InfeasibleException::new);
            solution.add(new Candidate(s, b_best, d_best));
        }
        return solution;
    }

    public Solution graspConstructivePhase(double alpha) {
        Random random = new Random();
        Solution solution = new Solution(this.instance);
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
                if (solution.driverWorkedTime(d) + s.getSdt() <= d.getMaxHours() * 60 && serviceOverlaps == 0) {
                    driverCandidateSet.add(d);
                }
            }
            if (driverCandidateSet.isEmpty() || busCandidateSet.isEmpty()) {
                throw new InfeasibleException();
            }

            double [] driverCosts = driverCandidateSet.stream()
                    .mapToDouble(d -> evaluateDriverCost(s, d, solution))
                    .sorted()
                    .toArray();
            List<Driver> driverRCL_min = driverCandidateSet.stream()
                    .filter(d -> evaluateDriverCost(s, d, solution) <= driverCosts[0] + alpha * (driverCosts[driverCosts.length - 1] - driverCosts[0]))
                    .collect(Collectors.toList());
            if (driverRCL_min.isEmpty()) throw new InfeasibleException();
            Driver d_rand = driverRCL_min.get(random.nextInt(driverRCL_min.size()));

            double [] busCosts = busCandidateSet.stream()
                    .mapToDouble(b -> evaluateBusCost(s, b, solution))
                    .sorted()
                    .toArray();
            List<Bus> busRCL_min = busCandidateSet.stream()
                    .filter(b -> evaluateBusCost(s, b, solution) <= busCosts[0] + alpha * (busCosts[busCosts.length - 1] - busCosts[0]))
                    .collect(Collectors.toList());
            if (busRCL_min.isEmpty()) throw new InfeasibleException();
            Bus b_rand = busRCL_min.get(random.nextInt(busRCL_min.size()));
            solution.add(new Candidate(s, b_rand, d_rand));
        }
        return solution;
    }

    // First-improvement, this is, first better option is chosen, the rest omitted
    // Reassignment neighbourhood, reassign driver / bus from one service to another
    public Solution localSearch(final Solution solution) {
        List<Candidate> finalSolution = new ArrayList<>(solution);
        ListIterator<Candidate> it = finalSolution.listIterator();
        int overlaps [][] = instance.overlaps();
        while (it.hasNext()) {
            Candidate c = it.next();
            Service s = c.getService();
            Driver d_best = c.getDriver();
            Bus b_best = c.getBus();
            Solution auxSolution = new Solution(finalSolution, this.instance);
            if (c.getService().getSdt() < instance.getBM() && solution.driverWorkedTime(c.getDriver()) > instance.getBM()) {
                for (Driver d : instance.getDrivers()) {
                    if (!d.equals(c.getDriver())
                            && auxSolution.ofDriver(d).stream().mapToInt(o -> overlaps[c.getService().getIndex()][o.getService().getIndex()]).sum() == 0
                            && auxSolution.driverWorkedTime(d) + c.getService().getSdt() <= instance.getBM()) {
                        Solution alternateSolution = new Solution(solution, this.instance);
                        alternateSolution.remove(c);
                        Candidate newCandidate = new Candidate(c.getService(), c.getBus(), d);
                        alternateSolution.add(newCandidate);
                        if (alternateSolution.getCost() < solution.getCost()) {
                            d_best = d;
                            break;
                        }
                    }
                }
            }
            List<Bus> buses = solution.stream()
                    .map(Candidate::getBus)
                    .distinct()
                    .sorted(Comparator.comparing(b -> b.getEuros_min() * s.getSdt() + b.getEuros_km() * s.getSdd()))
                    .collect(Collectors.toList());
            for (Bus b : buses) {
                if (!b.equals(c.getBus())
                        && auxSolution.ofBus(b).stream().mapToInt(o -> overlaps[s.getIndex()][o.getService().getIndex()]).sum() == 0) {
                    Solution alternateSolution = new Solution(solution, this.instance);
                    alternateSolution.remove(c);
                    Candidate newCandidate = new Candidate(c.getService(), b, c.getDriver());
                    alternateSolution.add(newCandidate);
                    if (alternateSolution.getCost() < solution.getCost()) {
                        b_best = b;
                        break;
                    }
                }
            }
            Candidate newCandidate = new Candidate(s, b_best, d_best);
            it.set(newCandidate);
        }
        return new Solution(finalSolution, this.instance);
    }

}
