package quasylab.sibilla.core.server.util;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;


public class SSLUtils {

    private String keyStorePath;
    private String keyStorePass;
    private String keyStoreType;

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

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, null);

            return sslContext;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
