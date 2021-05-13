package com.vthmgnpipola.sigaad.comandos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe base para os comandos disponíveis no sigaad.
 * Cada comando enviado para o sigaad tem uma referência, que é o identificador do objeto no JSON, utilizada para
 * identificar o comando ao enviar a resposta para o cliente.
 *
 * @param <D> Tipo do objeto que conterá os dados ao ser recebido pelo sigaad.
 * @param <R> Tipo do objeto que conterá os resultados que serão enviados pelo sigaad.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Simplifica o processo de converter os dados do JSON com a tag 'comando'
public abstract class Comando<D, R> {
    protected String referencia;

    public abstract R executar();

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public abstract D getDados();

    // Não tenho certeza se isso é necessário, mas como quero que o Jackson reconheça "dados" como uma propriedade
    // sem existir uma variável para ele nessa classe coloquei essa anotação.
    @JsonProperty("dados")
    public abstract void setDados(D dados);
}
