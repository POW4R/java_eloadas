package hu.zkga6i.marketapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    private String id;
    private String currency;
    private Double balance;
    private Double nav;
    private Double unrealizedPL;
    private Double realizedPL;
    private Double marginUsed;
    private Double marginAvailable;
    private Integer openTradeCount;
    private Integer openPositionCount;
}

