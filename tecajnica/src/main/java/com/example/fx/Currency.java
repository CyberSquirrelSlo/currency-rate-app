package com.example.fx;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;
import java.util.*;

public  class Currency {
    private final StringProperty date;
    private final StringProperty rate;

    public Currency(String date, String rate) {
        this.date = new SimpleStringProperty(date);
        this.rate = new SimpleStringProperty(rate);
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String value) {
        date.set(value);
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getRate() {
        return rate.get();
    }

    public void setRate(String value) {
        rate.set(value);
    }

    public StringProperty rateProperty() {
        return rate;
    }
}
