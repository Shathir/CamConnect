package com.outdu.camconnect.ui.models

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for LayoutState and LayoutMode.
 */
class LayoutStateTest {

    @Test
    fun layoutMode_hasThreeValues() {
        val values = LayoutMode.values()
        Assert.assertEquals(3, values.size)
        Assert.assertEquals(LayoutMode.MINIMAL_CONTROL, values[0])
    }

    @Test
    fun layoutState_companionGetLayoutProportions() {
        val (left, right) = LayoutState.getLayoutProportions(LayoutMode.MINIMAL_CONTROL)
        Assert.assertEquals(0.9f, left, 0.01f)
        Assert.assertEquals(0.1f, right, 0.01f)
    }
}
