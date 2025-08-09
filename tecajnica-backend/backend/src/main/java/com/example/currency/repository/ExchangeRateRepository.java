package com.example.currency.repository;

import com.example.currency.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findByCurrencyCodeOrderByDate(String currencyCode);
    List<ExchangeRate> findByCurrencyCodeAndDateBetweenOrderByDate(String currencyCode, LocalDate from, LocalDate to);
}
