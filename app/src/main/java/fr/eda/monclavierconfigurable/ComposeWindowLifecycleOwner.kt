package fr.eda.monclavierconfigurable

import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import java.util.WeakHashMap

/**
 * A LifecycleOwner for a ComposeView that is hosted in a window that is not a regular Activity.
 * This is the case for InputMethodService.
 *
 * This class is essential for Jetpack Compose to work correctly inside an InputMethodService,
 * as it provides the necessary Lifecycle, ViewModelStore, and SavedStateRegistry owners.
 */
class ComposeWindowLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    // --- Lifecycle ---
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // --- ViewModelStore ---
    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    // --- SavedStateRegistry ---
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    /**
     * Attaches this lifecycle owner to a view's window.
     * It relies on the view being attached to a window to start the lifecycle, and detached to stop it.
     */
    fun attachToDecorView(view: View?) {
        if (view == null) {
            // If the view is null, it means we are destroying everything.
            destroy()
            return
        }

        // Add a listener to the view to get notified when it's attached to a window.
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                // When the view is attached, start the lifecycle.
                // Restore saved state if any.
                savedStateRegistryController.performRestore(null)
                // Move lifecycle to CREATED, then STARTED, then RESUMED.
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            override fun onViewDetachedFromWindow(v: View) {
                // When the view is detached, stop the lifecycle and clean up.
                destroy()
            }
        })
    }

    /**
     * Must be called when the view is detached or the service is destroyed.
     * This method transitions the lifecycle down and clears the ViewModelStore.
     */
    fun destroy() {
        // Transition the lifecycle down.
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        // Clear all ViewModels.
        viewModelStore.clear()
    }

    companion object {
        // A weak map to hold instances of the lifecycle owner per window token.
        // WeakHashMap prevents memory leaks by allowing the garbage collector to reclaim
        // keys (window tokens) when they are no longer in use.
        private val owners = WeakHashMap<Any, ComposeWindowLifecycleOwner>()

        /**
         * Gets a singleton instance of the ComposeWindowLifecycleOwner for a given window token.
         * This ensures that the same lifecycle owner is reused for the same window.
         */
        fun get(windowToken: Any): ComposeWindowLifecycleOwner {
            return owners.getOrPut(windowToken) {
                ComposeWindowLifecycleOwner()
            }
        }
    }
}