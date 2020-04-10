package quasylab.sibilla.core.server.network;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.master.MasterServerSimulationEnvironment;
import quasylab.sibilla.core.server.util.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Logger;

public class TCPSecureNetworkManager implements TCPNetworkManager {

    private static final Logger LOGGER = Logger.getLogger(TCPSecureNetworkManager.class.getName());

    private TCPDefaultNetworkManager netManager;

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

    private void buildWithSocket(SSLSocket socket) throws IOException {
        SSLSocket sslSocket = (SSLSocket) socket;

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        sslSocket.startHandshake();

        SSLSession sslSession = sslSocket.getSession();
        LOGGER.info(String.format("SSLSession Started:\n\tProtocol : %s\n\tCipher suite : %s\n\tPeer host : %s", sslSession.getProtocol(), sslSession.getCipherSuite(), sslSession.getPeerPrincipal().getName()));
        this.netManager = (TCPDefaultNetworkManager) TCPNetworkManager.createNetworkManager(TCPNetworkManagerType.DEFAULT, sslSocket);
    }

    @Override
    public byte[] readObject() throws Exception {
        return this.netManager.readObject();
    }

    @Override
    public void writeObject(byte[] toWrite) throws Exception {
        this.netManager.writeObject(toWrite);
    }

    @Override
    public void setTimeout(long timeout) throws SocketException {
        this.netManager.setTimeout(timeout);
    }

    @Override
    public Socket getSocket() {
        return this.netManager.getSocket();
    }

    @Override
    public void closeConnection() {
        this.netManager.closeConnection();
    }

    @Override
    public TCPNetworkManagerType getType() {
        return TCPNetworkManagerType.SECURE;
    }
}
