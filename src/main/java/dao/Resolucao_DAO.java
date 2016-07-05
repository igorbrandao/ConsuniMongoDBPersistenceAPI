package dao;

import br.ufg.inf.es.saep.sandbox.dominio.*;
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

class Resolucao_DAO implements ResolucaoRepository {

    private static Resolucao_DAO instance = null;
    private final MongoCollection<Document> resolucoesCollection;
    private final MongoCollection<Document> tiposCollection;

    private Resolucao_DAO(String connectionType) {
        this.resolucoesCollection = DBConnector.createConnection(connectionType).getCollection("resolucoes");
        this.tiposCollection = DBConnector.createConnection(connectionType).getCollection("tipos");
    }

    public static void main(String[] args) {
        for (Regra regra : getListaRegras("")) {
            System.out.print(
                    "-----------REGRAS----------" + "\n" +
                    "tipo:" + regra.getTipo() + "\n" +
                    "descricao:" + regra.getDescricao() + "\n" +
                    "tipoRelato:" + regra.getTipoRelato() + "\n" +
                    "expressao:" + regra.getExpressao() + "\n" +
                    "dependeDe:" + buildDependeDeString(regra.getDependeDe()) + "\n" +
                    "pontosPorItem:" + regra.getPontosPorItem() + "\n" +
                    "entao:" + regra.getEntao() + "\n" +
                    "senao0:" + regra.getSenao() + "\n" +
                    "minimo:" + regra.getValorMinimo() + "\n" +
                    "maximo:" + regra.getValorMaximo() + "\n" +
                    "variavel:" + regra.getVariavel());
        }
        for (Atributo atributos : getListaAtributos("")) {
            System.out.print(
                    "-----------ATRIBUTOS----------" + "\n" +
                            "tipo:" + atributos.getTipo() + "\n" +
                            "nome:" + atributos.getNome() + "\n" +
                            "descricao:" + atributos.getDescricao() + "\n");
        }
    }

    public static synchronized Resolucao_DAO getInstance(String connectionType) {
        return instance == null ? new Resolucao_DAO(connectionType) : instance;
    }

    @Override
    public boolean remove(String identificador) {
        return this.delete("id", identificador).wasAcknowledged();
    }

    @Override
    public List<String> resolucoes() {

        List<String> resolucoes = new ArrayList<>();

        for (Document resolucao : this.resolucoesCollection.find()) {
            resolucoes.add(resolucao.toJson());
        }

        return resolucoes.isEmpty() ? null : resolucoes;
    }

    private void save(Resolucao resolucao) {

        Document resolucaoDB = new Document()
                .append("id", resolucao.getId())
                .append("nome", resolucao.getNome())
                .append("descricao", resolucao.getDescricao())
                .append("dataAprovavao", resolucao.getDataAprovacao().toString())
                .append("regras", buildRegrasJSON(resolucao.getRegras()));

        this.resolucoesCollection.insertOne(resolucaoDB);
    }

    private String buildRegrasJSON(List<Regra> regras) {

        String regrasJSON = "{";

        for (int i = 0; i < regras.size(); i++) {

            //*
            regrasJSON +=
                    "{tipo:" + regras.get(i).getTipo() + "," +
                    "descricao:" + regras.get(i).getDescricao() + "," +
                    "tipoRelato:" + regras.get(i).getTipoRelato() + "," +
                    "expressao:" + regras.get(i).getExpressao() + "," +
                    "dependeDe:" + buildDependeDeString(regras.get(i).getDependeDe()) + "," +
                    "pontosPorItem:" + regras.get(i).getPontosPorItem() + "," +
                    "entao:" + regras.get(i).getEntao() + "," +
                    "senao:" + regras.get(i).getSenao() + "," +
                    "minimo:" + regras.get(i).getValorMinimo() + "," +
                    "maximo:" + regras.get(i).getValorMaximo() + "," +
                    "variavel:" + regras.get(i).getVariavel() + "}";
            //*/

            if (i < regras.size() - 1) {
                regrasJSON += ",";
            }

        }

        regrasJSON += "}";

        return regrasJSON;
    }

    private static String buildDependeDeString(List<String> dependeDe) {

        String dependeDeJSON = "";

        for (int i = 0; i < dependeDe.size(); i++) {

            dependeDeJSON += dependeDe.get(i);

            if (i < dependeDe.size() - 1) {
                dependeDeJSON += "=";
            }

        }

        return dependeDeJSON;
    }

    private Resolucao getOne(String chave, Object valor) {

        Document search = this.resolucoesCollection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Resolucao(
                search.getString("id"),
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

    private static List<Regra> getListaRegras(String regrasStr) {

        String mock =
                "{" +
                "tipo:2," +
                "descricao:descricao1," +
                "maximo:9.5," +
                "minimo:3.0," +
                "variavel:variavel1," +
                "expressao:expressao1," +
                "entao:entao1," +
                "senao:senao1," +
                "tipoRelato:tipoRelato1," +
                "pontosPorItem:1," +
                "dependeDe:dependeDe1=dependeDe2=dependeDe3," +
                "}," +
                "{" +
                "tipo:2," +
                "descricao:descricao2," +
                "maximo:8.3," +
                "minimo:4.1," +
                "variavel:variavel2," +
                "expressao:expressao2," +
                "entao:entao2," +
                "senao:senao2," +
                "tipoRelato:tipoRelato2," +
                "pontosPorItem:2," +
                "dependeDe:dependeDe4=dependeDe5=dependeDe6," +
                "}";

        List<Regra> listRegras = new ArrayList<>();
        String[] regras = mock.split("}");//regrasStr.split("}");

        List<String[]> chaves_valores_regras = new ArrayList<>();

        for (String regra : regras) {
            chaves_valores_regras.add(regra.split(","));
        }

        chaves_valores_regras.forEach(chaves_valores_regra ->
                {
                    HashMap<String, String> constructorParams = new HashMap<>();

                    for (String chave_valor_regra : chaves_valores_regra) {
                        String[] entryPair = chave_valor_regra.split(":");
                        constructorParams.put(entryPair[0], entryPair[1]);
                    }

                    listRegras.add(new Regra(
                                    Integer.parseInt(constructorParams.get("{tipo")),
                                    constructorParams.get("descricao"),
                                    Float.parseFloat(constructorParams.get("maximo")),
                                    Float.parseFloat(constructorParams.get("minimo")),
                                    constructorParams.get("variavel"),
                                    constructorParams.get("expressao"),
                                    constructorParams.get("entao"),
                                    constructorParams.get("senao"),
                                    constructorParams.get("tipoRelato"),
                                    Integer.parseInt(constructorParams.get("pontosPorItem")),
                                    getListDependeDe(constructorParams.get("dependeDe"))
                            )
                    );
                }
        );
        return listRegras;
    }

    private static List<String> getListDependeDe(String dependeDeStr) {
        List<String> listDependeDe = new ArrayList<>();

        Collections.addAll(listDependeDe, dependeDeStr.split("="));

        return listDependeDe;
    }

    @Override
    public Resolucao byId(String id) {
        return this.getOne("id", id);
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

    @Override
    public void persisteTipo(Tipo tipo) {
        Document tipoDB = new Document()
                .append("id", tipo.getId())
                .append("nome", tipo.getNome())
                .append("descricao", tipo.getDescricao())
                .append("atributos", buildAtributosJSON(tipo.getAtributos()));

        this.tiposCollection.insertOne(tipoDB);
    }

    private String buildAtributosJSON(Set<Atributo> atributos) {

        String atributosJSON = "{";

        int i = 0;

        for (Atributo atributo : (Atributo[]) atributos.toArray()) {

            atributosJSON +=
                    "{nome:" + atributo.getNome() + "," +
                    "descricao:" + atributo.getDescricao() + "," +
                    "tipo:" + atributo.getTipo() + "}";

            if (i < atributos.toArray().length - 1) {
                atributosJSON += ",";
            }
            i++;
        }

        atributosJSON += "}";

        return atributosJSON;
    }

    @Override
    public void removeTipo(String codigo) {
        this.tiposCollection.deleteOne(new Document("id", codigo));
    }

    @Override
    public Tipo tipoPeloCodigo(String codigo) {

        Document search = this.tiposCollection.find(new Document("id", codigo)).first();

        if (search == null) {
            return null;
        }

        return new Tipo(
                search.getString("id"),
                search.getString("nome"),
                search.getString("descricao"),
                getListaAtributos(search.getString("atributos"))
        );
    }

    @Override
    public List<Tipo> tiposPeloNome(String nome) {

        List<Tipo> tipos = new ArrayList<>();

        for (Document tipo : this.tiposCollection.find(new Document("nome", nome))) {
            tipos.add(new Tipo(
                            tipo.getString("id"),
                            tipo.getString("nome"),
                            tipo.getString("descricao"),
                            getListaAtributos(tipo.getString("atributos"))
                    )
            );
        }

        return tipos.isEmpty() ? null : tipos;
    }

    private static Set<Atributo> getListaAtributos(String atributosStr) {

        String mock =
                "{" +
                "nome:nome1," +
                "descricao:descricao1," +
                "tipo:1" +
                "}," +
                "nome:nome2," +
                "descricao:descricao2," +
                "tipo:2";

        Set<Atributo> setAtributos = new HashSet<>();
        String[] atributos = mock.split("}");//atributosStr.split("}");

        List<String[]> chaves_valores_atributos = new ArrayList<>();

        for (String atributo : atributos) {
            chaves_valores_atributos.add(atributo.split(","));
        }

        chaves_valores_atributos.forEach(chaves_valores_atributo ->
                {
                    HashMap<String, String> constructorParams = new HashMap<>();

                    for (String chave_valor_atributo : chaves_valores_atributo) {
                        String[] entryPair = chave_valor_atributo.split(":");
                        constructorParams.put(entryPair[0], entryPair[1]);
                    }

                    setAtributos.add(new Atributo(
                                    constructorParams.get("{nome"),
                                    constructorParams.get("descricao"),
                                    Integer.parseInt(constructorParams.get("tipo"))
                            )
                    );
                }
        );

        return setAtributos;

    }

}
