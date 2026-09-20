package com.ljp.corpdeposit.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductCatalogMapper {

    @Select("""
            SELECT sn_id AS productId, product_code AS productCode, product_name AS productName,
                   deposit_type AS depositType, status
              FROM deposit_product
             WHERE status = 1
             ORDER BY product_code
            """)
    List<ProductRow> findAllActiveProducts();

    @Select("""
            SELECT sn_id AS productId, product_code AS productCode, product_name AS productName,
                   deposit_type AS depositType, status
              FROM deposit_product
             WHERE sn_id = #{productId} AND status = 1
            """)
    ProductRow findActiveProduct(@Param("productId") long productId);

    @Select("""
            SELECT sn_id AS productTermId, term_code AS termCode, term_name AS termName,
                   term_days AS termDays, min_open_amount AS minOpenAmount,
                   min_retain_amount AS minRetainAmount, notice_days AS noticeDays
              FROM deposit_product_term
             WHERE product_id = #{productId} AND status = 1
             ORDER BY term_days, sn_id
            """)
    List<ProductTermRow> findActiveTerms(@Param("productId") long productId);

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

