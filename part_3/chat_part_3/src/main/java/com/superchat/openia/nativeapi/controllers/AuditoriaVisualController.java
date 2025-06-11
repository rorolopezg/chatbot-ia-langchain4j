package com.superchat.openia.nativeapi.controllers;


import com.superchat.openia.nativeapi.client.OpenAiVisionClient;
import com.superchat.openia.nativeapi.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria-visual")
public class AuditoriaVisualController {

    private final OpenAiVisionClient openAiClient;

    public AuditoriaVisualController(OpenAiVisionClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Value("${openai.api.key}")
    private String apiKey;

    @PostMapping
    public String auditar(@RequestBody ImagenComparativaRequest body) {
        OpenAiVisionRequest request = new OpenAiVisionRequest();
        request.setModel("gpt-4o");
        request.setTemperature(0.2);

        Message system = new Message();
        system.setRole("system");
        system.setContent(List.of(Content.ofText(
                //"Eres un sistema de auditoría visual. Devuelve un JSON estructurado con diferencias y riesgos visibles."
                """
                Eres un sistema de inspección visual preciso, orientado a seguridad y operaciones en entornos controlados (como restaurantes, áreas de atención al cliente o espacios higiénicos).
                
                 Recibirás tres imágenes:
                 - Las primeras dos representan la situación estándar esperada (correctas) de un entorno o ambiente.
                 - La tercera representa el estado actual (evidencia) del entorno.

                 Tu tarea es:
                 1. Analizar la imágenes estándares (las dos primeras) y hacer una descripción precisa de lo que ves en el entorno, tomando en cuenta lo que ves en ambas imágenes. Esa será la descripción del estándar.
                 2. Analiza la imagen de estado actual (evidencia) y compáralas con las imágenes estándares e identificar las diferencias visibles concretas. Cada elemento que aparece en la imagen de evidencia de situación actual, y que no está en las imágenes estándares, debes describirlo.
                 3. Detectar posibles situaciones de riesgo a partir de las diferencias detectadas.
                 4. Clasificar cada riesgo en una escala de severidad: baja, media o alta.

                 Criterios de severidad:

                 - "alta": riesgos que puedan causar daño físico directo (ej. cuchillos sueltos, vidrios rotos, líquidos derramados en el piso, conexiones eléctricas expuestas).
                 - "media": riesgos que afectan la higiene, el orden o la calidad del servicio (ej. superficies sucias, acumulación de basura, objetos fuera de lugar, utensilios no autorizados).
                 - "baja": observaciones menores o estéticas, sin impacto inmediato (ej. decoración desalineada, sillas giradas, pequeños residuos).

                 Tu respuesta debe estar estrictamente en formato JSON válido, como este:

                 {
                    "estandar": [
                        {
                            "descripcion_detallada_estandar": "string"
                        }
                    ],
                    "diferencias": [
                         {
                           "elemento": "string",
                           "descripcion_evidencia": "string",
                           "tipo_diferencia": "string"
                           "posibles_riesgos": [
                                {
                                    "riesgo": "string",
                                    "descripcion": "string",
                                    "severidad": "baja | media | alta"
                                }
                           ]
                         }
                    ]
                 }
                
                 No agregues texto adicional ni marques el bloque como código.
                 No incluyas texto fuera del JSON.
                 No inventes elementos que no estén visibles en las imágenes.
                 No describas personas ni intentes inferir el contexto más allá de lo visualmente evidente.
                """
        )));

        Message user = new Message();
        user.setRole("user");
        user.setContent(List.of(
                Content.ofText("Primeras dos imágenes: el estándar. Tercera imagen: evidencia actual. Describe solo lo visible. Salida en JSON."),
                Content.ofImageBase64(body.getBase64Estandar1()),
                Content.ofImageBase64(body.getBase64Estandar2() == null ? body.getBase64Estandar1() : body.getBase64Estandar2()),
                Content.ofImageBase64(body.getBase64Evidencia())
        ));

        request.setMessages(List.of(system, user));

        OpenAiVisionResponse response = openAiClient.analizarImagen("Bearer " + apiKey, request);
        return response.getChoices()[0].getMessage().getContent();
    }
}
