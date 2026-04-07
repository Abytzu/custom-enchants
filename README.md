# Custom Enchants

A Fabric mod for Minecraft 1.21.11 that adds 9 custom enchantments, anvil quality-of-life improvements, and an enchantment table reroll keybind.

**Author:** Abytzu &nbsp;|&nbsp; **Loader:** Fabric &nbsp;|&nbsp; **MC Version:** 1.21.11 &nbsp;|&nbsp; **License:** MIT

---

## ✨ Enchantments

| Enchantment | Levels | Applies To | Effect |
|---|---|---|---|
| **Telekinesis** | I | Mining tools | Auto-collects drops directly into your inventory |
| **Soulbound** | I | Any item | Keeps the item in your inventory on death |
| **Harvesting** | I–V | Hoe | Bonus crop drops; higher levels increase yield |
| **Replenish** | I | Hoe | Automatically replants crops after harvesting |
| **Smelting Touch** | I | Mining tools | Auto-smelts block drops. Exclusive with Silk Touch |
| **Fortune** | I–IV | Mining tools | Increases block drop quantities. Obtainable at level 30. Replaces vanilla Fortune |
| **Bedrock Breaker** | I | Mining tools | Allows breaking bedrock |
| **Efficiency VI–VII** | 6–7 | Mining tools | Enhanced mining speed. Combine two Efficiency V items in an anvil to reach VI, two VI items for VII |
| **Unbreakable** | I | Any item | Makes the item unbreakable. Exclusive with Unbreaking |

---

## 🔧 Anvil Improvements

- **Zero XP rename** — Renaming an item with no other changes costs 0 XP
- **Item return on close** — Items left in anvil input slots are returned to your inventory when you close the screen
- **Color codes** — Use `&` or `§` color codes in the rename field

---

## 🎲 Enchantment Table Reroll

Press **R** (rebindable in Controls → Gameplay) while the enchantment table is open to reroll the offered enchantments. The enchantment table sound plays on each reroll.

---

## ⚙️ Commands

| Command | Alias | Effect |
|---|---|---|
| `/customenchants add <enchant> [level]` | `/et add` | Add an enchantment to your held item |
| `/customenchants remove <enchant>` | `/et remove` | Remove an enchantment from your held item |
| `/customenchants list` | `/et list` | List enchantments on your held item |
| `/customenchants repair` | `/et repair` | Fully repair your held item |
| `/customenchants reload` | `/et reload` | Reload the config |
| `/av` | — | Open a vanilla anvil GUI anywhere |

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest `custom-enchants-x.x.x.jar` from [Releases](../../releases)
4. Drop all jars into your `.minecraft/mods` folder
5. Launch the game

### Optional dependencies
- [Mod Menu](https://modrinth.com/mod/modmenu) + [Cloth Config](https://modrinth.com/mod/cloth-config) — enables in-game config screen to toggle individual enchantments
- [Easy Magic](https://modrinth.com/mod/easy-magic) — pairs well with the reroll keybind

---

## 🛠️ Configuration

A config file is generated at `.minecraft/config/customenchants.json` on first launch. Each enchantment can be individually toggled on or off. If Mod Menu and Cloth Config are installed, the config is accessible in-game via the Mods screen.

---

## 🏗️ Building from Source

```bash
git clone https://github.com/Abytzu/custom-enchants.git
cd custom-enchants
gradlew build
```

The compiled jar will be in `build/libs/`.

Requires Java 21 and an internet connection for the first build (Gradle downloads Minecraft and Fabric dependencies automatically).

---

## 📄 License

MIT — see [LICENSE](LICENSE) for details.
