# Trickl Palette

[![build_status](https://travis-ci.com/trickl/palette.svg?branch=master)](https://travis-ci.com/trickl/palette)
[![Maintainability](https://api.codeclimate.com/v1/badges/68447bed3afc81bc7450/maintainability)](https://codeclimate.com/github/trickl/palette/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/68447bed3afc81bc7450/test_coverage)](https://codeclimate.com/github/trickl/palette/test_coverage)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This is a Java port of the Android Support Palette library to work outside the Android ecosystem.

From the Android support pages -
```
Good visual design is essential for a successful app, and color schemes are a primary component of design. The palette library is a support library that extracts prominent colors from images to help you create visually engaging apps.
```

##### Figure - An example image and its extracted color profiles given the default maximum color count (16) for the palette.

![Example Image](https://developer.android.com/training/material/images/palette-library-color-profiles_2-1_2x.png)

### Prerequisites

Requires Maven and a Java 6 compiler installed on your system.

## Usage

See the Junit tests for usage

### Installing

To download the library into a folder called "palette" run

```
git clone https://github.com/trickl/palette.git
```

To build the library run

```
mvn clean build
```

## Acknowledgments

* The original API for Android - https://developer.android.com/training/material/palette-colors
