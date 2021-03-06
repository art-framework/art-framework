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

package io.artframework.impl;

import io.artframework.*;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Collection;

@Log(topic = "art-framework")
public class DefaultArtProvider extends AbstractProvider implements ArtProvider {

    public DefaultArtProvider(@NonNull Scope scope) {
        super(scope);
    }

    @Override
    public ArtProvider addAll(Collection<ArtObjectMeta<?>> artObjects) {

        for (ArtObjectMeta<?> artObject : artObjects) {
            if (Action.class.isAssignableFrom(artObject.artObjectClass())) {
                actions().add(artObject.get());
            } else if (Requirement.class.isAssignableFrom(artObject.artObjectClass())) {
                requirements().add(artObject.get());
            } else if (Trigger.class.isAssignableFrom(artObject.artObjectClass())) {
                trigger().add(artObject.get());
            }
        }

        return this;
    }
}
