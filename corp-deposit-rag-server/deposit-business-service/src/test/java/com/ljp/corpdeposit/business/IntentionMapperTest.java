package com.ljp.corpdeposit.business;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntentionMapperTest {

    private SqlSessionFactory sessionFactory;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = new PooledDataSource(
                "org.h2.Driver", "jdbc:h2:mem:intention_" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute("""
                    CREATE TABLE deposit_intention (
                      sn_id BIGINT AUTO_INCREMENT PRIMARY KEY, intention_no VARCHAR(40) NOT NULL,
                      idempotency_key VARCHAR(64) NOT NULL, customer_no VARCHAR(32) NOT NULL,
                      requirement_text VARCHAR(1000), total_amount BIGINT NOT NULL, status INT NOT NULL,
                      source_channel INT NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)
                    """);
            statement.execute("""
                    CREATE TABLE deposit_intention_detail (
                      sn_id BIGINT AUTO_INCREMENT PRIMARY KEY, intention_id BIGINT NOT NULL,
                      detail_no VARCHAR(40) NOT NULL, product_id BIGINT NOT NULL, product_term_id BIGINT NOT NULL,
                      currency_code VARCHAR(8) NOT NULL, amount BIGINT NOT NULL, interest_rate BIGINT NOT NULL,
                      expected_interest BIGINT NOT NULL, status INT NOT NULL,
                      created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)
                    """);
            statement.execute("""
                    INSERT INTO deposit_intention
                    (intention_no,idempotency_key,customer_no,requirement_text,total_amount,status,source_channel,created_at,updated_at)
                    VALUES
                    ('INT001','idem1','CUST001','first',100,1,1,TIMESTAMP '2026-09-20 09:00:00',TIMESTAMP '2026-09-20 09:01:00'),
                    ('INT002','idem2','CUST002','second',200,2,2,TIMESTAMP '2026-09-20 10:00:00',TIMESTAMP '2026-09-20 10:01:00'),
                    ('INT003','idem3','CUST001','third',300,2,1,TIMESTAMP '2026-09-20 10:00:00',TIMESTAMP '2026-09-20 10:02:00')
                    """);
            statement.execute("""
                    INSERT INTO deposit_intention_detail
                    (intention_id,detail_no,product_id,product_term_id,currency_code,amount,interest_rate,expected_interest,status,created_at,updated_at)
                    VALUES
                    (1,'DET002',3,32,'001',40,10000,4,1,TIMESTAMP '2026-09-20 09:03:00',TIMESTAMP '2026-09-20 09:04:00'),
                    (1,'DET001',2,23,'001',60,15000,9,0,TIMESTAMP '2026-09-20 09:02:00',TIMESTAMP '2026-09-20 09:02:00')
                    """);
        }
        Configuration configuration = new Configuration(new Environment(
                "test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(IntentionMapper.class);
        sessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void findsAllOrderedByCreatedAtThenIdDescending() {
        try (SqlSession session = sessionFactory.openSession()) {
            assertThat(session.getMapper(IntentionMapper.class).findAll(null, null))
                    .extracting(IntentionMasterRow::getIntentionNo)
                    .containsExactly("INT003", "INT002", "INT001");
        }
    }

    @Test
    void filtersByCustomerStatusAndCombinationWhileIgnoringBlankCustomer() {
        try (SqlSession session = sessionFactory.openSession()) {
            IntentionMapper mapper = session.getMapper(IntentionMapper.class);
            assertThat(mapper.findAll("CUST001", null)).extracting(IntentionMasterRow::getIntentionNo)
                    .containsExactly("INT003", "INT001");
            assertThat(mapper.findAll(null, 2)).extracting(IntentionMasterRow::getIntentionNo)
                    .containsExactly("INT003", "INT002");
            assertThat(mapper.findAll("CUST001", 2)).extracting(IntentionMasterRow::getIntentionNo)
                    .containsExactly("INT003");
            assertThat(mapper.findAll("   ", null)).hasSize(3);
        }
    }

    @Test
    void findsMasterAndDetailsInIdOrderAndReturnsNullForUnknown() {
        try (SqlSession session = sessionFactory.openSession()) {
            IntentionMapper mapper = session.getMapper(IntentionMapper.class);
            MyBatisIntentionRepository repository = new MyBatisIntentionRepository(mapper);
            IntentionDetailView detail = repository.findByIntentionNo("INT001").orElseThrow();

            assertThat(detail.sourceChannel()).isEqualTo(1);
            assertThat(detail.createdAt()).isNotNull();
            assertThat(detail.totalAmountInCents()).isEqualTo(100L);
            assertThat(detail.details()).extracting(IntentionLineView::detailNo)
                    .containsExactly("DET002", "DET001");
            assertThat(detail.details().get(0).amountInCents()).isEqualTo(40L);
            assertThat(detail.details().get(0).expectedInterestInCents()).isEqualTo(4L);
            assertThatThrownBy(() -> detail.details().add(detail.details().get(0)))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThat(repository.findByIntentionNo("UNKNOWN")).isEmpty();
        }
    }

    @Test
    void returnsImmutableEmptyDetailsForMasterWithoutLines() {
        try (SqlSession session = sessionFactory.openSession()) {
            MyBatisIntentionRepository repository = new MyBatisIntentionRepository(
                    session.getMapper(IntentionMapper.class));

            IntentionDetailView detail = repository.findByIntentionNo("INT002").orElseThrow();

            assertThat(detail.details()).isEmpty();
            assertThatThrownBy(() -> detail.details().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
