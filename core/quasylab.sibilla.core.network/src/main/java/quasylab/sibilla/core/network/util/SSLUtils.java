/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.util;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Utility class used to store the data used to initiate a TLS connection and to easily create SSLContext instances
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class SSLUtils {

    /**
     * Path of the KeyStore file.
     */
    private String keyStorePath;

    /**
     * Password to access the KeyStore file.
     */
    private String keyStorePass;

    /**
     * Type of the KeyStore file.
     */
    private String keyStoreType;

    /**
     * Path of the TrustStore file.
     */
    private String trustStorePath;

    /**
     * Password to access the TrustStore file.
     */
    private String trustStorePass;

    /**
     * Type of the TrustStore file.
     */
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


    /**
     * Creates the SSLContext with the parameters that has been set previously.
     *
     * @return SSLContext used to create a secure connection
     * @throws IOException when TrustStores and KeyStores filer are invalid.
     */
    public SSLContext createSSLContext() throws IOException {
        try {
            if (this.keyStorePath == null || this.keyStorePass == null || this.keyStoreType == null) {
                throw new Exception("Missing KeyStore infos");
            }
            //Loads the keystore
            KeyStore keyStore = KeyStore.getInstance(this.keyStoreType);
            keyStore.load(ClassLoader.getSystemClassLoader().getSystemResourceAsStream(this.keyStorePath),
                    keyStorePass.toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePass.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            //Loads the truststore
            KeyStore trustStore = KeyStore.getInstance(this.trustStoreType);
            trustStore.load(ClassLoader.getSystemClassLoader().getSystemResourceAsStream(this.trustStorePath),
                    trustStorePass.toCharArray());

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, new SecureRandom());

            return sslContext;
        } catch (Exception e) {
            throw new IOException(String.format("SSL context exception %s", e.getClass()));
        }
    }
}
