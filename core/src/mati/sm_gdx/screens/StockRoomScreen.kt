package mati.sm_gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.kotcrab.vis.ui.widget.VisTextButton
import mati.sm_gdx.Constants
import mati.sm_gdx.SM
import mati.sm_gdx.Utils
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import supermarche.GameState
import supermarche.Item

class StockRoomScreen(context : SM) : MyScreen(context) {

    companion object {
        const val X = 50f
        const val Y1 = 580F
        const val Y2 = 530F
        const val Y3 = 160F
        const val Y4 = 110F
        const val Y5 = 60F

        const val DONE_X = 680f
        const val DONE_Y = Constants.HEIGHT - 630f

        const val SALE_Y = 550f
        const val SALE_START_X = 250f
        const val SALE_SEP = 160f
        const val SALE_Y_2 = 140f

    }

    private val stage = Stage(
        ScalingViewport(
            Scaling.stretch,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat(),
            OrthographicCamera()
        ), context.getBatch()
    )

    private val moveProduce = VisTextButton("<--- Produce")
    private val moveBakery = VisTextButton("<--- Bakery")
    private val moveDairy = VisTextButton("<-- Dairy")
    private val moveDry = VisTextButton("<--- Dry Goods")
    private val moveFrozen = VisTextButton("<--- Frozen")
    private val doneStocking = VisTextButton("Done")

    private val saleProduce = VisTextButton("Produce on sale: $${Item.PRODUCE.salePrice}")
    private val saleBakery = VisTextButton("Bakery on sale: $${Item.BAKERY.salePrice}")
    private val saleDairy = VisTextButton("Dairy on sale: $${Item.DAIRY.salePrice}")
    private val saleDry = VisTextButton("Dry goods on sale: $${Item.DRY_GOODS.salePrice}")
    private val saleFrozen = VisTextButton("Frozen on sale: $${Item.FROZEN.salePrice}")

    init {
        moveProduce.setPosition(X, Y1)
        moveBakery.setPosition(X, Y2)
        moveDairy.setPosition(X, Y3)
        moveDry.setPosition(X, Y4)
        moveFrozen.setPosition(X, Y5)
        doneStocking.setPosition(DONE_X, DONE_Y)

        saleProduce.setPosition(SALE_START_X, SALE_Y)
        saleBakery.setPosition(SALE_START_X + SALE_SEP * 1, SALE_Y)
        saleDairy.setPosition(SALE_START_X + SALE_SEP * 2, SALE_Y)
        saleDry.setPosition(SALE_START_X + SALE_SEP * 0, SALE_Y_2)
        saleFrozen.setPosition(SALE_START_X + SALE_SEP * 1 + 20, SALE_Y_2)

        moveProduce.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().fromStockToStore(Item.PRODUCE)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        moveBakery.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().fromStockToStore(Item.BAKERY)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        moveDairy.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().fromStockToStore(Item.DAIRY)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        moveDry.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().fromStockToStore(Item.DRY_GOODS)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        moveFrozen.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().fromStockToStore(Item.FROZEN)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        doneStocking.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().endStockingPhase()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        saleProduce.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().putOnSale(Item.PRODUCE)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        saleBakery.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().putOnSale(Item.BAKERY)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        saleDairy.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().putOnSale(Item.DAIRY)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        saleDry.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().putOnSale(Item.DRY_GOODS)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        saleFrozen.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().putOnSale(Item.FROZEN)
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })


        stage.addActor(moveProduce)
        stage.addActor(moveBakery)
        stage.addActor(moveDairy)
        stage.addActor(moveDry)
        stage.addActor(moveFrozen)
        stage.addActor(doneStocking)

        stage.addActor(saleProduce)
        stage.addActor(saleBakery)
        stage.addActor(saleDairy)
        stage.addActor(saleDry)
        stage.addActor(saleFrozen)
    }

    override fun show() {
        context.multiplexer().addProcessor(stage)
    }

    private fun updateButtons() {
        when(context.game().state.gameState) {
            GameState.STOCKING_PHASE,
            GameState.RESTOCKING_PHASE,
            GameState.RESTOCKING_PHASE_WHILE_SHOPPING -> {
                moveProduce.isVisible = true
                moveBakery.isVisible = true
                moveDairy.isVisible = true
                moveDry.isVisible = true
                moveFrozen.isVisible = true
                doneStocking.isVisible = true
            }
            else -> {
                moveProduce.isVisible = false
                moveBakery.isVisible = false
                moveDairy.isVisible = false
                moveDry.isVisible = false
                moveFrozen.isVisible = false
                doneStocking.isVisible = false
            }
        }

        if(context.game().state.gameState == GameState.STOCKING_PHASE) {
            saleProduce.isVisible = !context.game().state.wasOnSale[Item.PRODUCE]!!
            saleBakery.isVisible = !context.game().state.wasOnSale[Item.BAKERY]!!
            saleDairy.isVisible = !context.game().state.wasOnSale[Item.DAIRY]!!
            saleDry.isVisible = !context.game().state.wasOnSale[Item.DRY_GOODS]!!
            saleFrozen.isVisible = !context.game().state.wasOnSale[Item.FROZEN]!!
        } else {
            saleProduce.isVisible = false
            saleBakery.isVisible = false
            saleDairy.isVisible = false
            saleDry.isVisible = false
            saleFrozen.isVisible = false
        }
    }

    override fun render(delta: Float) {
        updateButtons()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        context.getBatch().begin()

        context.getBatch().draw(context.getAssets().get(Constants.STOCK_ROOM_TEXTURE, Texture::class.java), 0f, 0f)

        val stockRoom = context.game().state.stockRoom!!
        for(i in stockRoom.getIterationIndices()) {

            val pos = Constants.getStockRoomPosition(i)
            Utils.drawStackList(context, pos, stockRoom.containers[i]!!, 5)

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