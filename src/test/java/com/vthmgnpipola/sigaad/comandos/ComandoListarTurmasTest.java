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

import com.vthmgnpipola.sigaad.data.model.Turma;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComandoListarTurmasTest {

    @Test
    void testarProcessarAulas() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ComandoListarTurmas.class.getDeclaredMethod("processarAulas", String.class);
        method.setAccessible(true);
        Turma.HorarioAula[] rVazio = (Turma.HorarioAula[]) method.invoke(new ComandoListarTurmas(), "");
        Turma.HorarioAula[] expVazio = new Turma.HorarioAula[] {};

        Turma.HorarioAula[] rAsterisco = (Turma.HorarioAula[]) method.invoke(new ComandoListarTurmas(), "*");
        Turma.HorarioAula[] expAsterisco = new Turma.HorarioAula[] {};

        Turma.HorarioAula[] rSimples = (Turma.HorarioAula[]) method.invoke(new ComandoListarTurmas(), "2T34 4T12 *");
        Turma.HorarioAula[] expSimples = new Turma.HorarioAula[] {
                new Turma.HorarioAula(Turma.Dia.SEGUNDA, 7),
                new Turma.HorarioAula(Turma.Dia.SEGUNDA, 8),
                new Turma.HorarioAula(Turma.Dia.QUARTA, 5),
                new Turma.HorarioAula(Turma.Dia.QUARTA, 6)
        };

        Turma.HorarioAula[] rMultDias = (Turma.HorarioAula[]) method.invoke(new ComandoListarTurmas(), "25T2 *");
        Turma.HorarioAula[] expMultDias = new Turma.HorarioAula[] {
                new Turma.HorarioAula(Turma.Dia.SEGUNDA, 6),
                new Turma.HorarioAula(Turma.Dia.QUINTA, 6)
        };

        Assertions.assertArrayEquals(rVazio, expVazio);
        Assertions.assertArrayEquals(rAsterisco, expAsterisco);
        Assertions.assertArrayEquals(rSimples, expSimples);
        Assertions.assertArrayEquals(rMultDias, expMultDias);
    }
}