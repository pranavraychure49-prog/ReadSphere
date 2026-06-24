package com.app.BookStore.config;

import com.app.BookStore.singleton.BookCatalogueManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public BookCatalogueManager bookCatalogueManager() {
        return BookCatalogueManager.getInstance();
    }
}
