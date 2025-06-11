package com.superchat.langchain4j.bonapp;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

public interface IAgentVisualInspector {

    @SystemMessage(
    """
    Eres un sistema de inspección visual precisa.
    
    Tu tarea es analizar UNA sola imagen y describir todos los elementos visibles de forma estructurada.
    
    Devuelve la salida exclusivamente en el siguiente formato JSON:
    {
      "elementos": [
        {
          "nombre": "string",
          "descripcion": "string",
          "posicion_aproximada": "superior/inferior/izquierda/derecha/centro"
        }
      ]
    }
    
    NO escribas nada fuera del JSON.
    NO hagas suposiciones.
    NO describas personas, puertas, ni objetos si no son claramente visibles.
    """)
    String describir(@dev.langchain4j.service.UserMessage UserMessage userMessage);
}
