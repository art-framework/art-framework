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

import com.google.common.base.Strings;
import io.artframework.*;
import io.artframework.conf.Constants;
import io.artframework.conf.TriggerConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.*;

@SuppressWarnings("unused")
@Accessors(fluent = true)
public class DefaultTriggerContext extends AbstractArtObjectContext<Trigger> implements TriggerContext, Context {

    @Getter
    private final List<ActionContext<?>> actions = new ArrayList<>();
    @Getter
    private final List<RequirementContext<?>> requirements = new ArrayList<>();
    private final Map<Class<?>, Set<TriggerListener<?>>> listeners = new HashMap<>();
    @Getter
    private final Map<String, Variable<?>> variables = new HashMap<>();
    @Getter
    private final TriggerFactory factory;
    private final ConfigMap artObjectConfig;
    @Getter
    private final TriggerConfig config;
    private final Trigger trigger;

    public DefaultTriggerContext(
            @NonNull Scope scope,
            @NonNull TriggerConfig config,
            @NonNull TriggerFactory factory,
            @NonNull ConfigMap artObjectConfig) {
        super(scope, factory.meta());
        this.config = config;
        this.factory = factory;
        this.artObjectConfig = artObjectConfig;
        this.trigger = null;
    }

    public DefaultTriggerContext(@NonNull Scope scope, ArtObjectMeta<Trigger> information, Trigger trigger, TriggerConfig config) {

        super(scope, information);
        this.config = config;
        this.trigger = trigger;
        this.factory = null;
        this.artObjectConfig = null;
    }

    @Override
    public @NonNull String uniqueId() {

        if (Strings.isNullOrEmpty(config().identifier())) {
            return super.uniqueId();
        }

        return this.config().identifier();
    }

    @Override
    public void addAction(ActionContext<?> action) {

        this.actions.add(action);
    }

    @Override
    public void addRequirement(RequirementContext<?> requirement) {

        this.requirements.add(requirement);
    }

    public <TTarget> Trigger trigger(Target<TTarget> target, ExecutionContext<TriggerContext> context) {

        if (trigger != null) {
            return trigger;
        } else {
            return factory().create(artObjectConfig.resolve(scope(), target, context));
        }
    }

    @Override
    public void enable() {

        scope().configuration().trigger().register(this);
    }

    @Override
    public void disable() {

        scope().configuration().trigger().unregister(this);
    }

    @Override
    public void trigger(final Target<?>... targets) {

        trigger(ExecutionContext.of(
                scope(),
                this,
                targets
        ).next(this), targets);
    }

    @Override
    public void trigger(ExecutionContext<TriggerContext> context, Target<?>... targets) {

        Runnable runnable = () -> {
            // only execute the trigger for any target if the provided requirement matches
            if (!Arrays.stream(targets).allMatch(target -> testTriggerRequirement(target, context))) {
                return;
            }

            for (Target<?> target : targets) {
                if (cannotExecute(target)) continue;

                if (testRequirements(context).success()) {

                    if (increaseAndCheckCount(target)) {
                        store(target, Constants.Storage.LAST_EXECUTION, System.currentTimeMillis());

                        if (config().executeActions()) {
                            executeActions(target, context);
                        }

                        callListeners(context);
                    }
                }
            }
        };

        long delay = this.config().delay();
        if (configuration().scheduler().isPresent() && delay > 0) {
            configuration().scheduler().get().runTaskLater(runnable, delay);
        } else {
            runnable.run();
        }
    }

    @SuppressWarnings("unchecked")
    private <TTarget> boolean testTriggerRequirement(Target<TTarget> target, ExecutionContext<TriggerContext> context) {

        Trigger trigger = trigger(target, context);
        if (trigger instanceof Requirement) {
            RequirementContext<?> requirementContext = RequirementContext.of(scope(), meta().get(), (Requirement<Object>) trigger, config);
            if (requirementContext.isTargetType(target)) {
                return ((RequirementContext<TTarget>) requirementContext).test(target, context.next((RequirementContext<TTarget>) requirementContext)).success();
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private <TTarget> void callListeners(ExecutionContext<TriggerContext> executionContext) {
        for (Map.Entry<Class<?>, Set<TriggerListener<?>>> entry : listeners.entrySet()) {
            Target<TTarget>[] targets = executionContext.targets().stream()
                    .filter(target -> entry.getKey().isInstance(target.source()))
                    .toArray(Target[]::new);

            entry.getValue().stream()
                    .map(listener -> (TriggerListener<TTarget>) listener)
                    .forEach(listener -> {
                        for (Target<TTarget> target : targets) {
                            listener.onTrigger(target, executionContext);
                        }
                    });
        }
    }

    @Override
    public <TTarget> TriggerContext addListener(Class<TTarget> targetClass, TriggerListener<TTarget> listener) {

        if (!listeners.containsKey(targetClass)) {
            listeners.put(targetClass, new HashSet<>());
        }

        listeners.get(targetClass).add(listener);

        return this;
    }

    @Override
    public TriggerContext addListener(TriggerListener<Object> listener) {

        addListener(Object.class, listener);

        return this;
    }

    @Override
    public <TTarget> TriggerContext removeListener(TriggerListener<TTarget> listener) {

        listeners.values().forEach(triggerListeners -> triggerListeners.remove(listener));

        return this;
    }

    private <TTarget> boolean cannotExecute(Target<TTarget> target) {

        return wasExecutedOnce(target) || isOnCooldown(target);
    }

    private <TTarget> boolean increaseAndCheckCount(Target<TTarget> target) {

        if (config().count() < 1) return true;

        int count = count(target) + 1;
        store(target, Constants.Storage.COUNT, count);

        return count >= config().count();
    }

    private <TTarget> int count(Target<TTarget> target) {

        if (config().count() < 1) return 0;

        return store(target, Constants.Storage.COUNT, Integer.class).orElse(0);
    }

    /**
     * Checks if the {@link DefaultActionContext} has the execute_once option
     * and already executed once for the {@link Target}.
     *
     * @param target target to check
     * @param <TTarget> target type
     * @return true if action was already executed and should only execute once
     */
    private <TTarget> boolean wasExecutedOnce(Target<TTarget> target) {

        return this.config().executeOnce() && getLastExecution(target) > 0;
    }

    /**
     * Checks if the action is on cooldown for the given {@link Target}.
     * Will always return false if no cooldown is defined (set to zero).
     *
     * @param target target to check
     * @param <TTarget> target type
     * @return true if action is on cooldown
     */
    private <TTarget> boolean isOnCooldown(Target<TTarget> target) {
        long cooldown = this.config().cooldown();
        if (cooldown < 1) return false;

        long lastExecution = getLastExecution(target);

        if (lastExecution < 1) return false;

        return System.currentTimeMillis() < lastExecution + cooldown;
    }

    private <TTarget> long getLastExecution(Target<TTarget> target) {
        return store(target, Constants.Storage.LAST_EXECUTION, Long.class).orElse(0L);
    }
}
