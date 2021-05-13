package com.vthmgnpipola.sigaad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vthmgnpipola.sigaad.comandos.Comando;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Essa é a thread que executa os comandos recebidos pela thread de conexão. A lista de comandos a serem executados e
 * o {@link OutputStreamWriter} (usado para enviar os resultados dos comandos ao cliente) são passados como argumento
 * na hora da instanciação do executor.
 *
 * O executor não fica executando constantemente, com a thread de conexão, mas é instanciado e iniciado no momento em
 * que a thread de execução recebe um batch de comandos e os decodifica corretamente. Após terminar a execução dos
 * comandos o executor para.
 */
public class Executor extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final List<Comando<?, ?>> comandos;
    private final OutputStreamWriter outputStreamWriter;

    public Executor(List<Comando<?, ?>> comandos, OutputStreamWriter outputStreamWriter) {
        super("Executor");

        this.comandos = comandos;
        this.outputStreamWriter = outputStreamWriter;
    }

    @Override
    public void run() {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Comando<?, ?> comando : comandos) {
            logger.info("Executando comando {} (ref. {})...", comando.getClass().getName(), comando.getReferencia());
            Object resultado = comando.executar();

            try {
                objectMapper.writeValue(outputStreamWriter, resultado);
            } catch (IOException e) {
                logger.error("Não foi possível escrever o resultado do comando com referência '{}' para o cliente!\n{}",
                        comando.getReferencia(), e.getMessage());
            }
        }
    }
}
