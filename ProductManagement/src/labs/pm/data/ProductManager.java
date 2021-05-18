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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

/**
 * Factory methods for creating Drink and Food objects.
 * 
 * @author apellet
 */
public class ProductManager {
    private static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                   "en-US", new ResourceFormatter(Locale.US),
                   "fr-FR", new ResourceFormatter(Locale.FRANCE));
    
    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }
    
    private Map<Product, List<Review>> products = new HashMap<>();
    
    private ResourceFormatter formatter;
    
    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        changeLocale(languageTag);
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
        return reviewProduct(findProduct(id), rating, comments);
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
    
    public void printProductReport(int id) {
        printProductReport(findProduct(id));
    }
    
    public void printProductReport(Product product) {
        StringBuilder txt = new StringBuilder();
        
        txt.append(formatter.formatProduct(product));
        txt.append('\n');
        
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        
        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no.review"));
            txt.append('\n');
        } else {
            txt.append(reviews.stream()
                              .map(r -> formatter.formatReview(r) + '\n')
                              .collect(Collectors.joining()));
        }
        
        System.out.println(txt);
    }
    
    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
        StringBuilder txt = new StringBuilder();
        
        products.keySet()
                .stream()
                .sorted(sorter)
                .filter(filter)
                .forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
        
        System.out.println(txt);
    }
    
    public Product findProduct(int id) {
        return products.keySet()
                       .stream()
                       .filter(p -> p.getId() == id)
                       .findFirst()
                       .orElseGet(() -> null);
    }
    
    public void changeLocale(String languageTag) {
        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
    }
    
    public Map<String, String> getDiscount() {
        return products.keySet().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getRating().getStars(),
                        Collectors.collectingAndThen(
                                Collectors.summarizingDouble(p -> p.getDiscount().doubleValue()), 
                                d -> formatter.moneyFormat.format(d))));
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
