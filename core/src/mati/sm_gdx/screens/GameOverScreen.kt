package mati.sm_gdx.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import mati.sm_gdx.Constants.GAME_OVER_TEXTURE
import mati.sm_gdx.Constants.WIDTH
import mati.sm_gdx.SM

class GameOverScreen(context : SM) : MyScreen(context) {

    var money = 0
    override fun show() {

    }

    override fun render(delta: Float) {
        if(Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) context.changeScreen(SM.ScreenType.MENU)

        context.getBatch().begin()
        context.getBatch().draw(context.getAssets().get(GAME_OVER_TEXTURE, Texture::class.java), 0f, 0f)
        context.font().setColor(0f, 0f, 0f, 1f)
        context.font().draw(context.getBatch(), "Money: $$money", WIDTH - 300, 200F)
        context.getBatch().end()
    }

    override fun hide() {
    }

    override fun dispose() {
    }

}