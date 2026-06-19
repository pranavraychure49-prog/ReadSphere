# 📚 BookStore Application

A Spring Boot application that reads a books catalog from a local CSV file, transforms the data into Java objects, and saves the output as a formatted JSON file. It also provides in-memory operations like searching books, adding new books, and computing library statistics.

---

## 🗂️ Project Info

| Field         | Value                        |
|---------------|------------------------------|
| Project Name  | BookStore                    |
| Group ID      | com.app                      |
| Artifact ID   | BookStore                    |
| Version       | 0.0.1-SNAPSHOT               |
| Java Version  | 21                           |
| Spring Boot   | 4.1.0                        |
| Build Tool    | Maven                        |

---

## 📁 Project Structure

```
BookStore/
├── pom.xml
├── report.txt
├── README.md
└── src/
    ├── main/
    │   ├── java/com/app/BookStore/
    │   │   ├── BookStoreApplication.java       ← Entry point
    │   │   ├── model/
    │   │   │   ├── Book.java                   ← Book data model
    │   │   │   └── LibraryStatistics.java      ← Statistics model
    │   │   └── repository/
    │   │       └── readData.java               ← CSV reader + JSON writer
    │   └── resources/
    │       ├── application.properties
    │       └── data/
    │           ├── books_catalog.csv           ← Input file
    │           └── books_catalog.json          ← Output file (auto-generated)
    └── test/
        └── java/com/app/BookStore/
            └── BookStoreApplicationTests.java
```

---

## ⚙️ Dependencies

| Dependency                      | Version    | Purpose                          |
|---------------------------------|------------|----------------------------------|
| spring-boot-starter-webmvc      | 4.1.0      | Spring MVC web framework         |
| spring-boot-starter-webmvc-test | 4.1.0      | Testing support                  |
| lombok                          | managed    | Reduces boilerplate code         |
| opencsv                         | 5.9        | Parses CSV into Java beans       |
| jackson-databind                | 2.18.2     | Converts Java objects to JSON    |

---

## 🧩 Source Files Explained

### `BookStoreApplication.java`
The main entry point of the application. It starts the Spring context and immediately calls `convertCsvToJson()` to trigger the CSV → JSON conversion on startup.

```java
ApplicationContext context = SpringApplication.run(BookStoreApplication.class, args);
readData reader = context.getBean(readData.class);
reader.convertCsvToJson();
```

---

### `model/Book.java`
Represents a single book record. Each field maps to a column in `books_catalog.csv` using OpenCSV's `@CsvBindByName`. Lombok's `@Data` generates all getters, setters, and utility methods automatically.

| Field          | Type     | CSV Column      |
|----------------|----------|-----------------|
| id             | int      | id              |
| bookName       | String   | bookName        |
| authorName     | String   | authorName      |
| category       | String   | category        |
| publisher      | String   | publisher       |
| price          | double   | price           |
| quantity       | int      | quantity        |
| publishedYear  | int      | publishedYear   |
| isbn           | String   | isbn            |
| language       | String   | language        |

---

### `model/LibraryStatistics.java`
Holds computed statistics about the entire book catalog. Populated by `getLibraryStatistics()` in `readData.java`.

| Field                  | Type                  | Description                        |
|------------------------|-----------------------|------------------------------------|
| totalBooks             | Long                  | Total number of book titles        |
| totalInventory         | Long                  | Sum of all stock quantities        |
| totalInventoryValue    | Double                | Sum of (price × quantity)          |
| averageBookPrice       | Double                | Average price across all books     |
| highestPricedBook      | String                | Name of the most expensive book    |
| lowestPricedBook       | String                | Name of the cheapest book          |
| categoryWiseBooks      | Map\<String, Long\>   | Book count grouped by category     |
| authorWiseBooks        | Map\<String, Long\>   | Book count grouped by author       |
| averagePriceByCategory | Map\<String, Double\> | Average price per category         |
| publisherWiseBooks     | Map\<String, Long\>   | Book count grouped by publisher    |

---

### `repository/readData.java`
The core service class. Annotated with `@Component` so Spring manages it as a bean. Holds the in-memory `List<Book>` after loading.

#### Methods

**`convertCsvToJson()`**
Reads the CSV file using `CsvToBeanBuilder` and maps each row to a `Book` object. Then writes the full list as a pretty-printed JSON file using Jackson's `ObjectMapper`.

```
Input  → src/main/java/com/app/BookStore/data/books_catalog.csv
Output → src/main/java/com/app/BookStore/data/books_catalog.json
```

---

**`getBookByName(String name)`**
Searches the in-memory book list for a book whose name matches the given string (case-insensitive). Returns the `Book` if found, or `null` if not.

```java
Book book = reader.getBookByName("Clean Code");
```

---

**`addBooks(Book newBook)`**
Adds a new `Book` object to the in-memory list. Returns the added book on success, or `null` if the list hasn't been loaded yet.

```java
Book newBook = new Book();
newBook.setBookName("New Book");
reader.addBooks(newBook);
```

---

**`getLibraryStatistics()`**
Uses Java Streams to compute and return a `LibraryStatistics` object containing:
- Average book price
- Total book titles
- Total inventory (sum of quantities)
- Total inventory value (price × quantity)
- Name of highest-priced book
- Name of lowest-priced book

```java
LibraryStatistics stats = reader.getLibraryStatistics();
```

---

## 📊 Data Flow

```
[books_catalog.csv]
        |
        ▼
CsvToBeanBuilder (opencsv)
        |
        ▼
List<Book>  ──── in-memory ────┬──► getBookByName()        → Book
                               ├──► addBooks()             → Book
                               └──► getLibraryStatistics() → LibraryStatistics
        |
        ▼
ObjectMapper (jackson)
        |
        ▼
[books_catalog.json]
```

---

## 📄 CSV Data - books_catalog.csv

The input file contains **20 books** across multiple categories.

| Category       | Books                                                        |
|----------------|--------------------------------------------------------------|
| Programming    | Clean Code, Effective Java, Spring in Action, and more       |
| Architecture   | Microservices Patterns, Domain Driven Design, and more       |
| DevOps         | Kubernetes Up and Running, Docker Deep Dive, The Phoenix Project |
| Algorithms     | Grokking Algorithms, Introduction to Algorithms              |
| Software Design| Design Patterns, Refactoring                                 |
| System Design  | System Design Interview                                      |
| Interview Prep | Cracking the Coding Interview                                |
| Spring         | Spring Start Here                                            |
| JavaScript     | You Dont Know JS                                             |

**Price range:** ₹550 (Head First Java) — ₹1500 (Introduction to Algorithms)

---

## 📦 Output - books_catalog.json

The generated JSON file is saved locally at the same location as the CSV. Format example:

```json
[
  {
    "id" : 1,
    "bookName" : "Clean Code",
    "authorName" : "Robert C. Martin",
    "category" : "Programming",
    "publisher" : "Prentice Hall",
    "price" : 599.0,
    "quantity" : 20,
    "publishedYear" : 2008,
    "isbn" : "9780132350884",
    "language" : "English"
  },
  ...
]
```

---

## 🚀 How to Run

**Prerequisites**
- Java 21
- Maven (or use the bundled `mvnw` wrapper)

**Steps**

```bash
# 1. Clone or navigate to the project root
cd "Java Backend P99 Soft Training/BookStore/BookStore"

# 2. Build the project
mvnw clean install

# 3. Run the application
mvnw spring-boot:run
```

**Expected console output:**
```
Done! JSON saved to: src/main/java/com/app/BookStore/data/books_catalog.json
Total books converted: 20
```

---

## ⚠️ Known Limitations

- **Hardcoded file paths** — CSV and JSON paths are defined directly in `readData.java`. They should be moved to `application.properties`.
- **No REST API** — There are no HTTP endpoints yet. All logic runs at startup only.
- **No persistence** — `addBooks()` only adds to the in-memory list. It does not write back to the CSV or JSON file.
- **Incomplete statistics** — The Map-based fields in `LibraryStatistics` (category-wise, author-wise, etc.) are declared but not yet computed in `getLibraryStatistics()`.
- **No validation** — There is no input validation on the `Book` model fields.
- **No error handling** — Missing or malformed CSV files will cause an unhandled exception.

---

## 🔮 Future Improvements

- Move file paths to `application.properties`
- Add a `@RestController` with endpoints:
  - `GET /books` — list all books
  - `GET /books/{name}` — search by name
  - `POST /books` — add a new book
  - `GET /books/stats` — get library statistics
- Persist `addBooks()` changes back to JSON
- Complete the Map-based statistics computation
- Add Bean Validation (`@NotNull`, `@Min`, etc.)
- Add proper exception handling with `@ControllerAdvice`

---

## 👨‍💻 Author

Developed as part of **Java Backend P99 Soft Training**
