package dao;

import br.ufg.inf.es.saep.sandbox.dominio.Radoc;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
    }

    Radoc getOne(String chave, String valor) {

        Document search = radocsCollection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Radoc (search.getString("id"), search.getInteger("anoBase"), getListaRelatos(search.getString("relatos")));

    }

    private List<Relato> getListaRelatos(String relatosStr) {

        Type typeOfSrc = new TypeToken<List<Relato>>() {}.getType();

        return new Gson().fromJson(relatosStr, typeOfSrc);

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
