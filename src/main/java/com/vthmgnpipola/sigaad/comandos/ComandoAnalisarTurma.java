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
import com.vthmgnpipola.sigaad.data.payloads.PayloadAnalisarTurma;
import com.vthmgnpipola.sigaad.data.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.data.respostas.RespostaTurmas;
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import java.io.IOException;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComandoNomeado("analisarTurma")
public class ComandoAnalisarTurma extends Comando<PayloadAnalisarTurma, RespostaTurmas> {
    private static final Logger logger = LoggerFactory.getLogger(ComandoAnalisarTurma.class);

    @Override
    public RespostaTurmas executar() {
        WebscraperSigaa webscraper = WebscraperSigaa.getInstance();

        try {
            webscraper.paginaInicial(); // Homing para a página inicial
        } catch (IOException e) {
            logger.warn("Ocorreu um erro ao ler a página do discente do SIGAA!\n{}", e.getMessage());
            RespostaTurmas resposta = new RespostaTurmas();
            resposta.setEstado(EstadoResposta.FALHA);
            return resposta;
        }

        Document paginaTurma;
        try {
            paginaTurma = webscraper.dashboardTurma(dados.getId(), dados.getOrdem());
        } catch (IOException e) {
            logger.warn("Ocorreu um erro ao ler a página de turma do SIGAA!\n{}", e.getMessage());
            RespostaTurmas resposta = new RespostaTurmas();
            resposta.setEstado(EstadoResposta.FALHA);
            return resposta;
        }

        Turma turma = processar(paginaTurma);
        turma.setId(dados.getId());
        turma.setOrdem(dados.getOrdem());

        RespostaTurmas respostaTurmas = new RespostaTurmas();
        respostaTurmas.setEstado(EstadoResposta.SUCESSO);
        respostaTurmas.setTurmas(List.of(turma));
        return respostaTurmas;
    }

    private Turma processar(Document pagina) {
        Turma turma = new Turma();

        Element pCodigo = pagina.getElementById("linkCodigoTurma");
        String codigo = pCodigo.text().split("\s+")[0];
        turma.setCodigo(codigo);

        Element pNome = pagina.getElementById("linkNomeTurma");
        turma.setNome(pNome.text());

        return turma;
    }
}
