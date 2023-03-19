package mati.sm_gdx.audio

enum class AudioType(val filePath: String, val isMusic: Boolean, val volume: Float) {

    MAIN_1_MUSIC("audio/music/main_1.mp3", true, 1f),
//    MAIN_2_MUSIC("audio/music/main_2.mp3", true, 0.3f),
    DICE_ROLL_SOUND("audio/sounds/dice_rolled.wav", false, 1.3f),
    CARD_DRAWN_SOUND("audio/sounds/card_draw.mp3", false, 1.1f),
    BUTTON_CLICKED_SOUND("audio/sounds/button_click.mp3", false, 1f),
}