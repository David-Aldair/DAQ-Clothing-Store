package org.yearup.data.mysql;

import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    //constructor
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    //retrieves the shopping cart for a specific user
    @Override
    public ShoppingCart getByUserId(int userId) {

        ShoppingCart cart = new ShoppingCart();

        //join shopping_cart with products so we can return
        //complete product details instead of just product
        String sql = """
            SELECT
                sc.product_id,
                sc.quantity,
                p.*
            FROM
                shopping_cart sc
            JOIN
                products p ON p.product_id = sc.product_id
            WHERE
                sc.user_id = ?
            """;

        try (
                Connection connection = getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            //replace the ? with the user's id
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                //convert the product columns to an object
                Product product = mapProduct(resultSet);

                //reads how many of the product are in the cart
                int quantity = resultSet.getInt("quantity");

                ShoppingCartItem item = new ShoppingCartItem(product, userId, quantity);

                cart.add(item);
            }
        }
        catch (SQLException e){
            throw new RuntimeException("Error retrieving shopping cart", e);
        }

        //return the  completed shopping cart
        return cart;
    }

    /*if the product is not already in the cart, insert it with quantity = 1
    if the product is already in the cart, increase quantity by 1
    this logic is handled by SQL using on duplicate key update
    adds product to the user's shopping cart*/
    @Override
    public void addToCart(int productId, int userId)
    {
        String sql = """
            INSERT INTO (user_id, product_id, quantity)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE quantity = quantity + 1
            """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            //set the user id
            statement.setInt(1, userId);

            //set the product id
            statement.setInt(2, productId);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Error adding product to cart", e);
        }
    }

    //updates quantity of a product already in the cart
    //if the quantity  is > 0 it gets updated
    //if the quantity == 0 it gets removed from the cart
    @Override
    public void editCart(int productId, int userId, int quantity)
    {
        //update the quantity
        String updateSql = """
            UPDATE shopping_cart
            SET quantity = ?
            WHERE user_id = ?
              AND product_id = ?
            """;

        try (
                Connection connection = getConnection();
                PreparedStatement updateStmt =
                        connection.prepareStatement(updateSql)){

            updateStmt.setInt(1, quantity);
            updateStmt.setInt(2, userId);
            updateStmt.setInt(3, productId);

            updateStmt.executeUpdate();

            //if the quantity is == 0 we remove the row entirely
            if (quantity == 0)
            {
                String deleteSql = """
                    DELETE FROM
                        shopping_cart
                    WHERE
                        user_id = ? AND product_id = ?
                    """;

                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, userId);
                    deleteStmt.setInt(2, productId);
                    deleteStmt.executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating cart item", e);
        }
    }

    @Override
    public void clearCart(int userId) {

    }

    protected static Product mapProduct(ResultSet row) throws SQLException {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean featured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, subCategory, stock, featured, imageUrl);
    }

}
