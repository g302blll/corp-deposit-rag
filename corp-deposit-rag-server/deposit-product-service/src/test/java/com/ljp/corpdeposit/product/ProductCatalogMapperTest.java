package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.DepositType;
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

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:product-catalog;MODE=MySQL;DB_CLOSE_DELAY=-1");

        createSchema(dataSource);
        insertFixtures(dataSource);

        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(ProductCatalogMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void listsOnlyActiveProductsInProductCodeOrder() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBatisProductCatalogRepository repository = repository(session);

            assertThat(repository.findAllActiveProducts())
                    .extracting(ProductSummary::productCode)
                    .containsExactly("NOTICE", "TIME");
            assertThat(repository.findAllActiveProducts().get(0).depositType())
                    .isEqualTo(DepositType.NOTICE);
        }
    }

    @Test
    void loadsActiveProductAndActiveTermsWithMappedAmountsAndNoticeDays() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyBatisProductCatalogRepository repository = repository(session);

            assertThat(repository.findActiveProduct(3L))
                    .contains(new ProductSummary(3L, "NOTICE", "单位通知存款", DepositType.NOTICE, 1));
            assertThat(repository.findActiveProduct(4L)).isEmpty();
            assertThat(repository.findActiveProduct(999L)).isEmpty();
            assertThat(repository.findActiveTerms(3L)).containsExactly(
                    new ProductTermView(32L, "1D", "一天通知", 1, 5_000_000L, 4_000_000L, 1),
                    new ProductTermView(31L, "7D-A", "七天通知A", 7, 6_000_000L, 5_000_000L, 7),
                    new ProductTermView(33L, "7D-B", "七天通知B", 7, 7_000_000L, 6_000_000L, 7));
        }
    }

    private MyBatisProductCatalogRepository repository(SqlSession session) {
        return new MyBatisProductCatalogRepository(session.getMapper(ProductCatalogMapper.class));
    }

    private void createSchema(DataSource dataSource) throws SQLException {
        execute(dataSource,
                "DROP ALL OBJECTS",
                "CREATE TABLE deposit_product (sn_id BIGINT PRIMARY KEY, product_code VARCHAR(32) NOT NULL, product_name VARCHAR(100) NOT NULL, deposit_type INT NOT NULL, status INT NOT NULL)",
                "CREATE TABLE deposit_product_term (sn_id BIGINT PRIMARY KEY, product_id BIGINT NOT NULL, term_code VARCHAR(16) NOT NULL, term_name VARCHAR(64) NOT NULL, term_days INT NOT NULL, min_open_amount BIGINT NOT NULL, min_retain_amount BIGINT NOT NULL, notice_days INT, status INT NOT NULL)",
                "CREATE TABLE product_customer_scope (sn_id BIGINT PRIMARY KEY, product_id BIGINT NOT NULL, small_category_code INT NOT NULL, status INT NOT NULL)");
    }

    private void insertFixtures(DataSource dataSource) throws SQLException {
        execute(dataSource,
                "INSERT INTO deposit_product VALUES (2, 'TIME', '单位定期存款', 2, 1), (3, 'NOTICE', '单位通知存款', 3, 1), (4, 'LARGE', '停用大额存单', 4, 0)",
                "INSERT INTO deposit_product_term VALUES (31, 3, '7D-A', '七天通知A', 7, 6000000, 5000000, 7, 1), (32, 3, '1D', '一天通知', 1, 5000000, 4000000, 1, 1), (33, 3, '7D-B', '七天通知B', 7, 7000000, 6000000, 7, 1), (34, 3, 'OFF', '停用期限', 0, 1, 1, NULL, 0)");
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
