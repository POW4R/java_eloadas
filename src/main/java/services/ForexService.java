package hu.zkga6i.marketapp.services;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;
import com.oanda.v20.trade.TradeSpecifier;
import hu.zkga6i.marketapp.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForexService {

    private final Context ctx;
    private final AccountID accountId;

    public ForexService(
            @Value("${oanda.api.token}") String apiToken,
            @Value("${oanda.api.account-id}") String accountIdString,
            @Value("${oanda.api.url}") String apiUrl) {

        this.ctx = new ContextBuilder(apiUrl)
                .setToken(apiToken)
                .setApplication("MarketApp")
                .build();
        this.accountId = new AccountID(accountIdString);
    }

    public AccountInfo getAccountInfo() {
        try {
            AccountSummary summary = ctx.account.summary(accountId).getAccount();

            return new AccountInfo(
                    summary.getId().toString(),
                    summary.getCurrency().toString(),
                    summary.getBalance().doubleValue(),
                    summary.getNAV().doubleValue(),
                    summary.getUnrealizedPL().doubleValue(),
                    summary.getPl().doubleValue(),
                    summary.getMarginUsed().doubleValue(),
                    summary.getMarginAvailable().doubleValue(),
                    summary.getOpenTradeCount().intValue(),
                    summary.getOpenPositionCount().intValue()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get account info: " + e.getMessage(), e);
        }
    }

    public CurrentPrice getCurrentPrice(String instrument) {
        try {
            PricingGetRequest request = new PricingGetRequest(accountId, Collections.singletonList(instrument));
            PricingGetResponse response = ctx.pricing.get(request);

            if (response.getPrices().isEmpty()) {
                throw new RuntimeException("No price data available for " + instrument);
            }

            ClientPrice price = response.getPrices().getFirst();
            double bid = price.getBids().getFirst().getPrice().doubleValue();
            double ask = price.getAsks().getFirst().getPrice().doubleValue();

            return new CurrentPrice(
                    price.getInstrument().toString(),
                    bid,
                    ask,
                    ask - bid,
                    Instant.parse(price.getTime())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current price: " + e.getMessage(), e);
        }
    }

    public List<HistoricalPrice> getHistoricalPrices(String instrument, String granularity, int count) {
        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new com.oanda.v20.primitives.InstrumentName(instrument));
            request.setGranularity(CandlestickGranularity.valueOf(granularity));
            request.setCount((long) count);

            InstrumentCandlesResponse response = ctx.instrument.candles(request);

            return response.getCandles().stream()
                    .map(candle -> new HistoricalPrice(
                            Instant.parse(candle.getTime()),
                            candle.getMid().getO().doubleValue(),
                            candle.getMid().getH().doubleValue(),
                            candle.getMid().getL().doubleValue(),
                            candle.getMid().getC().doubleValue(),
                            candle.getVolume()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get historical prices: " + e.getMessage(), e);
        }
    }

    public TradeResponse openPosition(String instrument, int units) {
        try {
            MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
            marketOrderRequest.setInstrument(instrument);
            marketOrderRequest.setUnits(units);

            OrderCreateRequest request = new OrderCreateRequest(accountId);
            request.setOrder(marketOrderRequest);

            OrderCreateResponse response = ctx.order.create(request);

            return new TradeResponse(
                    response.getOrderFillTransaction().getId().toString(),
                    instrument,
                    (double) units,
                    response.getOrderFillTransaction().getPrice().doubleValue(),
                    0.0,
                    Instant.parse(response.getOrderFillTransaction().getTime()),
                    "OPENED"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to open position: " + e.getMessage(), e);
        }
    }


    public List<Trade> getOpenTrades() {
        try {
            List<com.oanda.v20.trade.Trade> trades = ctx.trade.list(accountId).getTrades();

            return trades.stream()
                    .map(trade -> {
                        double currentUnits = trade.getCurrentUnits().doubleValue();
                        double openPrice = trade.getPrice().doubleValue();
                        double unrealizedPL = trade.getUnrealizedPL().doubleValue();

                        // Get current price for the instrument
                        double currentPrice = openPrice; // Default to open price
                        try {
                            CurrentPrice priceData = getCurrentPrice(trade.getInstrument().toString());
                            currentPrice = currentUnits > 0 ? priceData.getBid() : priceData.getAsk();
                        } catch (Exception e) {
                            // Use open price if current price fetch fails
                        }

                        return new Trade(
                                trade.getId().toString(),
                                trade.getInstrument().toString(),
                                currentUnits,
                                openPrice,
                                currentPrice,
                                unrealizedPL,
                                Instant.parse(trade.getOpenTime())
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get open trades: " + e.getMessage(), e);
        }
    }

    public TradeResponse closePosition(String tradeId) {
        try {
            TradeSpecifier tradeSpecifier = new TradeSpecifier(tradeId);
            TradeCloseRequest request = new TradeCloseRequest(accountId, tradeSpecifier);

            TradeCloseResponse response = ctx.trade.close(request);

            return new TradeResponse(
                    tradeId,
                    response.getOrderFillTransaction().getInstrument().toString(),
                    response.getOrderFillTransaction().getUnits().doubleValue(),
                    response.getOrderFillTransaction().getPrice().doubleValue(),
                    0.0,
                    Instant.parse(response.getOrderFillTransaction().getTime()),
                    "CLOSED"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to close position: " + e.getMessage(), e);
        }
    }

    public List<String> getAvailableInstruments() {
        return Arrays.asList(
                "EUR_USD", "GBP_USD", "USD_JPY", "USD_CHF", "USD_CAD",
                "AUD_USD", "NZD_USD", "EUR_GBP", "EUR_JPY", "GBP_JPY"
        );
    }

    public List<String> getGranularities() {
        return Arrays.stream(CandlestickGranularity.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}

