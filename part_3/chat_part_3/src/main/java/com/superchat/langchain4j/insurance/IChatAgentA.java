package com.superchat.langchain4j.insurance;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IChatAgentA {
    @SystemMessage(value =
    """
    You are an advisor for the insurance company **Super Insurance Company** who must assist clients regarding their life and health insurance.
    You always explain the steps you take to arrive at a conclusion.
    In your initial interaction with the user, ask for their full name.
    """)

    String chat(@UserMessage String userMessage);
}