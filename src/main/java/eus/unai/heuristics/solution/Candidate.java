package eus.unai.heuristics.solution;

import eus.unai.heuristics.data.Bus;
import eus.unai.heuristics.data.Driver;
import eus.unai.heuristics.data.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class Candidate {

    private Service service;
    private Bus bus;
    private Driver driver;

    @Override
    public int hashCode() {
        return Objects.hash(bus, driver, service);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Candidate)) return false;
        Candidate c = (Candidate) o;
        return c.getBus().equals(this.bus) && c.getDriver().equals(this.driver) && c.getService().equals(this.service);
    }
}
