package com.vthmgnpipola.sigaad.comandos;

import com.vthmgnpipola.sigaad.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.respostas.RespostaSimples;

/**
 * Comando simples utilizado para testar a conexão com o sigaad. O comando não faz nada e retorna imediatamente uma
 * resposta de sucesso.
 */
@ComandoNomeado("ping")
public class ComandoPing extends Comando<Object, RespostaSimples> {
    @Override
    public RespostaSimples executar() {
        RespostaSimples resposta = new RespostaSimples();
        resposta.setReferencia(referencia);
        resposta.setEstado(EstadoResposta.SUCESSO);
        return resposta;
    }

    @Override
    public Object getDados() {
        return null;
    }

    @Override
    public void setDados(Object dados) {
    }
}
