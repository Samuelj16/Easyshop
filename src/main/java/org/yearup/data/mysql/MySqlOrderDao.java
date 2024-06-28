package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    public final ProfileDao profileDao;
    public final ShoppingCartDao shoppingCartDao;
    public final UserDao userDao;

    public MySqlOrderDao(DataSource dataSource, ProfileDao profileDao, ShoppingCartDao shoppingCartDao, UserDao userDao) {
        super(dataSource);
        this.profileDao = profileDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    @Override
    public void checkout(int userId) throws Exception {

        Profile profile = profileDao.getByUserId(userId);

        ShoppingCart cart = shoppingCartDao.getByUserId(userId);
        Connection connection = null;
        try {
            connection = getConnection();
            //Since we are performing two insert queries, treat each as a single unit
            connection.setAutoCommit(false);

            // Create the order
            LocalDateTime now = LocalDateTime.now();
            String address = profile.getAddress();
            String city = profile.getCity();
            String state = profile.getState();
            String zip = profile.getZip();

            int orderId = createOrder(userId, now, address, city, state, zip);

            // Add order line items
            for (ShoppingCartItem item : cart.getItems().values()) {
                Product product = item.getProduct();
                int productId = product.getProductId();
                int quantity = item.getQuantity();
                BigDecimal unitPrice = product.getPrice();
                BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));
                addOrderItem(orderId, productId, totalPrice, quantity);
            }

            // Clear the shopping cart
            shoppingCartDao.deleteShoppingCart(userId);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error during checkout process", e);
        }finally {
           if (connection != null) {
               connection.setAutoCommit(true);
           }
        }
    }

    public int createOrder(int userId, LocalDateTime date, String address, String city, String state, String zip) throws SQLException {
        String sql =
        """
        INSERT INTO orders
        (user_id, date, address, city, state, zip, shipping_amount) 
        VALUES (?, ?, ?, ?, ?, ?, ?) 
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setObject(2, date);
            statement.setString(3, address);
            statement.setString(4, city);
            statement.setString(5, state);
            statement.setString(6, zip);
            statement.setBigDecimal(7, BigDecimal.ONE);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    private void addOrderItem(int orderId, int productId, BigDecimal salesPrice, int quantity) throws SQLException {
        String sql = """
        INSERT INTO order_line_items 
        (order_id, product_id, sales_price, quantity, discount)
         VALUES (?, ?, ?, ?, ?)
         """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setBigDecimal(3, salesPrice);
            statement.setInt(4, quantity);
            statement.setBigDecimal(5, BigDecimal.ONE);

            statement.executeUpdate();
        }
    }
}



