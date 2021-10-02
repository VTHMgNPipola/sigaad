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
import com.vthmgnpipola.sigaad.data.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.data.respostas.RespostaTurmas;
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Este é o comando utilizado para listar todas as turmas nas quais o usuário está atualmente cadastrado.
 * <p>
 * Para fazer isso, retorna o webscraper para a página inicial do SIGAA (a "página discente") e analisa as
 * informações presentes no componente {@code <table style="margin-top: 1%;">} dentro de
 * {@code <div id="turmas-portal">}.
 */
@ComandoNomeado("listarTurmas")
public class ComandoListarTurmas extends Comando<Object, RespostaTurmas> {
    private static final Logger logger = LoggerFactory.getLogger(ComandoListarTurmas.class);

    @Override
    public RespostaTurmas executar() {
        Document pagina;
        try {
            pagina = WebscraperSigaa.getInstance().paginaInicial();
        } catch (Exception e) {
            logger.warn("Ocorreu um erro ao ler a página do discente do SIGAA!\n{}", e.getMessage());
            RespostaTurmas resposta = new RespostaTurmas();
            resposta.setEstado(EstadoResposta.FALHA);
            return resposta;
        }

        Element tbody = pagina.getElementById("turmas-portal").selectFirst("table[style*=\"margin-top: 1%;\"]")
                .selectFirst("tbody");
        List<Turma> turmas = processar(tbody);

        RespostaTurmas resposta = new RespostaTurmas();
        resposta.setEstado(EstadoResposta.SUCESSO);
        resposta.setTurmas(turmas);
        return resposta;
    }

    private List<Turma> processar(Element tbody) {
        List<Turma> turmas = new ArrayList<>();

        for (Element tr : tbody.select("tr[class]")) {
            Turma turma = new Turma();

            Element tdDescricao = tr.selectFirst("td[class=descricao]");
            Element form = tdDescricao.selectFirst("form");

            // Ordem
            String ordemStr = form.attr("id").replaceAll("\\D+", "");
            if (!ordemStr.isBlank()) {
                turma.setOrdem(Integer.parseInt(ordemStr));
            }

            // ID
            String idStr = form.selectFirst("input[name=idTurma]").attr("value");
            turma.setId(Integer.parseInt(idStr));

            // Nome
            String nome = form.selectFirst("a").text();
            turma.setNome(nome);

            // Local
            Elements infos = tr.select("td[class=info]");
            Element tdLocal = infos.first();
            String local = tdLocal.text();
            if (!local.isBlank()) {
                turma.setLocal(local);
            }

            // Horários
            Element centerAulas = infos.last().selectFirst("center");
            List<Turma.HorarioAula> horarioAulas = processarAulas(centerAulas.text());
            turma.setHorarioAulas(horarioAulas);

            turmas.add(turma);
        }

        return turmas;
    }

    private List<Turma.HorarioAula> processarAulas(String str) {
        // Por algum motivo começaram a colocar '*' nos horários do SIGAA, então tenho que filtrá-las aqui.
        List<String> horariosStr = Arrays.stream(str.split("\s+"))
                .filter(s -> !s.equals("*")).collect(Collectors.toList());
        List<Turma.HorarioAula> aulas = new ArrayList<>();

        for (String horario : horariosStr) {
            boolean isAula = false;
            int offset = 0;
            List<Turma.Dia> dias = new ArrayList<>();
            for (char c : horario.toCharArray()) {
                boolean numero = false;
                switch (c) {
                    case 'M' -> {
                        offset = 0;
                        isAula = true;
                    }
                    case 'T' -> {
                        offset = 4;
                        isAula = true;
                    }
                    case 'N' -> { // Supõe-se que N seja usado para as aulas noturnas
                        offset = 8;
                        isAula = true;
                    }
                    default -> numero = true;
                }

                if (numero) {
                    if (!isAula) {
                        dias.add(Turma.Dia.values()[Character.getNumericValue(c) - 1]);
                    } else {
                        int aula = Character.getNumericValue(c) + offset;
                        for (Turma.Dia dia : dias) {
                            aulas.add(new Turma.HorarioAula(dia, aula));
                        }
                    }
                }
            }
        }

        return aulas;
    }
}
