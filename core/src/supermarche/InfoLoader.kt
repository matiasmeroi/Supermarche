package supermarche

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

object InfoLoader {

    private const val CUSTOMERS_FILE_PATH = "customers.json"
    private const val DISTRIBUTION_CARDS_FILE_PATH = "distributionCenterCards.json"

    fun getCustomerQueue() : CustomerQueue {
        val gson : Gson = Gson()

//        val inStream = javaClass.getResourceAsStream(CUSTOMERS_FILE_PATH)
//        val reader = BufferedReader(InputStreamReader(inStream))
//        val fileString = reader.readText()
        val fileString =  Gdx.files.internal(CUSTOMERS_FILE_PATH).readString()

        val infos = gson.fromJson(fileString, Array<CustomerInfo>::class.java)

        val aList = ArrayList<CustomerInfo>()
        for(inf in infos) aList.add(inf)
        aList.shuffle()

        val result = CustomerQueue()
        for(i in aList) result.add(Customer(i))

        println("Loaded ${result.size} customers")
        return result
    }

    fun getDistributionCenterCards() : DistributionCardQueue {
        val gson : Gson = Gson()

//        val inStream = javaClass.getResourceAsStream(DISTRIBUTION_CARDS_FILE_PATH)
//        val reader = BufferedReader(InputStreamReader(inStream))
//        val fileString = reader.readText()
        val fileString =  Gdx.files.internal(DISTRIBUTION_CARDS_FILE_PATH).readString()
        val infos = gson.fromJson(fileString, Array<DistributionCard>::class.java)

        val aList = ArrayList<DistributionCard>()
        for(inf in infos) aList.add(inf)
        aList.shuffle()

        val result = DistributionCardQueue()
        for(i in aList) result.add(i)

        println("Loaded ${result.size} distribution center cards")
        return result
    }

}
