package supermarche.sectors

import supermarche.ItemContainer
import supermarche.SuperMarche
import supermarche.SuperMarche.Companion.NEVER_EXPIRES

class StockRoom(game: SuperMarche.SuperMarcheState) : ItemContainer(game) {

    companion object {
        const val CAPACITY = 20
    }

    override fun getCapacity(): Int {
        return CAPACITY
    }

    override fun getIterationIndices(): Array<Int> {
        return arrayOf(1, 2, 3, 4, 5, 6, NEVER_EXPIRES)
    }


}