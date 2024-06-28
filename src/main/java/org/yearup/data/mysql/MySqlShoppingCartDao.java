package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    public ShoppingCart addToCart(int productId, int userId) throws SQLException {
        String selectSQL = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSQL = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateSQL = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection()) {
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                selectStmt.setInt(1, userId);
                selectStmt.setInt(2, productId);

                ResultSet resultSet = selectStmt.executeQuery();

                if (resultSet.next()) {
                    // Product is already in the cart, update quantity
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSQL)) {
                        updateStmt.setInt(1, userId);
                        updateStmt.setInt(2, productId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Product is not in the cart, insert new item
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, productId);
                        insertStmt.setInt(3, 1); // Starting quantity
                        insertStmt.executeUpdate();
                    }
                }
            }
            // Retrieve and return the updated shopping cart
            return getByUserId(userId);
        }
    }

    @Override
    public ShoppingCart getByUserId(int userId) throws SQLException {
        ShoppingCart cart = new ShoppingCart();

        String sql =
                """
                SELECT sc.product_id, sc.quantity, p.price, p.name,
                p.description,p.image_url,p.stock,p.color, p.featured,p.category_id
                FROM shopping_cart sc\s
                JOIN products p ON sc.product_id = p.product_id\s
                WHERE sc.user_id = ?
               \s""";
        Map<Integer, ShoppingCartItem> items = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("product_id");
                    int quantity = resultSet.getInt("quantity");
                    int categoryId = resultSet.getInt("category_id");
                    BigDecimal price = resultSet.getBigDecimal("price");
                    String name = resultSet.getString("name");
                    String imageUrl = resultSet.getString("image_url");
                    boolean featured = resultSet.getBoolean("featured");
                    String description = resultSet.getString("description");
                    int stock = resultSet.getInt("stock");
                    String color = resultSet.getString("color");

                   Product product = new Product(
                           productId,
                           name,
                           price,
                           categoryId,
                           description,
                           color,
                           stock,
                           featured,
                           imageUrl
                   );
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);

                    items.put(productId, item);
                    cart.setItems(items);
                }
            }
        }

        return cart;
    }

    @Override
    public ShoppingCart updateShoppingCart(int userId, int productId, int quantity) throws SQLException {
        String selectSQL = "SELECT * FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSQL = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateSQL = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection()) {
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                selectStmt.setInt(1, userId);
                selectStmt.setInt(2, productId);

                ResultSet resultSet = selectStmt.executeQuery();

                if (resultSet.next()) {
                    // Product is already in the cart, update quantity
                    int currentQuantity = resultSet.getInt("quantity");
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSQL)) {
                        updateStmt.setInt(1, currentQuantity + quantity);
                        updateStmt.setInt(2, userId);
                        updateStmt.setInt(3, productId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Product is not in the cart, insert new item
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, productId);
                        insertStmt.setInt(3, quantity);
                        insertStmt.executeUpdate();
                    }
                }
            }
            // Retrieve and return the updated shopping cart
            return getByUserId(userId);
        }

    }

    @Override
    public void deleteShoppingCart(int userId) throws SQLException {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
