package com.TimHan.Web;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class MortgageQueryParams {

    private Double Asking_Price;
    private Double Down_Payment;
    private Integer Payment_schedule;
    private String Amortization_Period;
    private Double payment_amount;
    @JsonProperty("Interest Rate")
    private Double Interest_Rate;


}
