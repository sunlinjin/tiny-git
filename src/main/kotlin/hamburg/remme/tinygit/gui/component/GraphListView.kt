package hamburg.remme.tinygit.gui.component

import hamburg.remme.tinygit.TinyGit
import hamburg.remme.tinygit.domain.Branch
import hamburg.remme.tinygit.domain.Commit
import hamburg.remme.tinygit.gui.builder.addClass
import hamburg.remme.tinygit.gui.builder.hbox
import hamburg.remme.tinygit.gui.builder.label
import hamburg.remme.tinygit.gui.builder.vbox
import hamburg.remme.tinygit.gui.component.skin.GraphListViewSkin
import hamburg.remme.tinygit.shortDateTimeFormat
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView

private const val DEFAULT_STYLE_CLASS = "graph-list-view"
private const val COMMIT_STYLE_CLASS = "commitId"
private const val DATE_STYLE_CLASS = "date"
private const val BRANCHES_STYLE_CLASS = "branches"
private const val MESSAGE_STYLE_CLASS = "message"
private const val AUTHOR_STYLE_CLASS = "author"
private const val BADGE_STYLE_CLASS = "badge"
private const val DETACHED_STYLE_CLASS = "detached"
private const val CURRENT_STYLE_CLASS = "current"

/**
 * This view has some heavy interaction with [GraphListViewSkin] but is still loosely coupled, as it would
 * work with the default list skin, just without Git log graph.
 *
 * The actual log graph is calculated asynchronously by [TinyGit.commitLogService] when the log changes.
 */
class GraphListView(commits: ObservableList<Commit>) : ListView<Commit>(commits) {

    var graphWidth: Double
        get() = graphPadding.get().left
        set(value) = graphPadding.set(Insets(0.0, 0.0, 0.0, value))
    var isGraphVisible
        get() = graphVisible.get()
        set(value) = graphVisible.set(value)
    val logGraph = TinyGit.commitLogService.logGraph
    private val service = TinyGit.branchService
    private val graphVisible = object : SimpleBooleanProperty(true) {
        override fun invalidated() = refresh()
    }
    private val graphPadding = SimpleObjectProperty<Insets>(Insets.EMPTY)

    init {
        addClass(DEFAULT_STYLE_CLASS)
        setCellFactory { CommitLogListCell() }
        service.head.addListener { _ -> refresh() }
        service.branches.addListener(ListChangeListener { refresh() })
    }

    override fun createDefaultSkin() = GraphListViewSkin(this)

    /**
     * This rather complex list cell is displaying brief information about the commit.
     * It will show its ID, commit time, message and author.
     *
     * It will also display any branch pointing to the commit.
     *
     * The [ListCell] will have a left padding bound to [GraphListView.padding] to leave space for the graph
     * that is drawn by the [GraphListViewSkin].
     *
     * @todo branches all have the same color which is not synchronized with the log graph
     */
    private inner class CommitLogListCell : ListCell<Commit>() {

        private val MAX_LENGTH = 60
        private val commitId = label { addClass(COMMIT_STYLE_CLASS) }
        private val date = label {
            addClass(DATE_STYLE_CLASS)
            graphic = Icons.calendar()
        }
        private val badges = hbox { addClass(BRANCHES_STYLE_CLASS) }
        private val message = label { addClass(MESSAGE_STYLE_CLASS) }
        private val author = label {
            addClass(AUTHOR_STYLE_CLASS)
            graphic = Icons.user()
        }

        init {
            graphic = vbox {
                paddingProperty().bind(graphPadding)
                +hbox {
                    alignment = Pos.CENTER_LEFT
                    +commitId
                    +date
                    +badges
                }
                +hbox {
                    alignment = Pos.CENTER_LEFT
                    +message
                    +author
                }
            }
        }

        override fun updateItem(item: Commit?, empty: Boolean) {
            super.updateItem(item, empty)
            graphic.isVisible = !empty
            item?.let { c ->
                commitId.text = c.shortId
                date.text = c.date.format(shortDateTimeFormat)
                badges.children.setAll(service.branches.filter { it.id == c.id }.toBadges())
                message.text = c.shortMessage
                author.text = c.authorName
            }
        }

        private fun List<Branch>.toBadges(): List<Node> {
            return map {
                label {
                    addClass(BADGE_STYLE_CLASS)
                    if (service.isDetached(it)) addClass(DETACHED_STYLE_CLASS)
                    else if (service.isHead(it)) addClass(CURRENT_STYLE_CLASS)
                    text = it.name.abbrev()
                    graphic = if (service.isDetached(it)) Icons.locationArrow() else Icons.codeFork()
                }
            }
        }

        private fun String.abbrev() = if (length > MAX_LENGTH) "${substring(0, MAX_LENGTH)}..." else this

    }

}