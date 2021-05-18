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
        
        pm.parseProduct("D, 101, Tea, 1.99, 0, 2021-05-18");
        pm.parseReview("101, 4, Nice hot cup of tea");
        pm.parseReview("101, 2, Rather weak Tea");
        pm.parseReview("101, 4, Fine tea");
        pm.parseReview("101, 4, Good tea");
        pm.parseReview("101, 5, Perfect tea");
        pm.parseReview("101, 3, just add some lemon");
        
        pm.parseProduct("D, 102, Coffee, 1.99, 0, 2021-05-18");
        pm.parseReview("102, 3, Coffee was ok");
        pm.parseReview("102, 1, Where is the milk?!?");
        pm.parseReview("102, 5, It's perfect with ten spoons of sugar");
        
        pm.parseProduct("F, 103, Cake, 3.99, 0, 2021-05-20");
        pm.parseReview("103, 5, Very nice cake");
        pm.parseReview("103, 4, It good, but I've expected more chocolate");
        pm.parseReview("103, 5, This cake is perfect");
        
        pm.printProductReport(101);
        pm.printProductReport(102);
        pm.printProductReport(103);
    }
}
