package com.TimHan.Web;

import org.springframework.http.ResponseEntity;

public interface MortgageCalculator {
    ResponseEntity getPaymentAmount(MortgageQueryParams queryParams);

    ResponseEntity getMortgageAmount(MortgageQueryParams queryParams);

    ResponseEntity setInterestRate(InterestRate newRate);
}
