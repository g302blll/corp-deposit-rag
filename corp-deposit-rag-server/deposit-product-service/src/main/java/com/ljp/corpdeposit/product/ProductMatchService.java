package com.ljp.corpdeposit.product;

import com.ljp.corpdeposit.core.ProductMatchRequest;
import com.ljp.corpdeposit.core.ProductPlan;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProductMatchService {

    private final ProductEligibilityService eligibilityService;
    private final ProductRateService rateService;
    private final InterestCalculationService interestCalculationService;

    public ProductMatchService(ProductEligibilityService eligibilityService,
                               ProductRateService rateService,
                               InterestCalculationService interestCalculationService) {
        this.eligibilityService = eligibilityService;
        this.rateService = rateService;
        this.interestCalculationService = interestCalculationService;
    }

    public List<ProductPlan> match(ProductMatchRequest request) {
        return eligibilityService.findEligibleTerms(request.smallCategoryCode()).stream()
                .filter(candidate -> request.amountInCents() >= candidate.minOpenAmount())
                .map(candidate -> toPlan(candidate, request))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(plan -> termDistance(plan, request.preferredDays())))
                .limit(5)
                .toList();
    }

    private Optional<ProductPlan> toPlan(ProductTermCandidate candidate, ProductMatchRequest request) {
        RateQuery query = new RateQuery(
                candidate.productId(), candidate.productTermId(), request.largeCategoryCode(),
                request.smallCategoryCode(), request.currencyCode(), request.businessDate());
        return rateService.findEffectiveRate(query).map(rate -> new ProductPlan(
                candidate.productId(), candidate.productCode(), candidate.productName(), candidate.depositType(),
                candidate.productTermId(), candidate.termCode(), candidate.termName(), candidate.termDays(),
                request.amountInCents(), rate,
                interestCalculationService.calculate(request.amountInCents(), rate, candidate.termDays()),
                request.currencyCode()));
    }

    private int termDistance(ProductPlan plan, Integer preferredDays) {
        return preferredDays == null ? plan.termDays() : Math.abs(plan.termDays() - preferredDays);
    }
}

