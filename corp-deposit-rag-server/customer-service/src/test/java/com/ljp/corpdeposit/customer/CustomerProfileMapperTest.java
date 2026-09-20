package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerProfileMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:customer-profile;MODE=MySQL;DB_CLOSE_DELAY=-1");

        createSchema(dataSource);
        insertFixtures(dataSource);

        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(CustomerProfileMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void listsOnlyActiveCustomersWithActiveCategoriesInCustomerNumberOrder() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<CustomerProfile> customers = session.getMapper(CustomerProfileMapper.class).findAllActive();

            assertThat(customers)
                    .extracting(CustomerProfile::customerNo)
                    .containsExactly("CUST001", "CUST002");
        }
    }

    @Test
    void returnsEmptyForCustomerWithDisabledCategory() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CustomerProfileMapper mapper = session.getMapper(CustomerProfileMapper.class);

            assertThat(mapper.findByCustomerNo("CUST004")).isEmpty();
            assertThat(mapper.findByCustomerNo("CUST005")).isEmpty();
        }
    }

    private void createSchema(DataSource dataSource) throws SQLException {
        execute(dataSource,
                "DROP ALL OBJECTS",
                "CREATE TABLE customer_large_category (sn_id BIGINT PRIMARY KEY, category_code INT NOT NULL, category_name VARCHAR(100) NOT NULL, status INT NOT NULL)",
                "CREATE TABLE customer_small_category (sn_id BIGINT PRIMARY KEY, large_category_id BIGINT NOT NULL, category_code INT NOT NULL, category_name VARCHAR(100) NOT NULL, status INT NOT NULL)",
                "CREATE TABLE customer (sn_id BIGINT PRIMARY KEY, customer_no VARCHAR(32) NOT NULL, customer_name VARCHAR(200) NOT NULL, small_category_id BIGINT NOT NULL, status INT NOT NULL)");
    }

    private void insertFixtures(DataSource dataSource) throws SQLException {
        execute(dataSource,
                "INSERT INTO customer_large_category VALUES (1, 4, '民营企业', 1), (2, 3, '国有企业', 0)",
                "INSERT INTO customer_small_category VALUES (11, 1, 401, '科技型民营企业', 1), (12, 1, 402, '停用小类', 0), (13, 2, 301, '停用大类下的小类', 1)",
                "INSERT INTO customer VALUES (1, 'CUST002', '客户二', 11, 1), (2, 'CUST001', '客户一', 11, 1), (3, 'CUST003', '停用客户', 11, 0), (4, 'CUST004', '停用小类客户', 12, 1), (5, 'CUST005', '停用大类客户', 13, 1)");
    }

    private void execute(DataSource dataSource, String... statements) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }
}
