package hu.zkga6i.marketapp.controllers;

import hu.zkga6i.marketapp.models.ExchangeRateData;
import hu.zkga6i.marketapp.services.MnbSoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/soap")
public class SoapController {

    @Autowired
    private MnbSoapService mnbSoapService;

    @GetMapping
    public String soapPage(Model model) {
        List<String> currencies = mnbSoapService.getCurrencies();
        model.addAttribute("currencies", currencies);
        return "soap";
    }

    @PostMapping("/rates")
    public String getExchangeRates(
            @RequestParam String currency,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {

        List<String> currencies = mnbSoapService.getCurrencies();
        model.addAttribute("currencies", currencies);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<ExchangeRateData> rates = mnbSoapService.getExchangeRates(currency, start, end);

        model.addAttribute("selectedCurrency", currency);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("rates", rates);

        return "soap";
    }
}

