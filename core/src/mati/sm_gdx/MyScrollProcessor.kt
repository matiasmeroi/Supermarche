package mati.sm_gdx

import com.badlogic.gdx.InputProcessor

class MyScrollProcessor(val context : SM) : InputProcessor {

    override fun keyDown(keycode: Int): Boolean {return false}

    override fun keyUp(keycode: Int): Boolean {return false}

    override fun keyTyped(character: Char): Boolean {return false}

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {return false}

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {return false}

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {return false}

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {return false}

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        context.scrolled = amountY
        return false
    }
}