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
import com.vthmgnpipola.sigaad.payloads.PayloadLogin;
import com.vthmgnpipola.sigaad.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.respostas.RespostaSimples;
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vthmgnpipola.sigaad.PropriedadesGlobais.LOGIN_USUARIO;
import static com.vthmgnpipola.sigaad.PropriedadesGlobais.SENHA_USUARIO;

@ComandoNomeado("logar")
public class ComandoLogar extends Comando<PayloadLogin, RespostaSimples> {
    private static final Logger logger = LoggerFactory.getLogger(ComandoLogar.class);

    private PayloadLogin payloadLogin;

    @Override
    public RespostaSimples executar() {
        EstadoResposta estadoResposta = EstadoResposta.SUCESSO;
        try {
            WebscraperSigaa.getInstance().login(payloadLogin.getUsuario(), payloadLogin.getSenha());
        } catch (Exception e) {
            logger.error("Não foi possível logar no SIGAA!\n{}", e.getMessage());
            estadoResposta = EstadoResposta.FALHA;
        }

        if (payloadLogin.isManterLogado() && estadoResposta == EstadoResposta.SUCESSO) {
            PropriedadesGlobais.getProperties().setProperty(LOGIN_USUARIO, payloadLogin.getUsuario());
            PropriedadesGlobais.getProperties().setProperty(SENHA_USUARIO, payloadLogin.getSenha());
        }

        RespostaSimples resposta = new RespostaSimples();
        resposta.setEstado(estadoResposta);
        return resposta;
    }

    @Override
    public PayloadLogin getDados() {
        return payloadLogin;
    }

    @Override
    public void setDados(PayloadLogin dados) {
        this.payloadLogin = dados;
    }
}