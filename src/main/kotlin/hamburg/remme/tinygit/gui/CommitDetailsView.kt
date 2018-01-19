package hamburg.remme.tinygit.gui

import hamburg.remme.tinygit.domain.service.CommitDetailsService
import hamburg.remme.tinygit.domain.service.CommitLogService
import hamburg.remme.tinygit.gui.builder.SplitPaneBuilder
import hamburg.remme.tinygit.gui.builder.addClass
import hamburg.remme.tinygit.gui.builder.splitPane
import hamburg.remme.tinygit.gui.builder.stackPane
import hamburg.remme.tinygit.gui.builder.toolBar
import hamburg.remme.tinygit.gui.builder.vbox
import hamburg.remme.tinygit.gui.builder.vgrow
import hamburg.remme.tinygit.gui.builder.visibleWhen
import hamburg.remme.tinygit.gui.builder.webView
import javafx.beans.binding.Bindings
import javafx.scene.layout.Priority
import javafx.scene.text.Text

class CommitDetailsView : SplitPaneBuilder() {

    init {
        addClass("commit-details-view")

        val files = FileStatusView(CommitDetailsService.commitStatus).vgrow(Priority.ALWAYS)

        +splitPane {
            +webView {
                isContextMenuEnabled = false
                CommitDetailsService.commitDetails.addListener { _, _, it -> engine.loadContent(it) }
            }
            +stackPane {
                +vbox {
                    +toolBar { +StatusCountView(files) }
                    +files
                }
                +stackPane {
                    addClass("overlay")
                    visibleWhen(Bindings.isEmpty(files.items))
                    +Text("This commit has no changes.")
                }
            }
        }
        +FileDiffView(files.selectionModel.selectedItemProperty(), CommitLogService.activeCommit)
    }

}
