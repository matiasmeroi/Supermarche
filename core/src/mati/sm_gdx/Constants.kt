package mati.sm_gdx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import supermarche.Item
import supermarche.SuperMarche.Companion.NEVER_EXPIRES
import kotlin.math.exp


object Constants {

    const val MENU_TEXTURE = "menu.png"
    const val GAME_OVER_TEXTURE = "game_over.png"

    const val STORE_ROOM_TEXTURE = "store.png"
    const val STOCK_ROOM_TEXTURE = "stock_room.png"
    const val DIST_CENTER_TEXTURE = "distribution_center.png"
    const val CUSTOMERS_TEXTURE = "customers.png"

    const val TOKEN_ATLAS_PATH = "tokens1/tokens.atlas"

    const val FONT_FNT = "font/my_font.fnt"
    const val FONT_PNG = "font/my_font.png"


    const val CUSTOMER_BACK_TEXTURE = "customers/back.png"
    const val CUSTOMER_INVISIBLE_TEXTURE = "customers/invisible.png"

    const val WIDTH = 1280f
    const val HEIGHT = 720f

    const val CUSTOMER_WIDTH = 246f
    const val CUSTOMER_HEIGHT = 318f

    const val TOKEN_LABEL_X_OFFSET_ONE_DIGIT = 38f
    const val TOKEN_LABEL_X_OFFSET_TWO_DIGITS = 30f
    const val TOKEN_LABEL_Y_OFFSET = 17f
    val TOKEN_LABEL_COLOR: Color = Color.BLUE

    const val TOKEN_SIZE = 50
    const val TOKEN_SEP = 10

    // Dist Center
    const val DIST_X_1 = 231f
    const val DIST_X_2 = 662f
    const val DIST_X_3 = 1064f
    const val DIST_Y_1 = HEIGHT - 328f
    const val DIST_Y_2 = HEIGHT - 461f
    const val DIST_Y_3 = HEIGHT - 402f

    fun getDistributionCenterPosition(item: Item) : Vector2 {
        val vec = Vector2()

        when(item) {
            Item.NO_ITEM -> vec.set(0f, 0f)
            Item.PRODUCE -> vec.set(DIST_X_1, DIST_Y_1)
            Item.BAKERY -> vec.set(DIST_X_1, DIST_Y_2)
            Item.DAIRY -> vec.set(DIST_X_2, DIST_Y_1)
            Item.DRY_GOODS -> vec.set(DIST_X_2, DIST_Y_2)
            Item.FROZEN -> vec.set(DIST_X_3, DIST_Y_3)
        }

        return vec
    }


    // StockRoom

    const val STRM_X_1 = 117f
    const val STRM_X_2 = 510f
    const val STRM_X_3 = 925f
    const val STRM_Y_1 = HEIGHT - 298f
    const val STRM_Y_2 = HEIGHT - 443f
    const val STRM_Y_3 = HEIGHT - 202f
    const val STRM_Y_4 = HEIGHT - 355f
    const val STRM_Y_5 = HEIGHT - 494f

    fun getStockRoomPosition(expiresAfter: Int) : Vector2 {
        val v = Vector2()
        when(expiresAfter) {
            1 -> v.set(STRM_X_1, STRM_Y_1)
            2 -> v.set(STRM_X_1, STRM_Y_2)
            3 -> v.set(STRM_X_2, STRM_Y_1)
            4 -> v.set(STRM_X_2, STRM_Y_2)
            5 -> v.set(STRM_X_3, STRM_Y_3)
            6 -> v.set(STRM_X_3, STRM_Y_4)
            NEVER_EXPIRES -> v.set(STRM_X_3, STRM_Y_5)
        }
        return v
    }



    // Store

    const val ST_BA_X_1 = 440f
    const val ST_BA_X_2 = 536f
    const val ST_BA_X_3 = 644f
    const val ST_BA_Y_1 = HEIGHT - 321f
    const val ST_BA_Y_2 = HEIGHT - 418f

    const val ST_FR_X = 1171f
    const val ST_FR_Y = HEIGHT - 684f

    const val ST_PR_X = 1086f
    const val ST_PR_Y = HEIGHT - 416f

    const val ST_DR_X_1 = 759f
    const val ST_DR_X_2 = 870f
    const val ST_DR_Y_1 = HEIGHT - 320f
    const val ST_DR_Y_2 = HEIGHT - 420f

    const val ST_DA_Y = HEIGHT - 154f
    const val ST_DA_X_1 = 626f
    const val ST_DA_X_2 = 730f
    const val ST_DA_X_3 = 825f
    const val ST_DA_X_4 = 922f
    const val ST_DA_X_5 = 1020f

    fun getStorePosition(item : Item, expiresAfter: Int) : Vector2 {
        val v = Vector2()
        when(item) {
            Item.NO_ITEM -> {}
            Item.BAKERY -> {
                when(expiresAfter) {
                    2 -> v.set(ST_BA_X_1, ST_BA_Y_1)
                    3 -> v.set(ST_BA_X_2, ST_BA_Y_1)
                    4 -> v.set(ST_BA_X_3, ST_BA_Y_1)
                    5 -> v.set(ST_BA_X_1, ST_BA_Y_2)
                    6 -> v.set(ST_BA_X_2, ST_BA_Y_2)
                    NEVER_EXPIRES -> v.set(ST_BA_X_3, ST_BA_Y_2)
                    else -> error("ba")
                }
            }
            Item.PRODUCE -> v.set(ST_PR_X, ST_PR_Y)
            Item.DAIRY -> {
                when(expiresAfter) {
                    3 -> v.set(ST_DA_X_1, ST_DA_Y)
                    4 -> v.set(ST_DA_X_2, ST_DA_Y)
                    5 -> v.set(ST_DA_X_3, ST_DA_Y)
                    6 -> v.set(ST_DA_X_4, ST_DA_Y)
                    NEVER_EXPIRES -> v.set(ST_DA_X_5, ST_DA_Y)
                    else -> error("da")
                }
            }
            Item.DRY_GOODS -> {
                when(expiresAfter) {
                    4 -> v.set(ST_DR_X_1, ST_DR_Y_1)
                    5 -> v.set(ST_DR_X_2, ST_DR_Y_1)
                    6 -> v.set(ST_DR_X_1, ST_DR_Y_2)
                    NEVER_EXPIRES -> v.set(ST_DR_X_2, ST_DR_Y_2)
                    else -> error("dr")
                }
            }
            Item.FROZEN -> {
                if(expiresAfter == NEVER_EXPIRES)
                    v.set(ST_FR_X, ST_FR_Y)
                else
                    error("fr")
            }
        }
        return v
    }

    const val SALE_BAKERY_X = 533f
    const val SALE_BAKERY_Y = HEIGHT - 535f
    const val SALE_DRY_X = 815f
    const val SALE_DRY_Y = HEIGHT - 551f
    const val SALE_FROZEN_X = 1172f
    const val SALE_FROZEN_Y = HEIGHT - 615f
    const val SALE_PRODUCE_X = 1086f
    const val SALE_PRODUCE_Y = HEIGHT - 349f
    const val SALE_DAIRY_X = 538f
    const val SALE_DAIRY_Y = HEIGHT - 173f


    // CUSTOMER CARDS
    const val CUSTOMER_ITEMS_Y = CUSTOMER_HEIGHT - 72f
    const val CUSTOMER_ITEMS_SEP = 2f
    const val CUSTOMER_ITEMS_SIZE = 40f

    const val CUSTOMER_COUPON_START_X = 12f
    const val CUSTOMER_COUPON_Y = CUSTOMER_HEIGHT - 28f
    const val CUSTOMER_COUPON_SEP = 4f
    const val CUSTOMER_COUPON_WIDTH = 28f
}


