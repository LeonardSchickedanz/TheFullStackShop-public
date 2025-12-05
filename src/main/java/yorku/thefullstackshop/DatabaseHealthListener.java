package yorku.thefullstackshop;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import yorku.thefullstackshop.daos.DatabaseConfig;

import java.sql.Connection;

@WebListener
public class DatabaseHealthListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("--------------------------------------------------------------");
        System.out.println("Checking Database Connection...");

        try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database Connection Successful");
            } else {
                System.out.println("Database Connection Failed");
            }
        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());

            if (e.getMessage().contains("Access denied")) {
                System.out.println("TIP: Check your username/password in db.properties");
            } else if (e.getMessage().contains("Communications link failure")) {
                System.out.println("TIP: Is your MySQL Server running? (Check Services)");
            } else if (e.getMessage().contains("Unknown database")) {
                System.out.println("TIP: Does the database schema exist?");
            }
        }
        System.out.println("--------------------------------------------------------------");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}