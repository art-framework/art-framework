package net.silthus.art.api.actions;

import java.util.Collection;

public interface ActionHolder {

    void addAction(ActionContext<?, ?> action);

    Collection<ActionContext<?, ?>> getActions();

    @SuppressWarnings("unchecked")
    default <TTarget> void executeActions(TTarget target) {
        getActions().stream()
                .filter(actionContext -> actionContext.isTargetType(target))
                .map(actionContext -> (ActionContext<TTarget, ?>) actionContext)
                .forEach(actionContext -> actionContext.execute(target));
    }
}