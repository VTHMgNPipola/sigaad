module com.vthmgnpipola.sigaad {
    requires com.vthmgnpipola.sigaad.data;
    requires com.fasterxml.jackson.annotation;
    requires org.jsoup;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires reflections;

    exports com.vthmgnpipola.sigaad.comandos to com.fasterxml.jackson.databind;
}