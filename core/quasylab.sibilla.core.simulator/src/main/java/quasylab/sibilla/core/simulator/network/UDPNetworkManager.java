package quasylab.sibilla.core.simulator.network;

import java.io.IOException;
import java.net.*;

import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.ServerInfo;

public interface UDPNetworkManager {

	public byte[] readObject() throws Exception;

	public void writeObject(byte[] toWrite, InetAddress address, int port) throws Exception;

	public void setTimeout(long timeout) throws SocketException;
	public UDPNetworkManagerType getType();
	public static UDPNetworkManager createNetworkManager(ServerInfo info, boolean toBroadcast) throws IOException {
		DatagramSocket socket = new DatagramSocket(info.getPort(), info.getAddress());
		socket.setBroadcast(toBroadcast);
		switch (info.getType().name()) {
			case "DEFAULT":
				return new UDPDefaultNetworkManager(socket);
		}
		return null;
	}

}
