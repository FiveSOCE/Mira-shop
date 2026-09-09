# MiraShop

## Download

**Latest compatibility release: v0.1.19**

[**Download MiraShop-0.1.19.jar**](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.19/MiraShop-0.1.19.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

MiraShop v0.1.16 is the first-party GUI economy shop for the Mira Paper server suite. It provides configurable buy/sell sections, bulk transactions, typed-spawner support, inventory selling, temporary sales and economy analytics backed by Vault, now with optional centralized MiraCosmetics transaction audio.

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation
- MiraSpawners recommended for typed-spawner support
- MiraCosmetics optional for centralized audio effects

## How MiraShop Works

MiraShop stores permanent shop sections/items and their base buy/sell prices in its shop data. Players browse the GUI by section, search for items, buy items when `mirashop.buy` is allowed and sell items when `mirashop.sell` is allowed. Items can be configured as Buy Only, Sell Only or Buy & Sell. The transaction GUI uses a compact 3-row layout with the selected item centered at the top. Buy controls sit on the left as `Buy 64`, `Buy 10`, `Buy 1`, while sell controls sit on the right as `Sell 1`, `Sell 10`, `Sell 64`, keeping the 1-item options closest to the selected item. Expensive-purchase confirmation is still enforced where configured.

`/sellall` sells eligible items from the player's hand or inventory using MiraShop's current matching/pricing rules. Typed MiraSpawners items retain their exact spawner identity instead of collapsing to generic vanilla spawner material pricing.

Temporary sale events apply runtime price modifiers without rewriting permanent values. A sale can target `all`, `section:<section>` or `item:<item>`, and can independently affect buy/sell pricing according to the configured sale parameters. MiraShop also records transaction analytics, supports economy overview/statistics, spawner ROI information and CSV economy exports. Successful transactions can be consumed by other Mira economy modules.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/shop` | `mirashop.use` | Opens the main MiraShop GUI. |
| `/shop <section>` | `mirashop.use` + applicable `mirashop.section.*` access | Opens a specific shop section. |
| `/shop search <item>` | `mirashop.use` | Searches configured shop entries for an item. |
| `/sellall hand` | `mirashop.sellall` | Sells eligible items matching the item held in hand. |
| `/sellall inventory` | `mirashop.sellall` | Sells eligible sellable items from the player's inventory. |
| `/sellall <material>` | `mirashop.sellall` | Sells matching eligible material/items from inventory. |
| `/mshop edit` | `mirashop.admin` | Opens/starts administrative shop editing. |
| `/mshop reload` | `mirashop.admin` | Reloads MiraShop configuration/shop data. |
| `/mshop stats <24h|7d|all>` | `mirashop.admin` | Shows transaction/economy statistics for the selected period. |
| `/mshop eco` | `mirashop.admin` | Shows the economy overview/analytics view. |
| `/mshop export` | `mirashop.admin` | Exports economy/shop analytics to CSV. |
| `/mshop setprice ...` | `mirashop.admin` | Administratively changes configured item pricing. |
| `/mshop addhand ...` | `mirashop.admin` | Adds the item held by the administrator to shop configuration. |
| `/mshop remove ...` | `mirashop.admin` | Removes a configured shop entry. |
| `/mshop sale start <id> <buy%> <sell%> <minutes> <scope>` | `mirashop.admin` | Starts a temporary runtime sale event without changing permanent prices. |
| `/mshop sale list` | `mirashop.admin` | Lists active sale events. |
| `/mshop sale stop <id>` | `mirashop.admin` | Stops an active sale event. |

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `mirashop.use` | Everyone | Allows opening/searching MiraShop. |
| `mirashop.buy` | Everyone | Allows purchasing shop items. |
| `mirashop.sell` | Everyone | Allows selling items through shop interfaces. |
| `mirashop.sellall` | Everyone | Allows `/sellall`. |
| `mirashop.admin` | OP | Allows shop editing, reloads, pricing, sales and analytics administration. |
| `mirashop.section.*` | Everyone | Wildcard/default access to shop sections; section-specific nodes can be used to restrict individual sections. |

## MiraCosmetics Audio Integration (0.1.11)

MiraCosmetics audio hooks cover successful purchases, sales and bulk inventory sales. One completed transaction emits one audio event.


## Factions Economy Overhaul (0.1.14)

The default catalog is now tuned around a competitive Factions economy rather than generic survival pricing.

Main storefront sections:
- Blocks
- Farming
- Food
- Ores & Wealth
- Mob Drops
- Redstone
- Raiding
- Utility
- Decoration
- Spawners

The main GUI was expanded to a 5-row storefront with category item counts, a cleaner dark presentation, a balance panel, and sale status.

Staple Factions farms are intentionally viable while still staying below high-end grinder/mining income. High-value mining assets such as Diamond and Netherite retain stronger sell ratios. PvP, raiding, crafted utility and convenience items are predominantly buy-only to prevent crafting arbitrage.

Default spawners now range from Chicken at $25,000 through Iron Golem at $2,500,000, with Creeper, Slime, Blaze, Enderman, Witch and Guardian tiers included.


### Existing installs

v0.1.16 includes a one-time catalog migration. Existing `plugins/MiraShop/shops.yml` files receive the new built-in Factions sections, prices, and spawner tiers automatically on first startup. Unrelated custom sections are preserved.


## Factions Fine Tune (0.1.16)

The storefront was simplified back toward the original MiraShop layout. The active built-in sections are Blocks, Farming, Food, Ores & Wealth, Mob Drops, Redstone, Raiding, Utility, Decoration and Spawners.

Staple automated farms now have meaningful sell values. A full 36-slot inventory of Cactus is worth about $16,128 and Sugar Cane about $18,432 at default pricing.

MiraShop also refuses to load materials disabled by MiraCombat, including Mace, Shield, Wind Charge, Respawn Anchor, End Crystal, Totem of Undying, Heavy Core, Breeze Rod, Trial Keys, Bundle, Recovery Compass and related restricted combat items. Shulker shells and all Shulker Box variants are also excluded from the shop.

Useful Nether and End materials were folded into the existing logical sections instead of getting their own category.
