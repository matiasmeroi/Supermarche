package mati.sm_gdx.audio

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import mati.sm_gdx.SM

object AudioManager {

    private var currentMusic: AudioType? = null
    private lateinit var context : SM

    fun load(context: SM) {
        for (audioType in AudioType.values()) {
            context.getAssets().load(
                audioType.filePath,
                if (audioType.isMusic) Music::class.java else Sound::class.java

            )
            println("Loaded $audioType")
        }
        this.context = context
    }

    fun play(audioType: AudioType) {
        if (audioType.isMusic) {
            if (currentMusic === audioType) {
                return
            } else if (currentMusic != null) {
                context.getAssets().get(
                    currentMusic!!.filePath,
                    Music::class.java
                ).stop()
            }
            val music: Music = context.getAssets().get(
                audioType.filePath,
                Music::class.java
            )
            music.volume = audioType.volume
            music.isLooping = true
            music.play()
            currentMusic = audioType
        } else if(!audioType.isMusic) {
            context.getAssets().get(audioType.filePath, Sound::class.java)
                .play(audioType.volume)
        }
    }

    fun stopMusic() {
        if(currentMusic != null) {
            context.getAssets().get(currentMusic!!.filePath, Music::class.java).stop()
            currentMusic = null
        }
    }

}