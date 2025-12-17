package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao{

    //a logger records what your application is doing especially when something goes wrong
    //so you can debug and monitor it without guessing
    private Logger logger = Logger.getLogger(MySqlCategoryDao.class.getName());

    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    //method to get all categories
    @Override

    //creating a list
    public List<Category> getAllCategories() {

        //list to hold all category objects retrieved from the database
        List<Category> categories = new ArrayList<>();

        //try-with-resources to manage connection, prepared statement, and result set
        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("""
                    SELECT
                        category_id,
                        name,
                        description
                    FROM
                        categories
                    """);
            ResultSet resultSet = statement.executeQuery();
            {
                //loop through each row in the resultSet
                while (resultSet.next()) {
                    Category category = new Category();
                    category.setCategoryId(resultSet.getInt("category_id"));
                    category.setName(resultSet.getString("name"));
                    category.setDescription(resultSet.getString("description"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            //log any exceptions with full stack trace
            logger.log(Level.SEVERE, "Error getting all the categories", e);
        }
        //return list of categories
        return categories;
    }

    //method to get categories by ID
    @Override
    public Category getById(int categoryId) {

    try (Connection connection = dataSource.getConnection();

         PreparedStatement stmt = connection.prepareStatement(
                 """
                        SELECT
                            category_id,
                            name,
                            description
                        FROM
                            categories
                        WHERE category_id = ?
                        """)) {

        stmt.setInt(1, categoryId);

        //nested try to ensure the result set is closed properly
        try (ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet.next()) {
                Category category = new Category();
                category.setCategoryId(resultSet.getInt("category_id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));
                return category;
            }
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error getting category by ID: " + categoryId, e);
    }
        return null;
    }


    //method to create a category
    @Override
    public Category create(Category category) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO categories (name, description)
                     VALUES (?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    category.setCategoryId(resultSet.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating category: " + category.getName(), e);
        }
        return category;
    }

    //method to update a category
    @Override
    public void update(int categoryId, Category category) {

        //coalesce keeps the existing value if the provided value is null
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
             """
             UPDATE
                categories
             SET
                 name = COALESCE(?, name),
                 description = COALESCE(?, description)
             WHERE
                category_id = ?
         """)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, categoryId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE,
                    "Error updating category ID: " + categoryId, e);
        }
    }

    //method to delete a category
    @Override
    public void delete(int categoryId)
    {
        // delete category
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
