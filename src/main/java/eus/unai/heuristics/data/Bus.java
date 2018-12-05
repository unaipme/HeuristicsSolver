package eus.unai.heuristics.data;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Bus {

    private int index;

    private int cap;
    private float euros_min;
    private float euros_km;

    @Override
    public int hashCode() {
        return Objects.hash(index, cap, euros_min, euros_km);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Bus)) return false;
        Bus b = (Bus) o;
        return b.getCap() == this.cap && b.getEuros_km() == this.euros_km && b.getEuros_min() == this.euros_min
                && b.getIndex() == this.index;
    }
}
