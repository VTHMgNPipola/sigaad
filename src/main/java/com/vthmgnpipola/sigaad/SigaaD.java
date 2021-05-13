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

import com.vthmgnpipola.sigaad.comandos.ProcessadorComando;
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

        ProcessadorComando.inicializar();

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
