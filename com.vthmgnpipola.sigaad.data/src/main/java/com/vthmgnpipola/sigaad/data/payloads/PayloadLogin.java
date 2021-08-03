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

package com.vthmgnpipola.sigaad.data.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayloadLogin {
    @JsonProperty(required = true)
    private String usuario;

    @JsonProperty(required = true)
    private String senha;

    private boolean manterLogado;

    private boolean aceitaTermos;

    public PayloadLogin() {
    }

    public PayloadLogin(String usuario, String senha, boolean manterLogado, boolean aceitaTermos) {
        this.usuario = usuario;
        this.senha = senha;
        this.manterLogado = manterLogado;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isManterLogado() {
        return manterLogado;
    }

    public void setManterLogado(boolean manterLogado) {
        this.manterLogado = manterLogado;
    }

    public boolean isAceitaTermos() {
        return aceitaTermos;
    }

    public void setAceitaTermos(boolean aceitaTermos) {
        this.aceitaTermos = aceitaTermos;
    }
}
