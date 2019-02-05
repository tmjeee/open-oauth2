package org.open.oauth2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="open.oauth2")
public class OpenOAuth2ConfigurationProperties {

    private Hsqldb hsqldb = new Hsqldb();
    private Webconsole webconsole = new Webconsole();

    public Hsqldb getHsqldb() {
        return hsqldb;
    }

    public Webconsole getWebconsole() {
        return webconsole;
    }

    public static class Hsqldb {
        private String dbName;
        private String dbPath;
        private int dbPort;

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }
        public String getDbName() {
            return dbName;
        }

        public void setDbPath(String dbPath) {
            this.dbPath = dbPath;
        }
        public String getDbPath() {
            return this.dbPath;
        }

        public void setDbPort(int dbPort) {
            this.dbPort = dbPort;
        }
        public int getDbPort() {
            return this.dbPort;
        }
    }

    public static class Webconsole {
        private String server;
        private String resourceOwnerAuthentication;
        private String resourceOwnerScopesApproval;
        private String encryptionKeyInHex;

        public void setServer(String server) {
            this.server = server;
        }
        public String getServer() {
            return this.server;
        }

        public void setResourceOwnerAuthentication(String resourceOwnerAuthentication) {
            this.resourceOwnerAuthentication = resourceOwnerAuthentication;
        }
        public String getResourceOwnerAuthentication() {
            return this.resourceOwnerAuthentication;
        }

        public void setResourceOwnerScopesApproval(String resourceOwnerScopesApproval) {
            this.resourceOwnerScopesApproval = resourceOwnerScopesApproval;
        }
        public String getResourceOwnerScopesApproval() {
            return resourceOwnerScopesApproval;
        }

        public void setEncryptionKeyInHex(String encryptionKeyInHex) {
            this.encryptionKeyInHex = encryptionKeyInHex;
        }

        public String getEncryptionKeyInHex() {
            return this.encryptionKeyInHex;
        }

    }
}
