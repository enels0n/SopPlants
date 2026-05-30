# SopPlants

`SopPlants` is a crop and pot management plugin focused on watering-based plant progression.

## What it does

- tracks watered crops and drying timers
- supports custom watering pots with durability and fullness
- handles crop rot, farmland drying, and growth timing
- saves active watered plant state to `watered.yml`

## Files

- `config.yml` - plant timings, pot settings, locale strings, and reward commands
- `watered.yml` - saved runtime state for watered crops

## Command

- `/sopplants`

## Notes

- depends on `SopLib` for shared text formatting via `TextUtils`
- custom item metadata is handled through `SopLib` item helpers instead of direct NMS
- built against `1.20.4`-style NMS imports
- produces a single final `SopPlants.jar` without `original-*` release clutter
