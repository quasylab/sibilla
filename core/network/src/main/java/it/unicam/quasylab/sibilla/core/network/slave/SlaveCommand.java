/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.network.slave;

/**
 * All the possible command and signals that can be sent from a slave server.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public enum SlaveCommand {
    /**
     * The command sent by a slave server respond to a master server ping command.
     */
    PONG,
    /**
     * The command sent by a slave server to respond to a master server init command.
     */
    INIT_RESPONSE,
    /**
     * The command sent by a slave server to inform that the connection with an host will be closed.
     */
    CLOSE_CONNECTION
}
