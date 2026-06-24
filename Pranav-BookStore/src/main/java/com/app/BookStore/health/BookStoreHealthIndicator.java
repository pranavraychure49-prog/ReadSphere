package com.app.BookStore.health;

import com.app.BookStore.singleton.BookCatalogueManager;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("bookStoreCatalogue")
public class BookStoreHealthIndicator implements HealthIndicator {

    private final BookCatalogueManager catalogueManager;

    public BookStoreHealthIndicator(BookCatalogueManager catalogueManager) {
        this.catalogueManager = catalogueManager;
    }

    @Override
    public Health health() {
        if (!catalogueManager.isLoaded()) {
            return Health.down()
                    .withDetail("status", "Catalogue not loaded yet")
                    .withDetail("totalBooks", 0)
                    .build();
        }

        int totalBooks = catalogueManager.size();

        if (totalBooks == 0) {
            return Health.outOfService()
                    .withDetail("status", "Catalogue is empty – no books available")
                    .withDetail("totalBooks", 0)
                    .build();
        }

        return Health.up()
                .withDetail("status", "Catalogue is loaded and ready")
                .withDetail("totalBooks", totalBooks)
                .build();
    }
}
