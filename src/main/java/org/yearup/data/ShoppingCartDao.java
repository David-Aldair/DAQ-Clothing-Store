package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here

    // Adds a product or increases quantity by 1
    void addToCart(int productId, int userId);

    // Updates quantity of an existing cart item
    void editCart(int productId, int userId, int quantity);

    // Clears the user's shopping cart
    void clearCart(int userId);
}
