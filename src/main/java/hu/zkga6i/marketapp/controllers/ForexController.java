package hu.zkga6i.marketapp.controllers;


import hu.zkga6i.marketapp.models.*;
import hu.zkga6i.marketapp.services.ForexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/forex")
public class ForexController {

    @Autowired
    private ForexService forexService;

    @GetMapping("/account")
    public String accountInfo(Model model) {
        try {
            AccountInfo account = forexService.getAccountInfo();
            model.addAttribute("account", account);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch account information: " + e.getMessage());
        }
        return "forex-account";
    }

    @GetMapping("/current-price")
    public String currentPrice(Model model) {
        List<String> instruments = forexService.getAvailableInstruments();
        model.addAttribute("instruments", instruments);
        return "forex-current-price";
    }

    @PostMapping("/current-price")
    public String getCurrentPrice(@RequestParam String instrument, Model model) {
        List<String> instruments = forexService.getAvailableInstruments();
        model.addAttribute("instruments", instruments);
        model.addAttribute("selectedInstrument", instrument);

        try {
            CurrentPrice price = forexService.getCurrentPrice(instrument);
            model.addAttribute("price", price);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch price: " + e.getMessage());
        }

        return "forex-current-price";
    }

    @GetMapping("/historical-price")
    public String historicalPrice(Model model) {
        List<String> instruments = forexService.getAvailableInstruments();
        List<String> granularities = forexService.getGranularities();
        model.addAttribute("instruments", instruments);
        model.addAttribute("granularities", granularities);
        return "forex-historical-price";
    }

    @PostMapping("/historical-price")
    public String getHistoricalPrice(
            @RequestParam String instrument,
            @RequestParam String granularity,
            Model model) {

        List<String> instruments = forexService.getAvailableInstruments();
        List<String> granularities = forexService.getGranularities();
        model.addAttribute("instruments", instruments);
        model.addAttribute("granularities", granularities);
        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("selectedGranularity", granularity);

        try {
            List<HistoricalPrice> prices = forexService.getHistoricalPrices(instrument, granularity, 10);
            model.addAttribute("prices", prices);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch historical prices: " + e.getMessage());
        }

        return "forex-historical-price";
    }

    @GetMapping("/open")
    public String openPosition(Model model) {
        List<String> instruments = forexService.getAvailableInstruments();
        model.addAttribute("instruments", instruments);
        return "forex-open";
    }

    @PostMapping("/open")
    public String openNewPosition(
            @RequestParam String instrument,
            @RequestParam int units,
            Model model) {

        List<String> instruments = forexService.getAvailableInstruments();
        model.addAttribute("instruments", instruments);

        try {
            TradeResponse response = forexService.openPosition(instrument, units);
            model.addAttribute("success", "Position opened successfully!");
            model.addAttribute("response", response);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to open position: " + e.getMessage());
        }

        return "forex-open";
    }

    @GetMapping("/positions")
    public String listPositions(Model model) {
        try {
            List<Trade> trades = forexService.getOpenTrades();
            model.addAttribute("positions", trades);
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch positions: " + e.getMessage());
        }
        return "forex-positions";
    }

    @GetMapping("/close")
    public String closePosition(@RequestParam(required = false) String tradeId, Model model) {
        // If tradeId is provided, close it directly and redirect
        if (tradeId != null && !tradeId.trim().isEmpty()) {
            // Validate tradeId format (should be numeric)
            if (!tradeId.matches("\\d+")) {
                return "redirect:/forex/positions?error=Invalid Trade ID format. Must be numeric.";
            }

            try {
                TradeResponse response = forexService.closePosition(tradeId.trim());
                model.addAttribute("success", "Position closed successfully! Trade ID: " + response.getTradeId() +
                                             ", Closed at: " + response.getPrice() +
                                             ", Units: " + response.getUnits());
                // Redirect to positions page
                return "redirect:/forex/positions?closed=true&tradeId=" + tradeId;
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg.contains("404") || errorMsg.contains("not found")) {
                    errorMsg = "Trade ID " + tradeId + " not found. It may have already been closed.";
                }
                return "redirect:/forex/positions?error=" + errorMsg;
            }
        }
        return "forex-close";
    }

    @PostMapping("/close")
    public String closePositionById(@RequestParam String tradeId, Model model) {
        // Validate tradeId is not empty
        if (tradeId == null || tradeId.trim().isEmpty()) {
            model.addAttribute("error", "Trade ID cannot be empty!");
            return "forex-close";
        }

        // Validate tradeId format (should be numeric)
        if (!tradeId.matches("\\d+")) {
            model.addAttribute("error", "Invalid Trade ID format. Must be numeric.");
            return "forex-close";
        }

        try {
            TradeResponse response = forexService.closePosition(tradeId.trim());
            model.addAttribute("success", "Position closed successfully!");
            model.addAttribute("response", response);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("404") || errorMsg.contains("not found")) {
                errorMsg = "Trade ID " + tradeId + " not found. It may have already been closed.";
            }
            model.addAttribute("error", "Unable to close position: " + errorMsg);
        }
        return "forex-close";
    }
}

