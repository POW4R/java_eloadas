package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalPrice {
    private Instant time;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}

