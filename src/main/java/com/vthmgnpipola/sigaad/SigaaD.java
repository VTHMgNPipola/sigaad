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
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Path propertiesFile = PathHelper.getPropertiesFile();
        if (args.length > 0) {
            propertiesFile = Path.of(args[0]);
        }
        try {
            if (!Files.exists(propertiesFile)) {
                Files.createDirectories(propertiesFile.getParent());
                Files.createFile(propertiesFile);
            }

            PropriedadesGlobais.carregar(propertiesFile);
            logger.debug("Arquivo de propriedades {} carregado.", propertiesFile);

            if (!Files.exists(PathHelper.getDataFolder())) {
                Files.createDirectories(PathHelper.getDataFolder());
                logger.debug("Pasta de dados criada.");
            }
        } catch (IOException e) {
            logger.error("Houve um erro durante a inicialização!\n{}", e.getMessage());
            System.exit(-1);
        }

        ProcessadorComando.inicializar();

        WebscraperSigaa.getInstance().checarCorrigirSessao();

        ThreadConexaoSocket threadConexaoSocket = new ThreadConexaoSocket();
        threadConexaoSocket.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                threadConexaoSocket.close();
                WebscraperSigaa.getInstance().close();
            } catch (IOException e) {
                logger.error("Não foi possível finalizar a conexão corretamente com o cliente ao fechar!");
            }
        }));
    }
}
