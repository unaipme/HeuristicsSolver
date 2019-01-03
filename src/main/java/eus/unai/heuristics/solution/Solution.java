package eus.unai.heuristics.solution;

import eus.unai.heuristics.data.Bus;
import eus.unai.heuristics.data.Driver;
import eus.unai.heuristics.data.Instance;
import eus.unai.heuristics.data.Service;
import eus.unai.heuristics.exception.InfeasibleException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Solution extends HashSet<Candidate> {

    private @NonNull Instance instance;

    private Double cost = null;
    private @Getter int usedBuses = 0;

    public Solution(Collection<? extends Candidate> collection, Instance instance) {
        this.instance = instance;
        collection.forEach(this::add);
    }

    @Override
    public boolean add(Candidate c) {
        if (ofBus(c.getBus()).isEmpty()) {
            if (usedBuses == instance.getMaxBuses()) throw new InfeasibleException();
            usedBuses++;
        }
        return super.add(c);
    }

    public double getCost() {
        if (this.cost == null) {
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
                    if (instance.getBM() >= driverWorkedTime(d)) {
                        driverCost.put(d, driverWorkedTime(d) * instance.getCBM());
                    } else {
                        driverCost.put(d, instance.getBM() * instance.getCBM() + instance.getCEM() * (driverWorkedTime(d) - instance.getBM()));
                    }
                }
            }
            this.cost = busCost.entrySet().stream().mapToDouble(Map.Entry::getValue).sum()
                    + driverCost.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
        }
        return this.cost;
    }

    public Set<Candidate> ofBus(Bus b) {
        return stream().filter(c -> c.getBus().equals(b)).collect(Collectors.toSet());
    }

    public Set<Candidate> ofDriver(Driver d) {
        return stream().filter(c -> c.getDriver().equals(d)).collect(Collectors.toSet());
    }

    public int driverWorkedTime(Driver d) {
        return ofDriver(d).stream().mapToInt(c ->  c.getService().getSdt()).sum();
    }

    public void print() {
        this.stream()
            .sorted(Comparator.comparing(c -> c.getService().getIndex()))
            .forEach(c ->
                System.out.println(String.format("Service %d. Driver %d. Bus %d", c.getService().getIndex() + 1, c.getDriver().getIndex(), c.getBus().getIndex()))
            );
        System.out.println("The cost of the solution is " + getCost());
    }

    public boolean forAllServices() {
        for (Service s : instance.getServices()) {
            boolean match = stream().anyMatch(c -> c.getService().equals(s));
            if (!match) return false;
        }
        return true;
    }

}
