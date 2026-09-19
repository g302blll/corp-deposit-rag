package com.ljp.corpdeposit.business;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IntentionMapper {

    @Select("SELECT sn_id, intention_no, customer_no, status, idempotency_key, requirement_text, total_amount " +
            "FROM deposit_intention WHERE idempotency_key = #{key}")
    IntentionMasterRow findByIdempotencyKey(@Param("key") String key);

    @Select("SELECT detail_no FROM deposit_intention_detail WHERE intention_id = #{intentionId} ORDER BY sn_id")
    List<String> findDetailNos(@Param("intentionId") long intentionId);

    @Insert("""
            INSERT IGNORE INTO deposit_intention
                (intention_no, idempotency_key, customer_no, requirement_text, total_amount, status, source_channel)
            VALUES (#{intentionNo}, #{idempotencyKey}, #{customerNo}, #{requirementText}, #{totalAmount}, 1, 1)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "snId")
    int insertMaster(IntentionMasterRow row);

    @Insert("""
            INSERT INTO deposit_intention_detail
                (intention_id, detail_no, product_id, product_term_id, currency_code, amount,
                 interest_rate, expected_interest, status)
            VALUES (#{intentionId}, #{detailNo}, #{productId}, #{productTermId}, #{currencyCode}, #{amount},
                    #{interestRate}, #{expectedInterest}, 0)
            """)
    int insertDetail(IntentionDetailRow row);
}

