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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TopicoForum {
    private String assunto;
    private String autor;
    private String urlAvatarAutor;
    private String mensagem;
    private List<String> arquivos;
    private LocalDateTime dataCriacao;
    private List<TopicoForum> respostas;

    public TopicoForum() {
        arquivos = new ArrayList<>();
        respostas = new ArrayList<>();
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getUrlAvatarAutor() {
        return urlAvatarAutor;
    }

    public void setUrlAvatarAutor(String urlAvatarAutor) {
        this.urlAvatarAutor = urlAvatarAutor;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<String> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<String> arquivos) {
        this.arquivos = arquivos;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public List<TopicoForum> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<TopicoForum> respostas) {
        this.respostas = respostas;
    }
}
