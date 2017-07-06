/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trickl.palette;

import static com.trickl.palette.ColorUtils.HSLToColor;
import static com.trickl.palette.ColorUtils.calculateContrast;
import static com.trickl.palette.TestUtils.loadSampleBufferedImage;
import java.awt.Color;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.trickl.test.filters.SmallTests;

@Category(SmallTests.class)
public class SwatchTests {

    private static final float MIN_CONTRAST_TITLE_TEXT = 3.0f;
    private static final float MIN_CONTRAST_BODY_TEXT = 4.5f;

    @Test
    public void testTextColorContrasts() {
        final Palette p = Palette.from(loadSampleBufferedImage()).generate();

        for (Palette.Swatch swatch : p.getSwatches()) {
            testSwatchTextColorContrasts(swatch);
        }
    }

    @Test
    public void testHslNotNull() {
        final Palette p = Palette.from(loadSampleBufferedImage()).generate();

        for (Palette.Swatch swatch : p.getSwatches()) {
            assertNotNull(swatch.getHsl());
        }
    }

    @Test
    public void testHslIsRgb() {
        final Palette p = Palette.from(loadSampleBufferedImage()).generate();

        for (Palette.Swatch swatch : p.getSwatches()) {
            assertEquals(HSLToColor(swatch.getHsl()), swatch.getColor());
        }
    }

    private void testSwatchTextColorContrasts(Palette.Swatch swatch) {
        final Color bodyTextColor = swatch.getBodyTextColor();
        assertTrue(calculateContrast(bodyTextColor, swatch.getColor()) >= MIN_CONTRAST_BODY_TEXT);

        final Color titleTextColor = swatch.getTitleTextColor();
        assertTrue(calculateContrast(titleTextColor, swatch.getColor()) >= MIN_CONTRAST_TITLE_TEXT);
    }

    @Test
    public void testEqualsWhenSame() {
        Palette.Swatch swatch1 = new Palette.Swatch(Color.WHITE, 50);
        Palette.Swatch swatch2 = new Palette.Swatch(Color.WHITE, 50);
        assertEquals(swatch1, swatch2);
    }

    @Test
    public void testEqualsWhenColorDifferent() {
        Palette.Swatch swatch1 = new Palette.Swatch(Color.BLACK, 50);
        Palette.Swatch swatch2 = new Palette.Swatch(Color.WHITE, 50);
        assertFalse(swatch1.equals(swatch2));
    }

    @Test
    public void testEqualsWhenPopulationDifferent() {
        Palette.Swatch swatch1 = new Palette.Swatch(Color.BLACK, 50);
        Palette.Swatch swatch2 = new Palette.Swatch(Color.BLACK, 100);
        assertFalse(swatch1.equals(swatch2));
    }

    @Test
    public void testEqualsWhenDifferent() {
        Palette.Swatch swatch1 = new Palette.Swatch(Color.BLUE, 50);
        Palette.Swatch swatch2 = new Palette.Swatch(Color.BLACK, 100);
        assertFalse(swatch1.equals(swatch2));
    }
}
