# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

**Current release: v0.1.5**

[Download MiraShop-0.1.5.jar](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.5/MiraShop-0.1.5.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation
- MiraSpawners recommended for typed spawner support

## v0.1.5 economy rebalance

MiraShop v0.1.5 applies the new Factions economy baseline across every active preset category while keeping the intentionally blocked equipment categories excluded.

Updated categories:

- Blocks
- Farming
- Food
- Ores & Minerals
- Mob Drops
- Redstone
- Spawners

A one-time v0.1.5 migration updates the known Mira preset entries inside an existing `shops.yml`. Custom/admin-added shop entries are left untouched. Once applied, the migration is marked complete and does not overwrite later manual edits.

### Spawner progression

Typed spawners remain buy-only.

| Spawner | Buy price |
| --- | ---: |
| Chicken | $50,000 |
| Pig | $75,000 |
| Cow | $100,000 |
| Zombie | $175,000 |
| Skeleton | $225,000 |
| Polar Bear | $350,000 |
| Blaze | $650,000 |
| Evoker | $1,250,000 |
| Iron Golem | $3,000,000 |

### Key farm values

| Item | Sell value each |
| --- | ---: |
| Rotten Flesh | $5.00 |
| Bone | $8.00 |
| Arrow | $6.00 |
| Leather | $4.00 |
| Feather | $3.00 |
| Gunpowder | $40.00 |
| Blaze Rod | $50.00 |
| Iron Ingot | $40.00 |
| Emerald | $85.00 |

The full preset economy is stored in `src/main/resources/shops.yml` and migrated into existing installations automatically.

## Custom-item support

MiraShop shop entries preserve exact `ItemStack` templates instead of collapsing everything to a Bukkit `Material`.

This preserves configured:

- custom names
- lore
- PersistentDataContainer data
- custom model data
- enchantments
- MiraSpawners mob-type metadata

The admin GUI's **Add Held Item** button saves the exact item in the player's hand. Buying that entry recreates the configured template. Custom items are sellable only when the inventory item exactly matches a configured shop template, so arbitrary named/PDC items remain protected.

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
build/libs/MiraShop-0.1.5.jar
```
