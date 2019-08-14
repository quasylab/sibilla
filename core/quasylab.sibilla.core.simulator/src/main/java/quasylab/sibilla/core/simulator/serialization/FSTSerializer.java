package quasylab.sibilla.core.simulator.serialization;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.nustaq.net.TCPObjectSocket;
import org.nustaq.serialization.FSTConfiguration;

public class FSTSerializer implements Serializer {
    private Socket socket;
    private TCPObjectSocket FSTSocket;
    private FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();

    public FSTSerializer(Socket socket) throws IOException {
        this.socket = socket;
        FSTSocket = new TCPObjectSocket(socket, conf);
    }

    public FSTSerializer(Socket socket, ClassLoader classLoader) throws IOException {
        this.socket = socket;
        conf.setClassLoader(classLoader);
        FSTSocket = new TCPObjectSocket(socket, conf);
    }

    @Override
    public Object readObject() throws Exception {
        return FSTSocket.readObject();
    }

    @Override
    public void writeObject(Object toWrite) throws Exception {
        FSTSocket.writeObject(toWrite);
        FSTSocket.flush();
    }

    @Override
    public void setTimeout(long timeout) throws SocketException {
        socket.setSoTimeout((int)(timeout / 1000000));
    }

    @Override
    public Socket getSocket() {
        return socket;
    }


    
}