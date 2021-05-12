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

import labs.pm.data.Product;
import labs.pm.data.Drink;
import labs.pm.data.Food;
import labs.pm.data.Rating;
import java.math.BigDecimal;
import java.time.LocalDate;

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
        Product p1 = new Drink(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        Product p2 = new Drink(102, "Coffee", BigDecimal.valueOf(1.50), Rating.FOUR_STAR);
        
        Product p3 = new Food(103, "Cake", BigDecimal.valueOf(2.30), Rating.FIVE_STAR,
                LocalDate.now().plusDays(2));
        
        Product p4 = p3.applyRating(Rating.THREE_STAR);
        
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);
        
        System.out.println(p2.getBestBefore());
        System.out.println(p3.getBestBefore());
    }
}
