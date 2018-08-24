package io.ipoli.android.quest.schedule.today

object TodayImageUrlProvider {

    private const val QUALITY_SETTINGS = "&fm=jpg&w=768&crop=entropy&fit=max&q=40"

    private val IMAGE_URLS = listOf(
        "https://images.unsplash.com/photo-1432256851563-20155d0b7a39?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=7744813a62e994e17044d8ecb1516265",
        "https://images.unsplash.com/photo-1431794062232-2a99a5431c6c?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=13b58b0343d8efc06a88c55e843f624f",
        "https://images.unsplash.com/photo-1437422061949-f6efbde0a471?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=e864d8eb30d32ccccb910a4b49de0d92",
        "https://images.unsplash.com/photo-1442850473887-0fb77cd0b337?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=6b527796946e88db3a0ec912ebe1a613",
        "https://images.unsplash.com/photo-1443890923422-7819ed4101c0?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=4f18acb8db93abc281f66dcfd2ad9e61",
        "https://images.unsplash.com/photo-1446292532430-3e76f6ab6444?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=1b7369f413e3a8640aa00d97f46e26f8",
        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=88dbc8a18037b2ac4800955d981db909",
        "https://images.unsplash.com/photo-1465189684280-6a8fa9b19a7a?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=29640b60327a3b66e91817e39adc433a",
        "https://images.unsplash.com/photo-1448375240586-882707db888b?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=dd1c3895999a4f9c5e4ac57e6e9fa2fb",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=e471a27144269e4b573dfdee025e7327",
        "https://images.unsplash.com/photo-1469474968028-56623f02e42e?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=79f96121806d64f986dc5e5d9308afb1",
        "https://images.unsplash.com/photo-1465146344425-f00d5f5c8f07?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=fa21066bea145bff86d1cf16b652d97c",
        "https://images.unsplash.com/photo-1471513671800-b09c87e1497c?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=263d1948dcd0e71c59c4929e8fcf3f46",
        "https://images.unsplash.com/photo-1482192505345-5655af888cc4?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=a14ca13b83f7bce82764a45d13576418",
        "https://images.unsplash.com/photo-1474524955719-b9f87c50ce47?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=c83ef66968ed8c6a5c1ac1111ee78c1e",
        "https://images.unsplash.com/photo-1474524955719-b9f87c50ce47?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=c83ef66968ed8c6a5c1ac1111ee78c1e",
        "https://images.unsplash.com/photo-1484542603127-984f4f7d14cb?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=d3e647e84d0f24a63f41bf45ee21b17d",
        "https://images.unsplash.com/photo-1500534623283-312aade485b7?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=70020367be7df3c19faec09be1db669f",
        "https://images.unsplash.com/photo-1488711500009-f9111944b1ab?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=db8d52e77ea934255d46dcd0f49353bf",
        "https://images.unsplash.com/photo-1500993855538-c6a99f437aa7?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=72a0229d410f0e7c7701ebfc53b68a65",
        "https://images.unsplash.com/photo-1502261159926-e31d770eb6e1?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=9efa0661e53144c027f5cea31760a31a",
        "https://images.unsplash.com/photo-1501854140801-50d01698950b?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=a0eec664753ae7aac734aae0e883060d",
        "https://images.unsplash.com/photo-1504700610630-ac6aba3536d3?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=aab214af505877ba2f060ec9fee12cb9",
        "https://images.unsplash.com/photo-1504198266287-1659872e6590?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=cdc5b96cfce7ed1710409aaee2cd22c1",
        "https://images.unsplash.com/photo-1505245208761-ba872912fac0?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=608629c5a6a368d239d44bf9e84956b8",
        "https://images.unsplash.com/photo-1499858476316-343e284f1f67?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=2984b44b9037f1f962c654d98623f164",
        "https://images.unsplash.com/photo-1505765050516-f72dcac9c60e?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=74c51a189ca1ab912a127122cf6ba45f",
        "https://images.unsplash.com/photo-1501535033-a594139be346?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=a5ca982ba1f44c18875bd8b72e67e4d4",
        "https://images.unsplash.com/photo-1421217336522-861978fdf33a?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=9a94d8eeeab21910577f3aa06616c2a3",
        "https://images.unsplash.com/photo-1531722569936-825d3dd91b15?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=78373e82cef9f932331f900b22bf112f",
        "https://images.unsplash.com/photo-1495837174058-628aafc7d610?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=25a869d0508052fc6ee345447cb090c1",
        "https://images.unsplash.com/photo-1527048322413-4e4e56c8ab3b?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=69287e502ab9e858262921ee9dfcd271",
        "https://images.unsplash.com/photo-1486334803289-1623f249dd1e?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=14fad02f7a44318650b93dd27fc8435b",
        "https://images.unsplash.com/photo-1527580795266-e93c8e079c22?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=03b0b98b9a6dcf12f74a59374cbbdf57",
        "https://images.unsplash.com/photo-1521337098078-16e72eed5e7f?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=2e4d953a9cb48c8d0b5715ce62a1e207",
        "https://images.unsplash.com/photo-1531177071211-ed1b7991958b?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=211aa96cc231a701af846cc1c32691ee",
        "https://images.unsplash.com/photo-1526565331177-f84e9890993c?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=e14fea89b7fc51795dc8bda5d9621e4e",
        "https://images.unsplash.com/photo-1483400783404-0aba8bec2b3b?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=6cbe9bcc3203df879220567b09e88da9",
        "https://images.unsplash.com/photo-1476231682828-37e571bc172f?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=27ebaa494d10180da25d396abeb638f9",
        "https://images.unsplash.com/photo-1471295253337-3ceaaedca402?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=214d4b3c50b289db475238a0c19c5ef5",
        "https://images.unsplash.com/photo-1526737511523-ba685f246e8c?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=7673dbfb790ac307819fe5eec639da7b"
    )

    fun getRandomImageUrl() = IMAGE_URLS.shuffled().first() + QUALITY_SETTINGS
}