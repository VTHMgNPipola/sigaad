module com.vthmgnpipola.sigaad.libsigaa {
    requires com.vthmgnpipola.sigaad.data;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;

    exports com.vthmgnpipola.sigaad.libsigaa.comandos;
    exports com.vthmgnpipola.sigaad.libsigaa.conexao;
}
