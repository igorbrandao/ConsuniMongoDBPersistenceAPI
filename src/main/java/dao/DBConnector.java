package dao;

import com.mongodb.client.MongoDatabase;

import java.util.logging.*;

import com.mongodb.*;

import java.net.*;

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

/**
 * Define o local do banco de dados MongoDB para conexão.
 */

public class DBConnector {

    private static String connectionAddress = null, dbName = null, _URI = null;
    private static int port;

    private DBConnector() {
    }

    static void defineCustomConnectionParams(String connectionAddress, String dbName, int port) {
        DBConnector.connectionAddress = connectionAddress;
        DBConnector.dbName = dbName;
        DBConnector.port = port;
    }

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
     * @param connectionType use "padrao" para conectar a um banco de dados em "localhost" utilizando a porta padrão, 'custom' para conexão local personalizada ou 'network' para conectar com um banco de dados em um endereço externo. Observe que para usar as conexões 'custom'  e 'network', os parâmetros devem ser definidos usando-se o respectivo método:<br>
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
