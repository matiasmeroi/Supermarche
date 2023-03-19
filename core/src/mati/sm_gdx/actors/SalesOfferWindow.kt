package mati.sm_gdx.actors

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.widget.VisList
import com.kotcrab.vis.ui.widget.VisWindow
import mati.sm_gdx.Constants.HEIGHT
import mati.sm_gdx.Constants.WIDTH
import mati.sm_gdx.SM
import supermarche.Item
import com.badlogic.gdx.utils.Array as GdxArray

class SalesOfferWindow(val context : SM) : VisWindow("Buy Item On Sale?") {

    interface SalesOfferObserver {
        fun onSalesOfferChosen(str : String)
    }

    private val lst = VisList<String>()

    init {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if(observer != null) observer!!.onSalesOfferChosen(this@SalesOfferWindow.lst.selected.toString())
            }
        })
        lst.isVisible = true
        this.add(lst)
        makeInvisible()
    }

    var observer : SalesOfferObserver? = null

    fun makeVisible() {
        var nItems = 1 // Decline
        for(i in Item.values()) {
            if(i != Item.NO_ITEM && context.game().state.isOnSale[i]!!) nItems++
        }

        val options = GdxArray<String>()

        options.add(Item.NO_ITEM.toString())
        for(i in Item.values()) {
            if(i == Item.NO_ITEM) continue

            if(context.game().state.isOnSale[i]!!) {
                options.add(i.toString())
            }
        }

        lst.setItems(options)
        lst.pack()

        this.pack()

        this.setPosition(WIDTH / 2 - width / 2, HEIGHT / 2 - height / 2)

        this.isVisible = true
    }

    fun makeInvisible() {
        this.isVisible = false
    }


}