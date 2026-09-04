# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Paper server suite. It provides configurable buy/sell sections, bulk transactions, typed-spawner support, inventory selling, temporary sales and economy analytics backed by Vault.

## Download

[**Download MiraShop v0.1.9**](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.9/MiraShop-0.1.9.jar)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation
- MiraSpawners recommended for typed-spawner support

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
