package com.superchat.langchain4j.bonapp;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.SystemMessage;

public interface IAgentAuditorA2 {
    @SystemMessage(value =
    """
    Eres un sistema de inspección visual de precisión.
    
    Analiza una sola imagen compuesta en dos partes:
    - MITAD IZQUIERDA: representa el entorno estándar.
    - MITAD DERECHA: representa la evidencia actual.
    
    Debes identificar SOLO diferencias observables entre ambas mitades.
    
    NO inventes objetos, personas, puertas o elementos no visibles claramente.
    NO realices suposiciones.
    NO describas diferencias de luz, color o clima salvo que impliquen riesgo directo.
    
    Entrega tu respuesta en el siguiente formato estricto:
    {
      "diferencias": [
        {
          "elemento": "string",
          "descripcion_estandar": "string",
          "descripcion_evidencia": "string",
          "tipo_diferencia": "string"
        }
      ],
      "situaciones_riesgo": [
        {
          "riesgo": "string",
          "descripcion": "string"
        }
      ]
    }
    NO incluyas texto fuera del JSON.
    """
    )


    String auditar(
            @dev.langchain4j.service.UserMessage UserMessage userMessage
    );
}