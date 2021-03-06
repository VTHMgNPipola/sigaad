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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropriedadesGlobais {
    private static final Logger logger = LoggerFactory.getLogger(PropriedadesGlobais.class);

    public static final String LOGIN_USUARIO = "usuario";
    public static final String SENHA_USUARIO = "usuario.senha";
    public static final String USER_AGENT = "user-agent";
    public static final String ACEITAR_NOVAS_CONEXOES = "conexoes.aceitar-novas";

    private static final Properties PROPERTIES = new Properties();

    public static boolean aceitandoConexoes = true;

    public static void carregar(Path arquivo) throws IOException {
        PROPERTIES.load(Files.newInputStream(arquivo));

        if (PROPERTIES.containsKey(ACEITAR_NOVAS_CONEXOES)) {
            aceitandoConexoes = Boolean.parseBoolean(PROPERTIES.getProperty(ACEITAR_NOVAS_CONEXOES));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                PROPERTIES.store(Files.newBufferedWriter(arquivo), "Arquivo de propriedades do sigaad. " +
                        "NÃO COMPARTILHE ESTE ARQUIVO COM NINGUÉM.");
            } catch (IOException e) {
                logger.error("Não foi possível salvar o arquivo de propriedades!\n{}", e.getMessage());
            }
        }));
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
