package com.TimHan.Web;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MortgageCalculatorImpl implements MortgageCalculator{
    public static Double annualInterestRate = 0.025;

    /**
     * calculate the recurring payment amount of a mortgage
     * Necessary information:Asking price,Down payment,Payment schedule,Amortization period
     * All information will be taken from query string of url
     * @param queryParams
     * @return
     */
    @Override
    @GetMapping(value = "/payment-amount",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPaymentAmount(@ModelAttribute MortgageQueryParams queryParams) {
        Double askingPrice = queryParams.getAsking_Price();
        Double downPayment = queryParams.getDown_Payment();
        Integer paymentSchedule = queryParams.getPayment_schedule();
        String amortizationPeriod = queryParams.getAmortization_Period();

        //Valid Payment schedule should lay between 5 years and 25 years
        if(paymentSchedule<5 || paymentSchedule>25){
            return ResponseEntity.badRequest().body("Not valid Payment schedule,please try again!");
        }

        //Down payment must be at least 5% of first $500k plus 10% of any amount above $500k
        if(!validDownPayment(downPayment,askingPrice)){
            return ResponseEntity.badRequest().body("Not valid Down payment,please try again!");
        }


        Double rate = getFactorRate(paymentSchedule,amortizationPeriod);

        if (rate==null){
            return ResponseEntity.badRequest().body("Could not generate feasible rate, please check query string");
        }

        Double result =(askingPrice-downPayment+ getInsuranceFee(downPayment,askingPrice))*rate;
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("Payment amount",Math.round(result*100)/100);
        return ResponseEntity.ok().body(resultMap);
    }


    /**
     *
     * calculate the maximum mortgage amount (principal)
     * Necessary information:Payment amount,Payment schedule,Amortization period
     * All information will be taken from query string of url
     * @param queryParams
     * @return
     */
    @Override
    @GetMapping(value = "/mortgage-amount",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMortgageAmount(@ModelAttribute MortgageQueryParams queryParams) {
        Double paymentAmount = queryParams.getPayment_amount();
        Integer paymentSchedule = queryParams.getPayment_schedule();
        String amortizationPeriod = queryParams.getAmortization_Period();

        //Valid Payment schedule should lay between 5 years and 25 years
        if(paymentSchedule<5 || paymentSchedule>25){
            return ResponseEntity.badRequest().body("Not valid Payment schedule,please try again!");
        }

        Double rate = getFactorRate(paymentSchedule,amortizationPeriod);

        if (rate==null || rate == 0){
            return ResponseEntity.badRequest().body("Could not generate feasible rate, please check query string");
        }

        Double result = paymentAmount/rate;
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("Maximum Mortgage",Math.round(result*100)/100);
        return ResponseEntity.ok().body(resultMap);
    }

    /**
     * Change the interest rate, and return updated new rate and old rate
     * @param
     * @return
     */
    @Override
    @PatchMapping(value = "/interest-rate",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setInterestRate(@RequestBody InterestRate iRate) {
        Double newRate = iRate.getRate();
        Double oldRate = annualInterestRate;
        if(newRate==null){
            return ResponseEntity.badRequest().body("Fail to get new rate");
        }else{
            annualInterestRate=newRate;
        }
        Map<String,Double> resultMap= new HashMap<>();
        resultMap.put("Old interest rate",oldRate);
        resultMap.put("New interest rate",annualInterestRate);
        return ResponseEntity.ok().body(resultMap);
    }


    /**
     * Calculate the factor used to transform between mortgage principal and monthly payment
     * @param yearsToPay
     * @param period
     * @return
     */
    public  Double getFactorRate(Integer yearsToPay,String period){
        Map<String,Integer> periodMap = new HashMap<>();
        periodMap.put("weekly",4);
        periodMap.put("biweekly",2);
        periodMap.put("monthly",1);
        try{
            int n = yearsToPay*12*periodMap.get(period.toLowerCase());
            double r = annualInterestRate/(12*periodMap.get(period.toLowerCase()));
            double power = Math.pow((1+r),n);
            return r*power/(power-1);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * examine if the input Down payment is valid
     * @param downPayment
     * @param askingPrice
     * @return
     */
    private boolean validDownPayment(Double downPayment,Double askingPrice){
        Double mortgageAmount= askingPrice - downPayment;
        if(mortgageAmount<=500000){
            return (downPayment/mortgageAmount)>=0.05;
        }
        return downPayment>=(mortgageAmount-500000)*0.1+25000;
    }

    /**
     * Get the Mortgage insurance based on the input  Down payment and Asking price
     * @param downPayment
     * @param askingPrice
     * @return
     */
    private double getInsuranceFee(Double downPayment,Double askingPrice){
        Double mortgage = askingPrice-downPayment;
        Double ratio = downPayment/mortgage;
        if(ratio<0.2 && downPayment<1000000){
            if(ratio>=0.05 && ratio<0.1){
                return mortgage*0.0315;
            }else if(ratio>=0.1 && ratio<0.15){
                return mortgage*0.024;
            }else if(ratio>=0.15 && ratio<0.2){
                return mortgage*0.018;
            }

        }

        return 0;
    }
}
