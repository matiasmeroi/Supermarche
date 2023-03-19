package mati.sm_gdx

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.kotcrab.vis.ui.VisUI
import mati.sm_gdx.Constants.FONT_FNT
import mati.sm_gdx.Constants.FONT_PNG
import mati.sm_gdx.audio.AudioManager
import mati.sm_gdx.audio.AudioType
import mati.sm_gdx.screens.*
import supermarche.Item
import supermarche.SuperMarche

class SM : Game() {

    enum class ScreenType {
        MENU, CUSTOMERS, STORE, STOCK, DISTRIBUTION, GAME_OVER
    }

    private lateinit var batch : SpriteBatch
    private lateinit var shapeRenderer : ShapeRenderer
    private lateinit var assets : AssetManager
    private lateinit var inputMultiplexer : InputMultiplexer

    private lateinit var font: BitmapFont

    private lateinit var menuScreen: MenuScreen
    private lateinit var gameOverScreen: GameOverScreen

    private lateinit var storeScreen: StoreScreen
    private lateinit var stockRoomScreen: StockRoomScreen
    private lateinit var deliveryCenterScreen: DeliveryCenterScreen
    private lateinit var customersScreen: CustomersScreen

    private lateinit var playState: PlayState

    private lateinit var currentScreen : ScreenType

    private var gameStarted = false

    var scrolled = 0f

    override fun create() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        assets = AssetManager()
        inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(MyScrollProcessor(this))
        Gdx.input.inputProcessor = inputMultiplexer

        load()

        menuScreen = MenuScreen(this)
        gameOverScreen = GameOverScreen(this)

        currentScreen = ScreenType.STORE

        changeScreen(ScreenType.MENU)
    }

    fun startGame(difficulty: SuperMarche.Difficulty) {
        storeScreen = StoreScreen(this)
        stockRoomScreen = StockRoomScreen(this)
        deliveryCenterScreen = DeliveryCenterScreen(this)
        customersScreen = CustomersScreen(this)

        playState = PlayState(this)
        playState.initializeGame(difficulty)

        AudioManager.play(AudioType.MAIN_1_MUSIC)

        gameStarted = true
    }

    fun endGame(won: Boolean, money: Int) {
        gameStarted = false
        AudioManager.stopMusic()
        gameOverScreen.money = money
        changeScreen(ScreenType.GAME_OVER)
    }

    private fun load() {
        VisUI.load()

        assets.load(Constants.MENU_TEXTURE, Texture::class.java)
        assets.load(Constants.GAME_OVER_TEXTURE, Texture::class.java)
        assets.load(Constants.STORE_ROOM_TEXTURE, Texture::class.java)
        assets.load(Constants.STOCK_ROOM_TEXTURE, Texture::class.java)
        assets.load(Constants.DIST_CENTER_TEXTURE, Texture::class.java)
        assets.load(Constants.CUSTOMERS_TEXTURE, Texture::class.java)
        assets.load(FONT_PNG, Texture::class.java)

        assets.load(Constants.TOKEN_ATLAS_PATH, TextureAtlas::class.java)

        for(i in 1..30) assets.load(Utils.getCustomerImagePath(i), Texture::class.java)
        assets.load(Constants.CUSTOMER_BACK_TEXTURE, Texture::class.java)
        assets.load(Constants.CUSTOMER_INVISIBLE_TEXTURE, Texture::class.java)

        AudioManager.load(this)

        assets.finishLoading()

        font = BitmapFont(Gdx.files.internal(FONT_FNT), TextureRegion(assets.get(FONT_PNG, Texture::class.java)))
    }

    fun game() : SuperMarche {
        return playState.supermarche
    }

    fun multiplexer() : InputMultiplexer {
        return inputMultiplexer
    }

    fun font() : BitmapFont {
        return font
    }

    fun changeScreen(type : ScreenType) {
        when(type) {
            ScreenType.MENU -> setScreen(menuScreen)
            ScreenType.STORE -> setScreen(storeScreen)
            ScreenType.STOCK -> setScreen(stockRoomScreen)
            ScreenType.DISTRIBUTION -> setScreen(deliveryCenterScreen)
            ScreenType.CUSTOMERS -> setScreen(customersScreen)
            ScreenType.GAME_OVER -> setScreen(gameOverScreen)
        }
        currentScreen = type
    }

    fun getCurrentScreen() : ScreenType {
        return currentScreen
    }

    override fun render() {
        super.render()


        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit()

        if(gameStarted) {
            playState.update()
            playState.render()
        }

        scrolled = 0f
    }

    fun getBatch() : SpriteBatch {
        return batch
    }

    fun sr() : ShapeRenderer {
        return shapeRenderer
    }

    fun getAssets() : AssetManager {
        return assets
    }

    fun tokenAtlas() : TextureAtlas {
        return assets.get(Constants.TOKEN_ATLAS_PATH, TextureAtlas::class.java)
    }

    fun getItemRegion(item : Item) : TextureRegion {
        return when(item) {
            Item.NO_ITEM -> {
                error("No item in getItemRegion")
            }
            Item.PRODUCE -> tokenAtlas().findRegion("produce")
            Item.BAKERY -> tokenAtlas().findRegion("bakery")
            Item.DAIRY -> tokenAtlas().findRegion("dairy")
            Item.DRY_GOODS -> tokenAtlas().findRegion("dry_goods")
            Item.FROZEN -> tokenAtlas().findRegion("frozen")
        }
    }

    fun getSaleRegion(item : Item) : TextureRegion {
        return when(item) {
            Item.NO_ITEM -> error("No item")
            Item.PRODUCE -> tokenAtlas().findRegion("sale_produce")
            Item.BAKERY -> tokenAtlas().findRegion("sale_bakery")
            Item.DAIRY -> tokenAtlas().findRegion("sale_dairy")
            Item.DRY_GOODS -> tokenAtlas().findRegion("sale_dry_goods")
            Item.FROZEN -> tokenAtlas().findRegion("sale_frozen")
        }
    }

    override fun dispose() {
        menuScreen.dispose()
        storeScreen.dispose()
        storeScreen.dispose()
        deliveryCenterScreen.dispose()

        batch.dispose()
        assets.dispose()
        shapeRenderer.dispose()

        VisUI.dispose()
    }
}


















