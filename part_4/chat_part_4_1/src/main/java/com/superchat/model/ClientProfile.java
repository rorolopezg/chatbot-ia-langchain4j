package com.superchat.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class ClientProfile {
    private String name;
    private Integer age;
    private String maritalStatus;
    private String expressionOfInterestInInsurance;
    private Boolean hasChildren;

    public Boolean isComplete() {
        return name != null && !name.isEmpty()
                && age != null
                // At least one of maritalStatus or hasChildren should be provided to get a more complete profile
                && ((maritalStatus != null && !maritalStatus.isEmpty()) || hasChildren != null);
    }

    public void applyJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            String name = getText(root, "name");
            if (name != null && !name.isEmpty())
                this.setName(name);

            String maritalStatus = getText(root, "maritalStatus");
            if (maritalStatus != null && !maritalStatus.isEmpty())
                this.setMaritalStatus(maritalStatus);

            Integer age = getInt(root, "age");
            if (age != null)
                this.setAge(age);

            Boolean hasChildren = getBoolean(root, "hasChildren");
            if (hasChildren != null)
                this.setHasChildren(hasChildren);

            String expressionOfInterestInInsurance = getText(root, "expressionOfInterestInInsurance");
            if (expressionOfInterestInInsurance != null && !expressionOfInterestInInsurance.isEmpty())
                this.setExpressionOfInterestInInsurance(expressionOfInterestInInsurance);

        } catch (Exception e) {
            log.error("Error building ClientProfile from JSON", e);
        }
    }

    private static String getText(JsonNode root, String field) {
        if (root.has(field) && !root.get(field).isNull()) {
            return root.get(field).asText();
        }
        return null;
    }

    private static Integer getInt(JsonNode root, String field) {
        if (root.has(field) && !root.get(field).isNull()) {
            return root.get(field).asInt();
        }
        return null;
    }

    private static Boolean getBoolean(JsonNode root, String field) {
        if (root.has(field) && !root.get(field).isNull()) {
            return root.get(field).asBoolean();
        }
        return null;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Error converting ClientProfile to JSON", e);
        }
    }

    private String buildAgeBand() {
        if (age == null)
            return "Unknown";

        if (age <= 12)
            return "Childhood";
        else if (age <= 20)
            return "Adolescence";
        else if (age <= 26)
            return "Youth";
        else if (age <= 59)
            return "Adulthood";
        else if (age <= 65)
            return "Senior";
        else
            return "Elderly";
    }

    public String friendlyProfileDescription() {
        String ageBand = this.buildAgeBand();
        String marital = (this.getMaritalStatus() == null || this.getMaritalStatus().isBlank())
                ? "Unknown marital status"
                : this.getMaritalStatus();
        String children  = (this.getHasChildren() == null)
                ? "Unknown if has children"
                : (this.getHasChildren() ? "With children" : "With No children");

        String insuredInterest = (expressionOfInterestInInsurance == null || expressionOfInterestInInsurance.isBlank())
                ? "interests: unknown"
                : "interested in " + expressionOfInterestInInsurance;

        return """
               Find target audience that matches: age %s (%s), %s, %s, %s.
               Return insurance products whose TARGET AUDIENCE best fits this profile.
               """.formatted(
                (age == null || age == 0 ? "unknown" : String.valueOf(age)),
                ageBand,
                marital,
                children,
                insuredInterest
        );
        /*
        return """
           I'm %s years old (%s), I'm %s, %s. Interest: %s.
           """.formatted(ageStr, ageBand, marital, children, insuredInterest);

         */
    }

    @Override
    public String toString() {
        return "ClientProfile{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", expressionOfInterestInInsurance='" + expressionOfInterestInInsurance + '\'' +
                ", hasChildren=" + hasChildren +
                '}';
    }
}
