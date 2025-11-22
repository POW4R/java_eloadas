package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateData {
    private LocalDate date;
    private String currency;
    private Double rate;
}

