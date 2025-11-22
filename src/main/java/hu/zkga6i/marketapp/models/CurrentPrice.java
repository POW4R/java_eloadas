package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPrice {
    private String instrument;
    private Double bid;
    private Double ask;
    private Double spread;
    private Instant time;
}

