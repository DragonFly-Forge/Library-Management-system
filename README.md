# Library Management System

A multi-platform **Library Management System** developed for the College IT Workshop assignment. It implements consistent book inventory, search, issue, and return logic with identical validation rules across three platforms: **Console (Kotlin)**, **Web (HTML/CSS/JS)**, and **Android (Kotlin/XML)**.

## Live Web Demo
- **GitHub Pages:** https://dragonfly-forge.github.io/Library-Management-system/
---

## Repository Structure

```text
LibraryAssignmentKotlin/
├── console/                  # Interactive Kotlin CLI application (Main.kt)
├── docs/                     # Responsive Web application hosted on GitHub Pages
│   ├── index.html
│   ├── style.css
│   └── script.js
├── app/                      # Native Android application (Kotlin + XML Views)
│   └── src/main/
│       ├── java/com/example/libraryassignmentkotlin/
│       │   ├── LibraryService.kt
│       │   └── MainActivity.kt
│       ├── res/layout/
│       │   └── activity_main.xml
│       └── AndroidManifest.xml
└── README.md