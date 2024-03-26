# Shin

[![Platforms](https://img.shields.io/badge/web-WebAssembly-blue)](https://sh.procyk.in)
[![Platforms](https://img.shields.io/badge/mobile-Android%20%7C%20iOS-blue)](https://github.com/avan1235/shin/releases/latest)
[![Platforms](https://img.shields.io/badge/desktop-Windows%20%7C%20macOS%20%7C%20Linux-blue)](https://github.com/avan1235/shin/releases/latest)

[![Build](https://img.shields.io/github/actions/workflow/status/avan1235/shin/release.yml?label=Build&color=green)](https://github.com/avan1235/shin/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/avan1235/shin?label=Release&color=green)](https://github.com/avan1235/shin/releases/latest)
[![Docker](https://img.shields.io/docker/v/avan1235/shin?label=Docker%20Hub&color=green)](https://hub.docker.com/repository/docker/avan1235/shin/tags?ordering=last_updated)

[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](./LICENSE.md)
[![GitHub Repo stars](https://img.shields.io/github/stars/avan1235/shin?style=social)](https://github.com/avan1235/shin/stargazers)
[![Fork Shin](https://img.shields.io/github/forks/avan1235/shin?logo=github&style=social)](https://github.com/avan1235/shin/fork)

## Download and run application

### Download compiled application

#### Google Play

Latest Android version is available on
[Google Play](https://play.google.com/store/apps/details?id=in.procyk.shin).

<a href='https://play.google.com/store/apps/details?id=in.procyk.shin'><img alt='Get it on Google Play' width="300" src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

#### GitHub Releases

You can download compiled version of application from
[GitHub Releases](https://github.com/avan1235/shin/releases).

You can find the compiled vesion of the application for Android, Linux, macOS and Windows.

Please note that for running unsigned version of macOS application, you need to temporarily
disable Gatekeeper, so after installing the application run

```shell
sudo xattr -dr com.apple.quarantine  /Applications/Shin.app
```

in the terminal. You can learn more about this
[here](https://web.archive.org/web/20230318124537/https://disable-gatekeeper.github.io/).

To install Linux version run:

```shell
sudo dpkg -i  shin.deb
```

### Build application locally

The project is configured with with Gradle and you can find the
latest release build commands in the [release.yml](./.github/workflows/release.yml) file.

Example build commands for particular platforms:
- desktop: `./gradlew composeApp:packageDistributionForCurrentOS`
- Android: `./gradlew composeApp:assembleDebug`
- iOS: open [iosApp.xcodeproj](./iosApp/iosApp.xcodeproj) in Xcode and run the build
(you might need to configure the `Team` in `Signing & Capabilities`)
