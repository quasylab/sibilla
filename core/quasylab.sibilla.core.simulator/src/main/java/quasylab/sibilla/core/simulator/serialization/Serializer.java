package quasylab.sibilla.core.simulator.serialization;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import quasylab.sibilla.core.simulator.server.ServerInfo;

public interface Serializer {
    public Object readObject() throws Exception;

    public void writeObject(Object toWrite) throws Exception;

    public void setTimeout(long timeout) throws SocketException;

    public Socket getSocket();

    public static Serializer createSerializer(ServerInfo info, ClassLoader classLoader) throws IOException {
        Socket socket = new Socket(info.getAddress(), info.getPort());
        switch (info.getType()) {
            case FST:
                return new FSTSerializer(socket, classLoader);
            case DEFAULT:
            default:
                return new DefaultSerializer(socket, classLoader);
        }
    }

    public static Serializer createSerializer(ServerInfo info) throws IOException {
        Socket socket = new Socket(info.getAddress(), info.getPort());
        switch (info.getType()) {
            case FST:
                return new FSTSerializer(socket);
            case DEFAULT:
            default:
                return new DefaultSerializer(socket);
        }
    }
}