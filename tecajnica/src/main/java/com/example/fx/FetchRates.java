package com.example.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import com.example.fx.FetchRates;

public class FetchRates {

    public static final String API_URL_TEMPLATE =
            "http://localhost:8081/api/rates/by-currency?currency=%s&from=%s&to=%s";

    public static List<RateEntry> fetchRates(String currency, LocalDate from, LocalDate to) throws Exception {
        String url = String.format(API_URL_TEMPLATE, currency, from, to);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        List<RateEntry> rates = new ArrayList<>();
        for (JsonNode node : root) {
            JsonNode dateNode = node.get("date");
            JsonNode rateNode = node.get("rate");
            if (dateNode != null && rateNode != null) {
                LocalDate parsedDate = LocalDate.parse(dateNode.asText());
                double rate = rateNode.asDouble();
                rates.add(new RateEntry(parsedDate, rate));
            }
        }
        return rates;
    }
}
