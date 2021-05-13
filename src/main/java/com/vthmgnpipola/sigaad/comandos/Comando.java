package com.vthmgnpipola.sigaad.comandos;

/**
 * Classe base para os comandos disponíveis no sigaad.
 * Cada comando enviado para o sigaad terá opcionalmente uma referência, que pode ser utilizada pelo cliente para
 * identificar o comando.
 *
 * @param <D> Tipo do objeto que conterá os dados ao ser recebido pelo sigaad.
 * @param <R> Tipo do objeto que conterá os resultados que serão enviados pelo sigaad.
 */
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

    public abstract void setDados(D dados);
}
