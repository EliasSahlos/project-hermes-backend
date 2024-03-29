package org.example.article.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.amqp.RabbitMQMessageProducer;
import org.example.article.Repositories.ArticleRepository;
import org.example.clients.article.Entities.Article;
import org.example.clients.article.dto.ArticlesResponse;
import org.example.clients.article.dto.ViewsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void getAllArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            articles = articleRepository.findAll();
            log.info("Fetched all articles");
            rabbitMQMessageProducer.publish(articles,"internal.exchange","internal.article.routing-key");
        } catch (Exception e) {
            log.error("Error occurred while getting articles", e);
        }

    }

    public Article getArticleByUuid(String uuid) {
        Article article = new Article();
        try {
            article = articleRepository.findByUuid(uuid);
            log.info("Fetched article by uuid {} ", uuid);
            rabbitMQMessageProducer.publish(article, "internal.exchange", "internal.article.routing-key");
        } catch (Exception e) {
            log.error("Error occurred while getting article", e);
        }
        return article;
    }

    public ArticlesResponse getArticlesByFilters(List<String> categories) {
        List<Article> articles = new ArrayList<>();
        try {
            articles = articleRepository.findByCategoryIn(categories);
            log.info("Fetching filters");
        } catch (Exception e) {
            log.error("Error occurred while getting articles");
        }
        return ArticlesResponse.builder()
                .articles(articles)
                .build();
    }

    public ViewsResponse updateArticleViewCount(String uuid) {
        ViewsResponse viewsResponse = new ViewsResponse();
        try {
            // Retrieve the article by UUID
            Article article = articleRepository.findByUuid(uuid);
            if (article != null) {
                // Increment the view count
                int updatedViews = article.getViews() + 1;
                article.setViews(updatedViews);
                articleRepository.save(article);

                // Prepare the response
                viewsResponse.setMessage("Article view count updated successfully");
                viewsResponse.setArticleViews(updatedViews);
            } else {
                // If article not found, set appropriate message in response
                viewsResponse.setMessage("Article not found with UUID: " + uuid);
                viewsResponse.setArticleViews(null);
            }
        } catch (Exception e) {
            log.error("Error occurred while updating views count", e);
            viewsResponse.setMessage("Failed to update views count");
            viewsResponse.setArticleViews(null);
        }
        return viewsResponse;
    }
}
