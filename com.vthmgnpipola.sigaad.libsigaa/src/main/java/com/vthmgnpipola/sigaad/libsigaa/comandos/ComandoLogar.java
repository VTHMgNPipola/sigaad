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

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vthmgnpipola.sigaad.data.payloads.PayloadLogin;
import com.vthmgnpipola.sigaad.data.respostas.RespostaSimples;
import java.util.function.Consumer;

public class ComandoLogar extends Comando<PayloadLogin, RespostaSimples> {
    public ComandoLogar(String referencia, Consumer<RespostaSimples> callback) {
        super(referencia, "logar", TypeFactory.defaultInstance().constructType(RespostaSimples.class), callback);
    }
}
