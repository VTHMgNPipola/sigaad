/*
 * sigaad
 * Copyright (C) 2021  VTHMgNPipola
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.vthmgnpipola.sigaad.comandos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Essa é a classe de processamento de comandos.
 * Após ser inicializada, essa classe ficará com um mapa que contém
 * todos os comandos anotados com {@link ComandoNomeado} enquanto {@link #inicializar()} foi chamado.
 * Quando recebe uma requisição para ser processada, o processador "digere" o JSON recebido e o transforma em uma
 * lista de comandos genéricos, que podem então ser executados por um {@link com.vthmgnpipola.sigaad.Executor}.
 *
 * O processo de digestão dos comandos ocorre da seguinte maneira:
 * <ol>
 *     <li>O JSON recebido é dividido em uma lista de {@link JsonNode}s, que (teoricamente) contém um comando</li>
 *     <li>O processamento de cada JsonNode começa, onde o primeiro passo é descobrir o tipo de comando que será
 *     executado. Isso é feito lendo o atributo de texto 'comando' dentro do JsonNode.</li>
 *     <li>Depois disso o JsonNode é passado para um {@link ObjectMapper} decodificar, utilizando como base a
 *     classe do comando, inferida utilizando o nome do comando no JsonNode e no mapa de comandos registrados.</li>
 *     <li>Finalmente a referência do comando é definida, que é simplesmente o nome do JsonNode.</li>
 * </ol>
 */
public class ProcessadorComando {
    private static final Logger logger = LoggerFactory.getLogger(ProcessadorComando.class);
    private static Map<String, Class<? extends Comando<?, ?>>> comandosRegistrados;

    public static void inicializar() {
        logger.info("Detectando comandos registrados...");

        comandosRegistrados = new HashMap<>();

        Reflections reflections = new Reflections("com.vthmgnpipola.sigaad.comandos");
        for (Class<?> tipo : reflections.getTypesAnnotatedWith(ComandoNomeado.class)) {
            if (!Comando.class.isAssignableFrom(tipo)) {
                logger.error("Uma classe anotada com @ComandoNomeado não é um comando!");
                System.exit(1);
            }

            comandosRegistrados.put(tipo.getAnnotation(ComandoNomeado.class).value(), (Class<? extends Comando<?, ?>>) tipo);
        }
    }

    public static List<Comando<?, ?>> processar(String request) {
        List<Comando<?, ?>> comandos = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            for (Iterator<Map.Entry<String, JsonNode>> iterator = objectMapper.readTree(request).fields();
                 iterator.hasNext();) {
                Map.Entry<String, JsonNode> nodeEntry = iterator.next();
                logger.debug("Iniciando processamento do comando '{}'...", nodeEntry.getKey());

                Class<? extends Comando<?, ?>> classComando = comandosRegistrados.get(nodeEntry.getValue()
                        .get("comando").asText());
                if (classComando == null) {
                    logger.error("Comando '{}' não reconhecido!", nodeEntry.getKey());
                    continue;
                }

                logger.trace("Decodificando JSON do comando...");
                Comando<?, ?> comando = objectMapper.treeToValue(nodeEntry.getValue(), classComando);
                logger.trace("Definindo a referência do comando...");
                comando.setReferencia(nodeEntry.getKey());
                comandos.add(comando);
            }
        } catch (Throwable e) {
            logger.error("Houve um erro processando a requisição do cliente!\n{}", e.getMessage());
            return null;
        }

        return comandos;
    }
}
