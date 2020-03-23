package quasylab.sibilla.core.simulator.network;

public enum UDPNetworkManagerType implements NetworkManagerType {
	DEFAULT;

	public static NetworkManagerType getType(UDPNetworkManager networkManager) {
		return DEFAULT;
	}
};