package supermarche

enum class GameEvent {
    SETUP,
    CUSTOMERS_DRAWN,
    DISTRIBUTION_CARD_DRAWN,
    ROUND_CHANGED,
    CARD_FLIPPED,
    GAME_STATE_CHANGE,
    MONEY_LOST,
    MONEY_EARNED,
    DISTRIBUTION_CENTER_UPDATED,
    STOCK_ROOM_UPDATED,
    OFFER_SALES,
    NEXT_CUSTOMER_CHOSEN,
    OFFERED_SALES_ACCEPTED,
    OFFERED_SALES_DECLINED,
    BUYING_ITEM,
    BUYING_ADDITIONAL_ITEM,
    PURCHASE_FAILED_NO_ITEM,
    PURCHASE_FAILED_RESTOCK,
    PROFIT_INCREASED,
    PROFIT_CLEARED,
    PURCHASE_COMMITED,
    PENALTY_APPLIED,
    NO_ITEMS_WASTED,
    ITEMS_WASTED,
    MESSAGE,
    STOCK_PHASE_END,
    DICE_ROLLED,
    GAME_LOST,
    GAME_WON
}