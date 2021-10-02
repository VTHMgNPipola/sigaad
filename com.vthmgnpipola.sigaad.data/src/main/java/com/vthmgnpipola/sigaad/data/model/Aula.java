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

package com.vthmgnpipola.sigaad.data.model;

import java.util.ArrayList;
import java.util.List;

public class Aula {
    private List<TopicoAula> topicos;
    private int ordem;

    public Aula() {
        topicos = new ArrayList<>();
    }

    public List<TopicoAula> getTopicos() {
        return topicos;
    }

    public void setTopicos(List<TopicoAula> topicos) {
        this.topicos = topicos;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
