package com.superchat.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class InsuranceQuoteTool {
    @Tool("Calculate the price (premium) of a specific insurance product")
    public String buscarPrecioSeguro(@P("Product ID") String productId,
                                     @P("Client's Name") String name,
                                     @P("Client's Age") Integer age,
                                     @P("CLient's marital status") String maritalStatus){

        log.info("Calculate the insurance premium: " + name);
        BigDecimal price;
        price = BigDecimal.valueOf( 20.0 * age * 0.05 );
        log.info("Estimated insurance price for " + name + " is " + price + " USD.");
        return "The insurance value is " + price + " USD. Remember that this is an estimated value and may vary depending on the risk assessment performed by our advisors.";
    }
}
