package mati.sm_gdx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import mati.sm_gdx.actors.MessageWindow
import mati.sm_gdx.actors.SalesOfferWindow
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import supermarche.*

class PlayState(val context : SM) : GameObserver, SalesOfferWindow.SalesOfferObserver {

    companion object {
        const val MONEY_X = 10f
        const val STATUS_Y = Constants.HEIGHT - 32f
    }

    private val stage = Stage(
        ScalingViewport(
            Scaling.stretch,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat(),
            OrthographicCamera()
        ), context.getBatch()
    )

    val supermarche : SuperMarche = SuperMarche()
    val salesOfferWindow = SalesOfferWindow(context)
    val msgWindow = MessageWindow()

    val scrollDelay = 15
    var scrollTimer = 0


    init {
        context.multiplexer().addProcessor(stage)
        supermarche.observer = this
        salesOfferWindow.observer = this
        salesOfferWindow.makeInvisible()
        msgWindow.makeInvisible()
        stage.addActor(salesOfferWindow)
        stage.addActor(msgWindow)
    }

    fun initializeGame(diff : SuperMarche.Difficulty) {
        supermarche.setup(diff)
        supermarche.prepareNextTurn()
    }

    fun update() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.A) || (context.scrolled < 0 && scrollTimer >= scrollDelay)) {
            scrollTimer = 0
            when(context.getCurrentScreen()) {
                SM.ScreenType.STORE -> context.changeScreen(SM.ScreenType.CUSTOMERS)
                SM.ScreenType.STOCK -> context.changeScreen(SM.ScreenType.STORE)
                SM.ScreenType.DISTRIBUTION -> context.changeScreen(SM.ScreenType.STOCK)
                else -> {}
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.D) || (context.scrolled > 0 && scrollTimer >= scrollDelay)) {
            scrollTimer = 0
            when(context.getCurrentScreen()) {
                SM.ScreenType.CUSTOMERS -> context.changeScreen(SM.ScreenType.STORE)
                SM.ScreenType.STORE -> context.changeScreen(SM.ScreenType.STOCK)
                SM.ScreenType.STOCK -> context.changeScreen(SM.ScreenType.DISTRIBUTION)
                else -> {}
            }
        }

        if(scrollTimer < scrollDelay) scrollTimer += 1

        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) context.changeScreen(SM.ScreenType.CUSTOMERS)
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) context.changeScreen(SM.ScreenType.STORE)
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) context.changeScreen(SM.ScreenType.STOCK)
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) context.changeScreen(SM.ScreenType.DISTRIBUTION)
    }

    private fun getStateText(s: GameState) : String {
        return when(s) {
            GameState.SETUP -> "Setup"

            GameState.PREPARE_PHASE -> "Preparing next turn"

            GameState.DELIVERY_PHASE -> "Delivery Phase"

            GameState.RESTOCKING_PHASE_WHILE_SHOPPING,
            GameState.RESTOCKING_PHASE,
            GameState.STOCKING_PHASE -> "Stocking Phase"

            GameState.WAITING_FLIP_NEXT -> "Flip a card"

            GameState.OFFER_ITEMS_ON_SALE -> "Offer Item On Sale"

            GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK -> "Choose next customer"

            GameState.WAITING_DICE_ROLL_OR_RESTOCK -> "Roll dice"

            GameState.PURCHASE_FAILED_NO_STOCK,
            GameState.PURCHASE_FAILED_RESTOCK,
            GameState.NORMAL_SALE_BUY,
            GameState.PURCHASE_SUCCESSFUL,
            GameState.NORMAL_BUY,
            GameState.COUPON_BUY,
            GameState.DICE_ROLLED -> "CustomerPhase"
            GameState.WASTE_PHASE -> "Waste Phase"
            GameState.GAME_END -> "Game Over"
        }
    }

    fun render() {
        val st = getStateText(context.game().state.gameState)
        val rn = "Round: ${context.game().state.round}"
        val m = "Money: $${context.game().state.money}"
        val statusString = "$st; $rn; $m"

        context.getBatch().begin()
        context.font().setColor(0f, 0f, 0f, 1f)
        context.font().draw(context.getBatch(), statusString, MONEY_X, STATUS_Y)
        context.getBatch().end()

        stage.draw()
    }

    override fun onGameEvent(event: GameEvent, msg: String) {
        when(event) {
            GameEvent.OFFER_SALES -> {
                salesOfferWindow.makeVisible()
            }

            GameEvent.GAME_STATE_CHANGE -> switchScreensOnStateChange(context.game().state.gameState)

            GameEvent.SETUP -> {}
            GameEvent.CUSTOMERS_DRAWN -> {}
            GameEvent.DISTRIBUTION_CARD_DRAWN -> {}
            GameEvent.ROUND_CHANGED -> {}
            GameEvent.CARD_FLIPPED -> {
                if(context.game().state.gameState == GameState.WAITING_FLIP_NEXT)
                    AudioManager.play(AudioType.CARD_DRAWN_SOUND)
            }
            GameEvent.DICE_ROLLED -> { AudioManager.play(AudioType.DICE_ROLL_SOUND) }
            GameEvent.MONEY_LOST -> {}
            GameEvent.MONEY_EARNED -> {}
            GameEvent.DISTRIBUTION_CENTER_UPDATED -> {}
            GameEvent.STOCK_ROOM_UPDATED -> {}
            GameEvent.NEXT_CUSTOMER_CHOSEN -> {}
            GameEvent.OFFERED_SALES_ACCEPTED -> {}
            GameEvent.OFFERED_SALES_DECLINED -> {}
            GameEvent.BUYING_ITEM -> {}
            GameEvent.BUYING_ADDITIONAL_ITEM -> {}
            GameEvent.PURCHASE_COMMITED -> showMessage("Purchase Completed. Total: $$msg")
            GameEvent.PURCHASE_FAILED_NO_ITEM -> showMessage("Purchase failed. No $msg in store")
            GameEvent.PURCHASE_FAILED_RESTOCK -> showMessage("Purchase failed!. ${context.game().state.shoppingCustomer!!.info.name} spent $msg")
            GameEvent.PROFIT_INCREASED -> {}
            GameEvent.PROFIT_CLEARED -> {}
            GameEvent.PENALTY_APPLIED -> showMessage("Penalty applied: $$msg")
            GameEvent.NO_ITEMS_WASTED -> showMessage("No items wasted this round")
            GameEvent.ITEMS_WASTED -> showMessage("Items wasted: $msg. Penalty: $$msg")
            GameEvent.MESSAGE -> showMessage(msg)
            GameEvent.STOCK_PHASE_END -> context.changeScreen(SM.ScreenType.STORE)
            GameEvent.GAME_WON -> {
                context.endGame(true, msg.toInt())
            }
            GameEvent.GAME_LOST -> {
                context.endGame(false, msg.toInt())
            }
        }
    }

    private fun switchScreensOnStateChange(state: GameState) {
        when(state) {
            GameState.DELIVERY_PHASE -> context.changeScreen(SM.ScreenType.DISTRIBUTION)
            GameState.STOCKING_PHASE -> context.changeScreen(SM.ScreenType.STOCK)
            GameState.RESTOCKING_PHASE -> context.changeScreen(SM.ScreenType.STOCK)
//            GameState.WAITING_FLIP_NEXT -> context.changeScreen(SM.ScreenType.CUSTOMERS)
//            GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK -> context.changeScreen(SM.ScreenType.CUSTOMERS)
            GameState.OFFER_ITEMS_ON_SALE -> context.changeScreen(SM.ScreenType.STORE)
            GameState.WAITING_DICE_ROLL_OR_RESTOCK -> context.changeScreen(SM.ScreenType.STORE)
            GameState.RESTOCKING_PHASE_WHILE_SHOPPING -> context.changeScreen(SM.ScreenType.STOCK)
            GameState.WASTE_PHASE -> context.changeScreen(SM.ScreenType.STORE)
            GameState.GAME_END -> {}
            else -> {}
        }
    }

    private fun showMessage(msg: String) {
        if(msgWindow.isVisible) {
            msgWindow.addMessage(msg)
        } else {
            msgWindow.makeVisible(msg)
        }
    }

    override fun onSalesOfferChosen(str: String) {
        when(str) {
            Item.NO_ITEM.toString() -> context.game().declineInitialSaleOffer()
            Item.PRODUCE.toString() -> context.game().acceptInitialSaleOffer(Item.PRODUCE)
            Item.BAKERY.toString() -> context.game().acceptInitialSaleOffer(Item.BAKERY)
            Item.DAIRY.toString() -> context.game().acceptInitialSaleOffer(Item.DAIRY)
            Item.DRY_GOODS.toString() -> context.game().acceptInitialSaleOffer(Item.DRY_GOODS)
            Item.FROZEN.toString() -> context.game().acceptInitialSaleOffer(Item.FROZEN)
            else -> { error("guat??") }
            }
        salesOfferWindow.makeInvisible()
    }

}