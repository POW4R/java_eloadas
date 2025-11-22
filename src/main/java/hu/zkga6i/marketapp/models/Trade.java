package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private String tradeId;
    private String instrument;
    private Double units;
    private Double price;
    private Double currentPrice;
    private Double unrealizedPL;
    private Instant openTime;
    
    // Helper method
    public boolean isLong() {
        return units > 0;
    }
}

