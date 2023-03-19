package supermarche

object CouponChart {

    private val data : Array<Int> =
        arrayOf(
            0, 0, // idx = 0
            0, 0, // idx = 1
            6, 7, // 2
            4, 10, // 3
            5, 9, // 4
            2, 10, // 5
            4, 12, // 6
            3, 8, // 7
            7, 12, // 8
            5, 11, // 9
            6, 11, // 10
            2, 9, // 11
            3,8 // 12
        )

    fun getForDiceValue(sum : Int) : Array<Int> {
        return arrayOf(data[sum * 2], data[sum * 2 + 1])
    }
}