/*
 * com.vthmgnpipola.sigaad.libsigaa
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

package com.vthmgnpipola.sigaad.libsigaa.conexao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vthmgnpipola.sigaad.libsigaa.comandos.Comando;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLeitora extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ThreadLeitora.class);

    private final InputStreamReader isr;
    private final Map<String, Comando<?, ?>> comandosAguardando;

    public ThreadLeitora(InputStreamReader isr) {
        this.isr = isr;
        comandosAguardando = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        logger.info("Começando a ler InputStream...");

        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder resposta = new StringBuilder();
        try {
            int i;
            while ((i = isr.read()) != -1) {
                resposta.append((char) i);
                String respostaAtual = resposta.toString();
                logger.trace("Informação recebida, buffer atual: {}", respostaAtual);

                // Terminação de requisições/respostas do sigaad
                if (respostaAtual.endsWith("\r\n\r\n")) {
                    JsonNode jsonNode = objectMapper.readTree(respostaAtual);
                    resposta = new StringBuilder();

                    Comando<?, ?> comando = comandosAguardando.remove(jsonNode.get("referencia").asText());
                    if (comando == null) {
                        logger.warn("Resposta de um comando não enviado recebida!");
                        continue;
                    }

                    if (comando.getCallback() != null) {
                        comando.getCallback().accept(objectMapper.readValue(respostaAtual, comando.getTipoResposta()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void adicionarComandos(Map<String, Comando<?, ?>> comandosAguardando) {
        this.comandosAguardando.putAll(comandosAguardando);
    }
}
