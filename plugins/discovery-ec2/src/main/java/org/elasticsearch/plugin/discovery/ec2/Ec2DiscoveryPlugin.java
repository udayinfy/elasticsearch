/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.plugin.discovery.ec2;

import org.elasticsearch.cloud.aws.AwsEc2ServiceImpl;
import org.elasticsearch.cloud.aws.Ec2Module;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.discovery.DiscoveryModule;
import org.elasticsearch.discovery.ec2.AwsEc2UnicastHostsProvider;
import org.elasticsearch.discovery.ec2.Ec2Discovery;
import org.elasticsearch.plugins.Plugin;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class Ec2DiscoveryPlugin extends Plugin {
    
    static {
        // This internal config is deserialized but with wrong access modifiers,
        // cannot work without suppressAccessChecks permission right now. We force
        // a one time load with elevated privileges as a workaround.
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    Class.forName("com.amazonaws.internal.config.InternalConfig$Factory");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to initialize internal aws config", e);
                }
                return null;
            }
        });
    }

    @Override
    public String name() {
        return "discovery-ec2";
    }

    @Override
    public String description() {
        return "EC2 Discovery Plugin";
    }

    @Override
    public Collection<Module> nodeModules() {
        Collection<Module> modules = new ArrayList<>();
        modules.add(new Ec2Module());
        return modules;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        services.add(AwsEc2ServiceImpl.class);
        return services;
    }

    public void onModule(DiscoveryModule discoveryModule) {
        discoveryModule.addDiscoveryType("ec2", Ec2Discovery.class);
        discoveryModule.addUnicastHostProvider(AwsEc2UnicastHostsProvider.class);
    }
}
