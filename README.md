# AE2 BatchCraft

**Use one set of processing patterns to drive a group of machines in parallel.**

Minecraft 1.21.1 | NeoForge | Applied Energistics 2

**English** | [简体中文](README_zh-CN.md)

## What Does It Do?

AE2 BatchCraft adds a **one-to-many processing-pattern P2P network** to Applied Energistics 2.

A Pattern P2P input receives complete crafting jobs from a standard AE2 Pattern Provider and distributes them in round-robin order among multiple outputs or Pattern P2P Units. To expand a production line, add more endpoints and machines without copying patterns or consuming an AE channel for every machine.

## Included Content

| Item / Feature | Purpose |
| --- | --- |
| **Pattern P2P Tunnel (Input)** | Connects to a Pattern Provider, receives and distributes processing jobs, and collects returned products. Uses `1` AE channel. |
| **Pattern P2P Tunnel (Output)** | Sends ingredients into one adjacent machine and returns its products. Uses no AE channel and can queue up to `64` jobs. |
| **Pattern P2P Tunnel (Energy)** | Powers the AE subnet first, then distributes remaining FE among output machines and active Unit Energy Ports. Uses no channel or P2P frequency. |
| **Pattern P2P Unit (Manager)** | Receives one complete job and coordinates a group of bound functional ports. Available in fluix and all `16` AE2 cable colors. |
| **Pattern P2P Unit Ports** | Eight port types provide Transfer, Drop, Place, Return, Pickup, Break, Redstone, and Energy functions. Managers and ports use no additional channels. |
| **Product Extraction Card** | Lets a Pattern Provider and its linked outputs actively extract filtered products from adjacent machines. Supports items, fluids, and compatible integration resource types. |
| **AE Component Placer** | Batch-places AE cables and cable-attached parts over a point, line, or plane up to `16 x 16`, with optional P2P frequency assignment. |
| **Per-material Configuration** | Assigns an ingredient input side for standard outputs or selects Normal, Drop, and Place forms for Pattern P2P Unit ports. |

## Why Use It?

- **Parallel processing:** complete jobs are distributed among available machines in round-robin order.
- **Fewer channels:** only the Pattern P2P input uses `1` channel; outputs, Unit Managers, and Unit ports use none.
- **Centralized patterns:** processing patterns remain in one standard Pattern Provider instead of being copied to every machine.
- **Flexible automation:** Pattern P2P Units can transfer materials, place or break blocks, collect drops, return products, emit redstone, and supply FE as one task endpoint.
- **Automatic product extraction:** a Product Extraction Card can pull results from machines without a separate output pipe.
- **Centralized power:** an Energy Tunnel can use even or round-robin distribution across eligible machines.
- **Fast expansion:** the AE Component Placer can build a complete row or plane of cable-and-part endpoints.
- **Resource compatibility:** generic AE resource handling supports items, fluids, and compatible types such as Applied Mekanistics chemicals when the integration is installed.

## Quick Start

### Build a Parallel Machine Group

1. Put processing patterns in a standard AE2 **Pattern Provider** on the main network.
2. Install a **Pattern P2P Tunnel (Input)** on an AE subnet cable with its front face against the Pattern Provider's output face.
3. Install one **Pattern P2P Tunnel (Output)** in front of each processing machine on the same subnet.
4. `Shift + Right-click` the input with an AE2 Memory Card to generate and save a frequency.
5. Right-click every output with the same Memory Card to assign that frequency.
6. Request a crafting job. The input sends each complete job to the next available output.

The input and all endpoints must be on the same AE subnet, and their chunks must be loaded. Frequency `0000` means unconfigured. Offline, unloaded, busy, or blocked endpoints are skipped.

> A job is never split between machines. For example, a complete `10000 A -> 10000 B` job is sent to one endpoint.

### Build a Pattern P2P Unit

Use a Unit when a process needs several interfaces or world interaction instead of one machine face.

1. Install a **Pattern P2P Unit (Manager)** as a cable center on the subnet.
2. Load the Pattern P2P input frequency from a Memory Card onto the Manager.
3. `Shift + Right-click` the Manager with a Memory Card to save its identity.
4. Right-click each functional port with that card to bind it to the Manager.
5. In processing-pattern mode, hover over an input ingredient and press `Ctrl + Middle Mouse Button` to select Normal, Drop, or Place output.

The Manager joins the same round-robin group as standard outputs but processes only one job at a time. Its ports must be on the same AE subnet and remain bound to that Manager's identity.

### Optional Automation

- Install a **Product Extraction Card** in the Pattern Provider, then open its extraction settings from the left toolbar to configure interval, amount, and whitelist or blacklist filters. Linked outputs inherit these settings.
- Place a **Pattern P2P Tunnel (Energy)** toward an FE source to power the subnet and eligible machines. Right-click it to choose passive/active input and even/round-robin distribution.
- Use the **AE Component Placer** to select a point, line, or plane, choose a cable and part, optionally load a Memory Card frequency, and place the configured endpoints in one action.

## In-game Guide

With [GuideME](https://modrinth.com/mod/guideme) installed, hover over any item from this mod and press AE2's guide key, `G`. The English and Chinese guide contains recipes, complete setup instructions, Unit port behavior, product return modes, energy settings, and important task-reset warnings.

## Screenshot

<img width="2227" height="1199" alt="AE2 BatchCraft in-game setup" src="https://github.com/user-attachments/assets/c5ad93a2-4e99-4b5d-aae8-54771553ebdb" />

## Requirements

| Component | Version | Required |
| --- | --- | --- |
| Minecraft | `1.21.1` | Yes |
| NeoForge | `21.1+` | Yes |
| Applied Energistics 2 | `19.x` | Yes |
| GuideME | `21.1.16+` | No; enables the in-game guide |
| Applied Mekanistics | `1.6.0+` | No; enables compatible chemical handling |

Install AE2 BatchCraft and its required dependencies in the `mods` directory on both the client and server.

## License

AE2 BatchCraft is licensed under the [GNU General Public License v3.0](LICENSE) (`GPL-3.0-only`).
