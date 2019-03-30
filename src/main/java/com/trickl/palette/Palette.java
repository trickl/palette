/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trickl.palette;

import com.trickl.util.TimingLogger;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * A helper class to extract prominent colors from an image.
 *
 * <p>A number of colors with different profiles are extracted from the image:
 *
 * <ul>
 *   <li>Vibrant
 *   <li>Vibrant Dark
 *   <li>Vibrant Light
 *   <li>Muted
 *   <li>Muted Dark
 *   <li>Muted Light
 * </ul>
 *
 * These can be retrieved from the appropriate getter method.
 *
 * <p>Instances are created with a {@link com.trickl.palette.Palette.Builder} which supports several options to tweak the
 * generated Palette. See that class' documentation for more information.
 *
 * <p>Generation should always be completed on a background thread, ideally the one in which you
 * load your image on. {@link com.trickl.palette.Palette.Builder} supports both synchronous and asynchronous generation:
 *
 * <pre>
 * // Synchronous
 * Palette p = Palette.from(bitmap).generate();
 *
 * // Asynchronous
 * Palette.from(bitmap).generate(new PaletteAsyncListener() {
 *     public void onGenerated(Palette p) {
 *         // Use generated instance
 *     }
 * });
 * </pre>
 *
 * @author tgee
 * @version $Id: $Id
 */
public final class Palette {

  static final int DEFAULT_RESIZE_BITMAP_AREA = 112 * 112;
  static final int DEFAULT_CALCULATE_NUMBER_COLORS = 16;

  static final float MIN_CONTRAST_TITLE_TEXT = 3.0f;
  static final float MIN_CONTRAST_BODY_TEXT = 4.5f;

  static final String LOG_TAG = "Palette";
  static final boolean LOG_TIMINGS = false;

  /**
   * Start generating a {@link com.trickl.palette.Palette} with the returned {@link com.trickl.palette.Palette.Builder} instance.
   *
   * @param bitmap a {@link java.awt.image.BufferedImage} object.
   * @return a {@link com.trickl.palette.Palette.Builder} object.
   */
  public static Builder from(BufferedImage bitmap) {
    return new Builder(bitmap);
  }

  /**
   * Generate a {@link com.trickl.palette.Palette} from the pre-generated list of {@link com.trickl.palette.Palette.Swatch} swatches. This
   * is useful for testing, or if you want to resurrect a {@link com.trickl.palette.Palette} instance from a list of
   * swatches. Will return null if the {@code swatches} is null.
   *
   * @param swatches a {@link java.util.List} object.
   * @return a {@link com.trickl.palette.Palette} object.
   */
  public static Palette from(List<Swatch> swatches) {
    return new Builder(swatches).generate();
  }

  /**
   * <p>generate.</p>
   *
   * @param bitmap a {@link java.awt.image.BufferedImage} object.
   * @return a {@link com.trickl.palette.Palette} object.
   */
  @Deprecated
  public static Palette generate(BufferedImage bitmap) {
    return from(bitmap).generate();
  }

  /**
   * <p>generate.</p>
   *
   * @param bitmap a {@link java.awt.image.BufferedImage} object.
   * @param numColors a int.
   * @return a {@link com.trickl.palette.Palette} object.
   */
  @Deprecated
  public static Palette generate(BufferedImage bitmap, int numColors) {
    return from(bitmap).maximumColorCount(numColors).generate();
  }

  private final List<Swatch> mSwatches;
  private final List<Target> mTargets;

  private final Map<Target, Swatch> mSelectedSwatches;
  private final Map<Color, Boolean> mUsedColors;

  private final Swatch mDominantSwatch;

  Palette(List<Swatch> swatches, List<Target> targets) {
    mSwatches = swatches;
    mTargets = targets;

    mUsedColors = new HashMap<>();
    mSelectedSwatches = new HashMap<>();

    mDominantSwatch = findDominantSwatch();
  }

  /**
   * Returns all of the swatches which make up the palette.
   *
   * @return a {@link java.util.List} object.
   */
  @NonNull
  public List<Swatch> getSwatches() {
    return Collections.unmodifiableList(mSwatches);
  }

  /**
   * Returns the targets used to generate this palette.
   *
   * @return a {@link java.util.List} object.
   */
  @NonNull
  public List<Target> getTargets() {
    return Collections.unmodifiableList(mTargets);
  }

  /**
   * Returns the most vibrant swatch in the palette. Might be null.
   *
   * @see Target#VIBRANT
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getVibrantSwatch() {
    return getSwatchForTarget(Target.VIBRANT);
  }

  /**
   * Returns a light and vibrant swatch from the palette. Might be null.
   *
   * @see Target#LIGHT_VIBRANT
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getLightVibrantSwatch() {
    return getSwatchForTarget(Target.LIGHT_VIBRANT);
  }

  /**
   * Returns a dark and vibrant swatch from the palette. Might be null.
   *
   * @see Target#DARK_VIBRANT
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getDarkVibrantSwatch() {
    return getSwatchForTarget(Target.DARK_VIBRANT);
  }

  /**
   * Returns a muted swatch from the palette. Might be null.
   *
   * @see Target#MUTED
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getMutedSwatch() {
    return getSwatchForTarget(Target.MUTED);
  }

  /**
   * Returns a muted and light swatch from the palette. Might be null.
   *
   * @see Target#LIGHT_MUTED
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getLightMutedSwatch() {
    return getSwatchForTarget(Target.LIGHT_MUTED);
  }

  /**
   * Returns a muted and dark swatch from the palette. Might be null.
   *
   * @see Target#DARK_MUTED
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getDarkMutedSwatch() {
    return getSwatchForTarget(Target.DARK_MUTED);
  }

  /**
   * Returns the most vibrant color in the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getVibrantSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getVibrantColor(final Color defaultColor) {
    return getColorForTarget(Target.VIBRANT, defaultColor);
  }

  /**
   * Returns a light and vibrant color from the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getLightVibrantSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getLightVibrantColor(final Color defaultColor) {
    return getColorForTarget(Target.LIGHT_VIBRANT, defaultColor);
  }

  /**
   * Returns a dark and vibrant color from the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getDarkVibrantSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getDarkVibrantColor(final Color defaultColor) {
    return getColorForTarget(Target.DARK_VIBRANT, defaultColor);
  }

  /**
   * Returns a muted color from the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getMutedSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getMutedColor(final Color defaultColor) {
    return getColorForTarget(Target.MUTED, defaultColor);
  }

  /**
   * Returns a muted and light color from the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getLightMutedSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getLightMutedColor(final Color defaultColor) {
    return getColorForTarget(Target.LIGHT_MUTED, defaultColor);
  }

  /**
   * Returns a muted and dark color from the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getDarkMutedSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getDarkMutedColor(final Color defaultColor) {
    return getColorForTarget(Target.DARK_MUTED, defaultColor);
  }

  /**
   * Returns the selected swatch for the given target from the palette, or {@code null} if one could
   * not be found.
   *
   * @param target a {@link com.trickl.palette.Target} object.
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getSwatchForTarget(@NonNull final Target target) {
    return mSelectedSwatches.get(target);
  }

  /**
   * Returns the selected color for the given target from the palette as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @param target a {@link com.trickl.palette.Target} object.
   * @return a {@link java.awt.Color} object.
   */
  public Color getColorForTarget(@NonNull final Target target, final Color defaultColor) {
    Swatch swatch = getSwatchForTarget(target);
    return swatch != null ? swatch.getColor() : defaultColor;
  }

  /**
   * Returns the dominant swatch from the palette.
   *
   * <p>The dominant swatch is defined as the swatch with the greatest population (frequency) within
   * the palette.
   *
   * @return a {@link com.trickl.palette.Palette.Swatch} object.
   */
  public Swatch getDominantSwatch() {
    return mDominantSwatch;
  }

  /**
   * Returns the color of the dominant swatch from the palette, as an RGB packed int.
   *
   * @param defaultColor value to return if the swatch isn't available
   * @see #getDominantSwatch()
   * @return a {@link java.awt.Color} object.
   */
  public Color getDominantColor(Color defaultColor) {
    return mDominantSwatch != null ? mDominantSwatch.getColor() : defaultColor;
  }

  void generate() {
    // We need to make sure that the scored targets are generated first. This is so that
    // inherited targets have something to inherit from
    for (int i = 0, count = mTargets.size(); i < count; i++) {
      final Target target = mTargets.get(i);
      target.normalizeWeights();
      mSelectedSwatches.put(target, generateScoredTarget(target));
    }
    // We now clear out the used colors
    mUsedColors.clear();
  }

  private Swatch generateScoredTarget(final Target target) {
    final Swatch maxScoreSwatch = getMaxScoredSwatchForTarget(target);
    if (maxScoreSwatch != null && target.isExclusive()) {
      // If we have a swatch, and the target is exclusive, add the color to the used list
      mUsedColors.put(maxScoreSwatch.getColor(), true);
    }
    return maxScoreSwatch;
  }

  private Swatch getMaxScoredSwatchForTarget(final Target target) {
    float maxScore = 0;
    Swatch maxScoreSwatch = null;
    for (int i = 0, count = mSwatches.size(); i < count; i++) {
      final Swatch swatch = mSwatches.get(i);
      if (shouldBeScoredForTarget(swatch, target)) {
        final float score = generateScore(swatch, target);
        if (maxScoreSwatch == null || score > maxScore) {
          maxScoreSwatch = swatch;
          maxScore = score;
        }
      }
    }
    return maxScoreSwatch;
  }

  private boolean shouldBeScoredForTarget(final Swatch swatch, final Target target) {
    // Check whether the HSL values are within the correct ranges, and this color hasn't
    // been used yet.
    final float hsl[] = swatch.getHsl();
    return hsl[1] >= target.getMinimumSaturation()
        && hsl[1] <= target.getMaximumSaturation()
        && hsl[2] >= target.getMinimumLightness()
        && hsl[2] <= target.getMaximumLightness()
        && !mUsedColors.containsKey(swatch.getColor());
  }

  private float generateScore(Swatch swatch, Target target) {
    final float[] hsl = swatch.getHsl();

    float saturationScore = 0;
    float luminanceScore = 0;
    float populationScore = 0;

    final int maxPopulation = mDominantSwatch != null ? mDominantSwatch.getPopulation() : 1;

    if (target.getSaturationWeight() > 0) {
      saturationScore =
          target.getSaturationWeight() * (1f - Math.abs(hsl[1] - target.getTargetSaturation()));
    }
    if (target.getLightnessWeight() > 0) {
      luminanceScore =
          target.getLightnessWeight() * (1f - Math.abs(hsl[2] - target.getTargetLightness()));
    }
    if (target.getPopulationWeight() > 0) {
      populationScore =
          target.getPopulationWeight() * (swatch.getPopulation() / (float) maxPopulation);
    }

    return saturationScore + luminanceScore + populationScore;
  }

  private Swatch findDominantSwatch() {
    int maxPop = Integer.MIN_VALUE;
    Swatch maxSwatch = null;
    for (int i = 0, count = mSwatches.size(); i < count; i++) {
      Swatch swatch = mSwatches.get(i);
      if (swatch.getPopulation() > maxPop) {
        maxSwatch = swatch;
        maxPop = swatch.getPopulation();
      }
    }
    return maxSwatch;
  }

  private static float[] copyHslValues(Swatch color) {
    final float[] newHsl = new float[3];
    System.arraycopy(color.getHsl(), 0, newHsl, 0, 3);
    return newHsl;
  }

  /**
   * Represents a color swatch generated from an image's palette. The RGB color can be retrieved by
   * calling {@link #getRgb()}.
   */
  public static final class Swatch {
    private final int mRed, mGreen, mBlue;
    private final Color mColor;
    private final int mPopulation;

    private boolean mGeneratedTextColors;
    private Color mTitleTextColor;
    private Color mBodyTextColor;

    private float[] mHsl;

    public Swatch(Color color, int population) {
      mRed = color.getRed();
      mGreen = color.getGreen();
      mBlue = color.getBlue();
      mColor = color;
      mPopulation = population;
    }

    /** @return this swatch's color value */
    public Color getColor() {
      return mColor;
    }

    /** @return this swatch's RGB color value */
    public int getRgb() {
      return mColor.getRGB();
    }

    /**
     * Return this swatch's HSL values. hsv[0] is Hue [0 .. 360) hsv[1] is Saturation [0...1] hsv[2]
     * is Lightness [0...1]
     * @return HSL values
     */
    public float[] getHsl() {
      if (mHsl == null) {
        mHsl = new float[3];
      }
      ColorUtils.RGBToHSL(mRed, mGreen, mBlue, mHsl);
      return mHsl;
    }

    /** @return the number of pixels represented by this swatch */
    public int getPopulation() {
      return mPopulation;
    }

    /**
     * Returns an appropriate color to use for any 'title' text which is displayed over this {@link
     * Swatch}'s color. This color is guaranteed to have sufficient contrast.
    * @return An appropriate color
     */
    public Color getTitleTextColor() {
      ensureTextColorsGenerated();
      return mTitleTextColor;
    }

    /**
     * Returns an appropriate color to use for any 'body' text which is displayed over this {@link
     * Swatch}'s color. This color is guaranteed to have sufficient contrast.
     * @return An appropriate color
     */
    public Color getBodyTextColor() {
      ensureTextColorsGenerated();
      return mBodyTextColor;
    }

    private void ensureTextColorsGenerated() {
      if (!mGeneratedTextColors) {
        // First check white, as most colors will be dark
        final int lightBodyAlpha =
            ColorUtils.calculateMinimumAlpha(Color.WHITE, mColor, MIN_CONTRAST_BODY_TEXT);
        final int lightTitleAlpha =
            ColorUtils.calculateMinimumAlpha(Color.WHITE, mColor, MIN_CONTRAST_TITLE_TEXT);

        if (lightBodyAlpha != -1 && lightTitleAlpha != -1) {
          // If we found valid light values, use them and return
          mBodyTextColor = ColorUtils.setAlphaComponent(Color.WHITE, lightBodyAlpha);
          mTitleTextColor = ColorUtils.setAlphaComponent(Color.WHITE, lightTitleAlpha);
          mGeneratedTextColors = true;
          return;
        }

        final int darkBodyAlpha =
            ColorUtils.calculateMinimumAlpha(Color.BLACK, mColor, MIN_CONTRAST_BODY_TEXT);
        final int darkTitleAlpha =
            ColorUtils.calculateMinimumAlpha(Color.BLACK, mColor, MIN_CONTRAST_TITLE_TEXT);

        if (darkBodyAlpha != -1 && darkBodyAlpha != -1) {
          // If we found valid dark values, use them and return
          mBodyTextColor = ColorUtils.setAlphaComponent(Color.BLACK, darkBodyAlpha);
          mTitleTextColor = ColorUtils.setAlphaComponent(Color.BLACK, darkTitleAlpha);
          mGeneratedTextColors = true;
          return;
        }

        // If we reach here then we can not find title and body values which use the same
        // lightness, we need to use mismatched values
        mBodyTextColor =
            lightBodyAlpha != -1
                ? ColorUtils.setAlphaComponent(Color.WHITE, lightBodyAlpha)
                : ColorUtils.setAlphaComponent(Color.BLACK, darkBodyAlpha);
        mTitleTextColor =
            lightTitleAlpha != -1
                ? ColorUtils.setAlphaComponent(Color.WHITE, lightTitleAlpha)
                : ColorUtils.setAlphaComponent(Color.BLACK, darkTitleAlpha);
        mGeneratedTextColors = true;
      }
    }

    @Override
    public String toString() {
      return new StringBuilder(getClass().getSimpleName())
          .append(" [RGB: #")
          .append(Integer.toHexString(getRgb()))
          .append(']')
          .append(" [HSL: ")
          .append(Arrays.toString(getHsl()))
          .append(']')
          .append(" [Population: ")
          .append(mPopulation)
          .append(']')
          .append(" [Title Text: #")
          .append(Integer.toHexString(getTitleTextColor().getRGB()))
          .append(']')
          .append(" [Body Text: #")
          .append(Integer.toHexString(getBodyTextColor().getRGB()))
          .append(']')
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Swatch swatch = (Swatch) o;
      return mPopulation == swatch.mPopulation && mColor.equals(swatch.mColor);
    }

    @Override
    public int hashCode() {
      return 31 * mColor.getRGB() + mPopulation;
    }
  }

  /** Builder class for generating {@link Palette} instances. */
  public static final class Builder {
    private final List<Swatch> mSwatches;
    private final BufferedImage mBufferedImage;

    private final List<Target> mTargets = new ArrayList<>();

    private int mMaxColors = DEFAULT_CALCULATE_NUMBER_COLORS;
    private int mResizeArea = DEFAULT_RESIZE_BITMAP_AREA;
    private int mResizeMaxDimension = -1;

    private final List<Filter> mFilters = new ArrayList<>();
    private Rectangle mRegion;

    /** Construct a new {@link Builder} using a source {@link BufferedImage}
    * @param bitmap source image */
    public Builder(BufferedImage bitmap) {
      if (bitmap == null) {
        throw new IllegalArgumentException("BufferedImage is not valid");
      }
      mFilters.add(DEFAULT_FILTER);
      mBufferedImage = bitmap;
      mSwatches = null;

      // Add the default targets
      mTargets.add(Target.LIGHT_VIBRANT);
      mTargets.add(Target.VIBRANT);
      mTargets.add(Target.DARK_VIBRANT);
      mTargets.add(Target.LIGHT_MUTED);
      mTargets.add(Target.MUTED);
      mTargets.add(Target.DARK_MUTED);
    }

    /**
     * Construct a new {@link Builder} using a list of {@link Swatch} instances. Typically only used
     * for testing.
     * @param swatches list of swatches
     */
    public Builder(List<Swatch> swatches) {
      if (swatches == null || swatches.isEmpty()) {
        throw new IllegalArgumentException("List of Swatches is not valid");
      }
      mFilters.add(DEFAULT_FILTER);
      mSwatches = swatches;
      mBufferedImage = null;
    }

    /**
     * Set the maximum number of colors to use in the quantization step when using a {@link
     * java.awt.image.BufferedImage} as the source.
     *
     * <p>Good values for depend on the source image type. For landscapes, good values are in the
     * range 10-16. For images which are largely made up of people's faces then this value should be
     * increased to ~24.
     * @param colors maximum number of colors
     * @return a new builder
     */
    @NonNull
    public Builder maximumColorCount(int colors) {
      mMaxColors = colors;
      return this;
    }

    /**
     * Set the resize value when using a {@link java.awt.image.BufferedImage} as the source. If
     * the bitmap's largest dimension is greater than the value specified, then the bitmap will be
     * resized so that its largest dimension matches {@code maxDimension}. If the bitmap is smaller
     * or equal, the original is used as-is.
     *
     * @return A new builder
     * @deprecated Using {@link #resizeBufferedImageArea(int)} is preferred since it can handle
     *     abnormal aspect ratios more gracefully.
     * @param maxDimension the number of pixels that the max dimension should be scaled down to, or
     *     any value &lt;= 0 to disable resizing.
     */
    @NonNull
    @Deprecated
    public Builder resizeBufferedImageSize(final int maxDimension) {
      mResizeMaxDimension = maxDimension;
      mResizeArea = -1;
      return this;
    }

    /**
     * Set the resize value when using a {@link java.awt.image.BufferedImage} as the source. If
     * the bitmap's area is greater than the value specified, then the bitmap will be resized so
     * that its area matches {@code area}. If the bitmap is smaller or equal, the original is used
     * as-is.
     *
     * <p>This value has a large effect on the processing time. The larger the resized image is, the
     * greater time it will take to generate the palette. The smaller the image is, the more detail
     * is lost in the resulting image and thus less precision for color selection.
     *
     * @param area the number of pixels that the intermediary scaled down BufferedImage should
     *     cover, or any value &lt;= 0 to disable resizing.
     * @return A new builder
     */
    @NonNull
    public Builder resizeBufferedImageArea(final int area) {
      mResizeArea = area;
      mResizeMaxDimension = -1;
      return this;
    }

    /**
     * Clear all added filters. This includes any default filters added automatically by {@link
     * Palette}.
     * @return A new builder
     */
    @NonNull
    public Builder clearFilters() {
      mFilters.clear();
      return this;
    }

    /**
     * Add a filter to be able to have fine grained control over which colors are allowed in the
     * resulting palette.
     *
     * @param filter filter to add.
     * @return A new builder
     */
    @NonNull
    public Builder addFilter(Filter filter) {
      if (filter != null) {
        mFilters.add(filter);
      }
      return this;
    }

    /**
     * Set a region of the bitmap to be used exclusively when calculating the palette.
     *
     * <p>This only works when the original input is a {@link BufferedImage}.
     *
     * @param left The left side of the rectangle used for the region.
     * @param top The top of the rectangle used for the region.
     * @param right The right side of the rectangle used for the region.
     * @param bottom The bottom of the rectangle used for the region.
     * @return A new builder
     */
    @NonNull
    public Builder setRegion(int left, int top, int right, int bottom) {
      if (mBufferedImage != null) {
        if (mRegion == null) mRegion = new Rectangle();
        // Set the Rect to be initially the whole BufferedImage
        mRegion.setRect(0, 0, mBufferedImage.getWidth(), mBufferedImage.getHeight());
        // Now just get the intersection with the region
        if (!mRegion.intersects(left, top, right - left, bottom - top)) {
          throw new IllegalArgumentException(
              "The given region must intersect with " + "the BufferedImage's dimensions.");
        }
      }
      return this;
    }

    /** Clear any previously region set via {@link #setRegion(int, int, int, int)}.
    * @return A new builder */
    @NonNull
    public Builder clearRegion() {
      mRegion = null;
      return this;
    }

    /**
     * Add a target profile to be generated in the palette.
     *
     * <p>You can retrieve the result via {@link Palette#getSwatchForTarget(Target)}.
     * @param target Target profile
     * @return A new builder
     */
    @NonNull
    public Builder addTarget(@NonNull final Target target) {
      if (!mTargets.contains(target)) {
        mTargets.add(target);
      }
      return this;
    }

    /**
     * Clear all added targets. This includes any default targets added automatically by {@link
     * Palette}.
     * @return A new builder
     */
    @NonNull
    public Builder clearTargets() {
      if (mTargets != null) {
        mTargets.clear();
      }
      return this;
    }

    /** Generate and return the {@link Palette} synchronously.
     * @return The generated palette */
    @NonNull
    public Palette generate() {
      final TimingLogger logger = LOG_TIMINGS ? new TimingLogger(LOG_TAG, "Generation") : null;

      List<Swatch> swatches;

      if (mBufferedImage != null) {
        // We have a BufferedImage so we need to use quantization to reduce the number of colors

        // First we'll scale down the bitmap if needed
        final BufferedImage bitmap = scaleBufferedImageDown(mBufferedImage);

        if (logger != null) {
          logger.addSplit("Processed BufferedImage");
        }

        final Rectangle region = mRegion;
        if (bitmap != mBufferedImage && region != null) {
          // If we have a scaled bitmap and a selected region, we need to scale down the
          // region to match the new scale
          final double scale = bitmap.getWidth() / (double) mBufferedImage.getWidth();
          region.x = (int) Math.floor(region.x * scale);
          region.y = (int) Math.floor(region.y * scale);
          region.width = Math.min((int) Math.ceil(region.width * scale), bitmap.getWidth());
          region.height = Math.min((int) Math.ceil(region.height * scale), bitmap.getHeight());
        }

        // Now generate a quantizer from the BufferedImage
        final ColorCutQuantizer quantizer =
            new ColorCutQuantizer(
                getPixelsFromBufferedImage(bitmap),
                mMaxColors,
                mFilters.isEmpty() ? null : mFilters.toArray(new Filter[mFilters.size()]));

        swatches = quantizer.getQuantizedColors();

        if (logger != null) {
          logger.addSplit("Color quantization completed");
        }
      } else {
        // Else we're using the provided swatches
        swatches = mSwatches;
      }

      // Now create a Palette instance
      final Palette p = new Palette(swatches, mTargets);
      // And make it generate itself
      p.generate();

      if (logger != null) {
        logger.addSplit("Created Palette");
        logger.dumpToLog();
      }

      return p;
    }

    private int[] getPixelsFromBufferedImage(BufferedImage bitmap) {
      final int bitmapWidth = bitmap.getWidth();
      final int bitmapHeight = bitmap.getHeight();
      final int[] pixels = new int[bitmapWidth * bitmapHeight];
      final boolean hasAlphaChannel = bitmap.getAlphaRaster() != null;
      final int[] data =
          bitmap.getRaster().getPixels(0, 0, bitmapWidth, bitmapHeight, (int[]) null);

      if (hasAlphaChannel) {
        final int channels = 4;
        for (int pixel = 0; pixel < pixels.length; pixel += 1) {
          int argb = 0;
          int offset = pixel * channels;
          argb += (((int) data[offset + 3] & 0xff) << 24); // alpha
          argb += (((int) data[offset] & 0xff) << 16); // red
          argb += (((int) data[offset + 1] & 0xff) << 8); // green
          argb += ((int) data[offset + 2] & 0xff); // blue
          pixels[pixel] = argb;
        }
      } else {
        final int pixelLength = 3;
        for (int pixel = 0; pixel < pixels.length; pixel += 1) {
          int argb = 0;
          int offset = pixel * pixelLength;
          argb += -16777216; // 255 alpha
          argb += (((int) data[offset] & 0xff) << 16); // red
          argb += (((int) data[offset + 1] & 0xff) << 8); // green
          argb += ((int) data[offset + 2] & 0xff); // blue
          pixels[pixel] = argb;
        }
      }

      if (mRegion == null) {
        // If we don't have a region, return all of the pixels
        return pixels;
      } else {
        // If we do have a region, lets create a subset array containing only the region's
        // pixels
        final int regionWidth = mRegion.width;
        final int regionHeight = mRegion.height;
        // pixels contains all of the pixels, so we need to iterate through each row and
        // copy the regions pixels into a new smaller array
        final int[] subsetPixels = new int[regionWidth * regionHeight];
        for (int row = 0; row < regionHeight; row++) {
          System.arraycopy(
              pixels,
              ((row + mRegion.y) * bitmapWidth) + mRegion.x,
              subsetPixels,
              row * regionWidth,
              regionWidth);
        }
        return subsetPixels;
      }
    }

    /** Scale the bitmap down as needed. */
    private BufferedImage scaleBufferedImageDown(final BufferedImage bitmap) {
      double scaleRatio = -1;

      if (mResizeArea > 0) {
        final int bitmapArea = bitmap.getWidth() * bitmap.getHeight();
        if (bitmapArea > mResizeArea) {
          scaleRatio = Math.sqrt(mResizeArea / (double) bitmapArea);
        }
      } else if (mResizeMaxDimension > 0) {
        final int maxDimension = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (maxDimension > mResizeMaxDimension) {
          scaleRatio = mResizeMaxDimension / (double) maxDimension;
        }
      }

      if (scaleRatio <= 0) {
        // Scaling has been disabled or not needed so just return the BufferedImage
        return bitmap;
      }

      BufferedImage scaledImage =
          new BufferedImage(
              (int) Math.ceil(bitmap.getWidth() * scaleRatio),
              (int) Math.ceil(bitmap.getHeight() * scaleRatio),
              BufferedImage.TYPE_INT_RGB);

      Graphics g = scaledImage.createGraphics();
      g.drawImage(
          bitmap,
          0,
          0,
          (int) Math.ceil(bitmap.getWidth() * scaleRatio),
          (int) Math.ceil(bitmap.getHeight() * scaleRatio),
          null);
      g.dispose();

      return scaledImage;
    }
  }

  /**
   * A Filter provides a mechanism for exercising fine-grained control over which colors are valid
   * within a resulting {@link Palette}.
   */
  public interface Filter {
    /**
     * Hook to allow clients to be able filter colors from resulting palette.
     *
     * @param rgb the color in RGB888.
     * @param hsl HSL representation of the color.
     * @return true if the color is allowed, false if not.
     */
    boolean isAllowed(int rgb, float[] hsl);
  }

  /** The default filter. */
  static final Filter DEFAULT_FILTER =
      new Filter() {
        private static final float BLACK_MAX_LIGHTNESS = 0.05f;
        private static final float WHITE_MIN_LIGHTNESS = 0.95f;

        @Override
        public boolean isAllowed(int rgb, float[] hsl) {
          return !isWhite(hsl) && !isBlack(hsl) && !isNearRedILine(hsl);
        }

        /** @return true if the color represents a color which is close to black. */
        private boolean isBlack(float[] hslColor) {
          return hslColor[2] <= BLACK_MAX_LIGHTNESS;
        }

        /** @return true if the color represents a color which is close to white. */
        private boolean isWhite(float[] hslColor) {
          return hslColor[2] >= WHITE_MIN_LIGHTNESS;
        }

        /** @return true if the color lies close to the red side of the I line. */
        private boolean isNearRedILine(float[] hslColor) {
          return hslColor[0] >= 10f && hslColor[0] <= 37f && hslColor[1] <= 0.82f;
        }
      };
}
