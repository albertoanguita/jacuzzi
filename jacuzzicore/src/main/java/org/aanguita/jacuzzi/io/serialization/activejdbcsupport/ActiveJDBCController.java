package org.aanguita.jacuzzi.io.serialization.activejdbcsupport;

import org.javalite.activejdbc.DB;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller provides unified connection and disconnection methods so several projects
 * relying on active jdbc can work simultaneously
 */
public class ActiveJDBCController {

    private static class DBInfo {

        private final String dbName;

        private final DB db;

        private final String path;

        public DBInfo(String database, String path) {
            this.dbName = database;
            this.db = getDatabaseConnection(database);
            this.path = path;
        }

        public void connect() {
            db.open("org.sqlite.JDBC", "jdbc:sqlite:" + path, "", "");
        }

        public void disconnect() {
            db.close();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DBInfo dbInfo = (DBInfo) o;
            return db.equals(dbInfo.db) && path.equals(dbInfo.path);
        }

        @Override
        public int hashCode() {
            int result = db.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "DBInfo{" +
                    "db=" + dbName +
                    "@'" + path + '\'' +
                    '}';
        }
    }

    private static final ThreadLocal<ArrayDeque<DBInfo>> connectionsStack = new ThreadLocal();

    private static final Map<String, DB> databaseConnections = new HashMap<>();

    private static ArrayDeque<DBInfo> getConnectionsStack() {
        if (connectionsStack.get() == null) {
            connectionsStack.set(new ArrayDeque<>());
        }
        return connectionsStack.get();
    }

    public static DB connect(String database, String dbPath) {
        DBInfo newConnection = new DBInfo(database, dbPath);
        DBInfo currentConnection = getConnectionsStack().peek();
        if (currentConnection != null && !currentConnection.equals(newConnection)) {
            // we must first disconnect the current connection
            currentConnection.disconnect();
        }
        if (currentConnection == null || !currentConnection.equals(new DBInfo(database, dbPath))) {
            // we must perform a new connection to
            newConnection.connect();
        }
        getConnectionsStack().push(newConnection);
        return newConnection.db;
    }

    public static void disconnect() {
        DBInfo disconnectedConnection = getConnectionsStack().pop();
        DBInfo newCurrentConnection = getConnectionsStack().peek();
        boolean hasDisconnected = false;
        if (!disconnectedConnection.equals(newCurrentConnection)) {
            // we must disconnect from this connection
            disconnectedConnection.disconnect();
            hasDisconnected = true;
        }
        if (newCurrentConnection != null && hasDisconnected) {
            // we must connect to the new current connection
            newCurrentConnection.connect();
        }
    }

    public static DB getDB() {
        return getConnectionsStack().peek().db;
    }

    private static synchronized DB getDatabaseConnection(String database) {
        if (!databaseConnections.containsKey(database)) {
            databaseConnections.put(database, new DB(database));
        }
        return databaseConnections.get(database);
    }
}
