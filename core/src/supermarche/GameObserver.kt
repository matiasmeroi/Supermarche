package supermarche

interface GameObserver {

    companion object {
        const val DELIM = "::::"
    }

    fun onGameEvent(event: GameEvent, msg : String)
}