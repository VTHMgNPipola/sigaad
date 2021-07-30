module com.vthmgnpipola.sigaad.data {
    requires com.fasterxml.jackson.annotation;

    exports com.vthmgnpipola.sigaad.data.model;
    exports com.vthmgnpipola.sigaad.data.payloads;
    exports com.vthmgnpipola.sigaad.data.respostas;

    opens com.vthmgnpipola.sigaad.data.model to com.fasterxml.jackson.databind;
    opens com.vthmgnpipola.sigaad.data.payloads to com.fasterxml.jackson.databind;
    opens com.vthmgnpipola.sigaad.data.respostas to com.fasterxml.jackson.databind;
}