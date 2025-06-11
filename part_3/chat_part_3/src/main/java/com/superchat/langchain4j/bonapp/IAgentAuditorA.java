package com.superchat.langchain4j.bonapp;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

public interface IAgentAuditorA {
    @SystemMessage(value =
    """
    Eres un preciso sistema de análisis visual. Tu única función es comparar dos imágenes: una 'estándar' y una 'evidencia'.
    Recibirás dos imágenes. La primera es el estándar, la segunda es la evidencia. Compara visualmente.
    Identifica y describe diferencias VISUALES CONCRETAS entre ellas.
    NO hagas suposiciones ni interpretaciones. SOLO analiza lo visible.
    NO inventes elementos que no están presentes.
    Tu respuesta debe estar en formato JSON con dos secciones: 'diferencias' y 'situaciones_riesgo'.
    No escribas texto fuera del JSON.
    Responde en español.
    """
    )


    String auditar(
            @dev.langchain4j.service.UserMessage UserMessage userMessage
    );
}