package quasylab.sibilla.core.server.ssl;


import quasylab.sibilla.core.server.client.ClientCommand;
import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

public class MasterExample {

  /*  private void createKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] pwdArray = "password".toCharArray();
        ks.load(null, pwdArray);

        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode("CHIAVE DI SASSI");
        // rebuild key using SecretKeySpec
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(pwdArray);
        ks.setEntry("db-encryption-secret", secret, password);

        try (FileOutputStream fos = new FileOutputStream("newKeyStoreFileName.jks")) {
            ks.store(fos, pwdArray);
        }
    }*/

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "E:\\Programmi\\GitHub\\sibilla\\core\\quasylab.sibilla.core.server\\src\\main\\java\\quasylab\\sibilla\\core\\server\\ssl\\serverKeyStore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "sibilla");
        SSLServerSocket socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(10000);

        /*socket.setNeedClientAuth(true);
        socket.setEnabledCipherSuites(new String[]{"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});*/
        SSLSocket sasso = (SSLSocket) socket.accept();
        TCPNetworkManager client = TCPNetworkManager.createNetworkManager(TCPNetworkManagerType.SECURE, sasso);
        while(true){
            ClientCommand result = (ClientCommand) ObjectSerializer.deserializeObject(client.readObject());
            System.out.printf("Ho letto: %s\n", result);
            if (result.equals(ClientCommand.DATA)) {
                client.writeObject(ObjectSerializer.serializeObject(MasterCommand.CLOSE_CONNECTION));
                break;
            }
        }
        System.out.printf("Client ha chiuso");

    }
}