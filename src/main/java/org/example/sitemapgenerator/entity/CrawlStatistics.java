package org.example.sitemapgenerator.entity;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.function.IntUnaryOperator;
import lombok.Getter;

@Getter
public class CrawlStatistics {
    private final IntegerProperty discoveredUrls = new SimpleIntegerProperty(0);
    private final IntegerProperty processedUrls = new SimpleIntegerProperty(0);
    private final IntegerProperty queuedUrls = new SimpleIntegerProperty(0);
    private final IntegerProperty errorCount = new SimpleIntegerProperty(0);

    public void incrementDiscoveredUrls() {
        updateProperty(discoveredUrls, n -> n + 1);
    }

    public void incrementProcessedUrls() {
        updateProperty(processedUrls, n -> n + 1);
    }

    public void incrementError() {
        updateProperty(errorCount, n -> n + 1);
    }

    public void incrementQueueUrls() {
        updateProperty(queuedUrls, n -> n + 1);
    }

    public void decrementQueueUrls() {
        updateProperty(queuedUrls, n -> n - 1);
    }

    private void updateProperty(IntegerProperty property, IntUnaryOperator operator) {
        Platform.runLater(() ->
                property.set(operator.applyAsInt(property.get()))
        );
    }
}
