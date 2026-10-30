
class AyahOrnamentDigitsTest {
    @org.junit.Test fun localizesOnlyOrnaments() {
        org.junit.Assert.assertEquals("x ﴿١٢٧﴾ 5", com.midroid.prayertimes.ui.localizeAyahOrnaments("x ﴿127﴾ 5", true))
        org.junit.Assert.assertEquals("x ﴿127﴾", com.midroid.prayertimes.ui.localizeAyahOrnaments("x ﴿127﴾", false))
    }
}
