package supermarche

class Customer(val info : CustomerInfo) {

    var faceUp : Boolean = false

    var optionalSaleSlot : Item = Item.NO_ITEM
    var cart : Array<Item> = Array<Item>(info.cartSize) { p -> Item.NO_ITEM }
    var additionalItems : Array<Item> = Array<Item>(5) { p -> Item.NO_ITEM }

    var couponsUsed : Int = 0
    var doneShopping : Boolean = false

    fun hasFreeSpaceInCart() : Boolean {
        for(i in cart) if(i == Item.NO_ITEM) return true
        return false
    }

    fun addInNextAvailableSlot(newItem : Item) {
        var idx = 0
        var added = false
        while(!added && idx < cart.size + additionalItems.size) {
            if(idx < cart.size) {
                if(cart[idx] == Item.NO_ITEM) {
                    cart[idx] = newItem
                    added = true
                } else idx++
            } else {
                val i = idx - cart.size
                if(additionalItems[i] == Item.NO_ITEM) {
                    additionalItems[i] = newItem
                    added = true
                } else idx++
            }
        }
    }

    fun description() : String{
        val upOrDown = if(faceUp) "U" else "D"
        val done = if(doneShopping) "DONE" else ""
        return "<[$upOrDown]${info.name}; P: ${info.penalty}; C: $couponsUsed, ${info.numCoupons}; $done"
    }

}