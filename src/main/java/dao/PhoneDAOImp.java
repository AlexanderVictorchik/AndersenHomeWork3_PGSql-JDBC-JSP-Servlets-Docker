package dao;

import entity.Phone;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhoneDAOImp implements PhoneDAO {

    private static final String INSERT_VENDORS_SQL =
            "insert into vendor (name, site) values ( ?, ?) returning id";
    private static final String INSERT_PHONES_SQL =
            "insert into phone (price,model,vendor_id) values ( ?, ?, ?)";

    private static final String SELECT_PHONE_BY_ID =
            "select phone_id,price,model,name,site from phone " +
                    "join vendor ON vendor.id = phone.vendor_id where phone.phone_id = ?";

    private static final String SELECT_ALL_PHONES =
            "select phone_id,price,model,name,site from phone join vendor on vendor.id = phone.vendor_id";

    private static final String DELETE_PHONES_SQL = "delete from phone where phone_id = ?";

    private static final String UPDATE_VENDORS_SQL =
            "update vendor set name = ? ,site = ? where id = ?";
    private static final String UPDATE_PHONES_SQL =
            "update phone set price = ?, model = ? where phone_id = ? returning vendor_id";

    Connection con = PsqlConnection.getConnection();
    void startConnection(Connection con) {
        this.con = con;
    }

    public boolean insert(Phone phone) throws SQLException, ClassNotFoundException {
        boolean rowInsert;
        System.out.println(INSERT_VENDORS_SQL);
        System.out.println(INSERT_PHONES_SQL);
        try (Connection connection = PsqlConnection.getConnection();
             PreparedStatement pstatement1 = connection.prepareStatement(INSERT_VENDORS_SQL);
             PreparedStatement pstatement2 = connection.prepareStatement(INSERT_PHONES_SQL);) {
            pstatement1.setString(1, phone.getVendor_name());
            pstatement1.setString(2, phone.getVendor_site());
            ResultSet rs = pstatement1.executeQuery();
            int vendor_id = 0;
            if (rs.next()) {
                vendor_id = rs.getInt("id");
            }
            pstatement2.setInt(1, phone.getPrice());
            pstatement2.setString(2, phone.getModel());
            pstatement2.setInt(3, vendor_id);
            rowInsert = pstatement2.executeUpdate() > 0;
            return rowInsert;
        }
    }

    public Optional<Phone> select(String id) {
        Phone phone = null;
        try (Connection connection = PsqlConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PHONE_BY_ID);) {
            preparedStatement.setInt(1, Integer.parseInt(id));
            System.out.println(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int phone_id = rs.getInt("phone_id");
                int price = rs.getInt("price");
                String model = rs.getString("model");
                String vendor_name = rs.getString("name");
                String vendor_site = rs.getString("site");
                phone = new Phone(phone_id, price, model, vendor_name, vendor_site);
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return Optional.of(phone);
    }


    public List<Phone> selectAll() {
        List<Phone> phones = new ArrayList<>();
        try (Connection connection = PsqlConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PHONES);) {
            System.out.println(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("phone_id");
                int price = rs.getInt("price");
                String model = rs.getString("model");
                String vendor_name = rs.getString("name");
                String vendor_site = rs.getString("site");
                phones.add(new Phone(id, price, model, vendor_name, vendor_site));
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return phones;
    }

    public boolean delete(Phone phone) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = PsqlConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PHONES_SQL);) {
            statement.setInt(1, phone.getId());
            rowDeleted = statement.executeUpdate() > 0;
            return rowDeleted;
        }
    }

    public boolean update(Phone phone) throws SQLException {
        boolean rowUpdated;
        try (Connection connection = PsqlConnection.getConnection();
             PreparedStatement pstatement1 = connection.prepareStatement(UPDATE_PHONES_SQL);
             PreparedStatement pstatement2 = connection.prepareStatement(UPDATE_VENDORS_SQL);) {
            pstatement1.setInt(1, phone.getPrice());
            pstatement1.setString(2, phone.getModel());
            pstatement1.setInt(3, phone.getId());
            ResultSet rs = pstatement1.executeQuery();
            int vendor_id = 0;
            if (rs.next()) {
                vendor_id = rs.getInt("vendor_id");
            }
            pstatement2.setString(1, phone.getVendor_name());
            pstatement2.setString(2, phone.getVendor_site());
            pstatement2.setInt(3, vendor_id);

            rowUpdated = pstatement2.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}
