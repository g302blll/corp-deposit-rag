package com.ljp.corpdeposit.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductRateMapper {

    @Select("""
            SELECT interest_rate
              FROM deposit_product_rate
             WHERE product_id = #{productId}
               AND product_term_id = #{productTermId}
               AND large_category_code = #{largeCategoryCode}
               AND small_category_code = #{smallCategoryCode}
               AND currency_code = #{currencyCode}
               AND effective_date <= #{businessDate}
               AND (expire_date IS NULL OR expire_date >= #{businessDate})
               AND status = 1
             ORDER BY effective_date DESC, sn_id DESC
             LIMIT 1
            """)
    Long findEffectiveRate(RateQuery query);
}

