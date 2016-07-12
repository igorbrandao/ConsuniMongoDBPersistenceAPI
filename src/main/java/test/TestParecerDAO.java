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
import dao.Parecer_DAO;
import org.bson.Document;

import java.util.*;

public class TestParecerDAO {

    static private Parecer_DAO parecer_dao = Parecer_DAO.getInstance("local");
    static private MongoCollection<Document> pareceresCollection = DBConnector.createConnection("local").getCollection("pareceres");
    static private MongoCollection<Document> radocsCollection = DBConnector.createConnection("local").getCollection("radocs");
    static private boolean[] testResults = new boolean[9];

    static private Parecer parecer;
    static private Avaliavel relato3;
    static private Nota nota3;
    static private Nota notaNova;
    static private Radoc radoc;

    public static void main(String[] args) {

        setUpTestData();

        testResults[0] = test_persisteParecer();
        testResults[1] = test_byId();
        testResults[2] = test_adicionaNota();
        testResults[3] = test_removeNota();
        testResults[4] = test_atualizaFundamentacao();
        testResults[5] = test_persisteRadoc();
        testResults[6] = test_radocById();
        testResults[7] = test_removeRadoc();
        testResults[8] = test_removeParecer();

        System.out.println("-------------PARECER TEST-------------");

        //*
        System.out.println("test_persisteParecer() -> " + testResults[0] + "\n" +
                "test_byId() -> " + testResults[1] + "\n" +
                "test_adicionaNota() -> " + testResults[2] + "\n" +
                "test_removeNota() -> " + testResults[3] + "\n" +
                "test_atualizaFundamentacao() -> " + testResults[4] + "\n" +
                "test_persisteRadoc() -> " + testResults[5] + "\n" +
                "test_radocById() -> " + testResults[6] + "\n" +
                "test_removeRadoc() -> " + testResults[7] + "\n" +
                "test_removeParecer() -> " + testResults[8] + "\n");
        //*/
        /* Save JSON Object using Gson
        Gson gson = new Gson();
        Type type = new TypeToken<Map<ObjectKey, ObjectValue>>(){}.getType();
        return gson.fromJson(jsonResult, type);
        //*/
    }

    private static void setUpTestData(){

        Map<String, Valor> relato1Map = new HashMap<>();
        relato1Map.put("strValue1", new Valor("string1"));
        relato1Map.put("boolValue1", new Valor(new Random().nextBoolean()));
        relato1Map.put("intValue1", new Valor(new Random().nextInt(3) + 1));
        Map<String, Valor> relato2Map = new HashMap<>();
        relato2Map.put("strValue2", new Valor("string2"));
        relato2Map.put("boolValue2", new Valor(new Random().nextBoolean()));
        relato2Map.put("intValue2", new Valor(new Random().nextInt(3) + 1));
        Map<String, Valor> relato3Map = new HashMap<>();
        relato3Map.put("strValue3", new Valor("string3"));
        relato3Map.put("boolValue3", new Valor(new Random().nextBoolean()));
        relato3Map.put("intValue3", new Valor(new Random().nextInt(3) + 1));
        Map<String, Valor> relatoNovoMap = new HashMap<>();
        relatoNovoMap.put("strValueNovo", new Valor("stringNova"));
        relatoNovoMap.put("boolValueNovo", new Valor(new Random().nextBoolean()));
        relatoNovoMap.put("intValueNovo", new Valor(new Random().nextInt(3) + 1));

        Avaliavel relato1 = new Relato("tipoDoRelato1", relato1Map);
        Avaliavel relato2 = new Relato("tipoDoRelato2", relato2Map);
        relato3 = new Relato("tipoDoRelato3", relato3Map);
        Avaliavel relatoNovo = new Relato("tipoDoRelatoNovo", relato3Map);

        Avaliavel pontuacao1 = new Pontuacao("nomeDaPontuacao1", new Valor("valorDaPontuacao1"));
        Avaliavel pontuacao2 = new Pontuacao("nomeDaPontuacao2", new Valor(true));
        Avaliavel pontuacao3 = new Pontuacao("nomeDaPontuacao3", new Valor(false));
        Avaliavel pontuacaoNova = new Pontuacao("nomeDaPontuacaoNova", new Valor(3));

        Nota nota1 = new Nota(relato1, pontuacao1, "justificativaDaNota1");
        Nota nota2 = new Nota(pontuacao2, relato2, "justificativaDaNota2");
        nota3 = new Nota(relato3, pontuacao3, "justificativaDaNota3");

        notaNova = new Nota(relatoNovo, pontuacaoNova, "justificativaDaNotaNova");

        List<String> radocIds = new ArrayList<>();
        radocIds.add("idDoRadoc1");
        radocIds.add("idDoRadoc2");
        radocIds.add("idDoRadoc3");

        List<Pontuacao> pontuacoes = new ArrayList<>();
        pontuacoes.add(new Pontuacao("pontuacao1", new Valor("valorPontuacao1")));
        pontuacoes.add(new Pontuacao("pontuacao2", new Valor(new Random().nextBoolean())));
        pontuacoes.add(new Pontuacao("pontuacao3", new Valor(new Random().nextInt(3) + 1)));

        List<Nota> notas = new ArrayList<>();
        notas.add(nota1);
        notas.add(nota2);
        notas.add(nota3);

        List<Relato> relatos = new ArrayList<>();
        relatos.add((Relato) relato1);
        relatos.add((Relato) relato2);
        relatos.add((Relato)relato3);

        parecer = new Parecer("idDoParecer", "idDaResolucao", radocIds, pontuacoes, "fundamentacaoDoParecer", notas);

        radoc = new Radoc("idDoRadoc1", 2010, relatos);
    }

    private static boolean test_adicionaNota() {
        parecer_dao.adicionaNota("idDoParecer", notaNova);
        Parecer parecerModificado = parecer;
        parecerModificado.getNotas().add(notaNova);
        return parecerModificado.equals(parecer_dao.byId("idDoParecer"));
    }

    private static boolean test_removeNota() {
        parecer_dao.removeNota("idDoParecer", relato3);
        Parecer parecerModificado = parecer;
        parecerModificado.getNotas().remove(nota3);
        return parecerModificado.equals(parecer_dao.byId("idDoParecer"));
    }

    private static boolean test_persisteParecer() {
        parecer_dao.persisteParecer(parecer);
        return pareceresCollection.find(new Document().append("id", "idDoParecer")) != null;
    }

    private static boolean test_atualizaFundamentacao() {
        parecer_dao.atualizaFundamentacao("idDoParecer", "fundamentacaoNovaDoParecer");
        return "fundamentacaoNovaDoParecer".equals(parecer_dao.byId("idDoParecer").getFundamentacao());
    }

    private static boolean test_byId() {
        return parecer.equals(parecer_dao.byId("idDoParecer"));
    }

    private static boolean test_removeParecer() {
        parecer_dao.removeParecer("idDoParecer");
        return pareceresCollection.find(new Document().append("id", "idDoParecer")) == null;
    }

    private static boolean test_radocById() {
        return radoc.equals(parecer_dao.radocById("idDoRadoc1"));
    }

    private static boolean test_persisteRadoc() {
        parecer_dao.persisteRadoc(radoc);
        return radocsCollection.find(new Document().append("id", "idDoRadoc1")) != null;
    }

    private static boolean test_removeRadoc() {
        parecer_dao.removeRadoc("idDoRadoc1");
        return radocsCollection.find(new Document().append("id", "idDoRadoc1")) == null;
    }

}
