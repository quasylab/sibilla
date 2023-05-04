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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;

import java.util.Map;
import java.util.TreeMap;

/**
 * This is a utility class that is used to collect all the declared tag in a Slam system.
 */
public class MessageRepository {

    private final Map<String, MessageTag> tags;

    /**
     * Creates an empty message repository.
     */
    public MessageRepository() {
        this.tags = new TreeMap<>();
    }


    /**
     * Returns a new tag having the given name that is associated with content of the given type. An {@link IllegalArgumentException}
     * is thrown if a tag with the same name already exists in this repository.
     *
     * @param tagName tag name.
     * @param content type of message content.
     * @return the new created tag.
     */
    public synchronized MessageTag addTag(String tagName, SlamType[] content) {
        if (tags.containsKey(tagName)) {
            throw new IllegalArgumentException();
        }
        MessageTag tag = new MessageTag(tags.size(), tagName, content);
        tags.put(tagName, tag);
        return tag;
    }


    /**
     * Returns the tag associated with the given name if it exists in this repository.
     *
     * @param tagName tag name
     * @return the tag associated with the given name if it exists in this repository.
     */
    public synchronized MessageTag getTag(String tagName) {
        return tags.get(tagName);
    }


    /**
     * Returns true if a tag with the given name is stored in this repository.
     *
     * @param tagName tag name.
     * @return true if a tag with the given name is stored in this repository.
     */
    public boolean exists(String tagName) {
        return tags.containsKey(tagName);
    }

}
