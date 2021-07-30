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

package com.vthmgnpipola.sigaad.libsigaa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vthmgnpipola.sigaad.libsigaa.comandos.Batch;
import com.vthmgnpipola.sigaad.libsigaa.comandos.Comando;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConexaoSigaaD implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ConexaoSigaaD.class);
    private static final ConexaoSigaaD instance = new ConexaoSigaaD();

    private Socket socket;
    private OutputStreamWriter outputStreamWriter;
    private InputStreamReader inputStreamReader;

    private ThreadLeitora threadLeitora;

    private ConexaoSigaaD() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (IOException e) {
                logger.error("Erro finalizando sockets e streams de conexão com o sigaad!");
            }
        }));
    }

    public static ConexaoSigaaD getInstance() {
        return instance;
    }

    public void conectar(String host, int porta) throws IOException {
        if (threadLeitora != null && threadLeitora.isAlive()) {
            threadLeitora.interrupt();
        }

        socket = new Socket(host, porta);
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        inputStreamReader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        threadLeitora = new ThreadLeitora(inputStreamReader);
        threadLeitora.start();
    }

    public OutputStreamWriter getOutputStreamWriter() {
        return outputStreamWriter;
    }

    public InputStreamReader getInputStreamReader() {
        return inputStreamReader;
    }

    public void criarChamada(Batch batch) throws IOException {
        // Transforma a batch de comandos em um JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(batch);

        // Faz a requisição
        outputStreamWriter.write(json);
        outputStreamWriter.write("\r\n\r\n");
        outputStreamWriter.flush();

        // Se a requisição for bem sucedida, inclui os comandos na lista de comandos que estão aguardando resposta
        threadLeitora.adicionarComandos(batch.getComandos().parallelStream()
                .collect(Collectors.toMap(Comando::getReferencia, comando -> comando)));
    }

    @Override
    public void close() throws IOException {
        inputStreamReader.close();
        outputStreamWriter.close();
        socket.close();
    }
}
