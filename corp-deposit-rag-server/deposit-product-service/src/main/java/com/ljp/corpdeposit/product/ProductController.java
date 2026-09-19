package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.ProductMatchRequest;
import com.ljp.corpdeposit.core.ProductPlan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductEligibilityService eligibilityService;
    private final ProductRateService rateService;
    private final ProductMatchService matchService;

    public ProductController(ProductEligibilityService eligibilityService,
                             ProductRateService rateService,
                             ProductMatchService matchService) {
        this.eligibilityService = eligibilityService;
        this.rateService = rateService;
        this.matchService = matchService;
    }

    @GetMapping("/eligible")
    public List<ProductTermCandidate> eligible(@RequestParam int smallCategoryCode) {
        return eligibilityService.findEligibleTerms(smallCategoryCode);
    }

    @PostMapping("/rates/query")
    public Optional<Long> rate(@RequestBody RateQuery query) {
        return rateService.findEffectiveRate(query);
    }

    @PostMapping("/matches")
    public List<ProductPlan> match(@RequestBody ProductMatchRequest request) {
        return matchService.match(request);
    }
}

