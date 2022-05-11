package util.jdbc;

import org.slf4j.Logger;
import util.log4j2.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 采用c3p0的方式進行jdbc加载
 */
public class JDBCUtils {
    private static Logger logger = LoggerUtil.getLogger();

    /**
     * 执行非查询语句
     * @param sql
     * @param param
     */
    public static void execute(String sql,Object... param){
        PreparedStatement ps = null;
        Connection connection = null;
        try {
            connection = C3P0Util.getConnection();
            logger.info("connection----"+connection);
            ps = connection.prepareStatement(sql);
            for(int i=0;i<param.length;i++){
                ps.setObject(i+1,param[i]);
            }
            ps.execute();
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage());
        }finally {
            C3P0Util.release(connection,ps,null);
        }
    }

    /**
     * 执行查询语句
     * @param sql
     * @param param
     * @return
     */
    public static ResultSet executeQuery(String sql, Object... param){
        PreparedStatement ps = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = C3P0Util.getConnection();
            logger.info("connection----"+connection);
            ps = connection.prepareStatement(sql);
            for(int i=0;i<param.length;i++){
                ps.setObject(i+1,param[i]);
            }
            resultSet = ps.executeQuery();
            return resultSet;
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage());
        }
        return null;
    }
}
