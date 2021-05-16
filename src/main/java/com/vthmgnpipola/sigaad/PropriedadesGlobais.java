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

public class PropriedadesGlobais {
    public static final String LOGIN_USUARIO = "usuario";
    public static final String SENHA_USUARIO = "usuario.senha";
    public static final String USER_AGENT = "user-agent";
    private static final Properties PROPERTIES = new Properties();

    public static void carregar(Path arquivo) throws IOException {
        PROPERTIES.load(Files.newInputStream(arquivo));
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
