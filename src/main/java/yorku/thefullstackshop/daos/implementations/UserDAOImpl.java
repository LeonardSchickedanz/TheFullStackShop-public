package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.UserDAO;
import yorku.thefullstackshop.models.Role;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private final DataSource dataSource;

    public UserDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public UserDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO `User` (first_name, last_name, email, password, role_id) VALUES (?,?,?,?,?);";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getRole().getRoleId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving user: " + e.getMessage());
            throw new RuntimeException("Could not save user to the database.", e);
        }
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE `User` SET first_name = ?, last_name = ?, email = ?, password = ?, role_id = ? WHERE user_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getRole().getRoleId());
            ps.setInt(6, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating user: " + e.getMessage());
            throw new RuntimeException("Could not update user in the database.", e);
        }
        return user;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM `User` WHERE user_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting user: " + e.getMessage());
            throw new RuntimeException("Could not delete user from the database.", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM `User` WHERE user_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));

                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    user.setRole(role);

                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching user: " + e.getMessage());
            throw new RuntimeException("Could not fetch user from the database.", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM `User`;";
        List<User> users = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));

                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                user.setRole(role);

                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all users: " + e.getMessage());
            throw new RuntimeException("Could not fetch users from the database.", e);
        }
        return users;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM `User` WHERE email = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));

                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    user.setRole(role);

                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching user by email: " + e.getMessage());
            throw new RuntimeException("Could not fetch user by email.", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM `User` WHERE email = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Database error while checking email existence: " + e.getMessage());
            throw new RuntimeException("Could not check email existence.", e);
        }
    }
}