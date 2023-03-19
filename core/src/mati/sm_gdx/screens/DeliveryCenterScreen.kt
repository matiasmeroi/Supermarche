package mati.sm_gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import mati.sm_gdx.Constants
import mati.sm_gdx.Constants.HEIGHT
import mati.sm_gdx.Constants.TOKEN_LABEL_COLOR
import mati.sm_gdx.SM
import mati.sm_gdx.Utils
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import supermarche.GameState
import supermarche.Item
import java.awt.image.renderable.ContextualRenderedImageFactory
import java.util.*

class DeliveryCenterScreen(context : SM) : MyScreen(context)  {

    companion object {
        const val X1 = 185f
        const val X2 = 613f
        const val X3 = 1011f
        const val Y1 = HEIGHT - 247f
        const val Y2 = HEIGHT - 540f
        const val Y3 = HEIGHT - 315f

        const val DONE_X = 680f
        const val DONE_Y = HEIGHT - 630f
    }

    private val stage = Stage(
        ScalingViewport(
            Scaling.stretch,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat(),
            OrthographicCamera()
        ), context.getBatch()
    )

    val buyProduce = VisTextButton("Buy")
    val buyBakery = VisTextButton("Buy")
    val buyDairy = VisTextButton("Buy")
    val buyDry = VisTextButton("Buy")
    val buyFrozen = VisTextButton("Buy")
    val doneButton = VisTextButton("Done")

    init {
        buyProduce.setPosition(X1, Y1)
        buyBakery.setPosition(X1, Y2)
        buyDairy.setPosition(X2, Y1)
        buyDry.setPosition(X2, Y2)
        buyFrozen.setPosition(X3, Y3)
        doneButton.setPosition(DONE_X, DONE_Y)

        buyProduce.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().buyFromDistributionCenter(Item.PRODUCE)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        buyBakery.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().buyFromDistributionCenter(Item.BAKERY)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        buyDairy.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().buyFromDistributionCenter(Item.DAIRY)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        buyDry.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().buyFromDistributionCenter(Item.DRY_GOODS)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        buyFrozen.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().buyFromDistributionCenter(Item.FROZEN)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        doneButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().endDeliveryPhase()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        stage.addActor(buyProduce)
        stage.addActor(buyBakery)
        stage.addActor(buyDairy)
        stage.addActor(buyDry)
        stage.addActor(buyFrozen)
        stage.addActor(doneButton)
    }

    override fun show() {
        context.multiplexer().addProcessor(stage)
    }

    private fun updateButtons() {
        if(context.game().state.gameState != GameState.DELIVERY_PHASE) {
            buyProduce.isVisible = false
            buyBakery.isVisible = false
            buyDairy.isVisible = false
            buyDry.isVisible = false
            buyFrozen.isVisible = false

            doneButton.isVisible = false
        } else {
            val prices = context.game().state.currentDistributionCard!!.prices
            buyProduce.setText("Buy $${prices[Item.PRODUCE]}"); buyProduce.pack()
            buyBakery.setText("Buy $${prices[Item.BAKERY]}"); buyBakery.pack()
            buyDairy.setText("Buy $${prices[Item.DAIRY]}"); buyDairy.pack()
            buyDry.setText("Buy $${prices[Item.DRY_GOODS]}"); buyDry.pack()
            buyFrozen.setText("Buy $${prices[Item.FROZEN]}"); buyFrozen.pack()

            buyProduce.isVisible = true
            buyBakery.isVisible = true
            buyDairy.isVisible = true
            buyDry.isVisible = true
            buyFrozen.isVisible = true

            doneButton.isVisible = true
        }
    }

    override fun render(delta: Float) {
        updateButtons()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val distCenter = context.game().state.distributionCenter!!

        context.getBatch().begin()
        context.getBatch().draw(context.getAssets().get(Constants.DIST_CENTER_TEXTURE, Texture::class.java), 0f, 0f)

        for(item in Item.values()) {

            if(item != Item.NO_ITEM) {
                val pos = Constants.getDistributionCenterPosition(item)
                val quantity = distCenter.items[item]!!

                if(quantity != 0) {
                    Utils.drawStack(context, pos, item, quantity)
                }

            }

        }
        context.getBatch().end()

        stage.draw()

    }

    override fun hide() {
        context.multiplexer().removeProcessor(stage)
    }

    override fun dispose() {
        stage.dispose()
    }
}