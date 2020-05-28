/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.master;

/**
 * All the possible command and signals that can be sent from a master server.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public enum MasterCommand {
    /**
     * The command sent by a master server to initiate a new connection over the network.
     */
    INIT,
    /**
     * The command sent by a master server to ping an host which has a connection open with.
     */
    PING,
    /**
     * The command sent by a master server to inform that a batch of tasks will be sent over the network connection.
     */
    TASK,
    /**
     * The command sent by a master server to inform that a batch of simulations' results will be sent over the network connection.
     */
    RESULTS,
    /**
     * The command sent by a master server to reply to a ping request received by an host.
     */
    PONG,
    /**
     * The command sent by a master server that has received an INIT command from a client.
     */
    INIT_RESPONSE,
    /**
     * The command sent by a master server that has received a DATA command from a client.
     */
    DATA_RESPONSE,
    /**
     * The command sent by a master server to inform that the connection with an host will be closed.
     */
    CLOSE_CONNECTION
}
