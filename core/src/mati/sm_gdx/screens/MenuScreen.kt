package mati.sm_gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.kotcrab.vis.ui.widget.VisList
import com.badlogic.gdx.utils.Array as GdxArray
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTextButton
import mati.sm_gdx.Constants
import mati.sm_gdx.Constants.WIDTH
import mati.sm_gdx.SM
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import supermarche.SuperMarche

class MenuScreen(context : SM) : MyScreen(context) {

    companion object {
        const val TABLE_X = WIDTH - 200F
        const val TABLE_Y = 100F
    }

    private val stage = Stage(
        ScalingViewport(
            Scaling.stretch,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat(),
            OrthographicCamera()
        ), context.getBatch()
    )

    private val table = Table()
    private val playButton = VisTextButton("Play")
    private val diffList = VisList<SuperMarche.Difficulty>()
    private val hsButton = VisTextButton("High Scores (TODO)")
    private val exitButton = VisTextButton("Exit")

    init {
        val diffs = GdxArray<SuperMarche.Difficulty>()
        diffs.add(SuperMarche.Difficulty.EASY)
        diffs.add(SuperMarche.Difficulty.NORMAL)
        diffs.add(SuperMarche.Difficulty.HARD)

        diffList.setItems(diffs)
        diffList.selectedIndex = 1

        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.startGame(diffList.selected)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        exitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        table.setPosition(TABLE_X, TABLE_Y)

        table.defaults().pad(10f).center()
        table.add(playButton)
        table.row()
        table.add(diffList)
        table.row()
        table.add(hsButton)
        table.row()
        table.add(exitButton)

        table.pack()

        stage.addActor(table)
    }

    override fun show() {
        context.multiplexer().addProcessor(stage)
    }

    override fun render(delta: Float) {
        context.getBatch().begin()
        context.getBatch().draw(context.getAssets().get(Constants.MENU_TEXTURE, Texture::class.java), 0f, 0f)
        context.getBatch().end()

        stage.draw()
    }

    override fun hide() {
        context.multiplexer().removeProcessor(stage)
    }

    override fun dispose() {

    }
}