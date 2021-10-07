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

import com.vthmgnpipola.sigaad.data.model.Aula;
import com.vthmgnpipola.sigaad.data.model.Material;
import com.vthmgnpipola.sigaad.data.model.TipoMaterial;
import com.vthmgnpipola.sigaad.data.model.TopicoAula;
import com.vthmgnpipola.sigaad.data.model.Turma;
import com.vthmgnpipola.sigaad.data.payloads.PayloadAnalisarTurma;
import com.vthmgnpipola.sigaad.data.respostas.EstadoResposta;
import com.vthmgnpipola.sigaad.data.respostas.RespostaTurmas;
import com.vthmgnpipola.sigaad.sigaa.WebscraperSigaa;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComandoNomeado("analisarTurma")
public class ComandoAnalisarTurma extends Comando<PayloadAnalisarTurma, RespostaTurmas> {
    private static final Logger logger = LoggerFactory.getLogger(ComandoAnalisarTurma.class);

    private static final String patternDatasStringTotal = "\\((\\d{2}/\\d{2}/\\d{4}) - (\\d{2}/\\d{2}/\\d{4})\\)";
    private static final String patternDataString = "(\\d{2}/\\d{2}/\\d{4})";
    private static final Pattern patternDatas = Pattern.compile(patternDataString);
    private static final DateTimeFormatter formatterDatas = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String patternOrdemAulasString = ":(\\d+):";
    private static final Pattern patternOrdemAulas = Pattern.compile(patternOrdemAulasString);

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

        Elements spanAulas = pagina.select("span[id^=formAva:j_id_jsp_][id$=aulas]");
        for (Element spanAula : spanAulas) {
            Aula aula = new Aula();

            Matcher matcherAulaId = patternOrdemAulas.matcher(spanAula.id());
            boolean matcherAulaIdFound = matcherAulaId.find();
            assert matcherAulaIdFound;
            String aulaOrdemStr = matcherAulaId.group();
            aula.setOrdem(Integer.parseInt(aulaOrdemStr.substring(1, aulaOrdemStr.length() - 1)));

            Elements divTopicos = spanAula.select("div[id$=topico_aula]");
            for (Element divTopico : divTopicos) {
                TopicoAula topicoAula = new TopicoAula();

                String divTituloText = divTopico.select("div[id$=titulo]").first().text();

                Matcher matcherTitulo = patternDatas.matcher(divTituloText);
                boolean dataInicialMatcherFound = matcherTitulo.find();
                assert dataInicialMatcherFound;
                LocalDate dataInicial = LocalDate.parse(matcherTitulo.group(), formatterDatas);
                topicoAula.setDataInicial(dataInicial);

                boolean dataFinalMatcherFound = matcherTitulo.find();
                assert dataFinalMatcherFound;
                LocalDate dataFinal = LocalDate.parse(matcherTitulo.group(), formatterDatas);
                topicoAula.setDataFinal(dataFinal);

                String titulo = divTituloText.replaceAll(patternDatasStringTotal, "").trim();
                topicoAula.setTitulo(titulo);

                Elements spanMateriais = divTopico.select("span[id$=dndPanel]");
                for (Element spanMaterial : spanMateriais) {
                    Material material = new Material();

                    material.setTitulo(spanMaterial.select("a").first().text());

                    TipoMaterial tipoMaterial;
                    String srcMaterial = spanMaterial.select("img").first().attr("src");
                    srcMaterial = srcMaterial.substring(srcMaterial.lastIndexOf("/") + 1);
                    switch (srcMaterial) { // TODO: Checar se os nomes estão certos
                        case "pdf.png" -> tipoMaterial = TipoMaterial.PDF;
                        case "doc.png" -> tipoMaterial = TipoMaterial.DOC;
                        case "ppt.png" -> tipoMaterial = TipoMaterial.PPT;
                        case "zip.png" -> tipoMaterial = TipoMaterial.ZIP;
                        case "video.png" -> tipoMaterial = TipoMaterial.VIDEO;
                        case "conteudo.png" -> tipoMaterial = TipoMaterial.CONTEUDO;
                        case "site_add.png" -> tipoMaterial = TipoMaterial.WEBSITE;
                        case "forumava.png" -> tipoMaterial = TipoMaterial.FORUM;
                        case "tarefa.png" -> tipoMaterial = TipoMaterial.TAREFA;
                        default -> tipoMaterial = TipoMaterial.DESCONHECIDO;
                    }
                    material.setTipo(tipoMaterial);

                    topicoAula.getMateriais().add(material);
                }

                aula.getTopicos().add(topicoAula);
            }

            turma.getAulas().add(aula);
        }

        return turma;
    }
}
