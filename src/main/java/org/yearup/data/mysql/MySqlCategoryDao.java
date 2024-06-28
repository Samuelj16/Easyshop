package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {
    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        try (Connection connection = getConnection()) {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                categories.add((mapRow(rs)));
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {
                return mapRow(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories (name,description) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setCategoryId(generatedKeys.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return category;
    }

    @Override
    public void update(int categoryId, Category category) {
        Category oldCategory = getById(categoryId);
        if (oldCategory != null) {

            StringBuilder sql = getStringBuilder(categoryId, category);

            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql.toString())) {

                int paramIndex = 1;
                if (category.getName() != null) {
                    statement.setString(paramIndex++, category.getName());
                }

                if (category.getDescription() != null) {
                    statement.setString(paramIndex++, category.getDescription());
                }

                statement.setInt(paramIndex, categoryId);

                statement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);

            }
        }
    }

    private static StringBuilder getStringBuilder(int categoryId, Category category) {
        StringBuilder sql = new StringBuilder("UPDATE categories SET ");
        boolean changes = false;
        /*
        This logic is to allow the system update one or more fields at a time
        */

        if (category.getName() != null) {
            sql.append("name = ?, ");
            changes = true;
        }

        if (category.getDescription() != null) {
            sql.append("description = ?, ");
            changes = true;
        }

        if (!changes) {
            throw new RuntimeException("Category with id " + categoryId + " does not exist");
        }

        // Remove the last comma and space
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE category_id = ?");
        return sql;
    }

    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection =getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);
            statement.executeUpdate();

        } catch (SQLException e) {
           throw new RuntimeException(e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        return new Category() {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};
    }

}
