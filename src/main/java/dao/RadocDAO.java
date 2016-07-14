package dao;

import br.ufg.inf.es.saep.sandbox.dominio.Radoc;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.lang.reflect.Type;
import java.util.*;

class RadocDAO {

    private final MongoCollection<Document> radocsCollection;
    private static RadocDAO instance = null;

    private RadocDAO(String connectionType) {
        this.radocsCollection = DBConnector.createConnection(connectionType).getCollection("radocs");
    }

    /**
     * Instancia um data access object para a coleção de radocs;
     * @param connectionType string que define o tipo de conexão com o banco a ser feita. Veja {@link dao.DBConnector#createConnection(String) createConnection}
     * @return O objeto RadocDAO para operações em elementos dessa entidade no banco de dados.
     */

    static RadocDAO getInstance(String connectionType) {
        return instance == null ? new RadocDAO(connectionType) : instance;
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

}
