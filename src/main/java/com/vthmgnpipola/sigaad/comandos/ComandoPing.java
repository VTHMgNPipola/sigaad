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
