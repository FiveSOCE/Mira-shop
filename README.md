# MiraShop

MiraShop is the first-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

**Current release: v0.1.6**

[Download MiraShop-0.1.6.jar](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.6/MiraShop-0.1.6.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- A Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation
- MiraSpawners recommended for typed spawner support

## v0.1.6 economy intelligence and trading QoL

### Shop search

```text
/shop search <item>
```

Searches all sections the player can access and shows matching item names, section, trading mode, buy price and sell price.

### Trading modes

Every shop item now clearly displays one of:

- Buy Only
- Sell Only
- Buy & Sell
- Disabled

The existing `buy: -1` and `sell: -1` controls remain the source of truth.

### Bulk buying

Transaction menus provide:

```text
Buy 1
Buy 16
Buy 32
Buy 64
```

Large purchases use a configurable safety confirmation. By default, any purchase worth **$500,000 or more** requires the player to click the same Buy button again within 10 seconds.

Config:

```yaml
shop:
  buy-confirmation: true
  buy-confirmation-threshold: 500000.0
  buy-confirmation-seconds: 10
```

### Economy analytics

MiraShop records successful purchases and sales into `economy-stats.yml` using hourly aggregates plus all-time totals.

```text
/mshop stats 24h
/mshop stats 7d
/mshop stats all
```

Reports include:

- money created by player sales
- money removed by shop purchases
- net shop economy injection
- top money-generating items
- units sold
- per-item net injection

Hourly history is retained for 15 days while all-time totals remain persistent.

### Spawner ROI estimator

```text
/mshop eco
```

Shows each configured spawner's:

- purchase price
- estimated income per hour
- estimated break-even time

The estimator uses current MiraShop sell values for each mob's primary sellable drops. It intentionally excludes Looting and secondary/unconfigured loot.

The assumed per-spawner throughput is configurable:

```yaml
eco:
  estimated-kills-per-hour-per-spawner: 144.0
```

This lets the estimate be tuned later using actual server observations.

## v0.1.5 economy baseline

The active preset categories are:

- Blocks
- Farming
- Food
- Ores & Minerals
- Mob Drops
- Redstone
- Spawners

A one-time v0.1.5 migration updates known Mira preset entries inside an existing `shops.yml`. Custom/admin-added entries are left untouched.

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

## Custom-item support

MiraShop entries preserve exact `ItemStack` templates, including:

- custom names
- lore
- PersistentDataContainer data
- custom model data
- enchantments
- MiraSpawners mob-type metadata

The admin GUI's **Add Held Item** button saves the exact held item. Arbitrary unconfigured named/PDC/custom items remain protected from selling.

## Player commands

```text
/shop
/shop <section>
/shop search <item>
/sell
/sellhand
/sell hand
/sellall hand
/sellall inventory
/sellall <material>
```

## Admin commands

```text
/mshop
/mshop edit
/mshop reload
/mshop stats <24h|7d|all>
/mshop eco
/mshop setprice <price>
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
- `economy-stats.yml`

## Building

```bash
gradle clean build
```

Build output:

```text
build/libs/MiraShop-0.1.6.jar
```
