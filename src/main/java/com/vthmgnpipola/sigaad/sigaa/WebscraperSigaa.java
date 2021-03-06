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
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vthmgnpipola.sigaad.PropriedadesGlobais.LOGIN_USUARIO;
import static com.vthmgnpipola.sigaad.PropriedadesGlobais.SENHA_USUARIO;
import static com.vthmgnpipola.sigaad.PropriedadesGlobais.USER_AGENT;

public final class WebscraperSigaa implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(WebscraperSigaa.class);
    private static final WebscraperSigaa INSTANCE = new WebscraperSigaa();

    private static final String URL_PAGINA_ERRO = "https://sig.ifsc.edu.br/sigaa/verPortalDiscente.do?";
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

    private static final int TIMEOUT = Integer.parseInt(PropriedadesGlobais.getProperties()
            .getProperty("sigaa.timeout", "60000")); // Timeout em milissegundos

    private SessaoSigaa sessaoSigaa;

    private Timer timer;
    private ReloginTimerTask reloginTimerTask;

    private WebscraperSigaa() {
        if (Files.exists(PathHelper.getSessaoFile())) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(PathHelper.getSessaoFile()))) {
                sessaoSigaa = (SessaoSigaa) ois.readObject();
                logger.info("Sess??o encontrada e carregada com sucesso. ??ltimo acesso em {}.",
                        sessaoSigaa.getUltimoAcesso().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Houve um erro tentando ler o arquivo de sess??o!\n{}", e.getMessage());
                sessaoSigaa = new SessaoSigaa();
            }
        } else {
            logger.info("N??o existe uma sess??o salva previamente, criando uma nova...");
            sessaoSigaa = new SessaoSigaa();
        }

        timer = new Timer(true);
        reloginTimerTask = new ReloginTimerTask();
    }

    public static WebscraperSigaa getInstance() {
        return INSTANCE;
    }

    /**
     * Constr??i uma conex??o logada com a URL e m??todo POST. A conex??o ?? considerada "logada" porque o
     * JSESSIONID e javax.faces.ViewState est??o presentes na requisi????o.
     *
     * @param url URL da requisi????o.
     * @return Requisi????o constru??da, com o JSESSIONID e view state j?? preenchidos.
     */
    private Connection buildPostRequest(String url) {
        return Jsoup.connect(url).timeout(TIMEOUT).cookie(NOME_SESSION_ID, sessaoSigaa.getSessionId())
                .data(NOME_VIEW_STATE, "j_id" + sessaoSigaa.proximoViewState())
                .userAgent(VALOR_USER_AGENT).method(Connection.Method.POST);
    }

    /**
     * Constr??i uma conex??o logada com a URL e m??todo GET. A conex??o ?? considerada "logada" porque o
     * JSESSIONID est?? presente na requisi????o.
     *
     * @param url URL da requisi????o.
     * @return Requisi????o constru??da, com o JSESSIONID preenchido.
     */
    private Connection buildGetRequest(String url) {
        return Jsoup.connect(url).timeout(TIMEOUT).cookie(NOME_SESSION_ID, sessaoSigaa.getSessionId())
                .userAgent(VALOR_USER_AGENT).method(Connection.Method.GET);
    }

    /**
     * Executa uma chamada pronta. Caso o resultado retornado seja a p??gina de erro do SIGAA, espera por um tempo
     * pre-determinado pela propriedade {@code sigaa.erro.delay} no arquivo de propriedades (ou o padr??o de 1000ms) e
     * executa a chamada novamente. Realiza esse processo uma quantidade de vezes determinada pela propriedade {@code
     * sigaa.erro.tentativas} no arquivo de propriedades (ou o padr??o de 5 vezes), e lan??a
     * {@link SigaaInacessivelException} caso todas as tentativas retornem a p??gina de erro.
     *
     * @return Resultado da chamada sem qualquer processamento, exceto se os ??nicos resultados forem p??ginas de erro.
     */
    private Connection.Response tentarChamada(Connection connection) throws SigaaInacessivelException, IOException {
        logger.debug("Executando chamada...");
        Connection.Response response = connection.execute();

        int tentativas = 0;
        while (response.url().toString().equals(URL_PAGINA_ERRO)) {
            logger.debug("Chamada retornou p??gina de erro, tentando novamente...");
            if (tentativas >= ERRO_TENTATIVAS) {
                logger.warn("N??mero de tentativas para uma chamada excedido, n??o foi poss??vel acessar o SIGAA!");
                throw new SigaaInacessivelException("N??o foi poss??vel acessar o SIGAA, o n??mero de tentativas na " +
                        "p??gina de erro foi atingido.");
            }
            tentativas++;

            try {
                TimeUnit.MILLISECONDS.sleep(ERRO_TENTATIVA_DELAY);
            } catch (InterruptedException e) {
                logger.error("Erro aguardando para refazer requisi????o depois de receber erro!\n{}", e.getMessage());
                throw new SigaaInacessivelException("N??o foi poss??vel acessar o SIGAA, ocorreu um erro ao esperar " +
                        "para realizar uma nova requisi????o.");
            }

            response = connection.execute();
        }

        return response;
    }

    /**
     * Tenta executar uma chamada logada. Utiliza {@link #tentarChamada(Connection)} para receber uma resposta inicial.
     * Caso a resposta seja a p??gina de sess??o expirada tenta fazer o login e refaz a chamada, que ?? ent??o retornada.
     *
     * @param connection Conex??o que ser?? realizada.
     * @return Resposta da requisi????o, caso o usu??rio esteja logado ou seja poss??vel loga-lo novamente.
     * @throws IOException Caso ocorra um erro tentando acessar o SIGAA.
     */
    private Connection.Response executarChamada(Connection connection) throws IOException {
        // Inicia a requisi????o inicial
        Connection.Response response = tentarChamada(connection);

        // Caso a sess??o tenha expirada essa ?? a URL para qual a requisi????o ?? redirecionada
        if (response.url().toString().equals(URL_SESSAO_EXPIRADA)) {
            // Tenta logar caso a sess??o tenha expirado e os dados do usu??rio est??o salvos
            if (PropriedadesGlobais.getProperties().containsKey(SENHA_USUARIO)) {
                loginAutomatico();
            } else {
                throw new NaoLogadoException("N??o ?? poss??vel acessar essa parte do SIGAA sem estar logado!");
            }
            response = tentarChamada(connection); // Tenta mais uma vez ap??s o login
        }

        sessaoSigaa.setUltimoAcesso(LocalDateTime.now());
        atualizarTimerRelogin();

        return response;
    }

    public void loginAutomatico() throws IOException {
        login(PropriedadesGlobais.getProperties().getProperty(LOGIN_USUARIO),
                PropriedadesGlobais.getProperties().getProperty(SENHA_USUARIO));
    }

    public void login(String usuario, String senha) throws IOException {
        login(usuario, senha, false);
    }

    public void login(String usuario, String senha, boolean forcarLogin) throws IOException {
        if (sessaoSigaa.isValida() && !forcarLogin) {
            logger.info("Login n??o iniciado, a sess??o ainda ?? v??lida.");
            return;
        }

        Map<String, String> dados = new HashMap<>();
        // Dados obrigat??rios
        dados.put("width", "1920");
        dados.put("height", "1080");
        dados.put("urlRedirect", "");
        dados.put("subsistemaRedirect", "");
        dados.put("acao", "");
        dados.put("acessibilidade", "");

        // Usu??rio e senha
        dados.put("user.login", usuario);
        dados.put("user.senha", senha);

        // Consegue o session ID da p??gina de login
        Connection.Response telaLogin = tentarChamada(Jsoup.connect(URL_PAGINA_LOGIN).method(Connection.Method.GET));
        String sessionId = telaLogin.cookie(NOME_SESSION_ID);

        // Faz a chamada
        Connection loginConnection = Jsoup.connect(URL_LOGIN)
                .data(dados).cookie(NOME_SESSION_ID, sessionId).method(Connection.Method.POST);
        Connection.Response loginResponse = tentarChamada(loginConnection);

        // Verifica se houve sucesso
        if (loginResponse.url().toString().equals(URL_LOGIN)) {
            throw new CredenciaisInvalidasException("As credenciais fornecidas para realizar o login s??o inv??lidas!");
        }

        sessaoSigaa.setUltimoAcesso(LocalDateTime.now());
        sessaoSigaa.setUsuario(usuario);
        sessaoSigaa.setSessionId(sessionId);
        sessaoSigaa.reiniciarViewState();
        atualizarTimerRelogin();

        logger.info("Login realizado com sucesso.");
    }

    public void atualizarTimerRelogin() {
        reloginTimerTask.cancel();
        timer.purge();

        reloginTimerTask = new ReloginTimerTask();

        long delay = Duration.between(LocalDateTime.now(), sessaoSigaa.getUltimoAcesso().plusMinutes(80)).toMillis();
        delay = Math.max(0, delay);
        final long period = 4_800_000; // 80 minutos * 60 segundos * 1.000 milisegundos
        timer.scheduleAtFixedRate(reloginTimerTask, delay, period);
    }

    public void fecharSessao() throws IOException {
        logger.info("Removendo arquivo de sess??o e informa????es de login...");
        sessaoSigaa = new SessaoSigaa();
        PropriedadesGlobais.getProperties().remove(SENHA_USUARIO);
        PropriedadesGlobais.getProperties().remove(LOGIN_USUARIO);
        Files.deleteIfExists(PathHelper.getSessaoFile());
    }

    public void checarCorrigirSessao() {
        if (!sessaoSigaa.isValida() && PropriedadesGlobais.getProperties().containsKey(LOGIN_USUARIO)
                && PropriedadesGlobais.getProperties().containsKey(SENHA_USUARIO)) {
            try {
                logger.info("A sess??o n??o ?? v??lida mas um usu??rio e senha foram encontrados. Tentando login...");
                loginAutomatico();
                atualizarTimerRelogin();
            } catch (IOException e) {
                logger.error("Houve um erro tentando logar automaticamente no SIGAA!\n{}", e.getMessage());
            }
        }
    }

    public String getUsuarioLogado() {
        return sessaoSigaa.getUsuario();
    }

    public Document paginaInicial() throws IOException {
        Connection connection = buildGetRequest(URL_DISCENTE);
        return executarChamada(connection).parse();
    }

    public Document dashboardTurma(int id, int ordem) throws IOException {
        Connection connection = buildPostRequest(URL_DISCENTE);

        String ordemStr = ordem != 0 ? "j_id_" + ordem : "";
        String nomeForm = "form_acessarTurmaVirtual" + ordemStr;
        connection.data(nomeForm, nomeForm);

        connection.data("idTurma", "" + id);

        connection.data(nomeForm + ":turmaVirtual", nomeForm + ":turmaVirtual");

        return executarChamada(connection).parse();
    }

    @Override
    public void close() throws IOException {
        if (sessaoSigaa.getSessionId() != null) {
            Path sessaoFile = PathHelper.getSessaoFile();
            if (!Files.exists(sessaoFile)) {
                Files.createFile(sessaoFile);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(sessaoFile))) {
                oos.writeObject(sessaoSigaa);
            }
        } else {
            Files.deleteIfExists(PathHelper.getSessaoFile());
        }
    }

    private static class ReloginTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                WebscraperSigaa.getInstance().login(PropriedadesGlobais.getProperties().getProperty(LOGIN_USUARIO),
                        PropriedadesGlobais.getProperties().getProperty(SENHA_USUARIO), true);
                logger.info("Sess??o do SIGAA atualizada com sucesso.");
            } catch (IOException e) {
                logger.error("Houve um erro tentando refazer o login no SIGAA!\n{}", e.getMessage());
            }
        }
    }
}
