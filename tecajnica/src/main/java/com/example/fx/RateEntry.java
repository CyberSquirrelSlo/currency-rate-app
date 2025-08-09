package com.example.fx;

import java.time.LocalDate;
import java.util.*;

public  class RateEntry {
    LocalDate date;
    double rate;

    RateEntry(LocalDate date, double rate) {
        this.date = date;
        this.rate = rate;
    }
}