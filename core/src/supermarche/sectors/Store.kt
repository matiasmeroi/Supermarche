package supermarche.sectors

import supermarche.Item
import supermarche.ItemContainer
import supermarche.SuperMarche
import supermarche.SuperMarche.Companion.NEVER_EXPIRES

class Store(val game: SuperMarche.SuperMarcheState) {

    companion object {
        const val CAPACITY = 15
    }

    private val produce = object : ItemContainer(game) {
        override fun getCapacity(): Int {
            return UNLIMITED_CAPACITY
        }

        override fun getIterationIndices(): Array<Int> {
            return arrayOf(1, 2, 3, 4, 5, 6)
        }
    }

    private val bakery = object : ItemContainer(game) {
        override fun getCapacity(): Int {
            return UNLIMITED_CAPACITY
        }

        override fun getIterationIndices(): Array<Int> {
            return arrayOf(2, 3, 4, 5, 6, NEVER_EXPIRES)
        }
    }


    private val dairy = object : ItemContainer(game) {
        override fun getCapacity(): Int {
            return UNLIMITED_CAPACITY
        }

        override fun getIterationIndices(): Array<Int> {
            return arrayOf(3, 4, 5, 6, NEVER_EXPIRES)
        }
    }

    private val dryGoods = object : ItemContainer(game) {
        override fun getCapacity(): Int {
            return UNLIMITED_CAPACITY
        }

        override fun getIterationIndices(): Array<Int> {
            return arrayOf(4, 5, 6, NEVER_EXPIRES)
        }
    }

    private val frozen = object : ItemContainer(game) {
        override fun getCapacity(): Int {
            return UNLIMITED_CAPACITY
        }

        override fun getIterationIndices(): Array<Int> {
            return arrayOf(NEVER_EXPIRES)
        }
    }

    init {
        for(it in Item.values()) {
            if(it != Item.NO_ITEM) {
                getContainer(it).addItem(it, 3)
            }
        }
    }


    fun getContainer(type : Item) : ItemContainer {
        return when(type) {
            Item.NO_ITEM -> {
                error("NO_ITEM get Container")
            }
            Item.PRODUCE -> produce
            Item.BAKERY -> bakery
            Item.DAIRY -> dairy
            Item.DRY_GOODS -> dryGoods
            Item.FROZEN -> frozen
        }
    }

    fun getSize() : Int {
        return produce.size + bakery.size + dairy.size + dryGoods.size + frozen.size
    }

    fun isEmpty() : Boolean {
        return getSize() == 0
    }

    fun isFull() : Boolean {
        return getSize() == CAPACITY
    }

    fun hasRoomFor(quantity: Int) : Boolean {
        return spaceLeft() >= quantity
    }

    fun spaceLeft() : Int {
        return CAPACITY - getSize()
    }

    fun containsItem(item: Item) : Boolean {
        return getContainer(item).containsItem(item)
    }

    fun containsItem(item: Item, quantity: Int) : Boolean {
        return getContainer(item).containsItem(item, quantity)
    }

}