package hamburg.remme.tinygit.gui.dialog

import hamburg.remme.tinygit.I18N
import hamburg.remme.tinygit.gui.builder.addClass
import hamburg.remme.tinygit.gui.builder.label
import hamburg.remme.tinygit.gui.builder.managedWhen
import hamburg.remme.tinygit.gui.builder.textArea
import hamburg.remme.tinygit.gui.builder.textField
import hamburg.remme.tinygit.gui.builder.vbox
import hamburg.remme.tinygit.gui.builder.vgrow
import javafx.application.Platform
import javafx.scene.layout.Priority
import javafx.stage.Window

private const val DEFAULT_STYLE_CLASS = "text-dialog"

class TextInputDialog(ok: String, isTextArea: Boolean, window: Window) : Dialog<String>(window, I18N["dialog.input.title"], isTextArea) {

    var defaultValue: String
        get() = throw RuntimeException("Write-only property.")
        set(value) {
            input.text = value
        }
    var description: String
        get() = throw RuntimeException("Write-only property.")
        set(value) {
            label.text = value
        }
    private val label = label { managedWhen(textProperty().isNotEmpty) }
    private val input = if (isTextArea) textArea { vgrow(Priority.ALWAYS) } else textField {}

    init {
        Platform.runLater { input.requestFocus() }

        content = vbox {
            addClass(DEFAULT_STYLE_CLASS)
            +label
            +input
        }

        +DialogButton(DialogButton.ok(ok))
        +DialogButton(DialogButton.CANCEL)

        okAction = { input.text }
    }

}
