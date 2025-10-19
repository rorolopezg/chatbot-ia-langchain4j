package com.superchat.utils;

import com.superchat.model.ClientProfile;

public final class AudienceQueryBuilder {

    private AudienceQueryBuilder(){}

    public static String build(ClientProfile cp) {
        int age = cp.getAge() == null ? 0 : cp.getAge();

        String ageBand = (age >= 65) ? "senior, elderly, 65+, retiree"
                : (age <= 25 && age > 0) ? "young adult, student, early career"
                : (age > 0) ? "adult, working professional"
                : "age unknown";

        String children = (cp.getHasChildren() == null)
                ? "children: unknown"
                : (cp.getHasChildren() ? "has children/dependents" : "no children/no dependents");

        String marital = (cp.getMaritalStatus() == null || cp.getMaritalStatus().isBlank())
                ? "marital status: unknown"
                : cp.getMaritalStatus();

        String insuredInterest = (cp.getExpressionOfInterestInInsurance() == null || cp.getExpressionOfInterestInInsurance().isBlank())
                ? "interests: unknown"
                : "interested in " + cp.getExpressionOfInterestInInsurance();

        return """
               Find target audience that matches: age %s (%s), %s, %s, %s.
               Return insurance products whose TARGET AUDIENCE best fits this profile.
               """.formatted(
                (age == 0 ? "unknown" : String.valueOf(age)),
                ageBand,
                marital,
                children,
                insuredInterest
        );
    }
}
