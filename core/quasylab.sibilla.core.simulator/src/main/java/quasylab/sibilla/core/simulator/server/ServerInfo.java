package quasylab.sibilla.core.simulator.server;

import quasylab.sibilla.core.simulator.network.NetworkManagerType;

import java.io.Serializable;
import java.net.InetAddress;

public class ServerInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2428861423753648117L;
	private InetAddress address;
	private int port;
	private NetworkManagerType type;

	public ServerInfo(InetAddress address, int port, NetworkManagerType serType) {
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

	public NetworkManagerType getType() {
		return type;
	}

	public String toString() {
		return String.format("{ IP: [%s] - Port: [%d] - Communication type: [%s - %s] }", address.getHostName(), port,
				type.getClass(), type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ServerInfo other = (ServerInfo) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}
}
