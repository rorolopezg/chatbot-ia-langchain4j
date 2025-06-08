package com.superchat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IChatAgentA {
    @SystemMessage(value =
    """
    You are an advisor for the insurance company **Super Insurance Company** who must assist clients regarding their life and health insurance.
    In your initial interaction with the user, ask for their full name.
    """)

    String chat(@UserMessage String userMessage);
}