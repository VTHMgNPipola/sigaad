package com.vthmgnpipola.sigaad;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sigaad: a biblioteca de acesso programático ao SIGAA do IFSC.
 * Essa biblioteca é propriedade da Grande e Poderosa URCEL.
 *
 * @author VTHMgNPipola
 */
public class SigaaD {
    private static final Logger logger = LoggerFactory.getLogger(SigaaD.class);

    public static void main(String[] args) {
        logger.info("Iniciando sigaad...");
        logger.info("Leia o arquivo de especificações para saber os possíveis argumentos de inicialização.");

        for (String arg : args) {
            if (arg.matches("props=.*")) {
                String arquivoPropriedades = arg.split("=")[1];
                try {
                    PropriedadesGlobais.carregar(arquivoPropriedades);
                } catch (IOException e) {
                    logger.error("Não foi possível ler o arquivo de propriedades especificado!\n{}", e.getMessage());
                    System.exit(-1);
                }

                logger.info("Arquivo de propriedades ({}) carregado com sucesso.", arquivoPropriedades);
            }
        }

        ThreadConexaoSocket threadConexaoSocket = new ThreadConexaoSocket();
        threadConexaoSocket.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                threadConexaoSocket.close();
            } catch (IOException e) {
                logger.error("Não foi possível finalizar a conexão corretamente com o cliente ao fechar!");
            }
        }));
    }
}
