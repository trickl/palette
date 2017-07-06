/*
 * Copyright (C) 2016 The Android Open Source Project
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import lombok.extern.java.Log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;

@Log
class TestUtils {

    static BufferedImage loadSampleBufferedImage() {
        String samplePath = "frog.jpg";
        try {            
            InputStream imageStream = TestUtils.class.getResourceAsStream(samplePath);   
            return ImageIO.read(imageStream);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not load image resource " + samplePath, ex);
        }
        return null;
    }

    static void assertCloseColors(Color expected, Color actual) {
        assertEquals(expected.getRed(), actual.getRed(), 8);
        assertEquals(expected.getGreen(), actual.getGreen(), 8);
        assertEquals(expected.getBlue(), actual.getBlue(), 8);
    }

}
