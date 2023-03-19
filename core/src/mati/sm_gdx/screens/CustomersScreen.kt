package mati.sm_gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.kotcrab.vis.ui.widget.VisImageButton
import com.kotcrab.vis.ui.widget.VisLabel
import mati.sm_gdx.Constants
import mati.sm_gdx.Constants.CUSTOMER_HEIGHT
import mati.sm_gdx.Constants.CUSTOMER_WIDTH
import mati.sm_gdx.Constants.HEIGHT
import mati.sm_gdx.SM
import mati.sm_gdx.Utils
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import supermarche.GameState

class CustomersScreen(context : SM) : MyScreen(context)  {

    companion object {
        const val CARDS_START_X = 4
        const val CARDS_TOTAL_WIDTH = 5 * Constants.CUSTOMER_WIDTH
        const val CARDS_SEP = (Constants.WIDTH - CARDS_START_X * 2 - CARDS_TOTAL_WIDTH) / 4
        const val CARDS_Y = HEIGHT / 2 - CUSTOMER_HEIGHT / 2

        var InvisibleCardDrawable : TextureRegionDrawable? = null
    }

    private val stage = Stage(
        ScalingViewport(
            Scaling.stretch,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat(),
            OrthographicCamera()
        ), context.getBatch()
    )

    private val buttons : Array<VisImageButton> = arrayOf(
        VisImageButton(InvisibleCardDrawable),
        VisImageButton(InvisibleCardDrawable),
        VisImageButton(InvisibleCardDrawable),
        VisImageButton(InvisibleCardDrawable),
        VisImageButton(InvisibleCardDrawable),)

    init{
        InvisibleCardDrawable = TextureRegionDrawable(TextureRegion(context.getAssets().get(Constants.CUSTOMER_INVISIBLE_TEXTURE, Texture::class.java)))

        for(idx in 0..4) {
            val x = CARDS_START_X + idx * CUSTOMER_WIDTH + CARDS_SEP * idx
            buttons[idx].setPosition(x, CARDS_Y)
            buttons[idx].setSize(CUSTOMER_WIDTH, CUSTOMER_HEIGHT)

            buttons[idx].addListener(object : ClickListener() {

                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    when(context.game().state.gameState) {
                        GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK -> context.game().chooseNextToShop(idx)
                        GameState.WAITING_FLIP_NEXT -> context.game().flipCard(idx)
                        else -> {}
                    }
                }

            })

            stage.addActor(buttons[idx])
            buttons[idx].isVisible = true
        }
    }

    override fun show() {
        context.multiplexer().addProcessor(stage)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.draw()

        context.getBatch().begin()
        context.getBatch().draw(context.getAssets().get(Constants.CUSTOMERS_TEXTURE, Texture::class.java), 0f, 0f)


        if(context.game().state.drawnCustomers.isNotEmpty()) {

            var idx = 0
            val iter = context.game().state.drawnCustomers.iterator()
            while(iter.hasNext()) {
                val cus = iter.next()

                val x = CARDS_START_X + idx * CUSTOMER_WIDTH + CARDS_SEP * idx
                Utils.drawCustomer(context, cus, x, CARDS_Y, false)

                idx++
            }

        }

        context.getBatch().end()

    }

    private fun getButtonIndex(b : VisImageButton) : Int {
        var idx = 0
        for(i in buttons) {
            if(i.equals(b)) return idx
            idx++
        }
        return idx
    }

    override fun hide() {
        context.multiplexer().removeProcessor(stage)
    }

    override fun dispose() {
        stage.dispose()
    }
}