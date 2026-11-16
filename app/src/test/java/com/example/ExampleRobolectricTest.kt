package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ElementAffinity
import com.example.model.KouhakuMaps
import com.example.model.MonsterCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and verify monster catalog and maps`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kouhaku Brass & Silver", appName)

        // Verify all 18 original Kouhaku species and elemental chart
        assertEquals(18, MonsterCatalog.speciesList.size)
        val kitsunebi = MonsterCatalog.getSpecies(1)
        assertEquals("Kitsunebi", kitsunebi.name)
        assertTrue(ElementAffinity.FLAME.effectivenessAgainst(ElementAffinity.BRASS) > 1.0f)

        // Verify regional maps
        val momiji = KouhakuMaps.getMap("momiji_hamlet")
        assertEquals("Momiji Hamlet", momiji.name)
    }
}
