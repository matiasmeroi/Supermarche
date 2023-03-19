package supermarche

import supermarche.SuperMarche.Companion.NEVER_EXPIRES
import supermarche.sectors.DistributionCenter
import supermarche.sectors.Store

object PrettyPrinter {

    fun print(game : SuperMarche) {
        println("<SuperMarche>")

        tabln("GameState: ${game.state.gameState}")

        tabln("Money: ${game.state.money}")
        tabln("Round: ${game.state.round}")

        tabln("Die 1,2,sum: (${game.state.die1}, ${game.state.die2}, ${game.state.die1 + game.state.die2})")

        printStore(game.state.store!!)
        printDistributionCenter(game.state.distributionCenter!!)
        printContainer(game.state.stockRoom!!, "Stock room")

        tabln("Queue size: ${game.state.customerQueue.size}")
        tabln("Distribution Cards left: ${game.state.distributionCardQueue.size}")

        printDrawnCustomers(game.state.drawnCustomers)

        if(game.state.shoppingCustomer == null) tabln("No customer shopping")
        else tabln("Customer shopping: ${game.state.shoppingCustomer!!.info.name}")

        println("</SuperMarche>")
    }

    private fun printQueue(s: String, queue:CustomerQueue) {
        tab(s)
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val customer = iterator.next()
            print("${customer.info.name}, ")
        }
        println()
    }

    private fun printDistributionCenter(distributionCenter: DistributionCenter) {
        tabln("<Distribution center>")
        for(i in Item.values()) {
            if(i != Item.NO_ITEM) {
                tabln("$i: ${distributionCenter.items[i]!!}", 2)
            }
        }
        tabln("</Distribution center>")
    }

    private fun printContainer(container: ItemContainer, name : String = "Container", tabOffset : Int = 0) {
        tabln("<$name>")
        tabln("Size: ${container.size}", 2)
        for(i in container.getIterationIndices()) {

            if(i == NEVER_EXPIRES) tab("Never expires: ", 2 + tabOffset)
            else tab("Expires after round $i: ", 2 + tabOffset)

            val iterator = container.containers[i]!!.iterator()
            while (iterator.hasNext()) {
                val stack = iterator.next()
                print("{${stack.item}, ${stack.quantity}}, ")

            }
            print("\n")
        }
        tabln("</$name>")
    }

    private fun printStore(store: Store) {
        tabln("<Store>")
        tabln("Size: ${store.getSize()}", 2)
        for(item in Item.values())
        {
            if(item != Item.NO_ITEM) {
                printContainer(store.getContainer(item), item.toString(), 1)
            }
        }
        tabln("</Store>")
    }

    fun printDrawnCustomers(queue: CustomerQueue) {
        tabln("<Customers>")
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val customer = iterator.next()
            tabln(customer.description(), 2)
        }
        tabln("</Customers>")
    }

    private fun tabln(str : String, nTabs : Int = 1) {
        for(i in 1..nTabs) print("\t")
        println(" $str")
    }

    private fun tab(str : String, nTabs : Int = 1) {
        for(i in 1..nTabs) print("\t")
        print(" $str")
    }

}