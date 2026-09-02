# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

**Current release: v0.1.3**

[Download MiraShop-0.1.3.jar](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.3/MiraShop-0.1.3.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation

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

Tools, Armor, Brewing and Misc are removed automatically from existing `shops.yml` files. The Spawners section is added automatically.

Armor, weapons and tools are not sellable through MiraShop. Existing sell prices on those materials are automatically disabled.

Section GUIs resize automatically based on how many items they contain while keeping the glowing glass border and dead-space filler.

## Sell GUI

`/sell`, `/sellhand` and `/sell hand` are intercepted by MiraShop and open a 9x4 protected sell inventory.

- The top three rows mirror the player's 27-slot main inventory.
- Sellable stacks show the real item and their value.
- Unsellable stacks are replaced with Barrier icons.
- Empty slots use glowing grey glass.
- Clicking a sellable stack sells that stack.
- Players cannot take, place, shift-click or drag items through the GUI.
- The bottom-centre **Sell Inventory** button sells every eligible item in the full player inventory.
- Named items are always blocked.
- Mira/PDC custom items, enchanted items, custom-model items and damaged equipment are also protected.

## Essentials worth synchronisation

MiraShop forces its configured sell prices into Essentials worth data when the plugin starts and whenever MiraShop is reloaded.

Hold a configured item and run:

```text
/mshop setprice <price>
```

MiraShop updates its configured price and dispatches this command from console:

```text
/setworth <item_name> <price>
```

GUI and granular price edits also refresh Essentials worth data so the two systems do not drift apart.

Legacy granular pricing remains available:

```text
/mshop setprice <section> <item> <buy|sell> <price|-1>
```

Other admin commands:

- `/mshop edit`
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

- `config.yml`
- `shops.yml`

## Safety

MiraShop validates prices before transactions, checks money before withdrawal, verifies inventory capacity before purchases, and protects named/custom items from material-only selling.

Build output: `build/libs/MiraShop-0.1.3.jar`
