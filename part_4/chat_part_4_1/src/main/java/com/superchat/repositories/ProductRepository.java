package com.superchat.repositories;

import com.superchat.model.Product;

import java.util.ArrayList;
import java.util.List;

public final class ProductRepository {

    private ProductRepository(){}

    public static List<Product> findAllProducts() {
        List<Product> items = new ArrayList<>();

        items.add(new Product(
                "PROD_01",
                "Individual Life Insurance",
                "Insurance designed to provide financial protection to your loved ones in case of death.",
                """
                - Natural death: Provides a benefit for death due to natural causes.
                - Accidental death: Covers death by accidents, offering an additional benefit.
                """,
                """
                Adults aged 25-65, of any gender, who are primary income earners or have financial dependents
                (such as spouses, children, or elderly parents), seeking to ensure the financial security and
                well-being of their families in the event of unforeseen circumstances.
                """,
                25, 65, "life"
        ));

        items.add(new Product(
                "PROD_02",
                "Personal Accident Insurance",
                "Insurance that offers protection in case of accidents resulting in injuries or death.",
                """
                - Accidental death: Provides a benefit for death due to accidents.
                - Permanent disability: Covers permanent disability resulting from an accident, offering financial benefits.
                """,
                """
                Adults aged 25-65, of any gender, who are exposed to risks of accidents in their daily activities, such as workers,
                students, athletes, or people who frequently travel, and who wish to protect themselves and their
                families from the financial consequences of accidental injuries or death.
                """,
                25, 65, "accident"
        ));

        items.add(new Product(
                "PROD_03",
                "Health Insurance",
                "Insurance that covers medical expenses for illnesses or accidents.",
                """
                - Hospitalization: Covers costs of hospitalization due to illness or accident.
                - Surgical procedures: Covers expenses for surgeries required due to health issues.
                - Medical consultations: Provides coverage for medical consultations with specialists.
                """,
                """
                Individuals and families of all ages (0-120) who are concerned about potential medical expenses due to illness
                or accidents, including those with pre-existing health conditions, self-employed professionals, parents
                seeking coverage for their children, elderly individuals, and anyone who wants to ensure access to
                quality healthcare and financial protection against unexpected medical costs.
                """,
                0, 120, "health"
        ));

        items.add(new Product(
                "PROD_04",
                "Young Adult Travel Insurance",
                """
                A comprehensive travel insurance plan designed for young adults who seek adventure, exploration, and
                peace of mind while traveling. It offers essential protection against unexpected events that may
                occur during domestic or international trips, allowing you to focus on enjoying your journey without worries.
                """,
                """
                - Medical emergencies abroad: Covers medical expenses resulting from illness or accidents during your trip.
                - Trip cancellation or interruption: Provides reimbursement for non-refundable expenses if your trip is canceled or cut short due to covered reasons.
                - Lost or delayed baggage: Compensates for lost, stolen, or significantly delayed luggage.
                - Travel assistance services: Offers 24/7 support for emergencies, including medical evacuation, legal assistance, and travel advice.
                """,
                """
                Young adults aged 20–35, of any gender, who travel for leisure, study, or work and seek reliable
                protection against travel-related risks. Ideal for frequent travelers, digital nomads, students
                studying abroad, or professionals on business trips who value safety, flexibility, and peace of mind
                while exploring the world.
                """,
                20, 35, "travel"
        ));

        items.add(new Product(
                "PROD_05",
                "Pets Insurance",
                "Insurance that covers medical expenses for illnesses or accidents of your loved pet.",
                """
                - Hospitalization: Covers costs of hospitalization due to illness or accident.
                - Surgical procedures: Covers expenses for surgeries required due to health issues.
                - Medical consultations: Provides coverage for medical consultations with specialists.
                """,
                """
                Oriented to people of all ages (0-120), owners of pets such as dogs and cats, who want to provide them with protection against diseases.
                """,
                0, 120, "pet"
        ));

        return items;
    }
}
