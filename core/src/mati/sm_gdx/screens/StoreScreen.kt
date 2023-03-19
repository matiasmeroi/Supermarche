package mati.sm_gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import mati.sm_gdx.Constants
import mati.sm_gdx.Constants.CUSTOMER_HEIGHT
import mati.sm_gdx.Constants.CUSTOMER_WIDTH
import mati.sm_gdx.Constants.HEIGHT
import mati.sm_gdx.Constants.SALE_BAKERY_X
import mati.sm_gdx.Constants.SALE_BAKERY_Y
import mati.sm_gdx.Constants.SALE_DAIRY_X
import mati.sm_gdx.Constants.SALE_DAIRY_Y
import mati.sm_gdx.Constants.SALE_DRY_X
import mati.sm_gdx.Constants.SALE_DRY_Y
import mati.sm_gdx.Constants.SALE_FROZEN_X
import mati.sm_gdx.Constants.SALE_FROZEN_Y
import mati.sm_gdx.Constants.SALE_PRODUCE_X
import mati.sm_gdx.Constants.SALE_PRODUCE_Y
import mati.sm_gdx.SM
import mati.sm_gdx.Utils
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import supermarche.GameState
import supermarche.Item

class StoreScreen(context : SM) : MyScreen(context)  {

    companion object {
        const val CURRENT_CUSTOMER_X = 60f
        const val CURRENT_CUSTOMER_Y = HEIGHT / 2 - CUSTOMER_HEIGHT / 2
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
    private val buyButton = VisTextButton("Buy")
    private val useCouponButton = VisTextButton("Use Coupon")
    private val rollDice = VisTextButton("Roll Dice")
    private val diceLabel = VisLabel("")
    private val restockButton = VisTextButton("Restock")
    private val handleWasteButton = VisTextButton("Handle Waste")

    init {
        buyButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().continueWithNormalPurchase()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        useCouponButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().continueWithCouponPurchase()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        rollDice.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().rollDice()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        restockButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().restock()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        handleWasteButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                context.game().handleWaste()
                AudioManager.play(AudioType.BUTTON_CLICKED_SOUND)
            }
        })

        diceLabel.setColor(0f, 0f, 0f, 1f)

        table.defaults().pad(10f).center()
        table.add(buyButton)
        table.add(useCouponButton)
        table.row()
        table.add(rollDice)
        table.add(diceLabel)
        table.row()
        table.add(restockButton)
        table.add(handleWasteButton)

        table.setPosition(CURRENT_CUSTOMER_X + CUSTOMER_WIDTH / 2, CURRENT_CUSTOMER_Y - 100f)

        stage.addActor(table)
    }

    override fun show() {
        context.multiplexer().addProcessor(stage)
    }

    private fun update() {
        if(context.game().state.gameState == GameState.DICE_ROLLED) {
            diceLabel.setText("(${context.game().state.die1}, ${context.game().state.die2}, ${context.game().getDiceSum()})")
        } else {
            diceLabel.setText("(_, _, _)")
        }

        handleWasteButton.isVisible = context.game().state.gameState == GameState.WASTE_PHASE
    }

    override fun render(delta: Float) {
        update()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        context.getBatch().begin()
        context.getBatch().draw(context.getAssets().get(Constants.STORE_ROOM_TEXTURE, Texture::class.java), 0f, 0f)

        for(item in Item.values()) {
            if(item == Item.NO_ITEM) continue

            val container = context.game().state.store!!.getContainer(item)

            for(idx in container.getIterationIndices()) {
                val pos = Constants.getStorePosition(item, idx)
                Utils.drawStackList(context, pos, container.containers[idx]!!, 1)

            }

        }

        if(context.game().state.shoppingCustomer != null) {
            Utils.drawCustomer(context, context.game().state.shoppingCustomer!!,
                CURRENT_CUSTOMER_X, CURRENT_CUSTOMER_Y, true)
        }

        //sales
        val sales = context.game().state.isOnSale
        if(sales[Item.PRODUCE]!!) context.getBatch().draw(context.getSaleRegion(Item.PRODUCE), SALE_PRODUCE_X, SALE_PRODUCE_Y)
        if(sales[Item.BAKERY]!!) context.getBatch().draw(context.getSaleRegion(Item.BAKERY), SALE_BAKERY_X, SALE_BAKERY_Y)
        if(sales[Item.DAIRY]!!) context.getBatch().draw(context.getSaleRegion(Item.DAIRY), SALE_DAIRY_X, SALE_DAIRY_Y)
        if(sales[Item.DRY_GOODS]!!) context.getBatch().draw(context.getSaleRegion(Item.DRY_GOODS), SALE_DRY_X, SALE_DRY_Y)
        if(sales[Item.FROZEN]!!) context.getBatch().draw(context.getSaleRegion(Item.FROZEN), SALE_FROZEN_X, SALE_FROZEN_Y)

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