/*
 * Copyright 2020 ART-Framework Contributors (https://github.com/Silthus/art-framework)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.artframework;

import io.artframework.impl.DefaultConfiguration;
import io.artframework.impl.DefaultModuleProvider;

/**
 * The module provider handles the registration and creation of all art modules.
 * <p>
 * Use it to add your {@link Module} or load modules as JAR files from a given path.
 */
public interface ModuleProvider extends Provider {

    /**
     * Creates a new default instance of the module provider.
     *
     * @param configuration the configuration instance to use
     * @return a new default instance of the module provider
     */
    static ModuleProvider of(DefaultConfiguration configuration) {
        return new DefaultModuleProvider(configuration);
    }

    /**
     * Loads the given module into the art-framework instance.
     * <p>
     * This will try to load the configuration of the module (if one is needed),
     * check the dependencies and then call the {@link Module#onEnable(Configuration)} method.
     *
     * @param module the module that should be loaded
     * @return this module provider
     */
    ModuleProvider load(Module module);
}