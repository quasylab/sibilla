package quasylab.sibilla.core.server.util;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;


public class SSLUtils {

    private String keyStorePath;
    private String keyStorePass;
    private String keyStoreType;

    private String trustStorePath;
    private String trustStorePass;
    private String trustStoreType;

    private static SSLUtils instance;

    private SSLUtils() {
    }

    public static SSLUtils getInstance() {
        if (instance == null) {
            instance = new SSLUtils();
        }
        return instance;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public SSLContext createSSLContext() {
        try {
            if (this.keyStorePath == null || this.keyStorePass == null || this.keyStoreType == null) {
                throw new Exception("Missing KeyStore infos");
            }
            KeyStore keyStore = KeyStore.getInstance(this.keyStoreType);
            keyStore.load(new FileInputStream(this.keyStorePath),
                    keyStorePass.toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePass.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();

            KeyStore trustStore = KeyStore.getInstance(this.trustStoreType);
            keyStore.load(new FileInputStream(this.trustStorePath),
                    trustStorePass.toCharArray());

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, new SecureRandom());

            return sslContext;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
