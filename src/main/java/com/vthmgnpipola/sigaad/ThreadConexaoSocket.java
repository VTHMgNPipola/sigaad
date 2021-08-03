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

package com.vthmgnpipola.sigaad;

import com.vthmgnpipola.sigaad.comandos.Comando;
import com.vthmgnpipola.sigaad.comandos.ProcessadorComando;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Essa é a thread que realiza a conexão com o cliente que estará acessando o SIGAA programaticamente.
 * Os dados são transmitidos e recebidos em formato de texto UTF8, portanto qualquer linguagem de programação deve
 * conseguir utilizar o sigaad com a sua própria implementação de sockets. A porta padrão em que o sigaad estará
 * escutando é a 51327.
 * Cada requisição deve terminar com {@code \r\n\r\n}, então o socket do sigaad ficará esperando por esse padrão
 * antes de fazer qualquer coisa.
 */
public class ThreadConexaoSocket extends Thread implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ThreadConexaoSocket.class);

    public static final String PORTA_PADRAO = "51327";

    private ServerSocket serverSocket;
    private Socket socket;
    private OutputStreamWriter outputStreamWriter;

    public ThreadConexaoSocket() {
        super("ConexaoSocket");

        try {
            serverSocket = new ServerSocket(
                    Integer.parseInt(PropriedadesGlobais.getProperties()
                            .getProperty("socket.porta", PORTA_PADRAO)));
        } catch (IOException e) {
            logger.error("Erro inicializando o ServerSocket!\n{}", e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        while (PropriedadesGlobais.aceitandoConexoes) {
            try {
                logger.info("Aceitando conexões na porta {}...", serverSocket.getLocalPort());
                socket = serverSocket.accept();
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                logger.info("Conexão com {} iniciada com sucesso!", socket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                logger.error("Erro aceitando conexão!\n{}", e.getMessage());
                System.exit(-1);
            }

            StringBuilder request = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)) {
                int i;
                while ((i = isr.read()) != -1) { // Um EOF durante .read() é representado por um -1
                    request.append((char) i);

                    // Caso reconheça a terminação da requisição
                    if (request.toString().endsWith("\r\n\r\n")) {
                        logger.info("Requisição recebida, processando dados...");
                        List<Comando<?, ?>> comandos = ProcessadorComando.processar(request.toString());
                        request = new StringBuilder();

                        // Executa os comandos processados
                        Executor executor = new Executor(comandos, outputStreamWriter);
                        executor.start();
                    }
                }

                logger.info("Conexão com o cliente finalizada (não há mais nada para ser lido).");
            } catch (IOException e) {
                logger.error("Erro lendo dados do cliente!\n{}", e.getMessage());
                System.exit(-1);
            }
        }
    }

    @Override
    public void close() throws IOException {
        // Fecha o socket e output stream writer somente se uma conexão tiver sido realizada
        if (socket != null) {
            outputStreamWriter.close();
            socket.close();
        }
        serverSocket.close();
    }
}
