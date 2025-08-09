package com.example.currency.controller;

import com.example.currency.model.ExchangeRate;
import com.example.currency.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.example.currency.service.*;
import java.time.LocalDate;
import java.util.List;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {

    @Autowired
    private ExchangeRateParserService parserService;

    @Autowired
    private ExchangeRateRepository repository;

    @GetMapping
    public List<ExchangeRate> getAll() {
        return repository.findAll();
    }

    @GetMapping("/by-currency")
    public List<ExchangeRate> getByCurrency(@RequestParam String currency,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        System.out.println("Query currency: " + currency + ", from: " + from + ", to: " + to);
        List<ExchangeRate> results;

        if (from != null && to != null) {
            results = repository.findByCurrencyCodeAndDateBetweenOrderByDate(currency.toUpperCase(), from, to);
        } else {
            results = repository.findByCurrencyCodeOrderByDate(currency.toUpperCase());
        }

        System.out.println("Found " + results.size() + " rates");
        return results;
    }


    @GetMapping("/import")
    public List<ExchangeRate> importRates() throws IOException {
        try {
            return parserService.parseAndSave("https://www.bsi.si/_data/tecajnice/dtecbs-l.xml");
        } catch (Exception e) {
            throw new IOException("Failed to import exchange rates", e);
        }
    }
}
