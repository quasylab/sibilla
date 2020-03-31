package quasylab.sibilla.core.server.master;

/**
 * All the possible command that can be sent from a master
 */
public enum MasterCommand {
    INIT, PING, TASK, RESULTS, PONG, INIT_RESPONSE, DATA_RESPONSE;
}
