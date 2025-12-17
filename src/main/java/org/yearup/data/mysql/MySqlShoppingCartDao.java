package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        String sql = """
            SELECT shopping_cart.product_id,
                   shopping_cart.quantity,
                   products.product_id,
                   products.name,
                   products.price,
                   products.category_id,
                   products.description,
                   products.subcategory,
                   products.stock,
                   products.image_url,
                   products.featured
            FROM shopping_cart
            JOIN products ON shopping_cart.product_id = products.product_id
            WHERE shopping_cart.user_id = ?
        """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
            {
                ShoppingCartItem item = new ShoppingCartItem();
                Product product = new Product();
                product.setProductId(resultSet.getInt("product_id"));
                product.setName(resultSet.getString("name"));
                product.setPrice(resultSet.getBigDecimal("price"));
                product.setCategoryId(resultSet.getInt("category_id"));
                product.setDescription(resultSet.getString("description"));
                product.setSubCategory(resultSet.getString("subcategory"));
                product.setStock(resultSet.getInt("stock"));
                product.setImageUrl(resultSet.getString("image_url"));
                product.setFeatured(resultSet.getBoolean("featured"));
                item.setProduct(product);
                item.setQuantity(resultSet.getInt("quantity"));
                cart.add(item);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error retrieving shopping cart", e);
        }
        return cart;
    }

    @Override
    public void addProduct(int userId, int productId) {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) " +
            "VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE quantity = quantity + 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Unable to add product to shopping cart", e);
        }
    }

    @Override
    public void clearCart(int userId) {
        {
            String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql))
            {
                statement.setInt(1, userId);
                statement.executeUpdate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error clearing shopping cart", e);
            }
        }
    }
}
