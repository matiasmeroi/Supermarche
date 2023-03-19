package supermarche.sectors

import supermarche.Item
import supermarche.ItemContainer
import supermarche.SuperMarche
import java.util.Hashtable


class DistributionCenter(){

    val items = Hashtable<Item, Int>()

    init {
        for(i in Item.values()) {
            if(i != Item.NO_ITEM) {
                items.put(i, 10)
            }
        }
    }

}