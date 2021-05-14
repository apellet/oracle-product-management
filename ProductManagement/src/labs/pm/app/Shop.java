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
package labs.pm.app;

import labs.pm.data.ProductManager;
import labs.pm.data.Product;
import labs.pm.data.Rating;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

/**
 * {@code Shop} class represents an application that manages Products.
 *
 * @author apellet
 * @version 4.0
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProductManager pm = new ProductManager(Locale.CANADA);
        
        Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        pm.reviewProduct(101, Rating.FOUR_STAR, "Nice hot cup of tea");
        pm.reviewProduct(101, Rating.TWO_STAR, "Rather weak Tea");
        pm.reviewProduct(101, Rating.FOUR_STAR, "Fine tea");
        pm.reviewProduct(101, Rating.FOUR_STAR, "Good tea");
        pm.reviewProduct(101, Rating.FIVE_STAR, "Perfect tea");
        pm.reviewProduct(101, Rating.THREE_STAR, "just add some lemon");
        pm.printProductReport(101);
        
        Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        pm.reviewProduct(102, Rating.THREE_STAR, "Coffee was ok");
        pm.reviewProduct(102, Rating.ONE_STAR, "Where is the milk?!?");
        pm.reviewProduct(102, Rating.FIVE_STAR, "It's perfect with ten spoons of sugar");
        pm.printProductReport(102);
        
        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT_RATED, LocalDate.now().plusDays(2));
        pm.reviewProduct(103, Rating.FIVE_STAR, "Very nice cake");
        pm.reviewProduct(103, Rating.FOUR_STAR, "It good, but I've expected more chocolate");
        pm.reviewProduct(103, Rating.FIVE_STAR, "This cake is perfect");
        pm.printProductReport(103);
    }
}
