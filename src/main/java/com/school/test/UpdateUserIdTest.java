package com.school.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 更新用户ID测试类，将所有学习计划的user_id统一修改为1
 */
public class UpdateUserIdTest {

    public static void main(String[] args) {
        // 数据库连接信息
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "123456";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // 加载驱动
            Class.forName("org.postgresql.Driver");

            // 建立连接
            connection = DriverManager.getConnection(url, username, password);

            // 创建SQL语句
            String sql = "UPDATE public.study_plans SET user_id = 1";

            // 创建PreparedStatement
            preparedStatement = connection.prepareStatement(sql);

            // 执行更新
            int rowsAffected = preparedStatement.executeUpdate();

            // 打印结果
            System.out.println("成功更新 " + rowsAffected + " 条记录的 user_id 为 1");

        } catch (ClassNotFoundException e) {
            System.out.println("驱动加载失败: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("资源关闭失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}