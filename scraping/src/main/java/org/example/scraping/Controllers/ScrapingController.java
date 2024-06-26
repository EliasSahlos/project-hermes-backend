package org.example.scraping.Controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.clients.Entities.Article;
import org.example.scraping.Service.ScrapingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping("api/scraping")
@RequiredArgsConstructor
public class ScrapingController {

    private final ScrapingService scrapingService;

    @GetMapping("/status")
    public ResponseEntity<Boolean> checkScrapingServiceStatus() {
        log.info("Fetched service status");
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    @PostMapping("/scrape")
    public void scrapeArticles() {
        List<Article> fetchedArticles = scrapingService.scrapeArticles();
        scrapingService.saveArticles(fetchedArticles);
        scrapingService.saveToElastic();
    }

    @PostMapping("/scrape/elastic")
    private ResponseEntity<String> saveToElastic() {
        return scrapingService.saveToElastic();
    }

//    @Scheduled(fixedDelay = 120000) // 2 minutes delay
//    public void scheduledScrapeAndSaveToElastic() {
//        List<Article> fetchedArticles = scrapingService.scrapeArticles();
//        scrapingService.saveArticles(fetchedArticles);
//        scrapingService.saveToElastic();
//    }
}