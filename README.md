# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

**Current release: v0.1.1**

[Download MiraShop-0.1.1.jar](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.1/MiraShop-0.1.1.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider

## Player commands

- `/shop` - open the main shop
- `/shop <section>` - open a section directly
- `/sellall hand` - sell all sellable plain copies of the held material
- `/sellall inventory` - sell all sellable plain shop materials in your inventory
- `/sellall <material>` - sell all plain copies of one material

## Shop GUI

MiraShop ships with useful prices and sections out of the box instead of an empty configuration.

Preset sections:

- Blocks
- Farming
- Food
- Ores & Minerals
- Mob Drops
- Redstone
- Tools
- Armor
- Brewing
- Misc

The item trade screen supports:

- Buy 1
- Buy 16
- Buy 64
- Sell 1
- Sell 16
- Sell All
- Live sellable-item count

Items may be buy-only, sell-only, or both. A price of `-1` disables that side of the transaction.

## In-game editor

Permission: `mirashop.admin`

Run `/mshop` or `/mshop edit` to open the editor GUI.

The editor lets admins:

- Browse every shop section
- Browse and edit section items
- Change buy price through chat input
- Change sell price through chat input
- Disable buying/selling with `-1`
- Add the held material directly to a section
- Remove shop entries

Admin command fallbacks are also available:

- `/mshop reload`
- `/mshop setprice <section> <item> <buy|sell> <price|-1>`
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

MiraShop validates prices before transactions, rejects invalid buy values, checks money before withdrawal, verifies inventory capacity before a purchase, and restores money/items if an economy or delivery operation fails.

Material-only selling deliberately excludes items containing item metadata. That prevents enchanted, named, damaged, custom-model and Mira PDC-tagged items from being accidentally sold as ordinary vanilla shop stock.

Build output: `build/libs/MiraShop-0.1.1.jar`
