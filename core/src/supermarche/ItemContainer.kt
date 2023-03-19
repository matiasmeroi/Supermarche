package supermarche

import supermarche.sectors.ItemStack
import supermarche.sectors.StockRoom
import java.util.*

abstract class ItemContainer(private val game : SuperMarche.SuperMarcheState) {

    companion object {
        const val UNLIMITED_CAPACITY = -1
    }

    val containers : Hashtable<Int, ItemStackList> =
        Hashtable<Int, ItemStackList>()

    var size = 0

    init {
        initialize()
    }

    fun initialize() {
        for(i in getIterationIndices()) {
            containers.put(i, ItemStackList())
        }
    }

    fun isEmpty() : Boolean {
        return size == 0
    }

    fun isFull() : Boolean {
        return size == getCapacity()
    }

    fun hasRoomFor(quantity: Int) : Boolean {
        return spaceLeft() >= quantity
    }

    fun spaceLeft() : Int {
        return getCapacity() - size
    }

    /**
     * Siempre se busca desde los productos que van a expirar proximamente hacia
     * los que duran más
     * */

    fun containsItem(item: Item) : Boolean {
        return containsItem(item, 1)
    }
    fun containsItem(item : Item, quantity : Int) : Boolean {
        var count = 0

        for(i in getIterationIndices()) {
            for(stack in containers[i]!!) {
                if(stack.item == item)
                    count += stack.quantity
            }
        }

        return count >= quantity
    }

    fun removeItem(item: Item) {
        removeItem(item, 1)
    }
    fun removeItem(item : Item, quantity : Int) {
        assert(size - quantity >= 0)
        var removedCount = 0

        for(i in getIterationIndices()) {

            val iterator = containers[i]!!.iterator()
            while (iterator.hasNext() && removedCount < quantity) {
                val stack = iterator.next()

                if (stack.item == item) {
                    val qttToRemove = Integer.min(stack.quantity, quantity - removedCount)
                    stack.quantity -= qttToRemove
                    removedCount += qttToRemove
                    if (stack.quantity <= 0) iterator.remove()
                }

            }
        }

        assert(removedCount == quantity)
        size -= removedCount
        assert(size >= 0)
    }

    fun addItem(item: Item) : Boolean {
        return addItem(item, 1)
    }
    fun addItem(item : Item, quantity: Int) : Boolean{
        if(getCapacity() != UNLIMITED_CAPACITY && size + quantity > StockRoom.CAPACITY) return false

        val index = SuperMarche.getExpirationRound(item, game)
//        println("$index, ${getIterationIndices().toString()}")

        var added = false
        for(stack in containers[index]!!) {
            if(stack.item == item) {
                added = true
                stack.quantity += quantity
                break
            }
        }

        if(!added) {
            containers[index]!!.add(ItemStack(item, quantity))
        }

        size += quantity

        return true
    }

    fun countExpiresAfter(item: Item, round : Int) : Int {
        if(round !in getIterationIndices()) {
            error("Round not valid")
        }


        var count = 0

        for(i in getIterationIndices()) {

            if(i == round) {

                for(stack in containers[round]!!) {
                    if(item == stack.item) count += stack.quantity
                }

            }

        }

        return count
    }

    abstract fun getCapacity() : Int
    abstract fun getIterationIndices() : Array<Int>;

}