package com.app.BookStore.singleton;

import com.app.BookStore.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class BookCatalogueManager {

    private static final Logger logger = LoggerFactory.getLogger(BookCatalogueManager.class);

    private final List<Book> catalogue = new ArrayList<>();

    private final AtomicBoolean loaded = new AtomicBoolean(false);

    private BookCatalogueManager() {
        logger.info("[Singleton] BookCatalogueManager instance created.");
    }

    private static final class Holder {
        private static final BookCatalogueManager INSTANCE = new BookCatalogueManager();
    }


    public static BookCatalogueManager getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void loadCatalogue(List<Book> books) {
        catalogue.clear();
        if (books != null) {
            catalogue.addAll(books);
        }
        loaded.set(true);
        logger.info("[Singleton] Catalogue loaded with {} books.", catalogue.size());
    }

    public synchronized List<Book> getCatalogue() {
        return Collections.unmodifiableList(catalogue);
    }

    public synchronized List<Book> getMutableCatalogue() {
        return catalogue;
    }

    public boolean isLoaded() {
        return loaded.get();
    }

    public synchronized int size() {
        return catalogue.size();
    }
}
