package dao;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

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

class Parecer_DAO implements ParecerRepository{

    private final MongoCollection<Document> collection;
    private static Parecer_DAO instance = null;

    private Parecer_DAO(String connectionType) {
        this.collection = DBConnector.createConnection(connectionType).getCollection("pareceres");
    }

    public static synchronized Parecer_DAO getInstance(String connectionType) {
        return instance == null ? new Parecer_DAO(connectionType) : instance;
    }
    /*
    public void save( Parecer parecer) {
        Document parecerDB = new Document()
                .append("nome", atributo.getNome())
                .append("tipo", atributo.getTipo())
                .append("descricao", atributo.getDescricao());
        this.collection.insertOne(parecerDB);
    }

    public Atributo getOne(String chave, Object valor) {

        Document search = collection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Atributo (search.getString("nome"), search.getString("tipo"), search.getInteger("descricao"));

    }

    public void delete(String chave, Object valor) {
        collection.deleteOne(new Document(chave, valor));
    }

    public void update(String chave, Object valor, Parecer parecer) {

        Document parecerDB = new Document()
                .append("nome", parecer.getNome())
                .append("tipo", parecer.getTipo())
                .append("descricao", parecer.getDescricao());

        collection.updateOne(new Document(chave, valor), new Document("$set", parecerDB));
    }
    //*/
    @Override
    public void adicionaNota(String parecer, Nota nota) {

    }

    @Override
    public void removeNota(Avaliavel original) {

    }

    @Override
    public void persisteParecer(Parecer parecer) {

    }

    @Override
    public void atualizaFundamentacao(String parecer, String fundamentacao) {

    }

    @Override
    public Parecer byId(String id) {
        return null;
    }

    @Override
    public void removeParecer(String id) {

    }

    @Override
    public Radoc radocById(String identificador) {
        return null;
    }

    @Override
    public String persisteRadoc(Radoc radoc) {
        return null;
    }

    @Override
    public void removeRadoc(String identificador) {

    }
}
