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

package quasylab.sibilla.core.network;

import quasylab.sibilla.core.network.communication.NetworkManagerType;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = -2428861423753648117L;
    private InetAddress address;
    private final int port;
    private final NetworkManagerType type;

    public NetworkInfo(InetAddress address, int port, NetworkManagerType serType) {
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
        NetworkInfo other = (NetworkInfo) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return port == other.port;
    }

    public NetworkInfo clone() {
        NetworkInfo clone = null;
        try {
            clone = (NetworkInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        try {
            assert clone != null;
            clone.address = InetAddress.getByAddress(this.address.getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return clone;
    }
}
