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

package com.vthmgnpipola.sigaad.sigaa;

import com.vthmgnpipola.sigaad.PathHelper;
import com.vthmgnpipola.sigaad.PropriedadesGlobais;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vthmgnpipola.sigaad.PropriedadesGlobais.LOGIN_USUARIO;
import static com.vthmgnpipola.sigaad.PropriedadesGlobais.SENHA_USUARIO;
import static com.vthmgnpipola.sigaad.PropriedadesGlobais.USER_AGENT;
import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Connection.Method.POST;

public final class WebscraperSigaa implements Closeable {
    private static final WebscraperSigaa INSTANCE = new WebscraperSigaa();
    private static final Logger logger = LoggerFactory.getLogger(WebscraperSigaa.class);

    private static final String URL_PAGINA_ERRO = "URL-ERRO";
    private static final String URL_SESSAO_EXPIRADA = "https://sig.ifsc.edu.br/sigaa/expirada.jsp";
    private static final String URL_PAGINA_LOGIN = "https://sigaa.ifsc.edu.br/sigaa/verTelaLogin.do";
    private static final String URL_LOGIN = "https://sigaa.ifsc.edu.br/sigaa/logar.do?dispatch=logOn";
    private static final String URL_DISCENTE = "https://sigaa.ifsc.edu.br/sigaa/portais/discente/discente.jsf";

    private static final String NOME_SESSION_ID = "JSESSIONID";
    private static final String NOME_VIEW_STATE = "javax.faces.ViewState";
    private static final String VALOR_USER_AGENT = PropriedadesGlobais.getProperties().getProperty(USER_AGENT,
            "Mozilla/5.0 (Windows NT 10.0; rv:78.0) Gecko/20100101 Firefox/78.0");

    private static final int ERRO_TENTATIVA_DELAY = Integer.parseInt(PropriedadesGlobais.getProperties()
            .getProperty("sigaa.erro.delay", "1000"));
    private static final int ERRO_TENTATIVAS = Integer.parseInt(PropriedadesGlobais.getProperties()
            .getProperty("sigaa.erro.tentativas", "5"));

    private SessaoSigaa sessaoSigaa;

    private WebscraperSigaa() {
        if (Files.exists(PathHelper.getSessaoFile())) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(PathHelper.getSessaoFile()))) {
                sessaoSigaa = (SessaoSigaa) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Houve um erro tentando ler o arquivo de sessão!\n{}", e.getMessage());
                sessaoSigaa = new SessaoSigaa();
            }
        } else {
            sessaoSigaa = new SessaoSigaa();
        }
    }

    public static WebscraperSigaa getInstance() {
        return INSTANCE;
    }

    /**
     * Constrói uma conexão logada com a URL e método especificados. A conexão é considerada "logada" porque o
     * JSESSIONID e javax.faces.ViewState estão presentes na requisição.
     *
     * @param url URL da requisição.
     * @param method Método da requisição
     * @return Requisição construída, com o JSESSIONID e view state já preenchidos.
     */
    private Connection buildConexao(String url, Connection.Method method) {
        return Jsoup.connect(url).cookie(NOME_SESSION_ID, sessaoSigaa.getSessionId())
                .data(NOME_VIEW_STATE, "j_id" + sessaoSigaa.proximoViewState())
                .userAgent(VALOR_USER_AGENT).method(method);
    }

    /**
     * Executa uma chamada pronta. Caso o resultado retornado seja a página de erro do SIGAA, espera por um tempo
     * pre-determinado pela propriedade {@code sigaa.erro.delay} no arquivo de propriedades (ou o padrão de 1000ms) e
     * executa a chamada novamente. Realiza esse processo uma quantidade de vezes determinada pela propriedade {@code
     * sigaa.erro.tentativas} no arquivo de propriedades (ou o padrão de 5 vezes), e lança
     * {@link SigaaInacessivelException} caso todas as tentativas retornem a página de erro.
     *
     * @return Resultado da chamada sem qualquer processamento, exceto se os únicos resultados forem páginas de erro.
     */
    private Connection.Response tentarChamada(Connection connection) throws SigaaInacessivelException, IOException {
        logger.debug("Executando chamada...");
        Connection.Response response = connection.execute();

        int tentativas = 0;
        while (response.url().toString().equals(URL_PAGINA_ERRO)) {
            logger.debug("Chamada retornou página de erro, tentando novamente...");
            if (tentativas >= ERRO_TENTATIVAS) {
                logger.warn("Número de tentativas para uma chamada excedido, não foi possível acessar o SIGAA!");
                throw new SigaaInacessivelException("Não foi possível acessar o SIGAA, o número de tentativas na " +
                        "página de erro foi atingido.");
            }
            tentativas++;

            try {
                TimeUnit.MILLISECONDS.sleep(ERRO_TENTATIVA_DELAY);
            } catch (InterruptedException e) {
                logger.error("Erro aguardando para refazer requisição depois de receber erro!\n{}", e.getMessage());
                throw new SigaaInacessivelException("Não foi possível acessar o SIGAA, ocorreu um erro ao esperar " +
                        "para realizar uma nova requisição.");
            }

            response = connection.execute();
        }

        return response;
    }

    /**
     * Tenta executar uma chamada logada. Utiliza {@link #tentarChamada(Connection)} para receber uma resposta inicial.
     * Caso a resposta seja a página de sessão expirada tenta fazer o login e refaz a chamada, que é então retornada.
     *
     * @param connection Conexão que será realizada.
     * @return Resposta da requisição, caso o usuário esteja logado ou seja possível loga-lo novamente.
     * @throws IOException Caso ocorra um erro tentando acessar o SIGAA.
     */
    private Connection.Response executarChamada(Connection connection) throws IOException {
        // Inicia a requisição inicial
        Connection.Response response = tentarChamada(connection);

        // Caso a sessão tenha expirada essa é a URL para qual a requisição é redirecionada
        if (response.url().toString().equals(URL_SESSAO_EXPIRADA)) {
            // Tenta logar caso a sessão tenha expirado e os dados do usuário estão salvos
            if (PropriedadesGlobais.getProperties().containsKey(SENHA_USUARIO)) {
                loginAutomatico();
            } else {
                throw new NaoLogadoException("Não é possível acessar essa parte do SIGAA sem estar logado!");
            }
            response = tentarChamada(connection); // Tenta mais uma vez após o login
        }

        sessaoSigaa.setUltimoAcesso(LocalDateTime.now());

        return response;
    }

    public void loginAutomatico() throws IOException {
        login(PropriedadesGlobais.getProperties().getProperty(LOGIN_USUARIO),
                PropriedadesGlobais.getProperties().getProperty(SENHA_USUARIO));
    }

    public void login(String usuario, String senha) throws IOException {
        Map<String, String> dados = new HashMap<>();
        // Dados obrigatórios
        dados.put("width", "1920");
        dados.put("height", "1080");
        dados.put("urlRedirect", "");
        dados.put("subsistemaRedirect", "");
        dados.put("acao", "");
        dados.put("acessibilidade", "");

        // Usuário e senha
        dados.put("user.login", usuario);
        dados.put("user.senha", senha);

        // Consegue o session ID da página de login
        Connection.Response telaLogin = tentarChamada(Jsoup.connect(URL_PAGINA_LOGIN).method(GET));
        String sessionId = telaLogin.cookie(NOME_SESSION_ID);

        // Faz a chamada
        Connection loginConnection = Jsoup.connect(URL_LOGIN)
                .data(dados).cookie(NOME_SESSION_ID, sessionId).method(POST);
        Connection.Response loginResponse = tentarChamada(loginConnection);

        // Verifica se houve sucesso
        if (loginResponse.url().toString().equals(URL_LOGIN)) {
            throw new CredenciaisInvalidasException("As credenciais fornecidas para realizar o login são inválidas!");
        }

        sessaoSigaa.setUltimoAcesso(LocalDateTime.now());
        sessaoSigaa.setUsuario(usuario);
        sessaoSigaa.setSessionId(sessionId);
        sessaoSigaa.reiniciarViewState();
    }

    public Document paginaInicial() throws IOException {
        Connection connection = buildConexao(URL_DISCENTE, GET);
        return executarChamada(connection).parse();
    }

    @Override
    public void close() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(PathHelper.getSessaoFile()))) {
            oos.writeObject(sessaoSigaa);
        }
    }
}
