package dao;

import br.ufg.inf.es.saep.sandbox.dominio.Radoc;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.omg.CORBA.Object;

import java.lang.reflect.Type;
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

class Radoc_DAO {

    private final MongoCollection<Document> radocsCollection;
    private static Radoc_DAO instance = null;

    private Radoc_DAO(String connectionType) {
        this.radocsCollection = DBConnector.createConnection(connectionType).getCollection("radocs");
    }

    static synchronized Radoc_DAO getInstance(String connectionType) {
        return instance == null ? new Radoc_DAO(connectionType) : instance;
    }

    public static void main(String[] args) {

        List<Relato> relatos = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            Map<String, Valor> currentMap = new HashMap<>();
            currentMap.put("chaveDoValorString".concat(Integer.toString(i)), new Valor("stringValue".concat(Integer.toString(i))));
            currentMap.put("chaveDoValorInteger".concat(Integer.toString(i + 1)), new Valor(i + 1));
            currentMap.put("chaveDoValorBoolean".concat(Integer.toString(i + 2)), new Valor(new Random().nextBoolean()));
            relatos.add(new Relato("tipo".concat(Integer.toString(i)), currentMap));
        }

        String notasStr = "{" +
                "\"avaliavelClass\":\"br.ufg.inf.es.saep.sandbox.dominio.Pontuacao\"," +
                "\"atributo\":\"nome\"," +
                "\"valor\":\"true\"" +
                "}";

        System.out.println(Arrays.toString(notasStr.split("}")));
    }

    void save(Radoc radoc) {

        Document radocDB = new Document()
                .append("id", radoc.getId())
                .append("anoBase", radoc.getAnoBase())
                .append("relatos", buildRelatosJSON(radoc.getRelatos()));

        this.radocsCollection.insertOne(radocDB);

    }

    String buildRelatosJSON(List<Relato> relatos) {

        Type typeOfSrc = new TypeToken<List<Relato>>(){}.getType();

        return new Gson().toJson(relatos, typeOfSrc);

        //return new GsonBuilder().setPrettyPrinting().create().toJson(relatos, typeOfSrc);
    }

    Radoc getOne(String chave, String valor) {

        Document search = radocsCollection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Radoc (search.getString("id"), search.getInteger("anoBase"), getListaRelatos(search.getString("relatos")));

    }

    private List<Relato> getListaRelatos(String relatosStr) {

        List<Relato> listaRelatos = new ArrayList<>();
        ArrayList<String> tiposRelatos = new ArrayList<>();
        ArrayList<String[]> valores_relatos = new ArrayList<>();
        String[] relatos = relatosStr.split("}}},");

        for (String relato : relatos) {
            tiposRelatos.add(relato.substring(relato.indexOf(":") + 1, relato.indexOf(",")));
            valores_relatos.add(relato.substring(relato.indexOf(":{") + 2, relato.lastIndexOf("\"")).split("},"));
        }

        final int[] i = {0};

        valores_relatos.forEach(chaves_valores_valor_relato ->
            {
                HashMap<String, Valor> map = new HashMap<>();
                for(String chaves_valores_relato : chaves_valores_valor_relato) {
                    String[] valores_relato = chaves_valores_relato.split(":\\{");
                    String[] valores_valor =  valores_relato[1].split(",");
                    String valorKey = valores_relato[0];
                    Valor valorAtual;
                    try {
                        String stringValue = valores_valor[2].substring(10);
                        valorAtual = new Valor(stringValue);
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        String realValue = valores_valor[0].substring(7);
                        String logicoValue = valores_valor[1].substring(9);
                        if(Float.parseFloat(realValue) > 0){
                            valorAtual = new Valor(Float.parseFloat(realValue));
                        }
                        else{
                            if(logicoValue.equals("false")) {
                                valorAtual = new Valor(false);
                            }
                            else{
                                valorAtual = new Valor(true);
                            }
                        }
                    }
                    map.put(valorKey, valorAtual);
                }
                listaRelatos.add(new Relato(tiposRelatos.get(i[0]++), map));
            }
        );
        return listaRelatos;
    }

    void delete(String chave, String valor) {
        radocsCollection.deleteOne(new Document(chave, valor));
    }

    void update(String chave, String valor, Radoc radoc) {

        Document radocDB = new Document()
                .append("id", radoc.getId())
                .append("anoBase", radoc.getAnoBase())
                .append("relatos", buildRelatosJSON(radoc.getRelatos()));

        radocsCollection.updateOne(new Document(chave, valor), new Document("$set", radocDB));
    }

}
