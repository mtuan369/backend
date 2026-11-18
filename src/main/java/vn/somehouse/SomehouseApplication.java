package vn.somehouse;
import org.springframework.boot.SpringApplication;import org.springframework.boot.autoconfigure.SpringBootApplication;import java.sql.*;import java.util.*;
@SpringBootApplication public class SomehouseApplication {public static void main(String[] args)throws Exception{
 if(Arrays.asList(args).contains("--check-db")){String url=System.getenv("DB_URL");if(url==null||url.isBlank())throw new IllegalStateException("Chưa đặt DB_URL trong .env.");Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");Properties p=new Properties();p.setProperty("user",System.getenv().getOrDefault("DB_USER","somehouse_app"));p.setProperty("password",System.getenv().getOrDefault("DB_PASSWORD",""));p.setProperty("loginTimeout","10");try(Connection c=DriverManager.getConnection(url,p);Statement q=c.createStatement();ResultSet r=q.executeQuery("SELECT DB_NAME()")){r.next();System.out.println("SQL Server kết nối thành công. Database: "+r.getString(1));}return;}
 SpringApplication.run(SomehouseApplication.class,args);
}}
