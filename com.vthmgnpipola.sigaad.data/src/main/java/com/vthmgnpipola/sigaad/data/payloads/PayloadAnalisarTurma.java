/*
 * sigaad.data
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

public class PayloadAnalisarTurma {
    @JsonProperty(required = true)
    private int id;

    @JsonProperty(required = true)
    private int ordem;

    private boolean analisarTarefas;
    private boolean analisarQuestionarios;
    private boolean analisarForuns;
    private boolean analisarNoticias;
    private boolean analisarNotas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public boolean isAnalisarTarefas() {
        return analisarTarefas;
    }

    public void setAnalisarTarefas(boolean analisarTarefas) {
        this.analisarTarefas = analisarTarefas;
    }

    public boolean isAnalisarQuestionarios() {
        return analisarQuestionarios;
    }

    public void setAnalisarQuestionarios(boolean analisarQuestionarios) {
        this.analisarQuestionarios = analisarQuestionarios;
    }

    public boolean isAnalisarForuns() {
        return analisarForuns;
    }

    public void setAnalisarForuns(boolean analisarForuns) {
        this.analisarForuns = analisarForuns;
    }

    public boolean isAnalisarNoticias() {
        return analisarNoticias;
    }

    public void setAnalisarNoticias(boolean analisarNoticias) {
        this.analisarNoticias = analisarNoticias;
    }

    public boolean isAnalisarNotas() {
        return analisarNotas;
    }

    public void setAnalisarNotas(boolean analisarNotas) {
        this.analisarNotas = analisarNotas;
    }
}
