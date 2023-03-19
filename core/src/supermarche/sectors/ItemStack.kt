package supermarche.sectors

import supermarche.Item
import supermarche.SuperMarche

data class ItemStack(val item : Item, var quantity : Int = 0)
