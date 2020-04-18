package quasylab.sibilla.core.server.network;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.util.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class TCPSecureNetworkManager implements TCPNetworkManager {

    private static final Logger LOGGER = Logger.getLogger(TCPSecureNetworkManager.class.getName());

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    //The socket needs to be built - Example: Client-side connection
    public TCPSecureNetworkManager(ServerInfo serverInfo) throws IOException {
        if (serverInfo.getType().equals(TCPNetworkManagerType.SECURE)) {
            SSLContext sslContext = SSLUtils.getInstance().createSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverInfo.getAddress(), serverInfo.getPort());
            this.buildWithSocket(sslSocket);
        } else {
            throw new IOException("Wrong TCPNetworkManager type");
        }
    }

    //The socket has already been built - Example: Server-side connection
    public TCPSecureNetworkManager(Socket socket) throws IOException {
        if (socket instanceof SSLSocket) {
            this.buildWithSocket((SSLSocket) socket);
        } else {
            throw new IOException("Wrong Socket type");
        }
    }

    private void buildWithSocket(SSLSocket sslSocket) throws IOException {

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        sslSocket.startHandshake();

        SSLSession sslSession = sslSocket.getSession();
        LOGGER.info(String.format("SSLSession Started:\n\tProtocol : %s\n\tCipher suite : %s\n\tPeer host : %s %s", sslSession.getProtocol(), sslSession.getCipherSuite(), sslSession.getPeerHost(), sslSession.getPeerPrincipal().getName()));
        this.socket = sslSocket;
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(sslSocket.getOutputStream()));
        dataOutputStream.flush();
        dataInputStream = new DataInputStream(new BufferedInputStream(sslSocket.getInputStream()));
    }

    @Override
    public byte[] readObject() throws Exception {
        int length = dataInputStream.readInt();
        byte[] message = null;
        if (length > 0) {
            message = new byte[length];
            dataInputStream.readFully(message, 0, length);
        }
        return message;
    }

    @Override
    public void writeObject(byte[] toWrite) throws Exception {
        dataOutputStream.writeInt(toWrite.length);
        dataOutputStream.write(toWrite);
        dataOutputStream.flush();
    }

    @Override
    public void setTimeout(long timeout) throws SocketException {
        socket.setSoTimeout((int) (timeout / 1000000));
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void closeConnection() {
        try {
            this.socket.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TCPNetworkManagerType getType() {
        return TCPNetworkManagerType.SECURE;
    }
}
