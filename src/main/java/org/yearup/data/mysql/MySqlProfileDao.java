package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile)
    {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = getConnection())
        {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProfile(int userId, Profile profile) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE profiles SET ");
        boolean changes = false;

        if (profile.getFirstName() != null) {
            sql.append("first_name = ?, ");
            changes = true;
        }
        if (profile.getLastName() != null) {
            sql.append("last_name = ?, ");
            changes = true;
        }
        if (profile.getPhone() != null) {
            sql.append("phone = ?, ");
            changes = true;
        }
        if (profile.getEmail() != null) {
            sql.append("email = ?, ");
            changes = true;
        }
        if (profile.getAddress() != null) {
            sql.append("address = ?, ");
            changes = true;
        }
        if (profile.getCity() != null) {
            sql.append("city = ?, ");
            changes = true;
        }
        if (profile.getState() != null) {
            sql.append("state = ?, ");
            changes = true;
        }
        if (profile.getZip() != null) {
            sql.append("zip = ?, ");
            changes = true;
        }

        if (!changes) {
            throw new RuntimeException("No fields to update for profile with id " + userId);
        }

        // Remove the last comma and space
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE user_id = ?");

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (profile.getFirstName() != null) {
                stmt.setString(paramIndex++, profile.getFirstName());
            }
            if (profile.getLastName() != null) {
                stmt.setString(paramIndex++, profile.getLastName());
            }
            if (profile.getPhone() != null) {
                stmt.setString(paramIndex++, profile.getPhone());
            }
            if (profile.getEmail() != null) {
                stmt.setString(paramIndex++, profile.getEmail());
            }
            if (profile.getAddress() != null) {
                stmt.setString(paramIndex++, profile.getAddress());
            }
            if (profile.getCity() != null) {
                stmt.setString(paramIndex++, profile.getCity());
            }
            if (profile.getState() != null) {
                stmt.setString(paramIndex++, profile.getState());
            }
            if (profile.getZip() != null) {
                stmt.setString(paramIndex++, profile.getZip());
            }

            stmt.setInt(paramIndex, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public Profile getByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Profile(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("city"),
                            rs.getString("state"),
                            rs.getString("zip")
                    );
                } else {
                    throw new SQLException("No profile found with user_id: " + userId);
                }
            }
        }}

}
