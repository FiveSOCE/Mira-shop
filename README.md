# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

**Current release: v0.1.2**

[Download MiraShop-0.1.2.jar](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.2/MiraShop-0.1.2.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for `/mshop setprice` worth synchronisation

## Player commands

- `/shop` - open the main shop
- `/shop <section>` - open a section directly
- `/sellall hand` - sell all sellable plain copies of the held material
- `/sellall inventory` - sell all sellable plain shop materials in your inventory
- `/sellall <material>` - sell all plain copies of one material

## Shop GUI

The main shop is a compact 9x3 menu using the same Mira visual language as Kits, Warps and Enchantments:

- glowing grey stained-glass border/filler
- non-italic item names and lore
- seven shop sections across the middle row
- Close in the bottom centre
- Gold Ingot balance display in the bottom-right showing the player's live Vault balance

Preset sections:

- Blocks
- Farming
- Food
- Ores & Minerals
- Mob Drops
- Redstone
- Spawners

Tools, Armor, Brewing and Misc are removed automatically from existing `shops.yml` files when v0.1.2 loads. The Spawners section is added automatically.

Section GUIs resize automatically based on how many items they contain while keeping the glowing glass border and dead-space filler.

The item trade screen supports Buy 1/16/64, Sell 1/16/All and live sellable-item counts.

## In-game editor

Permission: `mirashop.admin`

Run `/mshop` or `/mshop edit` to open the editor GUI.

### Quick held-item pricing

Hold an item and run:

```text
/mshop setprice <price>
```

MiraShop updates every configured entry using that material so both its buy and sell price become the supplied value. It then dispatches EssentialsX's `setworth` command for the same material and price so Essentials worth data stays aligned.

Legacy granular pricing remains available:

```text
/mshop setprice <section> <item> <buy|sell> <price|-1>
```

Other admin commands:

- `/mshop reload`
- `/mshop addhand <section> <id> <buy> <sell>`
- `/mshop remove <section> <item>`

## Permissions

- `mirashop.use`
- `mirashop.buy`
- `mirashop.sell`
- `mirashop.sellall`
- `mirashop.section.*`
- `mirashop.section.<section>`
- `mirashop.admin`

## Files

- `config.yml` - GUI/currency/messages
- `shops.yml` - sections, materials, buy prices and sell prices

## Safety

MiraShop validates prices before transactions, checks money before withdrawal, verifies inventory capacity before a purchase, and restores money/items if an economy or delivery operation fails.

Material-only selling excludes items containing item metadata, preventing enchanted, named, damaged, custom-model and Mira PDC-tagged items from being accidentally sold as ordinary vanilla stock.

Build output: `build/libs/MiraShop-0.1.2.jar`
