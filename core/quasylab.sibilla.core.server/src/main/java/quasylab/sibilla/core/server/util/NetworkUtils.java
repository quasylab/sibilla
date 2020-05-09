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

package quasylab.sibilla.core.server.util;

import java.net.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class NetworkUtils {

    /**
     * Returns the local IPV4 address of the machine
     *
     * @return the address
     * @throws SocketException TODO Exception
     */
    public static InetAddress getLocalAddress() throws SocketException {
        return NetworkInterface.networkInterfaces().filter(networkInterface -> {
            try {
                return !networkInterface.isLoopback() && networkInterface.isUp();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return false;
        }).findFirst().map(NetworkInterface::getInterfaceAddresses).get().stream().filter(interfaceAddress -> interfaceAddress.getAddress() instanceof Inet4Address).findFirst().get().getAddress();
    }

    public static List<InetAddress> getBroadcastAddresses() throws SocketException {
        return NetworkInterface.networkInterfaces().filter(networkInterface -> {
            try {
                return !networkInterface.isLoopback() && networkInterface.isUp();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return false;
        }).map(networkInterface -> networkInterface.getInterfaceAddresses().stream()
                .map(InterfaceAddress::getBroadcast).filter(Objects::nonNull).collect(Collectors.toList())).reduce((list1, list2) -> {
            list1.addAll(list2);
            return list1;
        }).get();
    }


}
