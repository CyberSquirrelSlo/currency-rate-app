package com.example.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;
import com.example.fx.*;

public class ExchangeRateApp extends Application {

    private static final String[] ALL_CURRENCIES = {"USD", "AUD", "GBP", "CHF"};

    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<String> baseCurrencyComboBox;
    private ComboBox<String> targetCurrencyComboBox;
    private LineChart<String, Number> lineChart;
    private TableView<Currency> tableView;

    private CategoryAxis xAxis;
    private NumberAxis yAxis;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Exchange Rate & Opportunity Cost Comparison");

        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Rate");
        xAxis.setTickLabelRotation(90);
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Exchange Rates and Opportunity Cost");
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(false);

        fromDatePicker = new DatePicker(LocalDate.of(2025, 8, 1));
        toDatePicker = new DatePicker(LocalDate.now());

        baseCurrencyComboBox = new ComboBox<>();
        baseCurrencyComboBox.getItems().addAll(ALL_CURRENCIES);
        baseCurrencyComboBox.setValue(ALL_CURRENCIES[0]);

        targetCurrencyComboBox = new ComboBox<>();
        targetCurrencyComboBox.getItems().addAll(ALL_CURRENCIES);
        targetCurrencyComboBox.setValue(ALL_CURRENCIES.length > 1 ? ALL_CURRENCIES[1] : ALL_CURRENCIES[0]);

        Button showRatesButton = new Button("Show Rates");
        showRatesButton.setOnAction(e -> loadDataAsync());

        Button calcOpportunityButton = new Button("Calculate Opportunity Cost");
        calcOpportunityButton.setOnAction(e -> calculateOpportunityCostAsync());

        HBox controls = new HBox(10, fromDatePicker, toDatePicker, baseCurrencyComboBox, targetCurrencyComboBox, showRatesButton, calcOpportunityButton);
        controls.setPadding(new Insets(10));

        tableView = new TableView<>();
        TableColumn<Currency, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Currency, String> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("rate"));
        rateCol.setCellFactory(TextFieldTableCell.forTableColumn());

        tableView.getColumns().addAll(dateCol, rateCol);
        tableView.setEditable(false);

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setBottom(lineChart);
        root.setCenter(tableView);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/chart-style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        loadDataAsync();
    }

    private void loadDataAsync() {
        Platform.runLater(() -> {
            lineChart.getData().clear();
            xAxis.getCategories().clear();
            tableView.getItems().clear();
        });

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String currency = baseCurrencyComboBox.getValue();

        Task<List<RateEntry>> task = new Task<>() {
            @Override
            protected List<RateEntry> call() throws Exception {
                List<RateEntry> rates = FetchRates.fetchRates(currency, fromDate, toDate);
                rates.sort(Comparator.comparing(r -> r.date));
                return rates;
            }
        };

        task.setOnSucceeded(event -> {
            List<RateEntry> rates = task.getValue();

            Platform.runLater(() -> {
                lineChart.getData().clear();
                xAxis.getCategories().clear();
                tableView.getItems().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(currency);
                String prevDate = "";

                for (RateEntry r : rates) {
                    series.getData().add(new XYChart.Data<>(r.date.toString(), r.rate));
                    String currentDate = r.date.toString();
                    if (!currentDate.equals(prevDate)) {
                        tableView.getItems().add(new Currency(currentDate, String.valueOf(r.rate)));
                    }
                    prevDate = r.date.toString();
                }

                lineChart.getData().add(series);
                xAxis.setCategories(FXCollections.observableArrayList(
                        rates.stream().map(r -> r.date.toString()).toList()
                ));
            });
        });

        task.setOnFailed(event -> task.getException().printStackTrace());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void calculateOpportunityCostAsync() {
        Platform.runLater(() -> {
            lineChart.getData().clear();
            xAxis.getCategories().clear();
            tableView.getItems().clear();
        });

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String baseCurrency = baseCurrencyComboBox.getValue();
        String targetCurrency = targetCurrencyComboBox.getValue();

        if (baseCurrency.equals(targetCurrency)) {
            System.err.println("Base and target currencies must be different for opportunity cost calculation.");
            return;
        }

        Task<Map<LocalDate, Double>> task = new Task<>() {
            @Override
            protected Map<LocalDate, Double> call() throws Exception {
                List<RateEntry> baseRates = FetchRates.fetchRates(baseCurrency, fromDate, toDate);
                List<RateEntry> targetRates = FetchRates.fetchRates(targetCurrency, fromDate, toDate);

                Map<LocalDate, Double> baseMap = new HashMap<>();
                for (RateEntry r : baseRates) {
                    baseMap.put(r.date, r.rate);
                }

                Map<LocalDate, Double> targetMap = new HashMap<>();
                for (RateEntry r : targetRates) {
                    targetMap.put(r.date, r.rate);
                }

                Set<LocalDate> commonDates = new HashSet<>(baseMap.keySet());
                commonDates.retainAll(targetMap.keySet());

                Map<LocalDate, Double> opportunityCosts = new TreeMap<>();
                for (LocalDate date : commonDates) {
                    double diff = targetMap.get(date) - baseMap.get(date);
                    opportunityCosts.put(date, diff);
                }

                return opportunityCosts;
            }
        };

        task.setOnSucceeded(event -> {
            Map<LocalDate, Double> opportunityCosts = task.getValue();

            Platform.runLater(() -> {
                lineChart.getData().clear();
                xAxis.getCategories().clear();
                tableView.getItems().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Opportunity Cost: " + baseCurrency + " vs " + targetCurrency);

                List<String> categories = new ArrayList<>();
                for (Map.Entry<LocalDate, Double> entry : opportunityCosts.entrySet()) {
                    String dateStr = entry.getKey().toString();
                    series.getData().add(new XYChart.Data<>(dateStr, entry.getValue()));
                    tableView.getItems().add(new Currency(dateStr, String.valueOf(entry.getValue())));
                    categories.add(dateStr);
                }

                xAxis.setCategories(FXCollections.observableArrayList(categories));
                lineChart.getData().add(series);
            });
        });

        task.setOnFailed(event -> task.getException().printStackTrace());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
