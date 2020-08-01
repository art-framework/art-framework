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

package io.artframework.events;

import io.artframework.ResultStatus;
import io.artframework.Target;
import io.artframework.TargetedTestResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DefaultTargetedTestResult<TTarget> extends DefaultTestResult implements TargetedTestResult<TTarget> {

    private final Target<TTarget> target;
    private final Class<TTarget> targetClass;

    public DefaultTargetedTestResult(@NonNull ResultStatus result, Target<TTarget> target) {
        this(result, target, null, null);
    }

    @SuppressWarnings("unchecked")
    public DefaultTargetedTestResult(@NonNull ResultStatus result, Target<TTarget> target, @Nullable String[] failureReasons, @Nullable String[] errorReasons) {
        super(result, target, failureReasons, errorReasons);
        this.target = target;
        this.targetClass = (Class<TTarget>) target.source().getClass();
    }

    public DefaultTargetedTestResult(@NonNull ResultStatus result, Class<TTarget> targetClass) {
        this(result, targetClass, null, null);
    }

    public DefaultTargetedTestResult(@NonNull ResultStatus result, Class<TTarget> targetClass, @Nullable String[] failureReasons, @Nullable String[] errorReasons) {
        super(result, failureReasons, errorReasons);
        this.target = null;
        this.targetClass = targetClass;
    }

    @Override
    public Optional<Target<TTarget>> getTarget() {
        return Optional.ofNullable(target);
    }
}