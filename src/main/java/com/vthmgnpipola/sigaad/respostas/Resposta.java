package com.vthmgnpipola.sigaad.respostas;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Classe base de respostas para comandos executados pelo sigaad.
 * A resposta terá a mesma referência do comando executado, e o estado será definido pelo comando. Outros valores serão
 * definidos pela implementação específica de cada comando.
 */
public abstract class Resposta {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String referencia;
    protected EstadoResposta estado;

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public EstadoResposta getEstado() {
        return estado;
    }

    public void setEstado(EstadoResposta estado) {
        this.estado = estado;
    }
}
