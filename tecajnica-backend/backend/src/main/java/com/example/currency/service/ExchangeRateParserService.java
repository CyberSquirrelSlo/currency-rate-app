package com.example.currency.service;

import com.example.currency.model.ExchangeRate;
import com.example.currency.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeRateParserService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateParserService.class);

    @Autowired
    private ExchangeRateRepository repository;

    /**
     * Parses exchange rates from the given XML URL and saves them to the repository.
     *
     * @param urlString the URL string pointing to the XML data source
     * @return list of saved ExchangeRate entities
     * @throws IOException if an error occurs during parsing or saving
     */
    public List<ExchangeRate> parseAndSave(String urlString) throws IOException {
        List<ExchangeRate> savedRates = new ArrayList<>();

        try (InputStream inputStream = new URL(urlString).openStream()) {
            logger.info("Opening XML stream from URL: {}", urlString);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Important due to XML namespaces

            Document doc = factory.newDocumentBuilder().parse(inputStream);
            doc.getDocumentElement().normalize();

            String ns = "http://www.bsi.si"; // Namespace from the XML

            NodeList tecajnicaList = doc.getElementsByTagNameNS(ns, "tecajnica");

            logger.info("Found {} <tecajnica> elements", tecajnicaList.getLength());

            for (int i = 0; i < tecajnicaList.getLength(); i++) {
                Element tecajnica = (Element) tecajnicaList.item(i);

                String datumStr = tecajnica.getAttribute("datum");
                LocalDate date = LocalDate.parse(datumStr); // Format is YYYY-MM-DD

                NodeList tecajList = tecajnica.getElementsByTagNameNS(ns, "tecaj");
                logger.debug("Date {}: Found {} <tecaj> elements", datumStr, tecajList.getLength());

                for (int j = 0; j < tecajList.getLength(); j++) {
                    Element tecaj = (Element) tecajList.item(j);

                    String currencyCode = tecaj.getAttribute("oznaka");
                    String rateStr = tecaj.getTextContent().trim().replace(",", ".");
                    BigDecimal rate = new BigDecimal(rateStr);

                    ExchangeRate er = new ExchangeRate();
                    er.setCurrencyCode(currencyCode);
                    er.setDate(date);
                    er.setRate(rate);

                    savedRates.add(er);
                }
            }

            repository.saveAll(savedRates);
            logger.info("Saved {} exchange rates to the repository", savedRates.size());

        } catch (Exception e) {
            logger.error("Error parsing or saving XML exchange rates", e);
            throw new IOException("Error parsing and saving XML exchange rates: " + e.getMessage(), e);
        }

        return savedRates;
    }
}
