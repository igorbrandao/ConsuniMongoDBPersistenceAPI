package dao;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The MIT License
 *
 * Copyright 2015 Igor.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Created by Igor on 6/11/2016.
 *
 */

public class Resolucao_DAO implements ResolucaoRepository {

    private static Resolucao_DAO instance = null;
    private final MongoCollection<Document> resolucoesCollection;
    private final Tipo_DAO tipoDAOInstance;

    private Resolucao_DAO(String connectionType) {
        this.resolucoesCollection = DBConnector.createConnection(connectionType).getCollection("resolucoes");
        this.tipoDAOInstance = Tipo_DAO.getInstance(connectionType);
    }

    public static synchronized Resolucao_DAO getInstance(String connectionType) {
        return instance == null ? new Resolucao_DAO(connectionType) : instance;
    }

    @Override
    public void persisteTipo(Tipo tipo) {
        this.tipoDAOInstance.save(tipo);
    }

    @Override
    public void removeTipo(String idTipo) {

        Tipo tipoASerRemovido = tipoDAOInstance.getOne("id", idTipo);
        // Não há usos de Tipo em Resolução, então lançar uma ResolucaoUsaTipoException não se aplica.

        this.tipoDAOInstance.delete("id", idTipo);
    }

    @Override
    public Tipo tipoPeloCodigo(String codigo) {
        return this.tipoDAOInstance.getOne("id", codigo);
    }

    @Override
    public List<Tipo> tiposPeloNome(String nome) {
        return this.tipoDAOInstance.getListByName("nome", nome);
    }

    @Override
    public boolean remove(String id) {
        return this.delete("id", id).wasAcknowledged();
    }

    @Override
    public Resolucao byId(String id) {

        if (id == null || id.trim().isEmpty()){
            throw new CampoExigidoNaoFornecido("O campo id não foi informado ou veio em branco");
        }

        return this.getOne("id", id);
    }

    @Override
    public List<String> resolucoes() {

        List<String> resolucoes = new ArrayList<>();

        for (Document resolucao : this.resolucoesCollection.find()) {
            resolucoes.add(resolucao.toJson());
        }

        return resolucoes.isEmpty() ? null : resolucoes;
    }

    @Override
    public String persiste(Resolucao resolucao) {

        String thisId = null;
        if (this.getOne("id", resolucao.getId()) == null) {
            this.save(resolucao);
            thisId = resolucao.getId();
        }
        return thisId;

    }

    private void save(Resolucao resolucao) {

        Document resolucaoDB = new Document()
                .append("id", resolucao.getId())
                .append("nome", resolucao.getNome())
                .append("descricao", resolucao.getDescricao())
                .append("dataAprovacao", resolucao.getDataAprovacao().toString())
                .append("regras", buildRegrasJSON(resolucao.getRegras()));

        this.resolucoesCollection.insertOne(resolucaoDB);
    }

    private String buildRegrasJSON(List<Regra> regras) {

        String regrasJSON = "[";

        for (int i = 0; i < regras.size(); i++) {

            //*
            regrasJSON += "{\"tipo\":\"" + regras.get(i).getTipo() + "\"," +
                            "\"descricao\":\"" + regras.get(i).getDescricao() + ",\"" +
                            "\"tipoRelato\":\"" + regras.get(i).getTipoRelato() + "\"," +
                            "\"expressao\":\"" + regras.get(i).getExpressao() + "\"," +
                            "\"dependeDe\":" + buildDependeDeString(regras.get(i).getDependeDe()) + "," +
                            "\"pontosPorItem\":\"" + regras.get(i).getPontosPorItem() + "\"," +
                            "\"entao\":\"" + regras.get(i).getEntao() + "\"," +
                            "\"senao\":\"" + regras.get(i).getSenao() + "\"," +
                            "\"minimo\":\"" + regras.get(i).getValorMinimo() + "\"," +
                            "\"maximo\":\"" + regras.get(i).getValorMaximo() + "\"," +
                            "\"variavel\":\"" + regras.get(i).getVariavel() + "\"}";
            //*/

            if (i < regras.size() - 1) {
                regrasJSON += ",";
            }

        }

        regrasJSON += "]";

        return regrasJSON;
    }

    private String buildDependeDeString(List<String> dependeDe) {

        String dependeDeJSON = "[";

        for (int i = 0; i < dependeDe.size(); i++) {

            dependeDeJSON += "\"" + dependeDe.get(i) + "\"";

            if (i < dependeDe.size() - 1) {
                dependeDeJSON += ",";
            }

        }

        dependeDeJSON += "]";

        return dependeDeJSON;
    }

    private Resolucao getOne(String chave, Object valor) {

        Document search = this.resolucoesCollection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Resolucao(
                search.getString("id"),
                search.getString("nome"),
                search.getString("descricao"),
                getDateFromString(search.getString("dataAprovacao")),
                getListaRegras(search.getString("regras"))
        );
    }

    private Date getDateFromString(String dataAprovacao) {
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(dataAprovacao);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Regra> getListaRegras(String regrasStr) {

        String mock = "[" +
                "{" +
                "\"tipo\":\"2\"," +
                "\"descricao\":\"descricao1\"," +
                "\"maximo\":\"9.5\"," +
                "\"minimo\":\"3.0\"," +
                "\"variavel\":\"variavel1\"," +
                "\"expressao\":\"expressao1\"," +
                "\"entao\":\"entao1\"," +
                "\"senao\":\"senao1\"," +
                "\"tipoRelato\":\"tipoRelato1\"," +
                "\"pontosPorItem\":\"1\"," +
                "\"dependeDe\":[\"dependeDe1,dependeDe2,dependeDe3\"]" +
                "}," +
                "{" +
                "\"tipo\":\"2\"," +
                "\"descricao\":\"descricao2\"," +
                "\"maximo\":\"8.3\"," +
                "\"minimo\":\"4.1\"," +
                "\"variavel\":\"variavel2\"," +
                "\"expressao\":\"expressao2\"," +
                "\"entao\":\"entao2\"," +
                "\"senao\":\"senao2\"," +
                "\"tipoRelato\":\"tipoRelato2\"," +
                "\"pontosPorItem\":\"2\"," +
                "\"dependeDe\":\"dependeDe4=dependeDe5=dependeDe6\"" +
                "}" +
                "]";

        List<Regra> listRegras = new ArrayList<>();
        List<String[]> chaves_valores_regras = new ArrayList<>();
        String[] regras = regrasStr.split("}");//mock.split("}");

        for (int i = 0; i < regras.length - 1; i++) {
            regras[i] = regras[i].substring(2);
            chaves_valores_regras.add(regras[i].split(","));
        }

        chaves_valores_regras.forEach(chaves_valores_regra ->
                {
                    HashMap<String, String> constructorParams = new HashMap<>();

                    for (String chave_valor_regra : chaves_valores_regra) {
                        String[] entryPair = chave_valor_regra.split(":");
                        constructorParams.put(
                                entryPair[0].substring(1, entryPair[0].length() - 1),
                                entryPair[1].substring(1, entryPair[1].length() - 1)
                        );
                    }

                    listRegras.add(
                            new Regra(
                                    Integer.parseInt(constructorParams.get("tipo")),
                                    constructorParams.get("descricao"),
                                    Float.parseFloat(constructorParams.get("maximo")),
                                    Float.parseFloat(constructorParams.get("minimo")),
                                    constructorParams.get("variavel"),
                                    constructorParams.get("expressao"),
                                    constructorParams.get("entao"),
                                    constructorParams.get("senao"),
                                    constructorParams.get("tipoRelato"),
                                    Float.parseFloat(constructorParams.get("pontosPorItem")),
                                    getListDependeDe(constructorParams.get("dependeDe")))
                    );
                }
        );
        return listRegras;
    }

    private static List<String> getListDependeDe(String dependeDeStr) {
        List<String> listDependeDe = new ArrayList<>();

        Collections.addAll(listDependeDe, dependeDeStr.split(","));

        return listDependeDe;
    }

    private DeleteResult delete(String chave, Object valor) {
        return this.resolucoesCollection.deleteOne(new Document(chave, valor));
    }

    private void update(String chave, Object valor, Resolucao resolucao) {

        Document resolucaoDB = new Document()
                .append("id", resolucao.getId())
                .append("nome", resolucao.getNome())
                .append("descricao", resolucao.getDescricao())
                .append("dataAprovavao", resolucao.getDataAprovacao().toString())
                .append("regras", buildRegrasJSON(resolucao.getRegras()));

        this.resolucoesCollection.updateOne(new Document(chave, valor), new Document("$set", resolucaoDB));
    }

}
