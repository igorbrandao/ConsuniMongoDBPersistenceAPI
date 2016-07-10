package dao;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

class Tipo_DAO {

    private static Tipo_DAO instance = null;
    private final MongoCollection<Document> tiposCollection;

    private Tipo_DAO(String connectionType) {
        this.tiposCollection = DBConnector.createConnection(connectionType).getCollection("tipos");
    }

    static synchronized Tipo_DAO getInstance(String connectionType) {
        return instance == null ? new Tipo_DAO(connectionType) : instance;
    }

    void save(Tipo tipo) {
        Document tipoDB = new Document()
                .append("id", tipo.getId())
                .append("nome", tipo.getNome())
                .append("descricao", tipo.getDescricao())
                .append("atributos", buildAtributosJSON(tipo.getAtributos()));

        this.tiposCollection.insertOne(tipoDB);
    }

    private String buildAtributosJSON(Set<Atributo> atributos) {

        String atributosJSON = "[";

        int i = 0;

        for (Atributo atributo : (Atributo[]) atributos.toArray()) {

            atributosJSON += "{\"nome\":\"" + atributo.getNome() + "\"," +
                    "\"descricao\":\"" + atributo.getDescricao() + "\"," +
                    "\"tipo\":\"" + atributo.getTipo() + "\"}";

            if (i < atributos.toArray().length - 1) {
                atributosJSON += ",";
            }
            i++;
        }

        atributosJSON += "]";

        return atributosJSON;
    }

    Tipo getOne(String chave, Object valor) {

        Document search = this.tiposCollection.find(new Document(chave, valor)).first();

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

    private Set<Atributo> getListaAtributos(String atributosStr) {

        String mock = "[" +
                "{" +
                "\"nome\":\"nome1\"," +
                "\"descricao\":\"descricao1\"," +
                "\"tipo\":\"1\"" +
                "}," +
                "{" +
                "\"nome\":\"nome2\"," +
                "\"descricao\":\"descricao2\"," +
                "\"tipo\":\"2\"" +
                "}" +
                "]";

        Set<Atributo> setAtributos = new HashSet<>();
        List<String[]> chaves_valores_atributos = new ArrayList<>();
        String[] atributos = atributosStr.split("}");//mock.split("}");

        for (int i = 0; i < atributos.length - 1; i++) {
            atributos[i] = atributos[i].substring(2);
            chaves_valores_atributos.add(atributos[i].split(","));
        }

        chaves_valores_atributos.forEach(chaves_valores_atributo ->
                {
                    HashMap<String, String> constructorParams = new HashMap<>();

                    for (String chave_valor_atributo : chaves_valores_atributo) {
                        String[] entryPair = chave_valor_atributo.split(":");
                        constructorParams.put(
                                entryPair[0].substring(1, entryPair[0].length() - 1),
                                entryPair[1].substring(1, entryPair[1].length() - 1)
                        );
                    }

                    setAtributos.add(new Atributo(
                            constructorParams.get("nome"),
                            constructorParams.get("descricao"),
                            Integer.parseInt(constructorParams.get("tipo")))
                    );
                }
        );

        return setAtributos;

    }

    void delete(String chave, Object valor) {
        tiposCollection.deleteOne(new Document(chave, valor));
    }

    List<Tipo> getListByName(String chave, Object valor) {

        List<Tipo> tipos = new ArrayList<>();

        for (Document tipo : this.tiposCollection.find(new Document(chave, valor))) {
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

}
