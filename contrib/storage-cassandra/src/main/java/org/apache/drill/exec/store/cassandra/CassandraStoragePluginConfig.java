/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.cassandra;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.drill.common.logical.StoragePluginConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@JsonTypeName(CassandraStoragePluginConfig.NAME)
public class CassandraStoragePluginConfig extends StoragePluginConfig {
    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CassandraStoragePluginConfig.class);

    public static final String NAME = "cassandra";
    public Map<String, Object> config;

    @JsonIgnore
    private List<String> hosts;

    @JsonIgnore
    private int port;

    @JsonCreator
    public CassandraStoragePluginConfig(@JsonProperty("config") Map<String, Object> config) {
        this.config = config;
        if(config==null){
            config = Maps.newHashMap();
            return;
        }

        this.hosts = (ArrayList<String>)this.config.get(DrillCassandraConstants.CASSANDRA_CONFIG_HOSTS);
        this.port = (Integer)this.config.get(DrillCassandraConstants.CASSANDRA_CONFIG_PORT);
    }

    @Override
    public boolean  equals(Object that) {
        if (this == that) {
            return true;
        } else if (that == null || getClass() != that.getClass()) {
            return false;
        }
        CassandraStoragePluginConfig thatConfig = (CassandraStoragePluginConfig) that;
        return (this.hosts.equals(thatConfig.hosts)) && (this.port==thatConfig.port);

    }

    @Override
    public int hashCode() {
        return this.hosts != null ? this.hosts.hashCode() : 0;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public int getPort(){ return port; }
}