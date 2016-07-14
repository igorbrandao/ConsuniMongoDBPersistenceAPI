package dao;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.lang.reflect.Type;
import java.util.*;

public class ParecerDAO implements ParecerRepository {

    private static ParecerDAO instance = null;
    private final MongoCollection<Document> pareceresCollection;
    private final RadocDAO radocDAOInstance;

    private ParecerDAO(String connectionType) {
        this.pareceresCollection = DBConnector.createConnection(connectionType).getCollection("pareceres");
        this.radocDAOInstance = RadocDAO.getInstance(connectionType);
    }

    /**
     * Instancia um data access object para a coleção de pareceres;
     * @param connectionType string que define o tipo de conexão com o banco a ser feita. Veja {@link dao.DBConnector#createConnection(String) createConnection}
     * @return O objeto ParecerDAO para operações em elementos dessa entidade no banco de dados.
     */

    public static ParecerDAO getInstance(String connectionType) {
        return instance == null ? new ParecerDAO(connectionType) : instance;
    }

    @Override
    public void adicionaNota(String idParecer, Nota nota) {

        Parecer parecer = getOne("id", idParecer);

        if (parecer == null) {
            throw new IdentificadorDesconhecido("id do parecer inexistente.");
        }

        parecer.getNotas().add(nota);

        this.update("id", idParecer, parecer);

    }

    @Override
    public void removeNota(String idParecer, Avaliavel original) {

        boolean found = false;

        Parecer parecer = this.getOne("id", idParecer);

        if (parecer == null) {
            throw new IdentificadorDesconhecido("id do parecer inexistente.");
        }

        Nota notaAserRemovida = null;

        for (Nota nota : parecer.getNotas()) {
            if (nota.getItemOriginal().equals(original)) {
                notaAserRemovida = nota;
                found = true;
                break;
            }
        }

        if(found) {
            parecer.getNotas().remove(notaAserRemovida);
            this.update("id", idParecer, parecer);
        }

    }

    @Override
    public void persisteParecer(Parecer parecer) {

        if (this.getOne("id", parecer.getId()) != null) {
            throw new IdentificadorExistente("id do parecer já existe");
        }
        this.save(parecer);

    }

    @Override
    public void atualizaFundamentacao(String idParecer, String fundamentacao) {

        Parecer parecer = this.getOne("id", idParecer);

        if (parecer == null) {
            throw new IdentificadorDesconhecido("id do parecer inexistente.");
        }

        this.update("id", idParecer, new Parecer(
                        parecer.getId(),
                        parecer.getResolucao(),
                        parecer.getRadocs(),
                        parecer.getPontuacoes(),
                        fundamentacao,
                        parecer.getNotas()
                )
        );
    }

    @Override
    public Parecer byId(String id) {
        return this.getOne("id", id);
    }

    @Override
    public void removeParecer(String id) {
        this.delete("id", id);
    }

    @Override
    public Radoc radocById(String id) {
        return this.radocDAOInstance.getOne("id", id);
    }

    @Override
    public String persisteRadoc(Radoc radoc) {

        this.radocDAOInstance.save(radoc);

        return radoc.getId();

    }

    @Override
    public void removeRadoc(String id) {

        boolean radocIsRefereced = false;

        referenceSearchLoop : for(Document parecer : this.pareceresCollection.find()){
            JsonElement jsonElem = new JsonParser().parse(parecer.getString("radocIds"));
            JsonArray radocsJSONArray = jsonElem.getAsJsonArray();
            for (JsonElement radoc : radocsJSONArray){
                if (radoc.getAsString().equals(id)){
                    radocIsRefereced = true;
                    break referenceSearchLoop;
                }
            }
        }

        if(!radocIsRefereced) {
            radocDAOInstance.delete("id", id);
        }

    }

    private void save(Parecer parecer) {

        Document parecerDB = new Document()
                .append("id", parecer.getId())
                .append("resolucaoId", parecer.getResolucao())
                .append("radocIds", buildRadocsJSON(parecer.getRadocs()))
                .append("pontuacoes", buildPontuacoesJSON(parecer.getPontuacoes()))
                .append("fundamentacao", parecer.getFundamentacao())
                .append("notas", buildNotasJSON(parecer.getNotas()));

        this.pareceresCollection.insertOne(parecerDB);
    }

    private String buildRadocsJSON(List<String> radocs) {

        String radocsJSON = "[";

        for (int i = 0; i < radocs.size(); i++) {

            radocsJSON += "\"" + radocs.get(i).replace("\"", "") + "\"";

            if (i < radocs.size() - 1) {
                radocsJSON += ",";
            }

        }

        radocsJSON += "]";

        return radocsJSON;
    }

    private String buildNotasJSON(List<Nota> notas) {

        String notasJSON = "[";

        for (int i = 0; i < notas.size(); i++) {

            notasJSON += "{\"justificativa\":\"" + notas.get(i).getJustificativa() + "\"," +
                    "\"itemOriginal\":{" + buildAvaliavelJSON(notas.get(i).getItemOriginal()) + "}," +
                    "\"itemNovo\":{" + buildAvaliavelJSON(notas.get(i).getItemNovo()) + "}}";

            if (i < notas.size() - 1) {
                notasJSON += ",";
            }
        }

        notasJSON += "]";

        return notasJSON;
    }

    private String buildAvaliavelJSON(Avaliavel avaliavel) {

        String avaliavelJSON = "\"avaliavelClass\":\"" + avaliavel.getClass().toString().substring(6) + "\",";

        if (avaliavel instanceof Pontuacao) {

            Pontuacao pontuacao = (Pontuacao) avaliavel;
            ArrayList<Pontuacao> array = new ArrayList<>();
            array.add(pontuacao);
            avaliavelJSON += buildPontuacoesJSON(array).substring(2, buildPontuacoesJSON(array).length() - 2);

        } else if (avaliavel instanceof Relato) {

            Relato relato = (Relato) avaliavel;
            ArrayList<Relato> array = new ArrayList<>();
            array.add(relato);
            avaliavelJSON += radocDAOInstance.buildRelatosJSON(array).substring(2, radocDAOInstance.buildRelatosJSON(array).length() - 2);

        }

        return avaliavelJSON;

    }

    private String buildPontuacoesJSON(List<Pontuacao> pontuacoes) {

        String pontuacoesJSON = "[";

        for (int i = 0; i < pontuacoes.size(); i++) {

            pontuacoesJSON += "{\"nome\":\"" + pontuacoes.get(i).getAtributo().replace("\"", "") + "\"," +
                    "\"valor\":\"" + buildValorJSON(pontuacoes.get(i).getValor()) + "\"}";

            if (i < pontuacoes.size() - 1) {
                pontuacoesJSON += ",";
            }
        }

        pontuacoesJSON += "]";

        return pontuacoesJSON;
    }

    private String buildValorJSON(Valor valor) {

        if (valor.getString() != null) {
            return valor.getString().replace("\"", "");
        } else if (valor.getFloat() != 0.0f) {
            return Float.toString(valor.getFloat());
        } else {
            return valor.getBoolean() ? "true" : "false";
        }

    }

    private Parecer getOne(String chave, Object valor) {

        Document search = this.pareceresCollection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Parecer(
                search.getString("id"),
                search.getString("resolucaoId"),
                getRadocIdsList(search.getString("radocIds")),
                getPontuacoesList(search.getString("pontuacoes")),
                search.getString("fundamentacao"),
                getNotasList(search.getString("notas"))
        );

    }

    private List<String> getRadocIdsList(String radocIdsStr) {

        radocIdsStr = radocIdsStr.replace("[", "").replace("]", "");
        List<String> listRadocIds = new ArrayList<>();
        Collections.addAll(listRadocIds, radocIdsStr.split(","));
        return listRadocIds;

    }

    private List<Pontuacao> getPontuacoesList(String pontuacoesStr) {

        List<Pontuacao> pontuacoesList = new ArrayList<>();
        String[] pontuacoes = pontuacoesStr.split("}");

        ArrayList<String[]> valores_chaves_pontuacoes = new ArrayList<>();

        for (int i = 0; i < pontuacoes.length - 1; i++) {
            pontuacoes[i] = pontuacoes[i].substring(2) + "}";
            valores_chaves_pontuacoes.add(pontuacoes[i].substring(pontuacoes[i].indexOf("{") + 1, pontuacoes[i].indexOf("}")).split(","));
        }

        final Valor[] valorAtual = new Valor[1];
        final String[][] entrySet = new String[1][1];

        valores_chaves_pontuacoes.forEach(chaves_valores_pontuacao ->
                {
                    for (String chave_valor_pontuacao : chaves_valores_pontuacao) {
                        entrySet[0] = chave_valor_pontuacao.split(":");
                        try {
                            valorAtual[0] = new Valor(Float.parseFloat(entrySet[0][1]));
                        } catch (NumberFormatException e) {
                            if (entrySet[0][1].equalsIgnoreCase("true")) {
                                valorAtual[0] = new Valor(true);
                            } else if (entrySet[0][1].equalsIgnoreCase("false")) {
                                valorAtual[0] = new Valor(false);
                            } else {
                                valorAtual[0] = new Valor(entrySet[0][1]);
                            }
                        }
                    }
                    pontuacoesList.add(new Pontuacao(entrySet[0][0], valorAtual[0]));
                }
        );
        return pontuacoesList;
    }

    private List<Nota> getNotasList(String notasStr) {

        List<Nota> notasList = new ArrayList<>();

        JsonElement jsonElem = new JsonParser().parse(notasStr);
        JsonArray notasJSONArray = jsonElem.getAsJsonArray();

        for (JsonElement notaJSONElement : notasJSONArray) {

            JsonObject notaJSONObject = notaJSONElement.getAsJsonObject();

            String itemOriginalStr = notaJSONObject.get("itemOriginal").toString();
            String itemOriginalClass = notaJSONObject.get("itemOriginal").getAsJsonObject().get("avaliavelClass").toString();
            String itemNovoStr = notaJSONObject.get("itemNovo").getAsJsonObject().toString();
            String itemNovoClass = notaJSONObject.get("itemNovo").getAsJsonObject().get("avaliavelClass").toString();

            Avaliavel itemOriginal = getAvaliavelValue(
                    "{" + itemOriginalStr.substring(itemOriginalStr.indexOf(",") + 1),
                    itemOriginalClass.substring(1, itemOriginalClass.length() - 1)
            );
            Avaliavel itemNovo = getAvaliavelValue(
                    "{" + itemNovoStr.substring(itemNovoStr.indexOf(",") + 1),
                    itemNovoClass.substring(1, itemNovoClass.length() - 1)
            );

            notasList.add(new Nota(itemOriginal, itemNovo, notaJSONObject.get("justificativa").getAsString()));
        }
        return notasList;
    }

    private Avaliavel getAvaliavelValue(String avaliavelJSONStr, String avaliavelClass) {

        JsonElement avaliavelJSONElement = new JsonParser().parse(avaliavelJSONStr);

        if (avaliavelClass.equals(Pontuacao.class.toString().substring(6))) {
            JsonObject avaliavelJSONObject = avaliavelJSONElement.getAsJsonObject();
            return new Pontuacao(avaliavelJSONObject.get("nome").toString(), getValorValue(avaliavelJSONObject.get("valor").toString()));
        } else if (avaliavelClass.equals(Relato.class.toString().substring(6))) {
            Type typeOfSrc = new TypeToken<Relato>() {}.getType();
            return new Gson().fromJson(avaliavelJSONElement, typeOfSrc);
        }
        return null;
    }

    private Valor getValorValue(String valorStr) {

        try {
            return new Valor(Float.parseFloat(valorStr));
        } catch (NumberFormatException e) {
            if (valorStr.equalsIgnoreCase("true")) {
                return new Valor(true);
            } else if (valorStr.equalsIgnoreCase("false")) {
                return new Valor(false);
            } else {
                return new Valor(valorStr);
            }
        }
    }

    private void delete(String chave, Object valor) {
        this.pareceresCollection.deleteOne(new Document(chave, valor));
    }

    private void update(String chave, Object valor, Parecer parecer) {

        Document parecerDB = new Document()
                .append("id", parecer.getId())
                .append("resolucaoId", parecer.getResolucao())
                .append("radocIds", buildRadocsJSON(parecer.getRadocs()))
                .append("pontuacoes", buildPontuacoesJSON(parecer.getPontuacoes()))
                .append("fundamentacao", parecer.getFundamentacao())
                .append("notas", buildNotasJSON(parecer.getNotas()));

        this.pareceresCollection.updateOne(new Document(chave, valor), new Document("$set", parecerDB));
    }
}
