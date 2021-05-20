/*
 * Copyright (C) 2021 apellet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package labs.pm.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ProductManager {
    private static final ProductManager pm = new ProductManager();
    
    private static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                   "en-US", new ResourceFormatter(Locale.US),
                   "fr-FR", new ResourceFormatter(Locale.FRANCE),
                   "en-CA", new ResourceFormatter(Locale.CANADA));
    
    private static final Logger logger =
            Logger.getLogger(ProductManager.class.getName());
    
    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }
    
    public static ProductManager getInstance() {
        return pm;
    }
    
    private Map<Product, List<Review>> products = new HashMap<>();
    
    private ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    
    private MessageFormat productFormat =
            new MessageFormat(config.getString("product.data.format"));
    
    private MessageFormat reviewFormat =
            new MessageFormat(config.getString("review.data.format"));
    
    private Path reportsFolder = Path.of(config.getString("reports.folder"));
    private Path dataFolder = Path.of(config.getString("data.folder"));
    private Path tempFolder = Path.of(config.getString("temp.folder"));
    
    private ProductManager() {
        loadAllData();
    }
    
    public Product createProduct(int id, String name, BigDecimal price,
                                 Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        
        return product;
    }
    
    public Product createProduct(int id, String name, BigDecimal price,
                                 Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        
        return product;
    }
    
    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        
        return null;
    }
    
    public Product reviewProduct(Product product, Rating rating, String comments) {
        List<Review> reviews = products.get(product);
        products.remove(product, reviews);
        reviews.add(new Review(rating, comments));
        
        product = product.applyRating(Rateable.convert((int) Math.round(
                reviews.stream()
                       .mapToInt(r -> r.getRating().ordinal())
                       .average()
                       .orElse(0))));

        products.put(product, reviews);
        
        return product;
    }
    
    public void printProductReport(int id, String languageTag) {
        try {
            printProductReport(findProduct(id), languageTag);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error printing product report " + e.getMessage(), e);
        }
    }
    
    public void printProductReport(Product product, String languageTag) throws IOException {
        ResourceFormatter formatter =
                formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        
        Path productFile = reportsFolder.resolve(
                MessageFormat.format(config.getString("report.file"),
                                     product.getId()));
        
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(productFile,
                                      StandardOpenOption.CREATE),
                "UTF-8"))) {
            out.append(formatter.formatProduct(product));
            out.append('\n');
            
            List<Review> reviews = products.get(product);
            Collections.sort(reviews);
            
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.review"));
                out.append('\n');
            } else {
                out.append(reviews.stream()
                                  .map(r -> formatter.formatReview(r) + '\n')
                                  .collect(Collectors.joining()));
            }
        }
    }
    
    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter,
                              String languageTag) {
        ResourceFormatter formatter =
                formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        
        StringBuilder txt = new StringBuilder();
        
        products.keySet()
                .stream()
                .sorted(sorter)
                .filter(filter)
                .forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
        
        System.out.println(txt);
    }
    
    private Product parseProduct(String text) {
        Product product = null;
        
        try {
            Object[] values = productFormat.parse(text);
            
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            
            switch ((String) values[0]) {
                case "D":
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
                    break;
            }
        } catch (ParseException |
                 NumberFormatException |
                 DateTimeParseException e) {
            logger.log(Level.WARNING, "Error parsing product " + text + " " + e.getMessage());
        }
        
        return product;
    }
    
    private Review parseReview(String text) {
        Review review = null;
        
        try {
            Object[] values = reviewFormat.parse(text);
            
            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])),
                                (String) values[1]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing review " + text);
        }
        
        return review;
    }
    
    public Product findProduct(int id) throws ProductManagerException {
        return products.keySet()
                       .stream()
                       .filter(p -> p.getId() == id)
                       .findFirst()
                       .orElseThrow(() -> 
                               new ProductManagerException("Product with id " + id + " not found"));
    }
        
    public Map<String, String> getDiscount(String languageTag) {
        ResourceFormatter formatter =
                formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        
        return products.keySet().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getRating().getStars(),
                        Collectors.collectingAndThen(
                                Collectors.summarizingDouble(p -> p.getDiscount().doubleValue()), 
                                d -> formatter.moneyFormat.format(d))));
    }
    
    private void dumpData() {
        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectories(tempFolder);
            }
            
            Path tempFile = tempFolder.resolve(MessageFormat.format(config.getString("temp.file"), Instant.now()));
            
            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
                out.writeObject(products);
                products = new HashMap<>();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error dumping data " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void restoreData() {
        try {
            Path tempFile = Files.list(tempFolder)
                                 .filter(path -> path.getFileName().toString().endsWith("tmp"))
                                 .findFirst()
                                 .orElseThrow();
            
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {
                products = (HashMap) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error restoring data " + e.getMessage(), e);
        }
    }
    
    private void loadAllData() {
        try {
            products = Files.list(dataFolder)
                            .filter(file -> file.getFileName().toString().startsWith("product"))
                            .map(file -> loadProduct(file))
                            .filter(product -> product != null)
                            .collect(Collectors.toMap(product -> product,
                                                      product -> loadReviews(product)));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading data " + e.getMessage());
        }
    }
    
    private Product loadProduct(Path file) {
        Product product = null;
        
        try {
            product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8"))
                                        .findFirst()
                                        .orElseThrow());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading products " + e.getMessage());
        }
        
        return product;
    }
    
    private List<Review> loadReviews(Product product) {
        List<Review> reviews = null;
        
        Path file = dataFolder.resolve(MessageFormat.format(
                config.getString("reviews.data.file"), 
                product.getId()));
        
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                               .map(text -> parseReview(text))
                               .filter(review -> review != null)
                               .collect(Collectors.toList());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error loading reviews " + e.getMessage());
            }
        }
        
        return reviews;
    }
    
    private static class ResourceFormatter {
        private final Locale locale;
        private final ResourceBundle resources;
        private final DateTimeFormatter dateFormat;
        private final NumberFormat moneyFormat;
        
        private ResourceFormatter(Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                          .localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }
        
        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                                        product.getName(),
                                        moneyFormat.format(product.getPrice()),
                                        product.getRating().getStars(),
                                        dateFormat.format(product.getBestBefore()));
        }
        
        private String formatReview(Review review) {
            return MessageFormat.format(resources.getString("review"),
                                        review.getRating().getStars(),
                                        review.getComments());
        }
        
        private String getText(String key) {
            return resources.getString(key);
        }
    }
}
