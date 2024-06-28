package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.sql.SQLException;
import java.util.Map;

public interface ShoppingCartDao
{
    ShoppingCart addToCart(int productId, int userId) throws SQLException;

    ShoppingCart getByUserId(int userId) throws SQLException;
    // add additional method signatures here
    ShoppingCart updateShoppingCart(int userId,int productId, int quantity) throws SQLException;

    void deleteShoppingCart(int userId) throws SQLException;

}
