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

package com.vthmgnpipola.sigaad.sigaa;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Essa classe representa uma sessão no SIGAA. As requisições de usuários logados no SIGAA precisam de um session ID
 * (o JSESSIONID da requisição) e um view state.
 * A sessão também armazena quando foi a última vez que houve uma requisição ao SIGAA. Outras classes utilizam essa
 * informação para fazer requisições "keep alive" (voltar para a página inicial), a fim de manter o usuário logado
 * enquanto o sigaad estiver rodando (já que o SIGAA do IFSC invalida a sessão do usuário automaticamente após 1 hora
 * e 30 minutos).
 */
public final class SessaoSigaa {
    private static final SessaoSigaa INSTANCE = new SessaoSigaa();

    private String usuario;

    private String sessionId;
    private AtomicInteger viewState;

    private LocalDateTime ultimoAcesso;

    private SessaoSigaa() {
    }

    public static SessaoSigaa getInstance() {
        return INSTANCE;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public AtomicInteger getViewState() {
        return viewState;
    }

    public void setViewState(AtomicInteger viewState) {
        this.viewState = viewState;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
