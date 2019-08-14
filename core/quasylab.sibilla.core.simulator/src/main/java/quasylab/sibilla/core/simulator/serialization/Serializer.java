package quasylab.sibilla.core.simulator.serialization;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public interface Serializer {
    public Object readObject() throws Exception;
    public void writeObject(Object toWrite) throws Exception;
    public void setTimeout(long timeout) throws SocketException;
    public Socket getSocket();
    public static Serializer createSerializer(Socket socket, ClassLoader classLoader, String serialization) throws IOException {
        switch(serialization){
            case "FST": return new FSTSerializer(socket, classLoader);
            case "Default": 
            default: return new DefaultSerializer(socket, classLoader);
        }
    }

    public static Serializer createSerializer(Socket socket, String serialization) throws IOException {
        switch(serialization){
            case "FST": return new FSTSerializer(socket);
            case "Default": 
            default: return new DefaultSerializer(socket);
        }
    }
}