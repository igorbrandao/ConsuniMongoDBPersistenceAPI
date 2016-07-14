package dao;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResolucaoDAO implements ResolucaoRepository {

    private static ResolucaoDAO instance = null;
    private final MongoCollection<Document> resolucoesCollection;
    private final TipoDAO tipoDAOInstance;

    private ResolucaoDAO(String connectionType) {
        this.resolucoesCollection = DBConnector.createConnection(connectionType).getCollection("resolucoes");
        this.tipoDAOInstance = TipoDAO.getInstance(connectionType);
    }

    /**
     * Instancia um data access object para a coleção de resoluções;
     * @param connectionType string que define o tipo de conexão com o banco a ser feita. Veja {@link dao.DBConnector#createConnection(String) createConnection}
     * @return O objeto ResolicaoDAO para operações em elementos dessa entidade no banco de dados.
     */

    public static ResolucaoDAO getInstance(String connectionType) {
        return instance == null ? new ResolucaoDAO(connectionType) : instance;
    }

    @Override
    public void persisteTipo(Tipo tipo) {
        this.tipoDAOInstance.save(tipo);
    }

    @Override
    public void removeTipo(String idTipo) {

        Tipo tipoASerRemovido = tipoDAOInstance.getOne("id", idTipo);
        // Não há usos de Tipo em Resolução, então lançar uma ResolucaoUsaTipoException não se aplica.

        this.tipoDAOInstance.delete("id", idTipo);
    }

    @Override
    public Tipo tipoPeloCodigo(String codigo) {
        return this.tipoDAOInstance.getOne("id", codigo);
    }

    @Override
    public List<Tipo> tiposPeloNome(String nomeParcial) {
        return this.tipoDAOInstance.getListByPartialValue("nome", nomeParcial);
    }

    @Override
    public boolean remove(String id) {
        return this.delete("id", id).wasAcknowledged();
    }

    @Override
    public Resolucao byId(String id) {

        if (id == null || id.trim().isEmpty()){
            throw new CampoExigidoNaoFornecido("O campo id não foi informado ou veio em branco");
        }

        return this.getOne("id", id);
    }

    @Override
    public List<String> resolucoes() {

        List<String> resolucoes = new ArrayList<>();

        for (Document resolucao : this.resolucoesCollection.find()) {
            resolucoes.add(resolucao.getString("id"));
        }

        return resolucoes.isEmpty() ? null : resolucoes;
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

    private void save(Resolucao resolucao) {

        Document resolucaoDB = new Document()
                .append("id", resolucao.getId())
                .append("nome", resolucao.getNome())
                .append("descricao", resolucao.getDescricao())
                .append("dataAprovacao", resolucao.getDataAprovacao().toString())
                .append("regras", buildRegrasJSON(resolucao.getRegras()));

        this.resolucoesCollection.insertOne(resolucaoDB);
    }

    private String buildRegrasJSON(List<Regra> regras) {

        String regrasJSON = "[";

        for (int i = 0; i < regras.size(); i++) {

            regrasJSON += "{\"tipo\":\"" + regras.get(i).getTipo() + "\"," +
                            "\"descricao\":\"" + regras.get(i).getDescricao() + "\"," +
                            "\"tipoRelato\":\"" + regras.get(i).getTipoRelato() + "\"," +
                            "\"expressao\":\"" + regras.get(i).getExpressao() + "\"," +
                            "\"dependeDe\":" + buildDependeDeString(regras.get(i).getDependeDe()) + "," +
                            "\"pontosPorItem\":\"" + regras.get(i).getPontosPorItem() + "\"," +
                            "\"entao\":\"" + regras.get(i).getEntao() + "\"," +
                            "\"senao\":\"" + regras.get(i).getSenao() + "\"," +
                            "\"minimo\":\"" + regras.get(i).getValorMinimo() + "\"," +
                            "\"maximo\":\"" + regras.get(i).getValorMaximo() + "\"," +
                            "\"variavel\":\"" + regras.get(i).getVariavel() + "\"}";

            if (i < regras.size() - 1) {
                regrasJSON += ",";
            }

        }

        regrasJSON += "]";

        return regrasJSON;
    }

    private String buildDependeDeString(List<String> dependeDe) {

        String dependeDeJSON = "[";

        for (int i = 0; i < dependeDe.size(); i++) {

            dependeDeJSON += "\"" + dependeDe.get(i) + "\"";

            if (i < dependeDe.size() - 1) {
                dependeDeJSON += "=";
            }

        }

        dependeDeJSON += "]";

        return dependeDeJSON;
    }

    private Resolucao getOne(String chave, Object valor) {

        Document search = this.resolucoesCollection.find(new Document(chave, valor)).first();

        if (search == null) {
            return null;
        }

        return new Resolucao(
                search.getString("id"),
                search.getString("nome"),
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

    private List<Regra> getListaRegras(String regrasStr) {

        List<Regra> listRegras = new ArrayList<>();
        List<String[]> chaves_valores_regras = new ArrayList<>();
        String[] regras = regrasStr.split("}");

        for (int i = 0; i < regras.length - 1; i++) {
            regras[i] = regras[i].substring(2);
            chaves_valores_regras.add(regras[i].split(","));
        }

        chaves_valores_regras.forEach(chaves_valores_regra ->
                {
                    HashMap<String, String> constructorParams = new HashMap<>();

                    for (String chave_valor_regra : chaves_valores_regra) {
                        String[] entryPair = chave_valor_regra.split(":");
                        constructorParams.put(
                                entryPair[0].substring(1, entryPair[0].length() - 1),
                                entryPair[1].substring(1, entryPair[1].length() - 1)
                        );
                    }

                    listRegras.add(
                            new Regra(
                                    Integer.parseInt(constructorParams.get("tipo")),
                                    constructorParams.get("descricao"),
                                    Float.parseFloat(constructorParams.get("maximo")),
                                    Float.parseFloat(constructorParams.get("minimo")),
                                    constructorParams.get("variavel"),
                                    constructorParams.get("expressao"),
                                    constructorParams.get("entao"),
                                    constructorParams.get("senao"),
                                    constructorParams.get("tipoRelato"),
                                    Float.parseFloat(constructorParams.get("pontosPorItem")),
                                    getListDependeDe(constructorParams.get("dependeDe")))
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

    private DeleteResult delete(String chave, Object valor) {
        return this.resolucoesCollection.deleteOne(new Document(chave, valor));
    }

}
