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

package io.artframework.parser.flow;

import io.artframework.ActionFactory;
import io.artframework.ConfigMap;
import io.artframework.Scope;
import io.artframework.conf.ActionConfig;

import java.util.Iterator;
import java.util.Optional;

class ActionLineParser extends ArtObjectContextLineParser<ActionFactory<?>> {

    public ActionLineParser(Iterator<String> iterator, Scope scope) {
        super(scope, iterator, new FlowType("action", "!"));
    }

    @Override
    protected Optional<ActionFactory<?>> factory(String identifier) {
        return this.configuration().actions().get(identifier);
    }

    @Override
    protected ConfigMap configMap() {
        return ActionConfig.configMap();
    }
}
