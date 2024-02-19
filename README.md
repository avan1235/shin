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

## Introduction

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop, Server.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.

* `/iosApp` contains iOS applications. Eventhough we’re sharing UI with Compose Multiplatform,
  it's needed as an entry point for an iOS app.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
