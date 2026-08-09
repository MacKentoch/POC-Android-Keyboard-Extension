package fr.eda.monclavierconfigurable

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
// Assurez-vous que votre thème est bien importé, par exemple :
import fr.eda.monclavierconfigurable.ui.theme.MonClavierTheme
import fr.eda.monclavierconfigurable.R

class MonClavierService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private var keyboardLayout: KeyboardLayout? = null

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore by lazy { ViewModelStore() }

    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        keyboardLayout = KeyboardParser.parse(this, "keyboard_layout.json")
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        val w = window.window ?: return
        val lp = w.attributes
        lp.height = resources.getDimensionPixelSize(R.dimen.keyboard_height)
        w.attributes = lp
        Log.d("MonClavierService", "onInitializeInterface: Hauteur de la fenêtre définie à ${lp.height} pixels.")
    }

    override fun onCreateInputView(): View {
        window.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MonClavierService)
            setViewTreeSavedStateRegistryOwner(this@MonClavierService)
            setViewTreeViewModelStoreOwner(this@MonClavierService)

            setContent {
                MonClavierTheme {
                    keyboardLayout?.let { layout ->
                        ClavierView(
                            layout = layout,
                            onToucheClick = { keyModel ->
                                handleKeyPress(keyModel)
                            }
                        )
                    } ?: run {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Text("Erreur : Impossible de charger la configuration du clavier.")
                        }
                    }
                }
            }
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private fun handleKeyPress(key: KeyModel) {
        val inputConnection = currentInputConnection ?: return

        when (key.action) {
            KeyAction.CHARACTER -> inputConnection.commitText(key.value, 1)
            KeyAction.SPACE -> inputConnection.commitText(" ", 1)
            KeyAction.BACKSPACE -> inputConnection.deleteSurroundingText(1, 0)
            KeyAction.ENTER -> inputConnection.commitText("\n", 1)
            KeyAction.NEXT_KEYBOARD -> {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
    }
}
