module org.example.sitemapgenerator {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.xml;
    requires org.jsoup;
    requires static lombok;

    opens org.example.sitemapgenerator.controller to javafx.fxml;
    opens org.example.sitemapgenerator to javafx.fxml;
    exports org.example.sitemapgenerator;
}