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
import com.vthmgnpipola.sigaad.data.payloads.PayloadLogin;
import com.vthmgnpipola.sigaad.data.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.data.respostas.RespostaSimples;
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vthmgnpipola.sigaad.PropriedadesGlobais.LOGIN_USUARIO;
import static com.vthmgnpipola.sigaad.PropriedadesGlobais.SENHA_USUARIO;

@ComandoNomeado("logar")
public class ComandoLogar extends Comando<PayloadLogin, RespostaSimples> {
    private static final Logger logger = LoggerFactory.getLogger(ComandoLogar.class);

    @Override
    public RespostaSimples executar() {
        if (dados.isManterLogado() && !dados.isAceitaTermos()) {
            logger.info("Usuário tentou logar e salvar seus dados, mas não aceitou os termos de uso.");
            return new RespostaSimples(EstadoResposta.FALHA);
        }

        EstadoResposta estadoResposta = EstadoResposta.SUCESSO;
        try {
            WebscraperSigaa.getInstance().login(dados.getUsuario(), dados.getSenha());
        } catch (Exception e) {
            logger.error("Não foi possível logar no SIGAA!\n{}", e.getMessage());
            estadoResposta = EstadoResposta.FALHA;
        }

        if (dados.isManterLogado() && dados.isAceitaTermos() && estadoResposta == EstadoResposta.SUCESSO) {
            PropriedadesGlobais.getProperties().setProperty(LOGIN_USUARIO, dados.getUsuario());
            PropriedadesGlobais.getProperties().setProperty(SENHA_USUARIO, dados.getSenha());
            WebscraperSigaa.getInstance().atualizarTimerRelogin();
        }

        return new RespostaSimples(estadoResposta);
    }
}
