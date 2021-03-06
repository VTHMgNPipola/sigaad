/*
 * com.vthmgnpipola.sigaad.libsigaa
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

package com.vthmgnpipola.sigaad.libsigaa.comandos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class Comando<D, T> {
    private static final AtomicInteger INDICE_REFERENCIA = new AtomicInteger();

    @JsonIgnore
    private JavaType tipoResposta;

    @JsonIgnore
    private Consumer<T> callback;

    private String referencia;

    @JsonProperty("comando")
    private String tipo;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private D dados;

    public Comando(String referencia, String tipo, JavaType tipoResposta, Consumer<T> callback) {
        this.referencia = Objects.requireNonNullElseGet(referencia,
                () -> tipo + INDICE_REFERENCIA.getAndIncrement());

        this.tipo = tipo;
        this.tipoResposta = tipoResposta;
        this.callback = callback;
    }

    public JavaType getTipoResposta() {
        return tipoResposta;
    }

    public void setTipoResposta(JavaType tipoResposta) {
        this.tipoResposta = tipoResposta;
    }

    public Consumer<T> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<T> callback) {
        this.callback = callback;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public D getDados() {
        return dados;
    }

    public void setDados(D dados) {
        this.dados = dados;
    }
}
