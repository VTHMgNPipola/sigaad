package com.vthmgnpipola.sigaad;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vthmgnpipola.sigaad.comandos.Comando;
import com.vthmgnpipola.sigaad.comandos.ComandoNomeado;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Essa é a thread que realiza a conexão com o cliente que estará acessando o SIGAA programaticamente.
 * Os dados são transmitidos e recebidos em formato de texto UTF8, portanto qualquer linguagem de programação deve
 * conseguir utilizar o sigaad com a sua própria implementação de sockets. A porta padrão em que o sigaad estará
 * escutando é a 51327.
 */
public class ThreadConexaoSocket extends Thread implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ThreadConexaoSocket.class);
    private static Map<String, Class<? extends Comando<?, ?>>> comandosRegistrados;

    private ServerSocket serverSocket;
    private Socket socket;
    private OutputStreamWriter outputStreamWriter;

    public ThreadConexaoSocket() {
        super("ConexaoSocket");

        try {
            serverSocket = new ServerSocket(
                    Integer.parseInt(PropriedadesGlobais.getProperties()
                            .getProperty("socket.porta", "51327")));
        } catch (IOException e) {
            logger.error("Erro inicializando o ServerSocket!\n{}", e.getMessage());
            System.exit(-1);
        }

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

    @Override
    public void run() {
        try {
            logger.info("Aceitando conexões na porta {}...", serverSocket.getLocalPort());
            socket = serverSocket.accept();
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            logger.info("Conexão com {} iniciada com sucesso!", socket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            logger.error("Erro aceitando conexão!\n{}", e.getMessage());
            System.exit(-1);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                logger.info("Instruções recebidas, decodificando dados...");

                List<Comando<?, ?>> comandos = new ArrayList<>();

                boolean erro = false;
                for (Iterator<Map.Entry<String, JsonNode>> it = objectMapper.readTree(line).fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> nodeEntry = it.next();

                    Class<? extends Comando<?, ?>> tipoComando = comandosRegistrados.get(nodeEntry.getKey());
                    if (tipoComando == null) {
                        logger.error("Comando {} não reconhecido!", nodeEntry.getKey());
                        // TODO: Enviar erro ao cliente
                        erro = true;
                        break;
                    }
                    Comando<?, ?> comando = objectMapper.treeToValue(nodeEntry.getValue(), tipoComando);

                    comandos.add(comando);
                }

                if (!erro) {
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

    @Override
    public void close() throws IOException {
        outputStreamWriter.close();
        socket.close();
        serverSocket.close();
    }
}
