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

import java.nio.file.Path;
import java.util.Objects;

public final class PathHelper {
    public static Path getPropertiesFile() {
        Path userHome = Path.of(System.getProperty("user.home"));
        return switch (Objects.requireNonNull(getRunningOperatingSystem())) {
            case WINDOWS -> userHome.resolve("AppData/sigaad/sigaad.properties");
            case MACOS -> userHome.resolve("Library/Application Support/sigaad/sigaad.properties");
            case UNIX -> userHome.resolve(".config/sigaad/sigaad.properties");
        };
    }

    public static Path getDataFolder() {
        Path userHome = Path.of(System.getProperty("user.home"));
        return switch (Objects.requireNonNull(getRunningOperatingSystem())) {
            case WINDOWS -> userHome.resolve("AppData/sigaad/");
            case MACOS -> userHome.resolve("Library/Application Support/sigaad/");
            case UNIX -> userHome.resolve(".var/app/com.vthmgnpipola.sigaad/");
        };
    }

    public static Path getSessaoFile() {
        return getDataFolder().resolve("sessao.obj");
    }

    public static OperatingSystem getRunningOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MACOS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix") || osName.contains("bsd")) {
            return OperatingSystem.UNIX;
        } else {
            return null;
        }
    }

    public enum OperatingSystem {
        WINDOWS, MACOS, UNIX
    }
}
