package mati.sm_gdx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import mati.sm_gdx.Constants.CUSTOMER_COUPON_SEP
import mati.sm_gdx.Constants.CUSTOMER_COUPON_START_X
import mati.sm_gdx.Constants.CUSTOMER_COUPON_WIDTH
import mati.sm_gdx.Constants.CUSTOMER_COUPON_Y
import mati.sm_gdx.Constants.CUSTOMER_HEIGHT
import mati.sm_gdx.Constants.CUSTOMER_ITEMS_SEP
import mati.sm_gdx.Constants.CUSTOMER_ITEMS_SIZE
import mati.sm_gdx.Constants.CUSTOMER_ITEMS_Y
import mati.sm_gdx.Constants.CUSTOMER_WIDTH
import mati.sm_gdx.Constants.TOKEN_LABEL_X_OFFSET_ONE_DIGIT
import mati.sm_gdx.Constants.TOKEN_LABEL_X_OFFSET_TWO_DIGITS
import mati.sm_gdx.Constants.TOKEN_SEP
import mati.sm_gdx.Constants.TOKEN_SIZE
import supermarche.Customer
import supermarche.Item
import supermarche.ItemStackList
import supermarche.sectors.ItemStack

object Utils  {

    fun drawStack(context: SM, pos: Vector2, stack: ItemStack) {
        drawStack(context, pos, stack.item, stack.quantity)
    }

    fun drawStack(context : SM, pos : Vector2, item : Item, quantity : Int) {
        context.getBatch().draw(context.getItemRegion(item), pos.x, pos.y)
        context.font().color = Constants.TOKEN_LABEL_COLOR
        val offset = if(quantity < 10) TOKEN_LABEL_X_OFFSET_ONE_DIGIT else TOKEN_LABEL_X_OFFSET_TWO_DIGITS
        context.font().draw(context.getBatch(), "$quantity", pos.x + offset, pos.y + Constants.TOKEN_LABEL_Y_OFFSET)
    }

    fun drawStackList(context: SM, pos: Vector2, stackList: ItemStackList, cols: Int) {
        val stackPos = Vector2()
        var count = 0
        val iter = stackList.iterator()
        while(iter.hasNext()) {
            val stack = iter.next()

            val dx = (count % cols) * (TOKEN_SIZE + TOKEN_SEP)
            val dy = (count / cols) * (TOKEN_SIZE + TOKEN_SEP)
            stackPos.set(pos.x + dx, pos.y + dy)
            drawStack(context, stackPos, stack)

            count++
        }
    }

    fun drawCustomer(context: SM, cus: Customer, x : Float, y : Float, drawItems: Boolean) {
        val card =  if(cus.faceUp) context.getAssets().get(Utils.getCustomerImagePath(cus.info.id), Texture::class.java)
                    else context.getAssets().get(Constants.CUSTOMER_BACK_TEXTURE, Texture::class.java)

        context.getBatch().draw(card, x, y)

        val middleX = x + CUSTOMER_WIDTH / 2
        val startX = middleX - cus.info.cartSize * (CUSTOMER_ITEMS_SIZE + CUSTOMER_ITEMS_SEP) / 2

        val maxItems = cus.info.cartSize + cus.additionalItems.size
        var idx = 0
        while(idx < maxItems && drawItems) {
            if(idx < cus.info.cartSize && cus.cart[idx] == Item.NO_ITEM) break
            else if(idx >= cus.info.cartSize && cus.additionalItems[idx - cus.info.cartSize] == Item.NO_ITEM) break

            val currentItem = if(idx < cus.info.cartSize) cus.cart[idx] else cus.additionalItems[idx - cus.info.cartSize]

            context.getBatch().draw(context.getItemRegion(currentItem),
                startX + idx * (CUSTOMER_ITEMS_SIZE + CUSTOMER_ITEMS_SEP), y + CUSTOMER_ITEMS_Y,
                CUSTOMER_ITEMS_SIZE, CUSTOMER_ITEMS_SIZE)

            idx++
        }

        if(cus.optionalSaleSlot != Item.NO_ITEM && drawItems) {
            val halfCart = cus.info.cartSize / 2f
            context.getBatch().draw(context.getItemRegion(cus.optionalSaleSlot),
                middleX - (halfCart + 1) * (CUSTOMER_ITEMS_SIZE + CUSTOMER_ITEMS_SEP), y + CUSTOMER_ITEMS_Y,
                CUSTOMER_ITEMS_SIZE, CUSTOMER_ITEMS_SIZE)
        }

        if(cus.couponsUsed != 0 && drawItems) {
            for(i in 0 until cus.couponsUsed) {
                context.getBatch().draw(context.tokenAtlas().findRegion("coupon_cover"),
                    x + CUSTOMER_COUPON_START_X + i * (CUSTOMER_COUPON_SEP + CUSTOMER_COUPON_WIDTH), y + CUSTOMER_COUPON_Y)
            }
        }

        if(cus.doneShopping){
            context.font().setColor(0f, 0f, 0f, 1f)
            context.font().draw(context.getBatch(), "COMPLETED", x + CUSTOMER_WIDTH / 2 - 40, y - 10f)
        }
    }

    fun getCustomerImagePath(id : Int) : String {
        return "customers/$id.png"
    }

}