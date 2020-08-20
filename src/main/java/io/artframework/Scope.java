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

import io.artframework.impl.DefaultScope;

import java.util.List;
import java.util.function.Consumer;

public interface Scope {

    static Scope defaultScope() {

        return new DefaultScope();
    }

    static Scope of(Consumer<Configuration.ConfigurationBuilder> config) {
        return new DefaultScope(config);
    }

    static Scope of(Configuration configuration) {
        return new DefaultScope(configuration);
    }

    /**
     * The current configuration of this scope.
     * <p>
     * The configuration is never null but may change during the lifetime of the scope.
     * Directly access the configuration from the scope when you need it and do not cache it in a variable.
     * <p>
     * The configuration may change when the scope is being bootstrapped, but should stay the same after
     * bootstrapping has finished.
     *
     * @return the current configuration of this scope
     */
    Configuration configuration();

    ArtContext load(List<String> list);
}