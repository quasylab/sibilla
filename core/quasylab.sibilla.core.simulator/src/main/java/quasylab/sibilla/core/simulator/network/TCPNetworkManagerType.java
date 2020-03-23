package quasylab.sibilla.core.simulator.network;

public enum TCPNetworkManagerType implements NetworkManagerType {
    FST, DEFAULT;

    public static NetworkManagerType getType(TCPNetworkManager networkManager) {
        if (networkManager instanceof TCPFSTNetworkManager)
            return FST;
        if (networkManager instanceof TCPDefaultNetworkManager)
            return DEFAULT;
        else
            return DEFAULT;
    }
};