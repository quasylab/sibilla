package quasylab.sibilla.core.simulator.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPDefaultNetworkManager implements UDPNetworkManager {

	private DatagramSocket socket;

	public UDPDefaultNetworkManager(DatagramSocket socket){
		this.socket = socket;
	}

	@Override
	public byte[] readObject() throws Exception {
		DatagramPacket p = new DatagramPacket(new byte[1500], 1500);
		socket.receive(p);
		return p.getData();
	}

	@Override
	public void writeObject(byte[] toWrite, InetAddress address, int port) throws Exception {
		DatagramPacket p = new DatagramPacket(toWrite, toWrite.length, address, port);
		socket.send(p);
	}

	@Override
	public void setTimeout(long timeout) throws SocketException {
	socket.setSoTimeout((int) (timeout / 1000000));
	}

	@Override
	public UDPNetworkManagerType getType() {
		return UDPNetworkManagerType.DEFAULT;
	}

}
