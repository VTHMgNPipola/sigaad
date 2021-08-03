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

import com.vthmgnpipola.sigaad.PropriedadesGlobais;
import com.vthmgnpipola.sigaad.data.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.data.respostas.RespostaSimples;

/**
 * Comando usado para finalizar a execução do sigaad. Este comando não termina a execução do sigaad de fato, mas faz
 * com que ele não aceite mais conexões depois que a atual for fechada. Dessa forma, o sigaad vai finalizar sua
 * execução assim que a conexão com o cliente for terminada.
 */
@ComandoNomeado("finalizar")
public class ComandoFinalizar extends Comando<Object, RespostaSimples> {
    @Override
    public RespostaSimples executar() {
        PropriedadesGlobais.aceitandoConexoes = false;
        return new RespostaSimples(EstadoResposta.SUCESSO);
    }
}
