package eus.unai.heuristics.solution;

import eus.unai.heuristics.data.Bus;
import eus.unai.heuristics.data.Driver;
import eus.unai.heuristics.data.Instance;
import eus.unai.heuristics.data.Service;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Solution extends HashSet<Candidate> {

    private Instance instance;

    public double getCost() {
        Iterator<Candidate> it = this.iterator();
        Map<Bus, Double> busCost = new HashMap<>();
        Map<Driver, Float> driverCost = new HashMap<>();
        while (it.hasNext()) {
            Candidate c = it.next();
            Bus b = c.getBus();
            Driver d = c.getDriver();
            Service s = c.getService();
            busCost.put(b, busCost.getOrDefault(b, 0d) + b.getEuros_min() * s.getSdt() + b.getEuros_km() * s.getSdd());
            if (!driverCost.containsKey(d)) {
                if (instance.getBM() >= d.getWt()) driverCost.put(d, d.getWt() * instance.getCBM());
                else driverCost.put(d, instance.getBM() * instance.getCBM() + instance.getCEM() * (d.getWt() - instance.getBM()));
            }
        }
        return busCost.entrySet().stream().mapToDouble(Map.Entry::getValue).sum()
                + driverCost.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
    }

    public Set<Candidate> ofBus(Bus b) {
        return stream().filter(c -> c.getBus().equals(b)).collect(Collectors.toSet());
    }

    public Set<Candidate> ofDriver(Driver d) {
        return stream().filter(c -> c.getDriver().equals(d)).collect(Collectors.toSet());
    }

    public void print() {
        System.out.println("The cost of the solution is " + getCost());
        this.stream()
            .sorted(Comparator.comparing(c -> c.getService().getIndex()))
            .forEach(c ->
                System.out.println(String.format("Service %d. Driver %d. Bus %d", c.getService().getIndex(), c.getDriver().getIndex(), c.getBus().getIndex()))
            );
    }

}
