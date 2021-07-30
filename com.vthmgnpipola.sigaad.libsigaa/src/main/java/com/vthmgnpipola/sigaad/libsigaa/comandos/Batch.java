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

import java.util.ArrayList;
import java.util.List;

public class Batch {
    private List<Comando<?>> comandos;

    public Batch() {
        comandos = new ArrayList<>();
    }

    public List<Comando<?>> getComandos() {
        return comandos;
    }

    public void setComandos(List<Comando<?>> comandos) {
        this.comandos = comandos;
    }

    public void adicionarComando(Comando<?> comando) {
        comandos.add(comando);
    }
}
