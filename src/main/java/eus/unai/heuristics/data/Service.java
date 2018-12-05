package eus.unai.heuristics.data;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Service {

    private int index;

    private int st;
    private int sdt;
    private int sdd;
    private int dem;

    @Override
    public int hashCode() {
        return Objects.hash(index, st, sdt, sdd, dem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Service)) return false;
        Service s = (Service) o;
        return s.getDem() == this.dem && s.getSdd() == this.sdd && s.getSdt() == this.sdt && s.getSt() == this.st
                && s.getIndex() == this.index;
    }
}
