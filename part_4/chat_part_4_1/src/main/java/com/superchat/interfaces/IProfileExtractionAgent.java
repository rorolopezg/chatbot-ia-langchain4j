package com.superchat.interfaces;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IProfileExtractionAgent {
    @SystemMessage("""
        You are an assistant that extracts structured customer data to fill out their profile.
        From the submitted text, extract information from the following client's profile and return it in structured JSON format, considering the following attributes:
        - name (string or null): name of the client.
        - maritalStatus (string or null): marital status of the client. For example: single, married, divorced, widowed, etc. If not specified, set it to null.
        - age (integer or null): age of the client. If not specified, set it to null.
        - hasChildren (boolean or null): true if the client has children, false if not, null if not specified.
        - hasPets (boolean or null): true if the client has pets, false if not, null if not specified.
        - hasHouses (boolean or null): true if the client has houses, false if not, null if not specified.
        - hasApartments (boolean or null): true if the client has apartments, false if not, null if not specified.
        - hasCars (boolean or null): true if the client has cars, false if not, null if not specified.
        - likeTraveling (boolean or null): true if the client likes traveling, false if not, null if not specified.
        - expressionOfInterestInInsurance (string or null): Indicate the customer's interest in types of insurance, such as life insurance, health insurance, personal accident insurance, pet insurance, home insurance, etc.
        - expressionOfInterestInOthersThings (string or null): Indicate the customer's interest in other topics not related to insurance, such as travel, sports, technology, etc.
        ---
        If any attribute is missing from the text, set it to null.
        Don't explain anything; just respond with clean JSON.
        Don't add additional text, just the JSON.
        Always respond in English.
        Example output:
        {
          "name": "John Doe",
          "age": 35,
          "maritalStatus": "married",
          "hasChildren": true,
          "hasPets": false,
          "hasHouses": true,
          "hasApartments": null,
          "hasCars": true,
          "likeTraveling": true,
          "expressionOfInterestInInsurance": "life insurance, health insurance and travel insurance",
          "expressionOfInterestInOthersThings": "travel, technology and sports",
        }
    """)
    String extractData(@UserMessage String mensajeCliente);
}
