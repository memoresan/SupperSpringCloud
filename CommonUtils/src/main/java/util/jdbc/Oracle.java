package util.jdbc;

import java.sql.*;

public class Oracle {
        public static void main(String[]agrs){
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                Class.forName("oracle.jdbc.OracleDriver");
           /* conn =
                    DriverManager.getConnection(DmMicApacheLogConstants.BI_CONNECT, DmMicApacheLogConstants.BI_USER,
                            DmMicApacheLogConstants.BI_PASSWORD);*/
                /*conn =
                        DriverManager.getConnection("jdbc:oracle:thin:@192.168.73.15:5210:mic", "micoss2005",
                                "micoss2020#");*/
                conn = DriverManager.getConnection("jdbc:oracle:thin:@10.110.8.38:1521/cube","BI_READ","bi_read");

                conn.setReadOnly(true);
                stmt = conn.createStatement();
                rs = stmt.executeQuery("select  count(1) from ads_ab_tb_exchg_log");
                rs.next();
                System.out.println(rs.getInt(1));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
}
