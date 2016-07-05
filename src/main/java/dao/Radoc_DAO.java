package dao;

import br.ufg.inf.es.saep.sandbox.dominio.Radoc;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.omg.CORBA.Object;

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

    private final MongoCollection<Document> collection;
    private static Radoc_DAO instance = null;

    public static void main(String[] args) {
        //for (Relato relato : getListaRelatos("")) {
            //System.out.print("tipo: " + relato.getTipo() + "\n");
        //}
    }

    private Radoc_DAO(String connectionType) {
        this.collection = DBConnector.createConnection(connectionType).getCollection("radocs");
    }

    public static synchronized Radoc_DAO getInstance(String connectionType) {
        return instance == null ? new Radoc_DAO(connectionType) : instance;
    }

    public void save(Radoc radoc) {

        Document radocDB = new Document()
                .append("id", radoc.getId())
                .append("anoBase", radoc.getAnoBase())
                .append("relatos", buildRelatosJSON(radoc.getRelatos()));

        this.collection.insertOne(radocDB);

    }

    private String buildRelatosJSON(List<Relato> relatos) {

        String relatosJSON = "{";

        for(int i = 0; i < relatos.size(); i++){

            /*
            relatosJSON +=
                    "tipo:" + relatos.get(i).getTipo() + "," +
                    "valores:{" + buildValoresDeRelatoJSON(relatos.get(i).getValores()) + "}";
            //*/

            if(i < relatos.size() - 1){
                relatosJSON += ",";
            }
        }

        relatosJSON += "}";

        return relatosJSON;
        //{tipo:valorA,valores:{chave1:valor1,chave2:valor2},tipo:valorB,valores:{chave3:valor3,chave4:valor4}}
    }

    private String buildValoresDeRelatoJSON(Map<String, Valor> valores){

        int i = 0;

        String valoresJSON = "";

        for (Map.Entry<String, Valor> entry : valores.entrySet()){

            valoresJSON += entry.getKey() + ":" + entry.getValue();

            if(i < valores.entrySet().size() - 1){
                valoresJSON += ",";
            }

            i++;
        }

        return valoresJSON;
        //chave1:valor1,chave2:valor2
    }

    public Radoc getOne(String chave, Object valor) {

        Document search = collection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Radoc (search.getString("id"), search.getInteger("anoBase"), getListaRelatos(search.getString("relatos")));

    }

    private List<Relato> getListaRelatos(String relatosStr) {

        String mock = "{tipo:valorA,valores:{chave1:valor1,chave2:valor2},tipo:valorB,valores:{chave3:valor3,chave4:valor4}}";

        List<Relato> listaRelatos = new ArrayList<>();
        String[] relatos = mock.split("}");//relatosStr.split("}");

        ArrayList<String[]> valores_chaves_valores_relatos = new ArrayList<>();
        ArrayList<String> tiposRelatos = new ArrayList<>();

        for(int i = 0; i < relatos.length; i++){
            relatos[i] = relatos[i].substring(1) + "}";
            tiposRelatos.add(relatos[i].substring(relatos[i].indexOf(":") + 1, relatos[i].indexOf(",")));
        }

        for (String relato : relatos) {
            valores_chaves_valores_relatos.add(relato.substring(relato.indexOf("{") + 1, relato.indexOf("}")).split(","));
        }

        for(String relato : relatos){
            System.out.println(relato);
        }

        valores_chaves_valores_relatos.forEach(chaves_valores_relato ->
            {
                int i = 0;
                HashMap<String, Valor> map = new HashMap<>();
                for (String chave_valor_relato : chaves_valores_relato) {
                    String[] entryPair = chave_valor_relato.split(":");
                    map.put(entryPair[0], new Valor(entryPair[1]));
                }
                for(Map.Entry entry : map.entrySet()){
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                listaRelatos.add(new Relato(tiposRelatos.get(i++), map));
            }
        );

        return listaRelatos;
    }

    public void delete(String chave, Object valor) {
        collection.deleteOne(new Document(chave, valor));
    }

    public void update(String chave, String valor, Radoc radoc) {

        Document radocDB = new Document()
                .append("id", radoc.getId())
                .append("anoBase", radoc.getAnoBase())
                .append("relatos", buildRelatosJSON(radoc.getRelatos()));

        collection.updateOne(new Document(chave, valor), new Document("$set", radocDB));
    }

}
