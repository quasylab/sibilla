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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class is used to store the relevant features of the Agents declared in a model.
 */
public class AgentTable {

    private final Map<String, AgentInfo> agents = new HashMap<>();

    private final Map<String, SlamType> agentProperties = new HashMap<>();

    public SlamType getPropertyType(String name) {
        return agentProperties.getOrDefault(name, SlamType.NONE_TYPE);
    }

    public boolean isAnAgentProperty(String name) {
        return agentProperties.containsKey(name);
    }

    public boolean addAgent(String name, SlamModelParser.DeclarationAgentContext ctx) {
        if (agents.containsKey(name)) {
            return false;
        }
        this.agents.put(name, new AgentInfo(name, ctx));
        return true;
    }

    public boolean recordAgentAttribute(String agentName, String attributeName, SlamType type) {
        SlamType currentType = getPropertyType(attributeName);
        //
        return false;
    }


    public class AgentInfo {
        private final String agentName;
        private final SlamModelParser.DeclarationAgentContext ctx;

        private final Map<String, SlamType> attributes = new HashMap<>();

        private final Map<String, SlamType> views = new HashMap<>();

        private final Map<String, SlamType>  parameters = new HashMap<>();

        public AgentInfo(String agentName, SlamModelParser.DeclarationAgentContext ctx) {
            this.agentName = agentName;
            this.ctx = ctx;
        }

        public SlamModelParser.AttributeDeclarationContext getAttributeDeclaration(String name) {
            return Stream.concat(ctx.attributes.stream(), ctx.views.stream()).filter(ad -> name.equals(ad.name.getText())).findFirst().orElse(null);
        }

        public SlamModelParser.AgentParameterContext getAgentParameterDeclaration(String name) {
            return this.ctx.params.stream().filter(p -> name.equals(p.name)).findFirst().orElse(null);
        }

        public boolean isAttribute(String name) {
            return this.attributes.containsKey(name);
        }

        public boolean isView(String name) {
            return this.views.containsKey(name);
        }

        public boolean isParameter(String name) {
            return this.attributes.containsKey(name);
        }

        public SlamType getAttributeType(String name) {
            if (attributes.containsKey(name)) {
                return attributes.get(name);
            } else {
                return views.getOrDefault(name, SlamType.NONE_TYPE);
            }
        }
    }

}
