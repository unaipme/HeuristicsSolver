package eus.unai.heuristics.data;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Driver {

    private int index;

    private int maxHours;

    private @Builder.Default int wt = 0;

    @Override
    public int hashCode() {
        return Objects.hash(index, maxHours);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Driver)) return false;
        Driver d = (Driver) o;
        return d.getIndex() == this.index && d.getMaxHours() == this.maxHours;
    }

    public void addWorkedTime(int n) {
        this.wt += n;
    }

}
