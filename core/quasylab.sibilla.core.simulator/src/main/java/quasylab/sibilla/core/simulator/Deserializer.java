package quasylab.sibilla.core.simulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

public class Deserializer {
    ObjectInputStream ois;
    public Deserializer(InputStream is, final ClassLoader classLoader) throws IOException {
        ois = new ObjectInputStream(is) {
    
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), false, classLoader);
            }
    
        };
    }
  
    public  Serializable readObject() throws IOException, ClassNotFoundException {
        Serializable obj;
        obj = (Serializable) ois.readObject();
        return obj;
    }
    
}