package mati.sm_gdx.actors

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisWindow
import mati.sm_gdx.Constants.HEIGHT
import mati.sm_gdx.Constants.WIDTH
class MessageWindow : VisWindow("Message") {

    private val table = Table()
    private val label = VisLabel()
    private val okButton = VisTextButton("Ok")

    init {
        okButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                this@MessageWindow.makeInvisible()
            }
        })

        table.defaults().center().pad(10f)
        table.add(label)
        table.row()
        table.add(okButton)

        this.add(table)
    }

    fun makeVisible(msg: String) {
        label.setText(msg)

        packEveryThing()

        this.setPosition(WIDTH / 2 - width / 2, HEIGHT / 2 - height / 2)

        this.isVisible = true
    }

    fun addMessage(msg: String) {
        val currentText = label.text
        val newText = "$currentText\n$msg"
        label.setText(newText)
        packEveryThing()
        this.setPosition(WIDTH / 2 - width / 2, HEIGHT / 2 - height / 2)
    }

    private fun packEveryThing() {
        label.pack()
        table.pack()
        this.pack()
    }

    fun makeInvisible() {
        this.isVisible = false
    }


}