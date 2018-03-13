package hamburg.remme.tinygit.gui

import hamburg.remme.tinygit.TinyGit
import hamburg.remme.tinygit.domain.Commit
import hamburg.remme.tinygit.domain.service.CommitLogService
import hamburg.remme.tinygit.domain.service.TaskListener
import hamburg.remme.tinygit.gui.builder.Action
import hamburg.remme.tinygit.gui.builder.ActionGroup
import hamburg.remme.tinygit.gui.builder.HBoxBuilder
import hamburg.remme.tinygit.gui.builder.addClass
import hamburg.remme.tinygit.gui.builder.comboBox
import hamburg.remme.tinygit.gui.builder.confirmWarningAlert
import hamburg.remme.tinygit.gui.builder.contextMenu
import hamburg.remme.tinygit.gui.builder.errorAlert
import hamburg.remme.tinygit.gui.builder.label
import hamburg.remme.tinygit.gui.builder.managedWhen
import hamburg.remme.tinygit.gui.builder.progressIndicator
import hamburg.remme.tinygit.gui.builder.splitPane
import hamburg.remme.tinygit.gui.builder.stackPane
import hamburg.remme.tinygit.gui.builder.toolBar
import hamburg.remme.tinygit.gui.builder.vbox
import hamburg.remme.tinygit.gui.builder.vgrow
import hamburg.remme.tinygit.gui.builder.visibleWhen
import hamburg.remme.tinygit.gui.component.GraphListView
import hamburg.remme.tinygit.gui.component.Icons
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.layout.Priority
import javafx.scene.text.Text

class CommitLogView : Tab() {

    private val state = TinyGit.state
    private val logService = TinyGit.commitLogService
    private val branchService = TinyGit.branchService
    private val window get() = content.scene.window
    private val graph = GraphListView(logService.commits)
    private val graphSelection get() = graph.selectionModel.selectedItem

    init {
        text = "Commit Log (beta)"
        graphic = Icons.list()
        isClosable = false

        // TODO: should be exposed and used in the menu bar as well?!
        val checkoutCommit = Action("Checkout", { Icons.check() }, disable = state.canCheckoutCommit.not(),
                handler = { checkoutCommit(graphSelection) })
        val resetToCommit = Action("Reset", { Icons.refresh() }, disable = state.canResetToCommit.not(),
                handler = { resetToCommit(graphSelection) })
        val tagCommit = Action("Tag", { Icons.tag() }, disable = state.canTagCommit.not(),
                handler = { tagCommit(graphSelection) })

        graph.items.addListener(ListChangeListener { graph.selectionModel.selectedItem ?: graph.selectionModel.selectFirst() })
        graph.selectionModel.selectedItemProperty().addListener { _, _, it -> logService.activeCommit.set(it) }
        graph.contextMenu = contextMenu {
            isAutoHide = true
            +ActionGroup(checkoutCommit, resetToCommit)
        }
        // TODO: too buggy and needy right now.
//        graph.setOnScroll {
//            // TODO: buggy
//            if (it.deltaY < 0) {
//                val index = graph.items.size - 1
//                service.logMore()
//                graph.scrollTo(index)
//            }
//        }
//        graph.setOnKeyPressed {
//            if (it.code == KeyCode.DOWN && graph.selectionModel.selectedItem == graph.items.last()) {
//                service.logMore()
//                graph.scrollTo(graph.selectionModel.selectedItem)
//            }
//        }

        val indicator = FetchIndicator()
        content = vbox {
            +toolBar {
                +indicator
                addSpacer()
                +comboBox<CommitLogService.CommitType> {
                    items.addAll(CommitLogService.CommitType.values())
                    valueProperty().bindBidirectional(logService.commitType)
                    valueProperty().addListener { _, _, it -> graph.isGraphVisible = !it.isNoMerges }
                }
                +comboBox<CommitLogService.Scope> {
                    items.addAll(CommitLogService.Scope.values())
                    valueProperty().bindBidirectional(logService.scope)
                }
            }
            +stackPane {
                vgrow(Priority.ALWAYS)
                +splitPane {
                    addClass("log-view")
                    vgrow(Priority.ALWAYS)
                    +graph
                    +CommitDetailsView()
                }
                +stackPane {
                    addClass("overlay")
                    visibleWhen(Bindings.isEmpty(graph.items))
                    +Text("There are no commits.")
                }
            }
        }

        logService.logListener = indicator
        logService.logErrorHandler = { errorAlert(window, "Cannot Fetch From Remote", "Please check the repository settings.\nCredentials or proxy settings may have changed.") }
    }

    private fun checkoutCommit(commit: Commit) {
        branchService.checkoutCommit(
                commit,
                { errorAlert(window, "Cannot Checkout Commit", "There are local changes that would be overwritten by checkout.\nCommit or stash them.") })
    }

    private fun resetToCommit(commit: Commit) {
        if (!confirmWarningAlert(window, "Reset Branch", "Reset", "This will reset the current branch to '${commit.shortId}'.\nAll uncommitted changes will be lost.")) return
        branchService.reset(commit)
    }

    private fun tagCommit(commit: Commit) {
        // TODO
    }

    private class FetchIndicator : HBoxBuilder(), TaskListener {

        private val visible = SimpleBooleanProperty()

        init {
            visibleWhen(visible)
            managedWhen(visibleProperty())
            spacing = 6.0
            alignment = Pos.CENTER
            +progressIndicator(6.0)
            +label { +"Fetching..." }
        }

        override fun started() = visible.set(true)

        override fun done() = visible.set(false)

    }

}
