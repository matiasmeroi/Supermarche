package supermarche

enum class Item(val expiresAfter : Int, val price : Int, val salePrice : Int) {
    NO_ITEM(SuperMarche.NEVER_EXPIRES, 0, 0),
    PRODUCE(1, 3, 2),
    BAKERY(2, 4, 3),
    DAIRY(3, 4, 2),
    DRY_GOODS(4, 10, 7),
    FROZEN(SuperMarche.NEVER_EXPIRES, 7, 5)
}