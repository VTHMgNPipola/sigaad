package com.vthmgnpipola.sigaad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropriedadesGlobais {
    private static final Properties PROPERTIES = new Properties();

    public static void carregar(String arquivo) throws IOException {
        PROPERTIES.load(Files.newInputStream(Paths.get(arquivo)));
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
