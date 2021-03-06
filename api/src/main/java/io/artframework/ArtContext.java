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

import io.artframework.conf.ArtSettings;
import io.artframework.impl.DefaultArtContext;
import io.artframework.parser.flow.FlowParser;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * The art-context is a central component of the art-framework and allows you to
 * use your parsed actions, requirements and trigger.
 * <p>The art-context is created by loading (parsing) your configuration into valid art-objects.
 * Use the {@link Scope#load(String, Collection)} and {@link ART#load(String, Collection)} methods for parsing your config.
 * <p>Depending on your intention you can then use the methods of this context for one of the following actions:
 * <p><ul>
 *     <li>{@link #execute(Object...)} all {@link Action}s in this context for the given target.
 *          This will also check any requirements that are configured for the actions.
 *     <li>{@link #test(Object)} all {@link Requirement} in the context for the given target
 *     <li>{@link #enableTrigger()} all triggers that are configured in the context.
 *     <p>Use the {@link #onTrigger(Class, TriggerListener)} method to listen to any trigger calls
 *     invoked by this context.
 *     <p>By default every trigger that gets calls will also executed any configured actions below it.
 *     Set the {@link ArtSettings#executeActions(boolean)} flag in the {@link #settings()} to control the behaviour.
 *     <p><b>Important:</b> call {@link #disableTrigger()} when you dispose the context to allow garbage collection to run.
 * </ul>
 * Here is an example on how to load an ArtContext from a {@code List<String>} using the default {@link FlowParser}
 * <pre>{@code
 * try {
 *     ArtContext context = ART.load(config.getStringList("rewards"));
 *     context.execute(player);
 * } catch (ParseException e) {
 *     e.printStacktrace();
 * }
 * }</pre>
 */
public interface ArtContext extends Context, ResultCreator, TargetCreator {

    ///
    /// ART instance related methods
    ///

    static ArtContext empty() {
        return of(new ArrayList<>());
    }

        static ArtContext of(Scope scope, ArtSettings settings, Collection<ArtObjectContext<?>> art) {
        return new DefaultArtContext(scope, settings, art);
    }

    static ArtContext of(Scope scope, Collection<ArtObjectContext<?>> art) {
        return of(scope, scope.settings().artSettings(), art);
    }

    static ArtContext of(ArtSettings settings, Collection<ArtObjectContext<?>> art) {
        return of(ART.globalScope(), settings, art);
    }

    static ArtContext of(Collection<ArtObjectContext<?>> art) {
        return of(ART.globalScope(), art);
    }

    /**
     * Provides settings used in this {@link ArtContext}.
     * Use these settings to fine tune the executing and testing
     * of {@link ArtObject}s in this {@link ArtContext}.
     * <p>
     * By default the {@link Scope#settings()} will be used
     * you can override those settings by updating the underlying object.
     *
     * @return settings that control the behaviour of this {@link ArtContext}
     */
    ArtSettings settings();

    /**
     * Gets an immutable list of the art object contexts contained within this art context.
     *
     * @return list of all contexts within this context
     */
    Collection<ArtObjectContext<?>> artContexts();

    /**
     * Tests if all requirements for the given target pass.
     * Will return false if any requirement or global filter fail.
     * Will return true if requirements are empty after filtering for the target type.
     * <p>
     * Global filters are always checked before checking requirements.
     * This means that persistent counted requirements will never be checked and increased.
     *
     * @param target    target to check. Can be null.
     * @param <TTarget> type of the target. Any requirements not matching the target type will not be checked.
     * @return true if all requirement checks and filter pass or if the list of requirements is empty (after filtering the target type).
     * false if any filter or requirement check fails.
     */
    <TTarget> CombinedResult test(@NonNull Target<TTarget> target);

    /**
     * Wraps the given target into a {@link Target} and then calls {@link #test(Target)}.
     * Returns false if no {@link Target} wrapper was found for the given source.
     *
     * @param target    target object to wrap into a {@link Target}
     * @param <TTarget> type of the target
     * @return result of {@link #test(Target)} or false if no {@link Target} wrapper exists
     * @see #test(Target)
     */
    default <TTarget> CombinedResult test(@NonNull TTarget target) {
        return configuration().targets().get(target)
                .map(this::test)
                .orElse(CombinedResult.of(empty("Target of type " + target.getClass().getSimpleName() + " not found.")));
    }

    /**
     * Executes all {@link Action}s and child actions of actions against the given target.
     * Will do nothing if the target type does not match the target type of the action.
     * <p>
     * Any {@link Requirement}s will be checked before executing
     * the actions. No action will be executed if any filter or requirement fails.
     *
     * @param target    target to execute actions against. Can be null.
     */
    FutureResult execute(@NonNull Target<?>... target);

    /**
     * Wraps the given target into a {@link Target} and then calls {@link #execute(Target...)}.
     * Does nothing if no {@link Target} wrapper was found for the given source.
     *
     * @param targets    target to execute actions for
     */
    default FutureResult execute(@NonNull Object... targets) {
        return execute(Arrays.stream(targets)
                .map(target -> configuration().targets().get(target))
                .flatMap(Optional::stream)
                .toArray(Target[]::new)
        );
    }

    /**
     * Listens on all {@link Trigger}s in the {@link ArtContext} for the given target type.
     * You can add multiple {@link TriggerListener}s of the same target type
     * and all of them will get informed.
     * <p>
     * You will only get informed of the trigger execution after all previous
     * checks have passed and after all {@link Action}s of this {@link ArtContext}
     * have been executed.
     *
     *
     * @param targetClass class of the target you wish to listen for
     * @param listener function to react to the trigger
     * @param <TTarget> type of the target
     */
    <TTarget> ArtContext onTrigger(Class<TTarget> targetClass, TriggerListener<TTarget> listener);

    /**
     * Combines this {@link ArtContext} with the given {@link ArtContext}.
     * Both contexts will keep their order and {@link ArtObjectContext}s as they are.
     * The difference is a parent {@link ArtContext} that holds both of those contexts.
     *
     * @param context The {@link ArtContext} that should be combined with this context
     * @return the new combined {@link ArtContext}
     */
    ArtContext combine(ArtContext context);

    /**
     * Enables all configured trigger in this context, registering them as listeners.
     * <p>This method must be called for triggers to work in this art-context.
     * <p>Use this method if you want to register all trigger in the config as listeners.
     * Do not use this if triggers are not intended in your config.
     *
     * @return this art context
     */
    ArtContext enableTrigger();

    /**
     * Disables all triggers in this context, unregistering them as listeners.
     * <p>Make sure to call this method once you are done with the context
     * to assure garbage collection runs and no more triggers are fired.
     *
     * @return this art context
     */
    ArtContext disableTrigger();
}
