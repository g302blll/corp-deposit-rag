package com.ljp.corpdeposit.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductCatalogMapper {

    @Select("""
            SELECT p.sn_id AS productId, p.product_code AS productCode, p.product_name AS productName,
                   p.deposit_type AS depositType, t.sn_id AS productTermId, t.term_code AS termCode,
                   t.term_name AS termName, t.term_days AS termDays,
                   t.min_open_amount AS minOpenAmount, t.min_retain_amount AS minRetainAmount
              FROM product_customer_scope s
              JOIN deposit_product p ON p.sn_id = s.product_id AND p.status = 1
              JOIN deposit_product_term t ON t.product_id = p.sn_id AND t.status = 1
             WHERE s.small_category_code = #{smallCategoryCode} AND s.status = 1
            """)
    List<ProductTermRow> findEligibleTerms(@Param("smallCategoryCode") int smallCategoryCode);
}

