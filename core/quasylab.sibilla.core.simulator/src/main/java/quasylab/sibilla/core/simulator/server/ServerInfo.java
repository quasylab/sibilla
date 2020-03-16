package quasylab.sibilla.core.simulator.server;

import java.net.InetAddress;

import quasylab.sibilla.core.simulator.serialization.SerializationType;

public class ServerInfo {

    private InetAddress address;
    private int port;
    private SerializationType type;

    public ServerInfo(InetAddress address, int port, SerializationType serType) {
    	this.address = address;
    	this.port = port;
    	this.type = serType;
    	
    }

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public SerializationType getType() {
		return type;
	}
	
	public String toString() {
		return String.format("Server infos: IP %s - port %d - serialization type %s", address.getHostName(), port, type.name());
	}
}
