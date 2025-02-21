package org.example.sitemapgenerator.service;

import java.util.concurrent.ForkJoinPool;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import org.example.sitemapgenerator.entity.CrawlStatistics;

public class CrawlerService extends Service<Void> {
    private final String startUrl;
    private final int maxDepth;
    private final SitemapGenerator sitemapGenerator;

    @Getter
    private final CrawlStatistics statistics = new CrawlStatistics();

    public CrawlerService(String startUrl, int maxDepth, SitemapGenerator sitemapGenerator) {
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.sitemapGenerator = sitemapGenerator;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                ForkJoinPool pool = new ForkJoinPool();
                pool.invoke(new WebsiteCrawler(startUrl, maxDepth, statistics, sitemapGenerator));
                return null;
            }
        };
    }
}
