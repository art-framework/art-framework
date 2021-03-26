package io.artframework;

import io.artframework.annotations.ArtModule;
import io.artframework.annotations.OnEnable;
import io.artframework.annotations.OnLoad;
import io.artframework.annotations.OnReload;

import java.util.Optional;

public interface Module {

    /**
     * The metadata of the module is only available if using the
     * global scope and after the module has been registered.
     *
     * @return the metadata of this module
     */
    default ModuleMeta metadata() {

        return ART.scope().configuration().modules()
                .getMetadata(getClass())
                .or(() -> {
                    try {
                        return Optional.of(ModuleMeta.of(getClass()));
                    } catch (ArtMetaDataException e) {
                        e.printStackTrace();
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new ModuleRegistrationException(null,
                        ModuleState.INVALID_MODULE,
                        "module " + getClass().getCanonicalName() + " has an invalid registration state: no metadata found!"));
    }

    /**
     * Registers the given module with the global scope and immediately calls all methods that
     * represent the current lifecycle.
     * <p>
     * This means if modules have been enabled, this module will be loaded and then enabled.
     * If the lifecycle is still in the loading phase the module will only be loaded.
     * <p>
     * Make sure the class is annotated with @{@link ArtModule} or the registration will fail with an exception.

     * @throws ModuleRegistrationException if the registration of the module failed,
     *                                     e.g. if no {@code @ArtModule} annotation is present on the class
     *                                     or if one of the annotated methods encountered an exception.
     */
    default void register() throws ModuleRegistrationException {

        ART.register(this);
    }

    /**
     * The bootstrap method is called once on all modules before any module is loaded or enabled.
     * You can configure the {@link BootstrapScope} provide your own provider implementations.
     * <p>
     * Loading modules that require bootstrapping after the bootstrap stage is finished will fail.
     * Removing bootstrap modules and then reloading the art-framework will fail also. A complete restart is needed.
     * <p>
     * Make sure you only use this method if you really need it and are configuring parts of the art-framework.
     * If you do not use this method your module will be hot pluggable and can be loaded and unloaded on the fly without a restart.
     * <p>
     * The bootstrap lifecycle method is called exactly once in the lifecycle of the module.
     * <p>
     * Any dependencies of this module will be bootstrapped before this module.
     * The lifecycle methods of this module will never be called if this module has missing dependencies.
     * <p>
     * The class must be annotated with the @{@link ArtModule} annotation for the method to be called.
     *
     * @param scope the bootstrap scope that is loading this module
     */
    default void onBootstrap(BootstrapScope scope) throws Exception {}

    /**
     * The module is loaded after bootstrapping has finished and all configurations have been loaded and injected.
     * Use it to read values from your configuration, register your ART and setup your module.
     * <p>
     * Do not use it to start background jobs, open database connections or anything else that should
     * be running when your module is enabled.
     * Use the {@link OnEnable} lifecycle method for that.
     * <p>
     * The load lifecycle method is called exactly once in the lifecycle of the module.
     * Any reloading will happen with the @{@link OnReload} method.
     * <p>
     * Any dependencies of this module will be loaded before this module.
     * The lifecycle methods of this module will never be called if this module has missing dependencies.
     *
     * @param scope the scope that is loading this module
     */
    default void onLoad(Scope scope) throws Exception {}

    /**
     * The method will be called everytime an reload of the art-framework is requested.
     * Use it to reload your configurations and services. You should also clear any cached data to avoid memory leaks.
     * <p>
     * The reload lifecycle method may be called multiple times during the lifecycle of a module.
     *
     * @param scope the scope that is calling the reload on the module
     */
    default void onReload(Scope scope) throws Exception {}

    /**
     * The enable method is called after bootstrapping has finished and the @{@link OnLoad} method was called.
     * Use it to do the core tasks of your module, e.g. open a database connection, start services, and so on.
     * <p>
     * The enable lifecycle method is called exactly once in the lifecycle of the module.
     * Any reloading will happen with the @{@link OnReload} method.
     * <p>
     * Any dependencies of this module will be enabled before this module.
     * The lifecycle methods of this module will never be called if this module has missing dependencies.
     *
     * @param scope the scope that is enabling the module
     */
    default void onEnable(Scope scope) throws Exception {}

    /**
     * The disable method is called when your module was removed from the art-framework and gets disabled.
     * Use it to cleanup any connections, cached data, and so on to prevent memory leaks.
     * <p>
     * The disable lifecycle method is called exactly once in the lifecycle of the module.
     * Any reloading will happen with the @{@link OnReload} method.
     * <p>
     * Any modules that depend on this module will be disabled before disabling this module.
     *
     * @param scope the scope that is disabling the module
     */
    default void onDisable(Scope scope) throws Exception {}
}