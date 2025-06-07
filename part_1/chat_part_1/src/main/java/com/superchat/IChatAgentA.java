package com.superchat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IChatAgentA {
    @SystemMessage(value = """
    Eres un asesor de la compañía de seguros **Super Insurance Company** que debe asesporar a los clientes sobre sus seguros de vida y salud.
    """)

    String chat(@UserMessage String userMessage);
}