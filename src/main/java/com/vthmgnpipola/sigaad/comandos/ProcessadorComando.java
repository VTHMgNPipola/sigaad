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
import com.fasterxml.jackson.databind.node.ArrayNode;
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
 * todos os comandos anotados com {@link ComandoNomeado} quando {@link #inicializar()} foi chamado.
 * Quando recebe uma requisição para ser processada, o processador "digere" o JSON recebido e o transforma em uma
 * lista de comandos genéricos, que podem então ser executados por um {@link com.vthmgnpipola.sigaad.Executor}.
 * <p>
 * O processo de digestão dos comandos ocorre da seguinte maneira:
 * <ol>
 *     <li>O JSON recebido é processado em um {@link JsonNode}s, que (teoricamente) contém os comandos e quaisquer
 *     outros atributos;</li>
 *     <li>O node "comandos" é extraído do node base processado anteriormente, e este deve ser obrigatoriamente um
 *     array (caso contrário o processo é interrompido);</li>
 *     <li>O método itera sobre todos os itens dentro do array, processando cada comando da seguinte maneira:</li>
 *     <ol>
 *         <li>Extrai a referência do comando, contida no campo de texto "referencia";</li>
 *         <li>Descobre o tipo do comando através do valor do campo de texto "comando";</li>
 *         <li>Caso o tipo esteja cadastrado na lista de comandos deste processador, cria uma instância desse
 *         comando;</li>
 *         <li>Cria a payload do comando com base nos campos restantes.</li>
 *     </ol>
 * </ol>
 */
public class ProcessadorComando {
    private static final Logger logger = LoggerFactory.getLogger(ProcessadorComando.class);
    private static Map<String, Class<? extends Comando<?, ?>>> comandosRegistrados;

    /**
     * Registra todos os comandos contidos no pacote {@code com.vthmgnpipola.sigaad.comandos}.
     * Para fazer isso todos os tipos anotados com {@link ComandoNomeado} são listados e, caso sejam subclasses de
     * {@link Comando}, são adicionadas a lista de comandos registrados. Caso um tipo anotado com {@code ComandoNomeado}
     * não seja uma subclasse de {@code Comando}, um erro é lançado e a JVM é terminada.
     *
     * @see ComandoNomeado
     * @see Comando
     */
    @SuppressWarnings("unchecked")
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
            logger.debug("Comando {} registrado com sucesso!", tipo.getSimpleName());
        }
    }

    /**
     * Processa uma string que contém uma request bruta, obrigatoriamente em JSON, em uma lista de comandos.
     * O processo utilizado está descrito no javadoc da classe, mas basicamente um JSON contendo um "batch" de comandos
     * é processado e transformado em uma lista que contém todos os comandos e seus dados.
     *
     * @param request JSON bruto recebido do cliente para ser processado.
     * @return Lista contendo todos os comandos processados.
     */
    public static List<Comando<?, ?>> processar(String request) {
        List<Comando<?, ?>> comandos = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode baseNode = objectMapper.readTree(request);
            JsonNode comandosNodeRaw = baseNode.get("comandos");

            if (!comandosNodeRaw.isArray()) {
                logger.warn("Requisição recebida não é um array de comandos!");
                return null;
            }
            ArrayNode comandosNode = (ArrayNode) comandosNodeRaw;

            for (Iterator<JsonNode> iterator = comandosNode.elements();
                 iterator.hasNext(); ) {
                JsonNode node = iterator.next();
                String referencia = node.get("referencia").asText();
                logger.debug("Iniciando processamento do comando '{}'...", referencia);

                Class<? extends Comando<?, ?>> classComando = comandosRegistrados.get(node.get("comando").asText());
                if (classComando == null) {
                    logger.error("Comando '{}' não reconhecido!", referencia);
                    continue;
                }

                logger.trace("Decodificando JSON do comando...");
                Comando<?, ?> comando = objectMapper.treeToValue(node, classComando);
                logger.trace("Definindo a referência do comando...");
                comando.setReferencia(referencia);
                comandos.add(comando);
            }
        } catch (Throwable e) {
            logger.error("Houve um erro processando a requisição do cliente!\n{}", e.getMessage());
            return null;
        }

        return comandos;
    }
}
