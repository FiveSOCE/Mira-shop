# MiraShop

First-party GUI economy shop for the Mira Minecraft plugin suite.

## Download

Current release: **v0.1.8**

[**Download MiraShop v0.1.8**](https://github.com/FiveSOCE/Mira-shop/releases/download/v0.1.8/MiraShop-0.1.8.jar)

[View all releases](https://github.com/FiveSOCE/Mira-shop/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault
- Vault-compatible economy provider
- EssentialsX recommended for worth synchronisation
- MiraSpawners recommended for typed-spawner support

## v0.1.8 temporary sale events

Temporary sale events modify effective prices at runtime without changing permanent values in `shops.yml`.

Scopes:

```text
all
section:<section>
item:<item>
```

Examples:

```text
/mshop sale start weekend 20 0 120 all
/mshop sale start grinder 15 25 60 section:spawners
/mshop sale start igsale 25 0 30 item:iron_golem_spawner
/mshop sale list
/mshop sale stop <id>
```

Sale-aware pricing is shown in the GUI and used by purchase confirmation. Permanent base prices remain untouched.

## Economy tools

```text
/shop search <item>
/mshop stats <24h|7d|all>
/mshop eco
/mshop export
```

MiraShop provides Buy Only / Sell Only / Buy & Sell modes, bulk buying, expensive-purchase confirmation, economy transaction analytics, spawner ROI estimates and CSV economy exports.

Typed spawners preserve exact MiraSpawners item identity and remain configurable independently from vanilla materials.

## Building

```bash
gradle clean build
```

Output:

```text
build/libs/MiraShop-0.1.8.jar
```
