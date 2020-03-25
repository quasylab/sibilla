package quasylab.sibilla.core.simulator.network;

import quasylab.sibilla.core.simulator.server.ServerInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.Socket;
import java.net.SocketException;

public class TCPDefaultNetworkManager implements TCPNetworkManager {

	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private class CustomObjectInputStream extends ObjectInputStream {
		private ClassLoader classLoader;

		public CustomObjectInputStream(InputStream is, ClassLoader classLoader) throws IOException {
			super(is);
			this.classLoader = classLoader;
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			return Class.forName(desc.getName(), false, classLoader);
		}

	}

	public TCPDefaultNetworkManager(Socket socket) throws IOException {
		this.socket = socket;
		oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		oos.flush();
		ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	public TCPDefaultNetworkManager(Socket socket, ClassLoader classLoader) throws IOException {
		this.socket = socket;
		oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		oos.flush();
		ois = new CustomObjectInputStream(new BufferedInputStream(socket.getInputStream()), classLoader);
	}

	@Override
	public byte[] readObject() throws Exception {
		return (byte[]) ois.readObject();
	}

	@Override
	public void writeObject(byte[] toWrite) throws Exception {
		oos.writeObject(toWrite);
		oos.flush();
	}

	@Override
	public void setTimeout(long timeout) throws SocketException {
		socket.setSoTimeout((int) (timeout / 1000000));
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	@Override
	public TCPNetworkManagerType getType() {
		return TCPNetworkManagerType.DEFAULT;
	}

}