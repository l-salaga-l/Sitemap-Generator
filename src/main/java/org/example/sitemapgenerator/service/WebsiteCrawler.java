package org.example.sitemapgenerator.service;

import javafx.application.Platform;
import org.example.sitemapgenerator.entity.CrawlStatistics;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class WebsiteCrawler extends RecursiveAction {
    private static final int TIMEOUT_MS = 10_000;
    private static final double BASE_PRIORITY = 1.0;
    private static final double PRIORITY_DECAY = 0.15;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Pattern EXCLUDED_EXTENSIONS = Pattern.compile(".*\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz)$");
    private static final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    private final String currentUrl;
    private final int currentDepth;
    private final int maxDepth;
    private final CrawlStatistics statistics;
    private final SitemapGenerator sitemap;
    private final Set<String> visitedUrls;

    WebsiteCrawler(String url, int maxDepth, CrawlStatistics stats, SitemapGenerator sitemap) {
        this(url, 0, maxDepth, stats, sitemap, ConcurrentHashMap.newKeySet());
        markUrlAsVisited(url);
    }

    private WebsiteCrawler(String url, int currentDepth, int maxDepth,
                           CrawlStatistics stats, SitemapGenerator sitemap,
                           Set<String> visitedUrls) {
        this.currentUrl = url;
        this.currentDepth = currentDepth;
        this.maxDepth = maxDepth;
        this.statistics = stats;
        this.sitemap = sitemap;
        this.visitedUrls = visitedUrls;
        statistics.incrementQueueUrls();
    }

    @Override
    protected void compute() {
        if (isCancelled() || currentDepth > maxDepth) return;

        try {
            Document document = processCurrentUrl();
            createChildTasks(document).forEach(ForkJoinTask::invokeAll);
            statistics.incrementProcessedUrls();
        } catch (Exception e) {
            statistics.incrementError();
        } finally {
            updateProcessingStatistics();
        }
    }

    private Document processCurrentUrl() throws IOException {
        Connection.Response response = Jsoup.connect(currentUrl)
                .timeout(TIMEOUT_MS)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .execute();

        Document document = response.parse();
        Instant lastMod = parseLastModified(response);
        double priority = calculatePriority();

        sitemap.addUrl(currentUrl, lastMod, priority);
        return document;
    }

    private List<WebsiteCrawler> createChildTasks(Document document) {
        List<WebsiteCrawler> tasks = new ArrayList<>();
        Elements links = document.select("a[href]");

        links.stream()
                .map(link -> link.absUrl("href"))
                .filter(this::isValidUrl)
                .forEach(childUrl -> createTaskIfNew(childUrl, tasks));

        return tasks;
    }

    private boolean isValidUrl(String url) {
        return url.startsWith(currentUrl) &&
                !url.contains("#") &&
                !url.equals(currentUrl) &&
                !EXCLUDED_EXTENSIONS.matcher(url).matches();
    }

    private void createTaskIfNew(String url, List<WebsiteCrawler> tasks) {
        synchronized(visitedUrls) {
            if (visitedUrls.add(url)) {
                statistics.incrementDiscoveredUrls();
                tasks.add(new WebsiteCrawler(
                        url,
                        currentDepth + 1,
                        maxDepth,
                        statistics,
                        sitemap,
                        visitedUrls
                ));
            }
        }
    }

    private Instant parseLastModified(Connection.Response response) {
        try {
            String lastModHeader = response.header("Last-Modified");
            return lastModHeader != null ?
                    Instant.ofEpochMilli(format.parse(lastModHeader).getTime()) :
                    Instant.now();
        } catch (IllegalArgumentException | ParseException e) {
            return Instant.now();
        }
    }

    private double calculatePriority() {
        return Math.max(0.1, BASE_PRIORITY - (currentDepth * PRIORITY_DECAY));
    }

    private void updateProcessingStatistics() {
        Platform.runLater(statistics::decrementQueueUrls);
    }

    private void markUrlAsVisited(String url) {
        visitedUrls.add(url);
        statistics.incrementDiscoveredUrls();
    }
}