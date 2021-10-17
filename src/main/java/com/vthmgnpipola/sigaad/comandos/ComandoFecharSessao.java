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

import com.vthmgnpipola.sigaad.data.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.data.respostas.RespostaSimples;
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComandoNomeado("fecharSessao")
public class ComandoFecharSessao extends Comando<Object, RespostaSimples> {
    private static final Logger logger = LoggerFactory.getLogger(ComandoFecharSessao.class);

    @Override
    public RespostaSimples executar() {
        RespostaSimples resposta = new RespostaSimples(EstadoResposta.SUCESSO);
        try {
            WebscraperSigaa.getInstance().fecharSessao();
        } catch (IOException e) {
            logger.error("Não foi possível fechar a sessão do usuário!\n" + e.getMessage());
            resposta.setEstado(EstadoResposta.FALHA);
        }
        return resposta;
    }
}
