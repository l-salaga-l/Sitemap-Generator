package org.example.sitemapgenerator.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class SitemapGenerator {
    private final Set<UrlEntry> urlEntries = ConcurrentHashMap.newKeySet();

    public void addUrl(String url, Instant lastMod, double priority) {
        urlEntries.add(new UrlEntry(url, lastMod, priority));
    }

    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

            urlEntries.stream()
                    .sorted(Comparator.comparingDouble(UrlEntry::priority).reversed())
                    .forEach(entry -> writeUrlEntry(writer, entry));

            writer.println("</urlset>");
        }
    }

    private void writeUrlEntry(PrintWriter writer, UrlEntry entry) {
        writer.printf(
                """
                  <url>
                    <loc>%s</loc>
                    <lastmod>%s</lastmod>
                    <priority>%.1f</priority>
                  </url>
                """,
                escapeXml(entry.url),
                entry.lastMod.atZone(ZoneId.systemDefault()).format(ISO_DATE_TIME),
                entry.priority
        );
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private record UrlEntry(String url, Instant lastMod, double priority) { }
}