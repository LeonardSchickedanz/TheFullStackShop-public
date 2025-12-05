package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.PaymentInfoDAO;
import yorku.thefullstackshop.models.PaymentInfo;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentInfoDAOImpl implements PaymentInfoDAO {

    private final DataSource dataSource;

    public PaymentInfoDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public PaymentInfoDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public PaymentInfo save(PaymentInfo paymentInfo) {
        String sql = "INSERT INTO PaymentInfo (card_number, expiry_date, cvc, user_id) VALUES (?, ?, ?, ?);";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, paymentInfo.getCardNumber());
            ps.setString(2, paymentInfo.getExpiryDate());
            ps.setString(3, paymentInfo.getCvc());
            ps.setInt(4, paymentInfo.getUser().getUserId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    paymentInfo.setPaymentInfoId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving payment info: " + e.getMessage());
            throw new RuntimeException("Could not save payment info to the database.", e);
        }
        return paymentInfo;
    }

    @Override
    public PaymentInfo update(PaymentInfo paymentInfo) {
        String sql = "UPDATE PaymentInfo SET card_number = ?, expiry_date = ?, cvc = ?, user_id = ? WHERE payment_info_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, paymentInfo.getCardNumber());
            ps.setString(2, paymentInfo.getExpiryDate());
            ps.setString(3, paymentInfo.getCvc());
            ps.setInt(4, paymentInfo.getUser().getUserId());
            ps.setInt(5, paymentInfo.getPaymentInfoId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating payment info: " + e.getMessage());
            throw new RuntimeException("Could not update payment info in the database.", e);
        }
        return paymentInfo;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM PaymentInfo WHERE payment_info_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting payment info: " + e.getMessage());
            throw new RuntimeException("Could not delete payment info from the database.", e);
        }
    }

    @Override
    public Optional<PaymentInfo> findById(int id) {
        String sql = "SELECT * FROM PaymentInfo WHERE payment_info_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPaymentInfo(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching payment info: " + e.getMessage());
            throw new RuntimeException("Could not fetch payment info from the database.", e);
        }
    }

    @Override
    public List<PaymentInfo> findAll() {
        String sql = "SELECT * FROM PaymentInfo;";
        List<PaymentInfo> paymentInfos = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                paymentInfos.add(mapResultSetToPaymentInfo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all payment infos: " + e.getMessage());
            throw new RuntimeException("Could not fetch payment infos from the database.", e);
        }
        return paymentInfos;
    }

    @Override
    public Optional<PaymentInfo> findLatestPaymentInfoByUserId(int userId) {
        String sql = "SELECT * FROM PaymentInfo WHERE user_id = ? ORDER BY last_used DESC LIMIT 1;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPaymentInfo(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching latest payment info: " + e.getMessage());
            throw new RuntimeException("Could not fetch latest payment info.", e);
        }
    }

    private PaymentInfo mapResultSetToPaymentInfo(ResultSet rs) throws SQLException {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentInfoId(rs.getInt("payment_info_id"));
        paymentInfo.setCardNumber(rs.getString("card_number"));
        paymentInfo.setExpiryDate(rs.getString("expiry_date"));
        paymentInfo.setCvc(rs.getString("cvc"));

        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        paymentInfo.setUser(user);

        return paymentInfo;
    }
}