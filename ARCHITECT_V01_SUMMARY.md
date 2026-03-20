# Speccy OS E5 Ultra - Project Blueprint (v0.1 Legacy)

This document summarizes the core architecture and features of the initial "Architect Booster" version to be used as a foundation for **Speccy OS E5 Ultra V 0.2.1b**.

## 1. Core Concept
An AI-powered optimization suite and launcher custom-built for the **GameMT E5 Ultra** (Unisoc T620). It bridges high-level UI with low-level hardware control.

## 2. Technical Stack
- **Frontend:** React + TypeScript + Tailwind CSS + Lucide Icons + Recharts.
- **Backend:** Node.js Express server acting as a Hardware Abstraction Layer (HAL).
- **AI Integration:** Google Gemini (Architect Assistant) for system optimization advice and interaction.
- **Hardware Target:** Unisoc T620 (ARMv8), Android-based.

## 3. Key Features
- **Architect AI:** Neural bridge for system queries, performance tuning, and user assistance.
- **Performance Profiles:** Dynamic governor control (ECO, BALANCED, PERFORMANCE, EXTREME).
- **Universal Launcher:** Intent-based ROM launching for RetroArch and other emulators.
- **Hardware Sync:** Direct control over WiFi, Bluetooth, Brightness, and Fan Speed via `su` commands.
- **System Dashboard:** Real-time visualization of SoC status and library analytics.
- **Sync Center:** Automated ROM scanning and directory management.

## 4. Hardware Optimizations (init.speccy.rc)
- CPU Policy: `schedutil` with low rate limits.
- Input Latency: USB HID polling rate adjustments.
- Thermal Management: Manual fan level control.
- Task Scheduling: Foreground boost for the UI and Core engine.

## 5. Transition Notes for v0.2.1b
- Maintain the "Architect" identity but improve the integration.
- Refine the translation engine (already fully English-ready).
- Improve the scraper and metadata handling.
- Extend the HAL (server.js) for more granular control over the T620 SoC.
