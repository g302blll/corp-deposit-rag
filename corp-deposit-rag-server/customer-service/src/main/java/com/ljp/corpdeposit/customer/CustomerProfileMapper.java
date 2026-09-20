package com.ljp.corpdeposit.customer;

import com.ljp.corpdeposit.core.CustomerProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CustomerProfileMapper extends CustomerProfileRepository {

    @Override
    @Select("""
            SELECT c.customer_no AS customerNo,
                   c.customer_name AS customerName,
                   lc.category_code AS largeCategoryCode,
                   lc.category_name AS largeCategoryName,
                   sc.category_code AS smallCategoryCode,
                   sc.category_name AS smallCategoryName
              FROM customer c
              JOIN customer_small_category sc ON sc.sn_id = c.small_category_id AND sc.status = 1
              JOIN customer_large_category lc ON lc.sn_id = sc.large_category_id AND lc.status = 1
             WHERE c.customer_no = #{customerNo}
               AND c.status = 1
            """)
    Optional<CustomerProfile> findByCustomerNo(@Param("customerNo") String customerNo);

    @Override
    @Select("""
            SELECT c.customer_no AS customerNo,
                   c.customer_name AS customerName,
                   lc.category_code AS largeCategoryCode,
                   lc.category_name AS largeCategoryName,
                   sc.category_code AS smallCategoryCode,
                   sc.category_name AS smallCategoryName
              FROM customer c
              JOIN customer_small_category sc ON sc.sn_id = c.small_category_id AND sc.status = 1
              JOIN customer_large_category lc ON lc.sn_id = sc.large_category_id AND lc.status = 1
             WHERE c.status = 1
             ORDER BY c.customer_no ASC
            """)
    List<CustomerProfile> findAllActive();
}

