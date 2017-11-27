package hamburg.remme.tinygit.gui

import hamburg.remme.tinygit.git.LocalFile
import hamburg.remme.tinygit.gui.builder.FontAwesome
import hamburg.remme.tinygit.gui.builder.addClass
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode

class FileStatusView : ListView<LocalFile> {

    constructor() : super() {
        selectionModel.selectionMode = SelectionMode.SINGLE
    }

    constructor(list: ObservableList<LocalFile>, selectionMode: SelectionMode) : super(list) {
        selectionModel.selectionMode = selectionMode
    }

    init {
        addClass("file-status-view")
        setCellFactory { LocalFileListCell() }
    }

    private class LocalFileListCell : ListCell<LocalFile>() {

        override fun updateItem(item: LocalFile?, empty: Boolean) {
            super.updateItem(item, empty)
            text = item?.path
            graphic = when (item?.status) {
                LocalFile.Status.CONFLICT -> FontAwesome.exclamationTriangle("#d9534f")
                LocalFile.Status.ADDED -> FontAwesome.plus("#5cb85c")
                LocalFile.Status.RENAMED -> FontAwesome.edit("#5bc0de")
                LocalFile.Status.MODIFIED -> FontAwesome.pencil("#f0ad4e")
                LocalFile.Status.CHANGED -> FontAwesome.pencil("#f0ad4e")
                LocalFile.Status.REMOVED -> FontAwesome.minus("#d9534f")
                LocalFile.Status.MISSING -> FontAwesome.minus("#999")
                LocalFile.Status.UNTRACKED -> FontAwesome.question("#5bc0de")
                else -> null
            }
        }

    }

}
