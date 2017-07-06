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

import static com.trickl.palette.TestUtils.loadSampleBufferedImage;
import static org.junit.Assert.assertTrue;
import com.trickl.test.filters.SmallTests;

import org.junit.Test;

import org.junit.experimental.categories.Category;

@Category(SmallTests.class)
public class MaxColorsTest {

    @Test
    public void testMaxColorCount32() {
        testMaxColorCount(32);
    }

    @Test
    public void testMaxColorCount1() {
        testMaxColorCount(1);
    }

    @Test
    public void testMaxColorCount15() {
        testMaxColorCount(15);
    }

    private void testMaxColorCount(int colorCount) {
        Palette newPalette = Palette.from(loadSampleBufferedImage())
                .maximumColorCount(colorCount)
                .generate();
        assertTrue(newPalette.getSwatches().size() <= colorCount);
    }
}
