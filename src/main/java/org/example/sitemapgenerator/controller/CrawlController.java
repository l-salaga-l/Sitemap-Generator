package org.example.sitemapgenerator.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.example.sitemapgenerator.entity.CrawlStatistics;
import org.example.sitemapgenerator.service.CrawlerService;
import org.example.sitemapgenerator.service.SitemapGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;

public class CrawlController {
    private static final int INITIAL_DEPTH = 2;
    private static final int MIN_DEPTH = 1;
    private static final int MAX_DEPTH = 5;
    private static final String DEFAULT_SITEMAP_FILENAME = "sitemap.xml";

    @FXML private TextField urlInputField;
    @FXML private TextField directoryPathField;
    @FXML private Spinner<Integer> depthSpinner;
    @FXML private Label discoveredLabel;
    @FXML private Label processedLabel;
    @FXML private Label queuedUrlsLabel;
    @FXML private Label errorCountLabel;
    @FXML private Button startButton;
    @FXML private Button stopButton;

    private CrawlerService crawlerService;
    private SitemapGenerator sitemapGenerator;
    private Path selectedDirectory = Paths.get("");

    @FXML
    public void initialize() {
        configureDepthSpinner();
        stopButton.setDisable(true);
        directoryPathField.setDisable(true);
    }

    private void configureDepthSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_DEPTH, MAX_DEPTH, INITIAL_DEPTH);
        depthSpinner.setValueFactory(factory);
    }

    @FXML
    private void handleStartCrawling() {
        initializeCrawlerComponents();
        setupServiceBindings();
        startCrawlerService();
        toggleButtonsState(true);
    }

    @FXML
    private void handleStopCrawling() {
        if (crawlerService != null) {
            crawlerService.cancel();
        }
    }

    @FXML
    private void handleChooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку для сохранения");
        File selectedDir = directoryChooser.showDialog(directoryPathField.getScene().getWindow());

        if (selectedDir != null) {
            selectedDirectory = selectedDir.toPath();
            directoryPathField.setText(selectedDirectory.toString());
        }
    }

    private void initializeCrawlerComponents() {
        String initialUrl = urlInputField.getText().trim();
        int targetDepth = depthSpinner.getValue();
        sitemapGenerator = new SitemapGenerator();
        crawlerService = new CrawlerService(initialUrl, targetDepth, sitemapGenerator);
    }

    private void setupServiceBindings() {
        CrawlStatistics statistics = crawlerService.getStatistics();
        bindLabelsToStatistics(statistics);

        crawlerService.setOnSucceeded(event -> onCrawlingFinished());
        crawlerService.setOnCancelled(event -> onCrawlingFinished());
    }

    private void bindLabelsToStatistics(CrawlStatistics statistics) {
        discoveredLabel.textProperty().bind(statistics.getDiscoveredUrls().asString());
        processedLabel.textProperty().bind(statistics.getProcessedUrls().asString());
        queuedUrlsLabel.textProperty().bind(statistics.getQueuedUrls().asString());
        errorCountLabel.textProperty().bind(statistics.getErrorCount().asString());
    }

    private void startCrawlerService() {
        crawlerService.start();
    }

    private void onCrawlingFinished() {
        saveGeneratedSitemap();
        resetUIState();
    }

    private void saveGeneratedSitemap() {
        try {
            Path outputPath = selectedDirectory.resolve(DEFAULT_SITEMAP_FILENAME);
            sitemapGenerator.saveToFile(outputPath.toString());

            String message = String.format("Sitemap сохранен в:\n%s", outputPath);
            showAlert(INFORMATION, message);
        } catch (IOException e) {
            showAlert(ERROR, "Ошибка сохранения: " + e.getMessage());
        }
    }

    private void resetUIState() {
        toggleButtonsState(false);
        clearServiceReferences();
    }

    private void toggleButtonsState(boolean isRunning) {
        startButton.setDisable(isRunning);
        stopButton.setDisable(!isRunning);
    }

    private void clearServiceReferences() {
        crawlerService = null;
        sitemapGenerator = null;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}