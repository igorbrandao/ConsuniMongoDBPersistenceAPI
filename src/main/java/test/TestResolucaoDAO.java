package test;

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

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.mongodb.client.MongoCollection;
import dao.DBConnector;
import dao.Resolucao_DAO;
import org.bson.Document;

import java.util.*;

public class TestResolucaoDAO {

    static private Resolucao_DAO resolucao_dao = Resolucao_DAO.getInstance("local");
    static private MongoCollection<Document> resolucoesCollection = DBConnector.createConnection("local").getCollection("resolucoes");
    static private MongoCollection<Document> tiposCollection = DBConnector.createConnection("local").getCollection("tipos");
    static private boolean[] testResults = new boolean[8];

    private static Resolucao resolucao1;
    private static Resolucao resolucao2;
    private static Tipo tipo1;
    private static Tipo tipo2;

    public static void main(String[] args) {

        setUpTestData();

        testResults[0] = test_persiste();
        resolucao_dao.persiste(resolucao2);
        testResults[1] = test_byId();
        testResults[2] = test_resolucoes();
        testResults[3] = test_remove();
        testResults[4] = test_persisteTipo();
        resolucao_dao.persisteTipo(tipo2);
        testResults[5] = test_tipoPeloCodigo();
        testResults[6] = test_tiposPeloNome();
        testResults[7] = test_removeTipo();

        System.out.println("-------------RESOLUCAO TEST-------------");

        //*
        System.out.println("test_persiste() -> " + testResults[0] + "\n" +
                "test_byId() -> " + testResults[1] + "\n" +
                "test_resolucoes() -> " + testResults[2] + "\n" +
                "test_remove() -> " + testResults[3] + "\n" +
                "test_persisteTipo() -> " + testResults[4] + "\n" +
                "test_tipoPeloCodigo() -> " + testResults[5] + "\n" +
                "test_tiposPeloNome() -> " + testResults[6] + "\n" +
                "test_removeTipo() -> " + testResults[7] + "\n");
        //*/
        /* Save JSON Object using Gson
        Gson gson = new Gson();
        Type type = new TypeToken<Map<ObjectKey, ObjectValue>>(){}.getType();
        return gson.fromJson(jsonResult, type);
        //*/
    }

    private static void setUpTestData() {

        long currentTime = System.currentTimeMillis();

        List<String> dependeDe1 = new ArrayList<>();
        dependeDe1.add("dependeDe11");
        dependeDe1.add("dependeDe12");

        List<String> dependeDe2 = new ArrayList<>();
        dependeDe1.add("dependeDe21");
        dependeDe1.add("dependeDe22");

        List<String> dependeDe3 = new ArrayList<>();
        dependeDe1.add("dependeDe31");
        dependeDe1.add("dependeDe32");

        Regra regra1 = new Regra(1, "decricao1", 8.2f, 2.1f, "variavel1", "expressao1", "entao1", "senao1", "tipoRelato1", 2.5f, dependeDe1);
        Regra regra2 = new Regra(2, "decricao2", 6.2f, 2.2f, "variavel2", "expressao2", "entao2", "senao2", "tipoRelato2", 3.2f, dependeDe2);
        Regra regra3 = new Regra(3, "decricao3", 6.3f, 3.3f, "variavel3", "expressao3", "entao3", "senao3", "tipoRelato3", 3.3f, dependeDe3);

        List<Regra> regras = new ArrayList<>();
        regras.add(regra1);
        regras.add(regra2);
        regras.add(regra3);

        resolucao1 = new Resolucao("idDaResolucao1", "nomeDaResolucao1", "descricaoDaResolucao1", new Date(currentTime), regras);
        resolucao2 = new Resolucao("idDaResolucao2", "nomeDaResolucao2", "descricaoDaResolucao2", new Date(currentTime), regras);

        Set<Atributo> atributos = new HashSet<>();
        atributos.add(new Atributo("nomeDoAtributo1", "descricaoDoAtributo1", 1));
        atributos.add(new Atributo("nomeDoAtributo2", "descricaoDoAtributo2", 2));
        atributos.add(new Atributo("nomeDoAtributo3", "descricaoDoAtributo3", 3));

        tipo1 = new Tipo("idDoTipo1", "nomeDoTipo1", "descricaoDoTipo1", atributos);
        tipo2 = new Tipo("idDoTipo2", "nomeDoTipo2", "descricaoDoTipo2", atributos);

    }

    private static boolean test_byId(){
        return resolucao1.equals(resolucao_dao.byId("idDaResolucao1"));
    }

    private static boolean test_persiste(){
        resolucao_dao.persiste(resolucao1);
        return resolucoesCollection.find(new Document().append("id", "idDaResolucao1")) != null;
    }

    private static boolean test_remove(){
        resolucao_dao.remove("idDaResolucao1");
        return resolucoesCollection.find(new Document().append("id", "idDaResolucao1")) == null;
    }

    private static boolean test_resolucoes(){}

    private static boolean test_persisteTipo(){
        resolucao_dao.persisteTipo(tipo1);
        return tiposCollection.find(new Document().append("id", "idDoTipo1")) != null;
    }

    private static boolean test_removeTipo(){
        resolucao_dao.removeTipo("idDoTipo1");
        return tiposCollection.find(new Document().append("id", "idDoTipo1")) == null;
    }

    private static boolean test_tipoPeloCodigo(){
        return tipo1.equals(resolucao_dao.tipoPeloCodigo("idDoTipo1"));
    }

    private static boolean test_tiposPeloNome(){}

}
