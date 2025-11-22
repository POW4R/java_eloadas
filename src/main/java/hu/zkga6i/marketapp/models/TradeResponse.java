package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {
    private String tradeId;
    private String instrument;
    private Double units;
    private Double price;
    private Double pl;
    private Instant time;
    private String status;
}

