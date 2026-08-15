# Repository Guidelines

## Project

NeoForge 1.21.1 Minecraft mod.

```text
src/main/java/net/fodoth/skina/goldentweaks/
├── compat/       # Mod compatibility
├── config/       # Configuration
├── debug/        # Debug utilities
├── event/        # NeoForge events
├── gpubooster/   # GPU/rendering optimizations
├── mixin/        # Mixin injections
├── network/      # Network packets
└── util/         # Shared utilities
```

Resources: `src/main/resources/`
Dependencies: `libs/compileOnly/`, `libs/runtimeOnly/`, `libs/implementation/`
References: `libs/reference/`

## Build

Requires JDK 21.

    ./gradlew build
    ./gradlew genIntellijRuns
    ./gradlew runClient
    ./gradlew runServer

CI: `.github/workflows/`

## Code Style

- Java 21.
- Preserve existing formatting; do not reformat unrelated code.
- `PascalCase` classes, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants.
- Use `@NotNull` / `@Nullable` where appropriate.
- Prefer existing project dependencies over new implementations.

### Mixins

- All mixins belong under `mixin/` and must use `@Mixin`.
- Match package layout to the target class.
- Update `goldentweaks.mixins.json` when adding/removing mixins.
- Use `@Unique` members with `gt$` prefix.
- Prefer `@Inject(cancellable = true)` over `@Overwrite`.
- `@Overwrite` requires `@author` and `@reason`.
- Non-mixin helper classes do not belong in `mixin/`.
- Prefer Mixin for code modification. If Mixin is insufficient, use reflection or `VarHandle`; use ASM only as a last resort.
- Mixins may target any class from dependency libraries, including Minecraft, NeoForge, other mods, and their dependencies, when necessary to implement new features.

### Compatibility

Use one package per supported mod:

    compat/<mod_name>/

## Agent Rules

- Target NeoForge 1.21.1 only. No Fabric or multi-loader abstractions.
- Do not modify `build.gradle` or `settings.gradle` unless explicitly requested.
- Make surgical edits. Do not reorder imports, reformat files, or fix unrelated code.
- Put configuration in `config/`, not scattered constants.
- Prefer the smallest working implementation. Avoid speculative abstractions and unnecessary indirection.
- Build incrementally; do not replace working code with unfinished architecture.
- Keep components modular and concerns separated.
- Check existing APIs/dependencies before adding new code or libraries.
- Prefer maintainable solutions over temporary hacks.
- Bash corruption may be caused by the working directory no longer existing. You can create a new one.

## Testing

No formal test suite.

For changes:

- Run `./gradlew build` when practical.
- Use `./gradlew runClient` for in-game verification.
- See `run/logs/latest.log` for log, `run/logs/debug.log` for debug log, and `run/crash-reports` for crash reports.
- Compatibility fixes should be tested with the target mod both present and absent when practical.

## Git

Commit messages should be short and descriptive.

Release commits use version prefixes, e.g. `v 3.1`.

PRs should explain what changed and why; include screenshots for visual changes.

Before a major version update, update the root-level `update_log.md`. Only do this when the user explicitly triggers a major version update.

## Installed Agent Skills

Optional user-level skills may exist under `~/.agents/skills/`:

- `caveman` — compressed communication.
- `memory` — persistent project memory.
- `token-saver` — minimal responses and direct patches.
- `ponytail` — YAGNI / shortest-working-solution development.
- `headroom` — context compression proxy.
