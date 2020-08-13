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

import io.artframework.finder.ArtObjectFinder;
import io.artframework.finder.ModuleFinder;
import io.artframework.finder.TargetFinder;

import java.io.File;
import java.util.function.Predicate;

public interface Finder extends Scoped {

    static Finder[] defaults(Scope scope) {
        return new Finder[]{
                new ArtObjectFinder(scope),
                new TargetFinder(scope),
                new ModuleFinder(scope)
        };
    }

    /**
     * Tries to find and load classes from the given files our source root.
     * <p>
     * Use the {@link FinderResult#load(Scope)} method to load all found classes
     * and to register them with the {@link Scope}.
     *
     * @param file the jar file or sources root to search for classes
     * @return the result of the find operation
     */
    default FinderResult<?> findAllIn(File file) {
        return findAllIn(file, aClass -> true);
    }

    FinderResult<?> findAllIn(File file, Predicate<Class<?>> predicate);
}
