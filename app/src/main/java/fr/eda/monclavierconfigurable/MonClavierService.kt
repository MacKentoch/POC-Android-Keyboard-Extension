package fr.eda.monclavierconfigurable
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import android.content.Context

class MonClavierService : InputMethodService() {

    private var keyboardLayout: KeyboardLayout? = null

    override fun onCreate() {
        super.onCreate()
        keyboardLayout = KeyboardParser.parse(this, "keyboard_layout.json")
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            val windowToken = window!!.window!!.attributes.token
            val owner = ComposeWindowLifecycleOwner.get(windowToken)
            owner.attachToDecorView(this)
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)

            setContent {
                keyboardLayout?.let { layout ->
                    ClavierView(
                        layout = layout,
                        onToucheClick = { keyModel ->
                            handleKeyPress(keyModel)
                        }
                    )
                } ?: run {
                    Text("Erreur : Impossible de charger le clavier.")
                }
            }
        }
    }

    private fun handleKeyPress(key: KeyModel) {
        val inputConnection = currentInputConnection ?: return

        when (key.action) {
            KeyAction.CHARACTER -> inputConnection.commitText(key.value, 1)
            KeyAction.SPACE -> inputConnection.commitText(" ", 1)
            KeyAction.BACKSPACE -> inputConnection.deleteSurroundingText(1, 0)
            KeyAction.ENTER -> inputConnection.commitText("\n", 1)
            KeyAction.NEXT_KEYBOARD -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    switchToNextInputMethod(false)
                } else {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val token = window.window?.attributes?.token
                    @Suppress("DEPRECATION")
                    imm.switchToNextInputMethod(token, false)
                }
            }
        }
    }
}