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

package com.vthmgnpipola.sigaad.data.respostas;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Classe base de respostas para comandos executados pelo sigaad.
 * A resposta terá a mesma referência do comando executado, e o estado será definido pelo comando. Outros valores serão
 * definidos pela implementação específica de cada comando.
 */
public abstract class Resposta {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String referencia;
    protected EstadoResposta estado;

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public EstadoResposta getEstado() {
        return estado;
    }

    public void setEstado(EstadoResposta estado) {
        this.estado = estado;
    }
}
