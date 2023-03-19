package mati.sm_gdx.screens

import com.badlogic.gdx.Screen
import mati.sm_gdx.SM

abstract class MyScreen(val context: SM) : Screen{

    override fun resize(width: Int, height: Int) {}

    override fun pause() { }

    override fun resume() {}
}