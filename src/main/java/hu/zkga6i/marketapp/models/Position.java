package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private String instrument;
    private Double longUnits;
    private Double shortUnits;
    private Double unrealizedPL;
    private Double realizedPL;

    public Double getNetUnits() {
        return longUnits + shortUnits;
    }

    public boolean isLong() {
        return getNetUnits() > 0;
    }

    public boolean isShort() {
        return getNetUnits() < 0;
    }
}

