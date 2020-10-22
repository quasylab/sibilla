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

package it.unicam.quasylab.sibilla.core.network.util;

import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import it.unicam.quasylab.sibilla.core.network.communication.UDPNetworkManagerType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class used to create startup classes for new masters, slaves and servers.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class StartupUtils {

    /**
     * @param args from the console
     * @return Map containing all the console startup args and the related values
     */
    public static Map<String, String> parseOptions(String[] args) {
        final Map<String, String> options = new HashMap<>();

        String optionArgument = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return null;
                }
                optionArgument = a;
            } else if (optionArgument != null) {
                options.put(optionArgument.substring(1), a);
                optionArgument = null;
            }
        }
        return options;
    }

    /**
     * @param type name of the {@link TCPNetworkManagerType} to obtain
     * @return {@link TCPNetworkManagerType} related to the name passed as argument
     */
    public static TCPNetworkManagerType TCPNetworkManagerParser(String type) {
        return TCPNetworkManagerType.valueOf(type);
    }

    /**
     * @param type name of the {@link UDPNetworkManagerType} to obtain
     * @return {@link UDPNetworkManagerType} related to the name passed as argument
     */
    public static UDPNetworkManagerType UDPNetworkManagerParser(String type) {
        return UDPNetworkManagerType.valueOf(type);
    }

}
