package dao;

import com.mongodb.client.MongoDatabase;

import java.util.logging.*;

import com.mongodb.*;

import java.net.*;

/**
 * Define o local do banco de dados MongoDB para conexão.
 */

public class DBConnector {

    private static String connectionAddress = null, dbName = null, _URI = null;
    private static int port;

    /**
     * Customiza as variáveis necessárias para estabelecer uma conexão personalizada.
     * @param connectionAddress endereço da conexão.
     * @param dbName nome do banco de dados a ser criado.
     * @param port valor inteiro da porta a ser utilizada.
     */

    static void defineCustomConnectionParams(String connectionAddress, String dbName, int port) {
        DBConnector.connectionAddress = connectionAddress;
        DBConnector.dbName = dbName;
        DBConnector.port = port;
    }

    /**
     * Customiza as variáveis necessárias para estabelecer uma conexão pela rede.
     * @param _URI A cadeia de caracteres representando uma URI para o driver do Mongo.
     */

    static void defineNetworkConnectionURI(String _URI) {
        DBConnector._URI = _URI;
    }

    private static MongoDatabase defaultLocalConnection() throws UnknownHostException {
        return new MongoClient("localhost", 27017).getDatabase("consuniAPI");
    }

    private static MongoDatabase customConnection() throws UnknownHostException {
        return new MongoClient(connectionAddress, port).getDatabase(dbName);
    }

    private static MongoDatabase networkConnection() throws UnknownHostException {
        MongoClientURI connectionString = new MongoClientURI(_URI);
        return new MongoClient(connectionString).getDatabase("");
    }

    /**
     * @param connectionType use "local" para conectar a um banco de dados em "localhost" utilizando a porta padrão 27017, 'custom' para conexão local personalizada ou 'network' para conectar com um banco de dados em um endereço externo. Observe que para usar as conexões 'custom'  e 'network', os parâmetros devem ser definidos usando-se o respectivo método:<br>
     * {@link #defineCustomConnectionParams(String, String, int)}<br>
     * {@link #defineNetworkConnectionURI(String)}
     * @return a conexão ao banco especificado.
     */

    public static MongoDatabase createConnection(String connectionType) {
        try {
            switch (connectionType) {
                case "local":
                    return defaultLocalConnection();
                case "custom":
                    return customConnection();
                case "network":
                    return networkConnection();
                default:
                    return null;
            }
        } catch (UnknownHostException e) {
            Logger.getLogger(DBConnector.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }

    }
}
