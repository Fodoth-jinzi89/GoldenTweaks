# Repository Guidelines

## Active Skills

This project enables the following user-level skills (installed at `~/.agents/skills/`):

- **caveman** — ultra-compressed communication mode. Drops filler/hedging, keeps all technical substance exact. Levels: lite / full (default) / ultra / wenyan-lite / wenyan-full / wenyan-ultra. Off via `stop caveman`.
- **memory** — persistent global memory across conversations (file: `C:\Users\peiranyu\.codex\MEMORY.md`). Read at conversation start; append preferences, project changes, and decisions during the session.
- **token-saver** — lean responses + prompt-cache-friendly output. One sentence per response unless detail requested, no preambles, no re-reading files, patch directly.
- **ponytail** (https://github.com/DietrichGebert/ponytail) — lazy senior dev mode: forces the laziest solution that actually works (YAGNI, stdlib/native first, shortest diff). Levels: lite / full (default) / ultra; off via `stop ponytail`. Companions: ponytail-review (diff over-engineering review), ponytail-audit (repo audit), ponytail-debt (deferred shortcuts ledger), ponytail-gain (impact scoreboard), ponytail-help. Applies to any coding task.
- **headroom** (https://github.com/headroomlabs-ai/headroom) — context compression proxy. Installed via pip (`headroom-ai[proxy]`), CLI at `C:\Users\peiranyu\AppData\Local\Programs\Python\Python314\Scripts\headroom`. DeepCode routes through it: `.deepcode/settings.json` sets `BASE_URL=http://127.0.0.1:8787`; proxy forwards to DeepSeek. Start with `script\headroom-proxy.bat`; health check `curl http://127.0.0.1:8787/health`; savings dashboard at `http://127.0.0.1:8787/dashboard`.

## Project Structure & Module Organization

```
src/main/java/net/fodoth/skina/goldentweaks/
├── compat/       # Third-party mod compatibility patches
├── config/       # Mod configuration classes
├── debug/        # Debug utilities & logger suppression
├── event/        # NeoForge event handlers
├── gpubooster/   # GPU-side rendering optimizations (DSA, SIMD, OpenGL)
├── mixin/        # Mixin injections into vanilla & modded code
├── network/      # Custom network packets (C2S / S2C)
└── util/         # Shared helpers & enums
```

- **Libs**: JAR dependencies live in `libs/compileOnly/`, `libs/runtimeOnly/`, and `libs/implementation/`.
- **Assets**: Resources and `mods.toml` template live in `src/main/resources/` and `src/main/templates/`.

## Build, Test, and Development Commands

| Command | Purpose |
|---|---|
| `./gradlew build` | Compile and package the mod JAR into `build/libs/` |
| `./gradlew genIntellijRuns` | Generate IDE run configurations for debugging |
| `./gradlew runClient` | Launch a test Minecraft client with the mod loaded |
| `./gradlew runServer` | Launch a test server (no GUI) |

- Requires **JDK 21** and **NeoForge 1.21.1**.
- CI builds are triggered via GitHub Actions (`.github/workflows/`).

## Coding Style & Naming Conventions

- **Java 21** with Kotlin support in `build.gradle`.
- Indentation: follow existing file style (Tabs/Spaces as-is); do not reformat unrelated code.
- Class names: `PascalCase`; methods/variables: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Mixins: Should have `@Mixin`. Place in `mixin/` sub-packages matching the target class path (e.g., `mixin/fix/bountiful/`). Don't put classes without `@Mixin` in `mixin/` sub-packages. Should also update `src\main\resources\goldentweaks.mixins.json` accordingly.
- Compat patches: one package per mod under `compat/<mod_name>/`.
- Use `@NotNull` / `@Nullable` from `org.jetbrains.annotations`.

## Testing Guidelines

- No formal test suite is currently configured.
- Manual testing: launch `runClient` and verify changes in-game.
- When fixing a mod compatibility issue, test with that mod present and absent.

## Commit & Pull Request Guidelines

- Commit messages are short and descriptive (e.g., `v 3.1`, `Thaumcraft compat`).
- Prepend version tags for releases (`v 3.1`).
- PRs should describe what was changed and why, with screenshots for visual changes.
- Link related issues when applicable.

## Agent-Specific Instructions

- **Only target NeoForge 1.21.1** — do not introduce Fabric or multi-loader abstractions.
- **Never modify `build.gradle` or `settings.gradle`** without explicit request.
- Edit files surgically: do not reformat, rearrange imports, or "fix" unrelated code.
- When writing mixins, prefer `@Inject` with `cancellable = true` over `@Overwrite` unless necessary.
- Configuration options go through `config/` package, not scattered constants.
- Choose the simplest implementation that fully meets the current requirements. Avoid speculative abstractions, configuration, and indirection.
- Grow the system in layers. Start from the smallest version that works end to end, and add each new capability on top of a product that already works. Never trade a working product for unfinished complexity.
- Keep components modular and concerns clearly separated.
- Prefer established, well-maintained libraries when they reduce overall complexity or improve reliability. Do not reimplement common functionality without a clear reason.
- Lean on the dependencies already in the project before writing your own implementation or adding packages. Do not assume a library lacks a capability without checking its documentation and types.
- Make architectural decisions for the long term. Do not accept a stopgap that only works for now and is meant to be replaced later.
- Bash corruption may be caused by the working directory no longer existing. You can create a new one.