package quasylab.sibilla.core.simulator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.Socket;
import java.net.SocketException;

public class DefaultSerializer implements Serializer {

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private class CustomObjectInputStream extends ObjectInputStream{
        private ClassLoader classLoader;
        public CustomObjectInputStream (InputStream is, ClassLoader classLoader) throws IOException {
            super(is);
            this.classLoader = classLoader;
        }
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            return Class.forName(desc.getName(), false, classLoader);
        }

    }

    public DefaultSerializer(Socket socket) throws IOException {
        this.socket = socket;
        oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        oos.flush();
        ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public DefaultSerializer(Socket socket, ClassLoader classLoader) throws IOException {
        this.socket = socket;
        oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        oos.flush();
        ois = new CustomObjectInputStream(new BufferedInputStream(socket.getInputStream()), classLoader);
    }


    @Override
    public Object readObject() throws Exception {
        return ois.readObject();
    }

    @Override
    public void writeObject(Object toWrite) throws Exception {
        oos.writeObject(toWrite);
        oos.flush();
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