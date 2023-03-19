package supermarche

import supermarche.sectors.DistributionCenter
import supermarche.sectors.ItemStack
import supermarche.sectors.StockRoom
import supermarche.sectors.Store
import java.lang.Math.abs
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

typealias ItemStackList = ConcurrentLinkedQueue<ItemStack>
typealias CustomerQueue = ConcurrentLinkedQueue<Customer>
typealias DistributionCardQueue = ConcurrentLinkedQueue<DistributionCard>



class SuperMarche {

    companion object {
        const val TAG = "SuperMarche"

        const val TOTAL_ROUNDS = 6
        const val NEVER_EXPIRES = 0
        const val EXPIRES_EVERY_ROUND = -1

        fun getExpirationRound(item: Item, game: SuperMarcheState) : Int {
            if(item.expiresAfter == NEVER_EXPIRES) return NEVER_EXPIRES
            if(item.expiresAfter == EXPIRES_EVERY_ROUND) return game.round
            val value = game.round + item.expiresAfter - 1;
            return if(value <= TOTAL_ROUNDS) value else NEVER_EXPIRES
        }

        private fun getOnSaleHashTable() : Hashtable<Item, Boolean> {
            val res = Hashtable<Item, Boolean>()
            for(i in Item.values()) {
                if(i != Item.NO_ITEM) res.put(i, false)
            }
            return res
        }
    }

    enum class Difficulty(val initialMoney : Int) {
        EASY(30), NORMAL(15), HARD(30)
    }


    data class SuperMarcheState (
        var gameState : GameState = GameState.SETUP,

        var money : Int = 0,
        var round : Int = 0,

        var die1 : Int = 1,
        var die2 : Int = 2,

        var store : Store? = null,
        var distributionCenter: DistributionCenter? = null,
        var stockRoom: StockRoom? = null,

        var customerQueue : CustomerQueue,
        var distributionCardQueue: DistributionCardQueue,

        var drawnCustomers : CustomerQueue = CustomerQueue(),
        var currentDistributionCard: DistributionCard? = null,

        var shoppingCustomer : Customer? = null,

        var currentProfit : Int = 0,

        var restocked : Boolean = false,

        val isOnSale : Hashtable<Item, Boolean> = getOnSaleHashTable(),
        val wasOnSale: Hashtable<Item, Boolean> = getOnSaleHashTable(),
    )


    var verbose = true

    lateinit var state : SuperMarcheState
    var initialized = false
    var observer : GameObserver? = null

    // para ver si hay que incrementar round la primera vez
    var afterSetup = false

    fun setup(diff : Difficulty) {
        if(errAlreadyInitialized("setup")) return

        log("Initializing game. $diff")

        state = SuperMarcheState(
            customerQueue = InfoLoader.getCustomerQueue(),
            distributionCardQueue = InfoLoader.getDistributionCenterCards()
        )

        state.round = 1
        afterSetup = true

        state.distributionCenter = DistributionCenter()
        state.stockRoom = StockRoom(state)
        state.store = Store(state)

        log("Initial money: ${diff.initialMoney}")
        state.money = diff.initialMoney

        notify(GameEvent.SETUP)
        changeGameState(GameState.PREPARE_PHASE)
    }













    /**
     * state = PREPARE_PHASE
     * */
    fun prepareNextTurn() {
        if(errNotInState("prepareNextTurn", GameState.PREPARE_PHASE)) return

        if(!afterSetup) state.round++
        afterSetup = false

        if(state.round != 1) returnItemsBought()

        state.drawnCustomers.clear()
        draw5CustomerCards()

        state.currentDistributionCard = state.distributionCardQueue.poll()
        notify(GameEvent.DISTRIBUTION_CARD_DRAWN)

        notify(GameEvent.ROUND_CHANGED)

        state.restocked = false

        for(item in Item.values()) {
            if(item != Item.NO_ITEM) state.isOnSale[item] = false
        }

        state.shoppingCustomer = null

        val firstCardFlipped = abs(Random().nextInt() % 5)
        var secondCardFlipped = abs(Random().nextInt() % 5)
        while(secondCardFlipped == firstCardFlipped) {
            secondCardFlipped = abs(Random().nextInt() % 5)
        }

        flipCard(firstCardFlipped)
        flipCard(secondCardFlipped)

        changeGameState(GameState.DELIVERY_PHASE)
    }

    private fun returnItemsBought() {
        log("Returning Items from customers")
        for(customer in state.drawnCustomers) {
            val maxIndex = customer.info.cartSize + customer.additionalItems.size

            if(customer.optionalSaleSlot != Item.NO_ITEM) {
                state.distributionCenter!!.items[customer.optionalSaleSlot] = state.distributionCenter!!.items[customer.optionalSaleSlot]?.plus(1)
            }

            for(i in 0 until maxIndex) {
                val isInCart = i < customer.info.cartSize
                val idx = if(isInCart) i else i - customer.info.cartSize

                if((isInCart && customer.cart[idx] == Item.NO_ITEM)
                    || (!isInCart && customer.additionalItems[idx] == Item.NO_ITEM)) break

                if(isInCart) {
                    state.distributionCenter!!.items[customer.cart[idx]] = state.distributionCenter!!.items[customer.cart[idx]]?.plus(1)
                } else {
                    state.distributionCenter!!.items[customer.additionalItems[idx]] = state.distributionCenter!!.items[customer.additionalItems[idx]]?.plus(1)
                }
            }


        }
    }

    private fun draw5CustomerCards() {
        var count = 0
        val iter = state.customerQueue.iterator()
        while(count < 5 && iter.hasNext()) {
            val nextCustomer = iter.next()
            state.drawnCustomers.add(nextCustomer)
            iter.remove()
            count++
        }

        notify(GameEvent.CUSTOMERS_DRAWN)
    }

    fun flipCard(i : Int) {
        if(errNotInStates("flipCard", arrayOf(GameState.PREPARE_PHASE, GameState.WAITING_FLIP_NEXT))) return
        if(errCustomerFacingUp("flipCard", i)) return

        var index = 0
        val iter = state.drawnCustomers.iterator()
        while(iter.hasNext()) {
            val customer = iter.next()

            if(index == i) {
                customer.faceUp = true
                break
            } else {
                index++
            }
        }
        notify(GameEvent.CARD_FLIPPED)

        if(state.gameState == GameState.WAITING_FLIP_NEXT)
            changeGameState(GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK)
    }











    /**
     * state = DELIVERY_PHASE
     * */
    fun buyFromDistributionCenter(item : Item) {
        val op = "buyFromDistributionCenter"
        if(errNotInState(op, GameState.DELIVERY_PHASE)) return
        if(errIsNoItem(op, item)) return
        if(errNoItemsInDistributionCenter(op, item)) return
        if(errStockRoomFull(op)) return

        val moneyRequired = state.currentDistributionCard!!.prices[item]!!

        if(errNotEnoughMoney(op, moneyRequired)) return

        log("Buying $item from distribution center for $$moneyRequired")

        state.stockRoom!!.addItem(item)
        state.distributionCenter!!.items[item] = state.distributionCenter!!.items[item]!! - 1

        notify(GameEvent.DISTRIBUTION_CENTER_UPDATED)
        notify(GameEvent.STOCK_ROOM_UPDATED)

        loseMoney(moneyRequired)
    }

    fun endDeliveryPhase() {
        if(errNotInState("endDeliveryPhase", GameState.DELIVERY_PHASE)) return
        changeGameState(GameState.STOCKING_PHASE)
    }



/**
 * state = STOCKING_PHASE
 * */
    fun fromStockToStore(item: Item) {
        val op = "fromStockToStore"
        if (errNotInStates(
                op,
                arrayOf(GameState.STOCKING_PHASE, GameState.RESTOCKING_PHASE, GameState.RESTOCKING_PHASE_WHILE_SHOPPING)
            )
        ) return
        if(errIsNoItem(op, item)) return
        if(errStoreFull(op)) return
        if(errItemNotInContainer(op, state.stockRoom!!, item)) return

        log("Moving $item from stock room to store")

        state.stockRoom!!.removeItem(item)

        println(state.store!!)
        println(state.store!!.getContainer(item))
        state.store!!.getContainer(item).addItem(item)
    }

    fun endStockingPhase() {
        val op = "endStockingPhase"
        if(errNotInStates(op,
            arrayOf(GameState.STOCKING_PHASE, GameState.RESTOCKING_PHASE, GameState.RESTOCKING_PHASE_WHILE_SHOPPING))) return

        val itemsThatWentOnSale = countItemsPutOnSale()
        if(state.round == 6 && state.gameState == GameState.STOCKING_PHASE
            && itemsThatWentOnSale < 3) {
            log("Not enough items put on sale during the game")
            notify(GameEvent.MESSAGE, "You must put al least ${3 - itemsThatWentOnSale} more items on sale before the game ends")
            return
        }

        log("Ending stocking pahse")
        notify(GameEvent.STOCK_PHASE_END)

        when(state.gameState) {
            GameState.STOCKING_PHASE,
            GameState.RESTOCKING_PHASE -> {
                if(countCardsFacingDown() != 0 && countWaitingCardsFacingUp() < 2) {
                    changeGameState(GameState.WAITING_FLIP_NEXT)
                    log("There are cards to flip")
                } else if(countNotCompletedCards() != 0) {
                    log("There are cards waiting to buy")
                    changeGameState(GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK)
                } else {
                    log("No more cards waiting")
                    changeGameState(GameState.WASTE_PHASE)
                }
            }

            GameState.RESTOCKING_PHASE_WHILE_SHOPPING ->{
//                changeGameState(GameState.PURCHASE_FAILED_RESTOCK)
                triggerPurchaseFailRestock()
            }


            else -> errMsg(op, "When")
        }

    }

    fun restock() {
        val op = "restock"
        if(errNotInStates(op, arrayOf(GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK, GameState.WAITING_DICE_ROLL_OR_RESTOCK))) return
        if(errAlreadyRestocked(op)) return

        if(state.gameState == GameState.WAITING_DICE_ROLL_OR_RESTOCK) {
            if(state.shoppingCustomer!!.cart[0] == Item.NO_ITEM) {
                errMsg(op, "Customer must have bought at least one item before restocking")
                notify(GameEvent.MESSAGE, "Customer must have bought at least one item before restocking")
                return
            }
        }

        if(state.gameState == GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK && countCardsFacingDown() == 0) {
            val msg = "There must be al least one card facing down to discard"
            notify(GameEvent.MESSAGE, msg)
            errMsg(op, msg)
            return
        }

        if(state.gameState == GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK) {
            discardOneFacingDownCustomer()
        }

        log("Restocking...")

        state.restocked = true

        if(state.gameState == GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK) {
            changeGameState(GameState.RESTOCKING_PHASE)
        } else {
            changeGameState(GameState.RESTOCKING_PHASE_WHILE_SHOPPING)
        }
    }

    private fun discardOneFacingDownCustomer() {
        var discarded = false
        val iter = state.drawnCustomers!!.iterator()
        while(iter.hasNext() && !discarded) {
            val customer = iter.next()
            if(!customer.faceUp) {
                log("Discarding customer card")
                iter.remove()
                discarded = true
                notify(GameEvent.MESSAGE, "Customer card discarded!")
            }
        }
    }


    fun putOnSale(item : Item) {
        val op = "putOnSale"
        if(errNotInState(op, GameState.STOCKING_PHASE)) return
        if(errIsNoItem(op, item)) return
        if(errWasOnSaleBefore(op, item)) return
        if(errIsOnSale(op, item)) return

        log("Putting $item on sale")
        state.isOnSale[item] = true
        state.wasOnSale[item] = true
    }







    fun chooseNextToShop(idx: Int) {
        val op = "chooseNextToShop"
        if(errNotInState(op, GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK)) return
        if(idx !in 0..4) {
            errMsg(op, "Invalid Index. Must be 0 <= idx <= 4")
            return
        }
        if(errCustomerFacingDown(op, idx)) return
        if(errCustomerDoneShopping(op, idx)) return

        val customer = getCustomerFromIndex(idx)!!
        log("${customer.info.name} is next to shop")

        state.currentProfit = 0
        notify(GameEvent.PROFIT_CLEARED)

        state.shoppingCustomer = customer
        notify(GameEvent.NEXT_CUSTOMER_CHOSEN)

        if(isSomeItemOnSale()) {
            log("An Item is on sale. Offering it to ${customer.info.name}")
            notify(GameEvent.OFFER_SALES)
            changeGameState(GameState.OFFER_ITEMS_ON_SALE)
        } else {
            changeGameState(GameState.WAITING_DICE_ROLL_OR_RESTOCK)
        }
    }

    fun acceptInitialSaleOffer(item : Item) {
        val op = "acceptInitialSaleOffer"
        if(errNotInState(op, GameState.OFFER_ITEMS_ON_SALE)) return
        if(errIsNoItem(op, item))
        if(errNotOnSale(op, item)) return
        if(!state.store!!.containsItem(item)) {
            errMsg(op, "Store is out of $item")
            changeGameState(GameState.WAITING_DICE_ROLL_OR_RESTOCK)
            return
        }

        assert(state.shoppingCustomer!!.optionalSaleSlot == Item.NO_ITEM)

        notify(GameEvent.OFFERED_SALES_ACCEPTED)

        log("${state.shoppingCustomer!!.info.name} accepted $item on sale")

        purchase(state.shoppingCustomer!!, item, true)

        changeGameState(GameState.WAITING_DICE_ROLL_OR_RESTOCK)
    }



    fun declineInitialSaleOffer() {
        val op = "declineInitialSaleOffer"
        if(errNotInState(op, GameState.OFFER_ITEMS_ON_SALE)) return
        notify(GameEvent.OFFERED_SALES_DECLINED)
        log("${state.shoppingCustomer!!.info.name} declined sales")
        changeGameState(GameState.WAITING_DICE_ROLL_OR_RESTOCK)
    }



    /**
     * state = WAINTING_DICE_ROLL_OR_RESTOCK
     * */
    fun rollDice() {
        val op= "rollDice"
        if(errNotInState(op, GameState.WAITING_DICE_ROLL_OR_RESTOCK)) return
        log("Rolling dice")
        notify(GameEvent.DICE_ROLLED)
        state.die1 = kotlin.random.Random.nextInt(1, 7)
        state.die2 = kotlin.random.Random.nextInt(1, 7)
        log("Dice = (${state.die1}, ${state.die2})")
        changeGameState(GameState.DICE_ROLLED)
    }





    /**
     * state = DICE_ROLLED
     * */
    fun continueWithNormalPurchase() {
        val op = "performNormalPurchase"
        if(errNotInState(op, GameState.DICE_ROLLED)) return

        val customer = state.shoppingCustomer!!

        log("${customer.info.name} will not use a coupon")

        val wantedItem = customer.info.ranges[getDiceSum()]

        if(!state.store!!.containsItem(wantedItem)) {
            triggerPurchaseFailNoItem(wantedItem)
        } else if(state.isOnSale[wantedItem]!!) {
            log("Wanted item: $wantedItem is on sale. Must buy additional items")
            buyNormalOnSaleItem(customer, wantedItem)
        } else {
            log("Buying $wantedItem normally")
            purchase(customer, wantedItem)
            onCustomerItemPurchaseDone()
        }

    }

    private fun buyNormalOnSaleItem(customer: Customer, wantedItem: Item) {
        val otherItemsNums = CouponChart.getForDiceValue(getDiceSum())

        val additionalItem1 = customer.info.ranges[otherItemsNums[0]]
        val additionalItem2 = customer.info.ranges[otherItemsNums[1]]

        val store = state.store!!

        // puede pasar que algunos items sean iguales por lo que hay que ver si hay dos
        if(wantedItem == additionalItem1 && wantedItem == additionalItem2
            && !store.containsItem(wantedItem, 3)) { // 3 iguales
            triggerPurchaseFailNoItem(wantedItem)
        } else if(wantedItem == additionalItem1 && !store.containsItem(additionalItem1, 2)) { // w == 1
            triggerPurchaseFailNoItem(additionalItem1)
        } else if(wantedItem == additionalItem2 && !store.containsItem(additionalItem2, 2)) { // w == 2
            triggerPurchaseFailNoItem(additionalItem2)
        } else if(additionalItem2 == additionalItem1 && !store.containsItem(additionalItem1, 2)) { // 2 == 1
            triggerPurchaseFailNoItem(additionalItem1)
        } else if(!store.containsItem(additionalItem1)) {
            triggerPurchaseFailNoItem(additionalItem1)
        } else if (!store.containsItem(additionalItem2)) {
            triggerPurchaseFailNoItem(additionalItem2)
        } else {
            purchase(customer, wantedItem)

            log("Buying additional $additionalItem1")
            notify(GameEvent.BUYING_ADDITIONAL_ITEM, additionalItem1.toString())
            purchase(customer, additionalItem1)

            log("Buying additional $additionalItem2")
            notify(GameEvent.BUYING_ADDITIONAL_ITEM, additionalItem2.toString())
            purchase(customer, additionalItem2)
            onCustomerItemPurchaseDone()
        }
    }

    private fun purchase(customer: Customer, wantedItem: Item, isInitialOnSaleItem : Boolean = false) {
        log("${customer.info.name} buys $wantedItem")
        notify(GameEvent.BUYING_ITEM, wantedItem.toString())

        val price = getCurrentPrice(wantedItem)

        increseProfit(price)

        state.store!!.getContainer(wantedItem).removeItem(wantedItem)

        if(!isInitialOnSaleItem) customer.addInNextAvailableSlot(wantedItem)
        else customer.optionalSaleSlot = wantedItem
    }

    fun continueWithCouponPurchase() {
        val op = "doCouponPurchase"
        if(errNotInState(op, GameState.DICE_ROLLED)) return

        val customer = state.shoppingCustomer!!
        if(errNotEnoughCoupons(op, customer)) return

        val itemNums = CouponChart.getForDiceValue(getDiceSum())
        val item1 = customer.info.ranges[itemNums[0]]
        val item2 = customer.info.ranges[itemNums[1]]

        if(!state.store!!.containsItem(item1)) {
            triggerPurchaseFailNoItem(item1)
        } else if(!state.store!!.containsItem(item2)) {
            triggerPurchaseFailNoItem(item2)
        } else {
            log("${customer.info.name} uses a coupon")
            customer.couponsUsed++
            purchase(customer, item1)
            purchase(customer, item2)
            state.currentProfit -= 2
            onCustomerItemPurchaseDone()
        }
    }

    private fun triggerPurchaseFailNoItem(missingItem: Item) {
        log("Purchased failed. No $missingItem in store")

        notify(GameEvent.PURCHASE_FAILED_NO_ITEM, missingItem.toString())

        log("Applying a penalty of $${state.shoppingCustomer!!.info.penalty}")
        loseMoney(state.shoppingCustomer!!.info.penalty)

        notify(GameEvent.PENALTY_APPLIED, state.shoppingCustomer!!.info.penalty.toString())

        state.shoppingCustomer!!.doneShopping = true

        customerTripEndTansition()
    }

    private fun triggerPurchaseFailRestock() {
        log("Purchase failed because of restock")
        notify(GameEvent.PURCHASE_FAILED_RESTOCK, state.currentProfit.toString())

        state.shoppingCustomer!!.doneShopping = true

        log("Applying a penalty of $${state.shoppingCustomer!!.info.penalty}")
        loseMoney(state.shoppingCustomer!!.info.penalty)

        commitCurrentPurchase(false)

        customerTripEndTansition()
    }

    private fun onCustomerItemPurchaseDone() {
        if(state.shoppingCustomer!!.hasFreeSpaceInCart() && !state.shoppingCustomer!!.doneShopping) {
            log("${state.shoppingCustomer!!.info.name} can continue bying")
            changeGameState(GameState.WAITING_DICE_ROLL_OR_RESTOCK)
        } else {
            commitCurrentPurchase(true)
            state.shoppingCustomer!!.doneShopping = true
            customerTripEndTansition()
        }
    }

    private fun customerTripEndTansition() {
        if(countNotCompletedCards() == 0) {
            log("No more customers to flip")
            changeGameState(GameState.WASTE_PHASE)
        } else if(countCardsFacingDown() != 0 && countNotCompletedCardsFacingUp() < 2){
            log("There are still cards waiting to be flipped")
            changeGameState(GameState.WAITING_FLIP_NEXT)
        } else {
            log("Must choose next customer")
            changeGameState(GameState.CHOOSE_NEXT_TO_SHOP_OR_RESTOCK)
        }
    }

    private fun commitCurrentPurchase(applyBonus : Boolean = true) {
        earnMoney(state.currentProfit)
        if(applyBonus) earnMoney(state.shoppingCustomer!!.info.bonus)
        notify(GameEvent.PURCHASE_COMMITED, state.currentProfit.toString())
    }








    /**
     * state == WASTE_PHASE
     * */
    fun handleWaste() {
        var penalty = 0

        val currentRound = state.round
        val stock = state.stockRoom!!

        for(item in Item.values()) {
            if(item == Item.NO_ITEM) continue

            val container = state.store!!.getContainer(item)

            if(currentRound !in container.getIterationIndices()) continue

            val numWasteStore = container.countExpiresAfter(item, currentRound)
            val numWasteStock = stock.countExpiresAfter(item, currentRound)
            val totalWaste = numWasteStore + numWasteStock

            log("Sending ${totalWaste}x${item} to distribution center")
            container.removeItem(item, numWasteStore)
            stock.removeItem(item, numWasteStock)
            state.distributionCenter!!.items[item] = state.distributionCenter!!.items[item]!! + totalWaste

            penalty += totalWaste
        }

        log("Items wasted: $penalty")

        if(penalty == 0) {
            notify(GameEvent.NO_ITEMS_WASTED)
        } else {
            loseMoney(penalty)
            notify(GameEvent.ITEMS_WASTED, penalty.toString())
        }

        if(state.round != 6) {
            changeGameState(GameState.PREPARE_PHASE)
            notify(GameEvent.MESSAGE, "Preparing next turn")
            prepareNextTurn()
        } else {
            notify(GameEvent.GAME_WON, state.money.toString())
            changeGameState(GameState.GAME_END)
        }

    }



















    private fun changeGameState(newState : GameState) {
        log("State changed to $newState")
        state.gameState = newState
        notify(GameEvent.GAME_STATE_CHANGE)
    }

    private fun increseProfit(quantity: Int) {
        state.currentProfit += quantity
        log("Current purchase profic increased by $quantity. Accumulated: ${state.currentProfit}")
        notify(GameEvent.PROFIT_INCREASED, "$quantity")
    }

    private fun earnMoney(quantity: Int) {
        state.money += quantity
        log("Earned $$quantity")
        notify(GameEvent.MONEY_EARNED, "$quantity")
    }

    private fun loseMoney(quantity: Int) {
        state.money -= quantity
        log("Lost $$quantity")
        if(state.money < 0) {
            notify(GameEvent.GAME_LOST, state.money.toString())
            changeGameState(GameState.GAME_END)
        }
        notify(GameEvent.MONEY_LOST, "$quantity")
    }

    private fun countWaitingCardsFacingUp() : Int {
        var count = 0

        for(i in state.drawnCustomers) {
            if(i.faceUp) count++
        }

        return count
    }

    private fun countNotCompletedCardsFacingUp() : Int {
        var count = 0

        for(i in state.drawnCustomers) {
            if(i.faceUp && !i.doneShopping) count++
        }

        return count
    }
    private fun countCardsFacingDown() : Int {
        var count = 0

        for(i in state.drawnCustomers) {
            if(!i.faceUp) count++
        }

        return count
    }

    private fun countNotCompletedCards() : Int {
        var count = 0

        for(i in state.drawnCustomers) {
            if(!i.doneShopping) count++
        }

        return count
    }

    private fun getCustomerFromIndex(idx : Int) : Customer? {
        if(idx < 0 || idx > state.drawnCustomers.size - 1) {
            errMsg("getCustomerFromIndex", "Invalid index: $idx")
            return null
        }

        var count = 0
        val iter = state.drawnCustomers.iterator()
        while(iter.hasNext()) {
            val customer = iter.next()
            if(count == idx) {
                return customer
            } else {
                count++
            }
        }
        return null
    }

    private fun isSomeItemOnSale() : Boolean {
        for(i in Item.values()) {
            if(i != Item.NO_ITEM) {
                if(state.isOnSale.get(i)!!) return true
            }
        }
        return false
    }

    private fun countItemsPutOnSale() : Int {
        var count = 0
        for(i in Item.values()) {
            if(i != Item.NO_ITEM && state.wasOnSale[i]!!) count++
        }
        return count
    }

    private fun getCurrentPrice(item : Item) : Int {
        return if(state.isOnSale[item]!!) item.salePrice else item.price
    }

    fun getDiceSum() : Int{
        return state.die1 + state.die2
    }














    /**
     * Errores
     * */
    private fun errAlreadyInitialized(op: String) : Boolean {
        if(initialized) {
            errMsg(op, "Already initialized")
            return true
        }
        return false
    }

    private fun errNotInState(op: String, state: GameState) : Boolean {
        if(this.state.gameState != state) {
            errMsg(op, "Not in state: $state")
            return true
        }
        return false
    }

    private fun errNotInStates(op: String, states: Array<GameState>) : Boolean {
        if(state.gameState !in states) {
            var str = ""
            for(s in states) str += "$s, "
            errMsg(op, "Not in states: $str")
            return true
        }
        return false
    }

    private fun errIsNoItem(op: String, item : Item) : Boolean {
        if(item == Item.NO_ITEM) {
            errMsg(op, "NO_ITEM")
            return true
        }
        return false
    }

    private fun errStoreFull(op: String): Boolean {
        if(state.store!!.isFull()) {
            errMsg(op, "Store is full")
            return true
        }
        return false
    }

    private fun errStockRoomFull(op: String) : Boolean {
        if(state.stockRoom!!.isFull()) {
            errMsg(op, "Stock room is full")
            return true
        }
        return false
    }

    private fun errIsOnSale(op: String, item: Item) : Boolean {
        if(state.isOnSale[item]!!) {
            errMsg(op, "$item is already on sale")
            return true
        }
        return false
    }

    private fun errWasOnSaleBefore(op: String, item: Item) : Boolean {
        if(state.wasOnSale[item]!!) {
            errMsg(op, "$item was on sale before")
            return true
        }
        return false
    }

    private fun errNoItemsInDistributionCenter(op: String, it : Item) : Boolean {
        if(state.distributionCenter!!.items[it]!! == 0) {
            errMsg(op, "No $it in distribution center")
            return true
        }
        return false
    }

    private fun errNotEnoughMoney(op: String, required : Int) : Boolean {
        if(state.money < required) {
            errMsg(op, "You need $$required, you have ${state.money}")
            return true
        }
        return false
    }

    private fun errNotEnoughCoupons(op: String, customer: Customer) : Boolean {
        if(customer.couponsUsed == customer.info.numCoupons) {
            errMsg(op, "${customer.info.name} has used all coupons")
            return true
        }
        return false
    }

    private fun errCustomerFacingUp(op: String, idx : Int) : Boolean {
        if(getCustomerFromIndex(idx)!!.faceUp) {
            errMsg(op, "Customer[$idx] is facing up")
            return true
        }
        return false
    }

    private fun errCustomerFacingDown(op: String, idx : Int) : Boolean {
        if(!getCustomerFromIndex(idx)!!.faceUp) {
            errMsg(op, "Customer[$idx] is facing down")
            return true
        }
        return false
    }

    private fun errCustomerDoneShopping(op: String, idx : Int) : Boolean {
        if(getCustomerFromIndex(idx)!!.doneShopping) {
            errMsg(op, "Customer[$idx] has already done their shopping")
            return true
        }
        return false
    }

    private fun errItemNotInContainer(op: String, container: ItemContainer, item: Item) : Boolean {
        return errItemNotInContainer(op, container, item, 1)
    }

    private fun errItemNotInContainer(op: String, container: ItemContainer, item: Item, qtt : Int) : Boolean {
        if(!container.containsItem(item, qtt)) {
            errMsg(op, "Container doesn't have $qtt $item")
            return true
        }
        return false
    }

    private fun errNotOnSale(op: String, item: Item): Boolean {
        if(!state.wasOnSale[item]!!) {
            errMsg(op, "$item is not on sale")
            return true
        }
        return false
    }

    private fun errAlreadyRestocked(op: String): Boolean {
        if(state.restocked) {
            errMsg(op, "You have already restocked once in this round")
            return true
        }
        return false
    }

    private fun notify(event : GameEvent) {
        notify(event, "")
    }

    private fun notify(event : GameEvent, msg: String) {
        if(observer == null) {
            warningMsg("Listener not set")
            return
        }
        observer!!.onGameEvent(event, msg)
    }

    fun log(s : String) {
        if(!verbose) return
            println("[$TAG]: $s")
    }

    private fun warningMsg(msg : String) {
        if(verbose)
            println("[Warning]: $msg")
    }

    private fun errMsg(op: String, msg : String) {
        val red = "\u001b[31m"
        val resetColors = "\u001b[0m"

        println("$red[Error] -> $resetColors[fun $op]: $msg")
    }

}





















































