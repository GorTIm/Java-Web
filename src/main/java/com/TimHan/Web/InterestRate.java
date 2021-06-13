package com.TimHan.Web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Component;



@Data
public class InterestRate {
    @JsonProperty("Interest Rate")
    private Double rate;
}
