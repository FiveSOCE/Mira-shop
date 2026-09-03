# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

**Current release: v0.1.4**

[Download MiraShop-0.1.4.jar](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.4/MiraShop-0.1.4.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation
- MiraSpawners recommended for typed spawner support

## v0.1.4 custom-item support

MiraShop shop entries can now preserve an exact `ItemStack` template rather than collapsing everything to a Bukkit `Material`.

This preserves configured:

- custom names
- lore
- PersistentDataContainer data
- custom model data
- enchantments
- MiraSpawners mob-type metadata

The admin GUI's **Add Held Item** button saves the exact item in the player's hand. Buying that entry recreates the exact configured template. Custom items are sellable only when the inventory item exactly matches a configured shop template, so arbitrary named/PDC items remain protected.

### Typed spawner presets

The Spawners section now ships with buy-only MiraSpawners-compatible presets:

| Spawner | Buy price |
| --- | ---: |
| Chicken | $5,000 |
| Pig | $20,000 |
| Cow | $20,000 |
| Zombie | $150,000 |
| Skeleton | $200,000 |
| Polar Bear | $400,000 |
| Blaze | $550,000 |
| Evoker | $750,000 |
| Iron Golem | $1,250,000 |

A one-time v0.1.4 migration removes the old generic empty `Spawner` entry and injects these typed entries into an existing `shops.yml`. The migration is then marked complete and will not overwrite later manual edits.

### Mob/farm sell values

The v0.1.4 migration applies:

| Item | Sell value each |
| --- | ---: |
| Blaze Rod | $65.00 |
| Rotten Flesh | $10.00 |
| Arrow | $15.00 |
| Gunpowder | $80.00 |
| Bone | $12.00 |
| Leather | $4.50 |
| Feather | $3.50 |
| Iron Ingot | $85.00 |
| Emerald | $100.00 |

If an older configured buy price is lower than the new sell price, buying that entry is automatically disabled to prevent infinite buy/sell arbitrage.

## Player commands

- `/shop` - open the main shop
- `/shop <section>` - open a section directly
- `/sell`, `/sellhand`, `/sell hand` - open the protected sell GUI
- `/sellall hand`
- `/sellall inventory`
- `/sellall <material>`

## Shop GUI

The main shop is a compact 9x3 menu using the Mira visual language:

- glowing grey stained-glass border/filler
- non-italic item names and lore
- seven shop sections across the middle row
- Gold Ingot balance display in the bottom centre showing the player's live Vault balance
- no Close button

Preset sections:

- Blocks
- Farming
- Food
- Ores & Minerals
- Mob Drops
- Redstone
- Spawners

Tools, Armor, Brewing and Misc are removed automatically from existing `shops.yml` files.

Armor, weapons and tools are not sellable through MiraShop. Existing sell prices on those materials are automatically disabled.

## Sell GUI

`/sell`, `/sellhand` and `/sell hand` open a protected 9x4 sell inventory.

- Sellable stacks show the real item and value.
- Unsellable or unconfigured custom stacks show as barriers.
- Exact configured custom-item templates can be sold.
- Arbitrary named/PDC/custom-model/enchanted items remain protected.
- The bottom-centre **Sell Inventory** button sells every eligible configured item in the full player inventory.

## Essentials worth synchronisation

Normal material sell prices are synchronized into Essentials worth data on startup/reload. Typed/custom shop entries retain their MiraShop-specific identity and are not collapsed into a generic spawner identity.

Admin commands:

```text
/mshop
/mshop edit
/mshop reload
/mshop setprice <price>                 # hold the exact item
/mshop setprice <section> <item> <buy|sell> <price|-1>
/mshop addhand <section> <id> <buy> <sell>
/mshop remove <section> <item>
```

## Permissions

- `mirashop.use`
- `mirashop.buy`
- `mirashop.sell`
- `mirashop.sellall`
- `mirashop.section.*`
- `mirashop.section.<section>`
- `mirashop.admin`

## Files

- `config.yml`
- `shops.yml`

## Building

```bash
gradle clean build
```

Build output:

```text
build/libs/MiraShop-0.1.4.jar
```
