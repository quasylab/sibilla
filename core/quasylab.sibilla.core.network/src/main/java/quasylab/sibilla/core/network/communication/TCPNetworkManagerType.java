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

package quasylab.sibilla.core.network.communication;

/**
 * All of the names associated to the classes that implement {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} and are based upon the TCP transport protocol.
 *
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public enum TCPNetworkManagerType implements NetworkManagerType {
    /**
     * The simplest interface implementation.
     */
    DEFAULT,
    /**
     * The implementation that relies upon SSL.
     */
    SECURE
};