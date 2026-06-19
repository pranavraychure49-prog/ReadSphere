package com.app.BookStore;

import com.app.BookStore.repository.readData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BookStoreApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(BookStoreApplication.class, args);

        readData reader = context.getBean(readData.class);
        reader.convertCsvToJson();
    }
}
