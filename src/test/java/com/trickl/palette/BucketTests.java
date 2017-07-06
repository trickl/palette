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
import static com.trickl.palette.TestUtils.assertCloseColors;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.trickl.test.filters.SmallTests;

import org.junit.Test;

import java.util.ArrayList;

import org.junit.experimental.categories.Category;

@Category(SmallTests.class)
public class BucketTests {

    @Test(expected = UnsupportedOperationException.class)
    public void testSwatchesUnmodifiable() {
        Palette p = Palette.from(loadSampleBufferedImage()).generate();
        p.getSwatches().remove(0);
    }

    @Test
    public void testSwatchesBuilder() {
        ArrayList<Palette.Swatch> swatches = new ArrayList<>();
        swatches.add(new Palette.Swatch(Color.BLACK, 40));
        swatches.add(new Palette.Swatch(Color.GREEN, 60));
        swatches.add(new Palette.Swatch(Color.BLUE, 10));

        Palette p = Palette.from(swatches);

        assertEquals(swatches, p.getSwatches());
    }

    @Test
    public void testRegionWhole() {
        final BufferedImage sample = loadSampleBufferedImage();

        Palette.Builder b = new Palette.Builder(sample);
        b.setRegion(0, 0, sample.getWidth(), sample.getHeight());
        b.generate();
    }

    @Test
    public void testRegionUpperLeft() {
        final BufferedImage sample = loadSampleBufferedImage();

        Palette.Builder b = new Palette.Builder(sample);
        b.setRegion(0, 0, sample.getWidth() / 2, sample.getHeight() / 2);
        b.generate();
    }

    @Test
    public void testRegionBottomRight() {
        final BufferedImage sample = loadSampleBufferedImage();

        Palette.Builder b = new Palette.Builder(sample);
        b.setRegion(sample.getWidth() / 2, sample.getHeight() / 2,
                sample.getWidth(), sample.getHeight());
        b.generate();
    }

    @Test
    public void testOnePixelTallBufferedImage() {
        final BufferedImage bitmap = new BufferedImage(1000, 1, BufferedImage.TYPE_INT_ARGB);

        Palette.Builder b = new Palette.Builder(bitmap);
        b.generate();
    }

    @Test
    public void testOnePixelWideBufferedImage() {
        final BufferedImage bitmap = new BufferedImage(1, 1000, BufferedImage.TYPE_INT_ARGB);

        Palette.Builder b = new Palette.Builder(bitmap);
        b.generate();
    }

    @Test
    public void testBlueBufferedImageReturnsBlueSwatch() {
        final BufferedImage bitmap = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = bitmap.createGraphics();
        g.setPaint(Color.BLUE);
        g.fillRect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
        g.dispose();

        final Palette palette = Palette.from(bitmap).generate();

        assertEquals(1, palette.getSwatches().size());

        final Palette.Swatch swatch = palette.getSwatches().get(0);
        assertCloseColors(Color.BLUE, swatch.getColor());
    }

    @Test
    public void testBlueBufferedImageWithRegionReturnsBlueSwatch() {
        final BufferedImage bitmap = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bitmap.createGraphics();
        g.setPaint(Color.BLUE);
        g.fillRect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
        g.dispose();

        final Palette palette = Palette.from(bitmap)
                .setRegion(0, bitmap.getHeight() / 2, bitmap.getWidth(), bitmap.getHeight())
                .generate();

        assertEquals(1, palette.getSwatches().size());

        final Palette.Swatch swatch = palette.getSwatches().get(0);
        assertCloseColors(Color.BLUE, swatch.getColor());
    }

    @Test
    public void testDominantSwatch() {
        final BufferedImage bitmap = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        // First fill the canvas with blue
        Graphics2D g = bitmap.createGraphics();
        g.setPaint(Color.BLUE);
        g.fillRect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
        
        // Now we'll draw the top 10px tall rect with green
        g.setPaint(Color.GREEN);
        g.fillRect(0, 0, 100, 10);

        // Now we'll draw the next 20px tall rect with red
        g.setPaint(Color.RED);
        g.fillRect(0, 11, 100, 20);

        // Now generate a palette from the bitmap
        final Palette palette = Palette.from(bitmap).generate();

        // First assert that there are 3 swatches
        assertEquals(3, palette.getSwatches().size());

        // Now assert that the dominant swatch is blue
        final Palette.Swatch swatch = palette.getDominantSwatch();
        assertNotNull(swatch);
        assertCloseColors(Color.BLUE, swatch.getColor());
    }

}
