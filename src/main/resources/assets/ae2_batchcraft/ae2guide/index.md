---
navigation:
  title: AE2 BatchCraft
  icon: pattern_p2p_tunnel_input
  position: 1000
item_ids:
- ae2_batchcraft:pattern_p2p_tunnel_input
- ae2_batchcraft:pattern_p2p_tunnel_output
- ae2_batchcraft:pattern_p2p_tunnel_energy
- ae2_batchcraft:component_placer
- ae2_batchcraft:product_extraction_card
- ae2_batchcraft:pp2p_unit_manager
- ae2_batchcraft:white_pp2p_unit_manager
- ae2_batchcraft:light_gray_pp2p_unit_manager
- ae2_batchcraft:gray_pp2p_unit_manager
- ae2_batchcraft:black_pp2p_unit_manager
- ae2_batchcraft:lime_pp2p_unit_manager
- ae2_batchcraft:yellow_pp2p_unit_manager
- ae2_batchcraft:orange_pp2p_unit_manager
- ae2_batchcraft:brown_pp2p_unit_manager
- ae2_batchcraft:red_pp2p_unit_manager
- ae2_batchcraft:pink_pp2p_unit_manager
- ae2_batchcraft:magenta_pp2p_unit_manager
- ae2_batchcraft:purple_pp2p_unit_manager
- ae2_batchcraft:blue_pp2p_unit_manager
- ae2_batchcraft:light_blue_pp2p_unit_manager
- ae2_batchcraft:cyan_pp2p_unit_manager
- ae2_batchcraft:green_pp2p_unit_manager
- ae2_batchcraft:pp2p_unit_port_drop
- ae2_batchcraft:pp2p_unit_port_pickup
- ae2_batchcraft:pp2p_unit_port_place
- ae2_batchcraft:pp2p_unit_port_break
- ae2_batchcraft:pp2p_unit_port_transfer
- ae2_batchcraft:pp2p_unit_port_return
- ae2_batchcraft:pp2p_unit_port_redstone
- ae2_batchcraft:pp2p_unit_port_energy
---

<Column gap="24">
  <Column gap="0">

# AE2 BatchCraft

<Row gap="16">
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_input" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_output" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_energy" scale="3" />
  <ItemImage id="ae2_batchcraft:component_placer" scale="3" />
  <ItemImage id="ae2_batchcraft:product_extraction_card" scale="3" />
</Row>

Distribute the processing jobs from one Pattern Provider among several machines. The input connects to the Pattern Provider and the outputs connect to machines. Products return along the same route when each job finishes.

  </Column>

  <Column gap="0">

## Build This First

Follow these steps to run several identical machines in parallel.

<Column gap="0">

### 1. Install the Input

Install a **Pattern P2P Tunnel (Input)** on an AE subnet cable. Point its front face toward the output face of a Pattern Provider on the main network.

### 2. Install the Outputs

Install one **Pattern P2P Tunnel (Output)** in front of each processing machine. Point each output's front face toward its machine.

### 3. Pair the Frequency

`Shift + Right-click` the input with an AE2 Memory Card to generate and save a frequency. Then right-click every output with the same card.

### 4. Start Crafting

Put processing patterns in the Pattern Provider and request a crafting job.

The input sends each job to the next available output. The input uses `1` AE channel; outputs use none.

</Column>

### Before You Start

<Column gap="0">

#### Network and Chunks

The input and outputs must be on the same AE subnet, and their chunks must be loaded.

#### Frequency

Frequency `0000` means unconfigured. An endpoint does not work until it has a configured frequency.

</Column>

  </Column>

  <Column gap="0">

## Pattern P2P Tunnel (Input)

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_input" />

<Column gap="0">

### Purpose

Receives processing jobs from a Pattern Provider, distributes them among outputs on the same frequency in round-robin order, and collects returned products.

### How to Use

Install it on an AE subnet cable with its front face against the Pattern Provider's output face. `Shift + Right-click` it with a Memory Card to generate its frequency.

### Configuration

Right-click the input with an empty hand and choose a product return mode. Outputs on the same frequency inherit this choice while **Synchronize input settings** is enabled. If missing products leave tasks stuck, press **Reset Task State** and confirm to reset every loaded output and unit manager on this frequency.

### Result

Each complete job goes to only one machine; its quantities are never divided. Outputs that are offline, unloaded, busy, or unable to accept ingredients are skipped.

</Column>

  </Column>

  <Column gap="0">

## Pattern P2P Tunnel (Output)

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_output" />

<Column gap="0">

### Purpose

Inserts ingredients into the machine in front of it and sends machine products back to the input.

### How to Use

Point the output's front face toward the machine, then right-click it with the Memory Card that contains the input's frequency. Ingredients without a selected input side enter through the face connected to the output.

### Configuration

Right-click the output with an empty hand. It follows the input's product return mode by default. Disable synchronization to select a local mode for this output only. If its current task cannot finish, press **Reset Task State** and confirm to clear this output only.

### Result

An output uses no AE channel and can accumulate up to `64` jobs. In Strict mode, it waits for the active batch to finish before accepting a different pattern.

</Column>

  </Column>

  <Column gap="0">

## Pattern P2P Tunnel (Energy)

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_energy" />

<Column gap="0">

### Purpose

Powers its AE subnet first, then divides the remaining FE as evenly as possible among eligible machines.

### How to Use

Install it on an AE subnet cable with its front face toward an FE source. It needs no frequency, uses no AE channel, and does not participate in crafting jobs.

### Configuration

Right-click it with an empty hand and select a mode.

#### Passive receive

Only accepts FE that an adjacent block actively pushes into it.

#### Active pull

Accepts pushed FE and also pulls FE from the source against its front face.

### Result

It supplies the machine in front of every Pattern P2P Tunnel output on the same subnet whose frequency is not `0000`. Unit Energy Ports with active jobs also receive power. The recipients' specific P2P frequencies do not matter.

</Column>

  </Column>

  <Column gap="0">

## Pattern P2P Unit

<Row gap="12">
  <ItemImage id="ae2_batchcraft:pp2p_unit_manager" scale="3" />
  <ItemImage id="ae2_batchcraft:pp2p_unit_port_transfer" scale="2" />
  <ItemImage id="ae2_batchcraft:pp2p_unit_port_return" scale="2" />
  <ItemImage id="ae2_batchcraft:pp2p_unit_port_redstone" scale="2" />
  <ItemImage id="ae2_batchcraft:pp2p_unit_port_energy" scale="2" />
</Row>

A unit consists of one **Manager** and any number of **functional ports**. The Manager receives one complete processing job from the input. Its ports transfer ingredients, interact with the world, return products, output redstone, or supply power only while that job is active.

The Manager joins normal outputs in the input's round-robin distribution, but processes only one job at a time. Managers and ports use no additional AE channels.

<Column gap="0">

### 1. Install the Manager

Install the **Pattern P2P Unit (Manager)** as a cable center on the AE subnet. The target cable position must not already contain a center cable, cable part, or facade. The Manager connects to cables on all six sides.

### 2. Pair the Input Frequency

`Shift + Right-click` the Pattern P2P input with a Memory Card, then right-click the Manager with that card. The Manager now joins the input's job distribution group.

### 3. Bind the Functional Ports

`Shift + Right-click` the Manager with a Memory Card to save the Manager's identity. Then right-click every port that belongs to this unit with the same card.

Ports and their Manager must be on the same AE subnet. Managers never share ports, even when they use the same P2P frequency.

### 4. Select Ingredient Output Forms

In processing-pattern mode in the Pattern Encoding Terminal, hover over an input ingredient and press `Ctrl + Middle Mouse Button`. Then select its output form.

**Normal** goes to Transfer Ports, **Drop** to Drop Ports, and **Place** to Place Ports.

### 5. Configure Unit Behavior

Right-click the Manager with an empty hand. Product return, break recovery, and redstone settings follow the input by default. Disable **Synchronize input settings** to save independent settings for this Manager. If its current task cannot finish, press **Reset Task State** and confirm to clear this unit only.

</Column>

### Unit Ports

<Column gap="0">

#### Transfer Port

Receives ingredients set to **Normal** and inserts items or fluids into the machine, container, or tank against the port's front face.

#### Drop Port

Receives items set to **Drop** and releases them as item entities in front of the port.

#### Place Port

Receives block items or fluids set to **Place** and places them into the world in front of the port. Other ingredient types cannot use this form.

#### Return Port

Accepts items and fluids pushed by adjacent devices or pipes and returns them to the input as products of the active job.

#### Pickup Port

Collects item entities in the block space in front of it and returns them to the input. It also accepts products pushed by adjacent devices like a Return Port.

#### Break Port

Breaks blocks or picks up fluids in front of it. With **Recover broken items** enabled, drops return directly; otherwise item drops appear in the world. Source fluids always return directly.

#### Redstone Port

Outputs a redstone signal while a job is active. In the Manager, set its strength and choose a single trigger, periodic pulse, or continuous signal.

#### Energy Port

While a job is active, receives FE distributed by a Pattern P2P Tunnel (Energy) on the same AE subnet and supplies the device against its front face. It supplies no power without an active job.

</Column>

### Manager Colors

The Manager is available in fluix and `16` dyed variants. Its color follows AE cable connection rules and controls which cable colors can connect. Port ownership is still determined by the Manager identity saved on the Memory Card.

### Unit Recipes

<RecipeFor id="ae2_batchcraft:pp2p_unit_manager" />

<Column gap="16">
  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_transfer" />
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_return" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_drop" />
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_pickup" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_place" />
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_break" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_redstone" />
    <RecipeFor id="ae2_batchcraft:pp2p_unit_port_energy" />
  </Row>
</Column>

  </Column>

  <Column gap="0">

## Product Extraction Card

<RecipeFor id="ae2_batchcraft:product_extraction_card" />

<Column gap="0">

### Purpose

Makes a Pattern Provider or Pattern P2P output actively extract products from an adjacent machine, reducing the need for a separate output pipe.

### How to Use

Install the card in the Pattern Provider's upgrade slot. Open the Pattern Provider and select **Product Extraction Settings** on the left toolbar. Connected outputs automatically use the settings of their input.

### Configuration

Set the extraction interval and maximum amount per extraction. Place item markers in the filter slots, then choose a filter mode.

#### Blacklist

Extracts everything except the marked items.

#### Whitelist

Extracts only the marked items.

### Result

At each interval, it extracts up to the selected amount. The defaults are up to `64` items every `20` ticks. The same filter also limits products that the Pattern Provider accepts on return.

</Column>

  </Column>

  <Column gap="0">

## AE Component Placer

<RecipeFor id="ae2_batchcraft:component_placer" />

<Column gap="0">

### Purpose

Places AE cables and cable-attached parts across a selected point, line, or plane.

### 1. Select an Area

`Shift + Right-click` a block to set the first point, then right-click another position to complete the selection.

### 2. Select Components

Open the placer, mark a cable and a cable-attached part, then select the part's facing direction. Marker slots record item types without consuming the samples.

### 3. Set a Frequency

When placing P2P parts, right-click the four-color frequency square with a Memory Card to load its saved frequency. Left-click the four-color frequency square to reset it to `0000`.

### 4. Start Placement

Press **Place**. Materials are taken from the bound AE network first, then the nine local material slots. Occupied targets are skipped. Every newly placed P2P part receives the frequency shown by the placer.

### Area Limits

The selection must be a point, line, or plane. Each axis is limited to `16` blocks and a plane is limited to `16x16`.

</Column>

  </Column>

  <Column gap="0">

## Advanced Settings

<Column gap="0">

### Set Ingredient Input Sides

In an AE2 Pattern Encoding Terminal, switch to processing-pattern mode, hover over an input ingredient, and press `Ctrl + Middle Mouse Button`.

#### Automatic

The output inserts the ingredient only through the face where it connects to the machine.

#### Selected Direction

The output inserts through the selected absolute world direction and does not fall back to its connected face. This data is stored in the processing pattern. Pattern P2P outputs use it; standard Pattern Providers ignore it.

### Select a Product Return Mode

Choose a mode that matches the machine's product output path.

#### Unblocked

Does not filter returned products or wait for products from a previous job. Use it when each machine's product output is fully isolated.

#### Strict

While a batch is active, only the product and byproduct types declared by its pattern may return. The output waits for the batch to finish before accepting a different pattern. Use it when several products share a return path.

Strict mode checks only returned item types, not whether the machine actually completed its recipe. Returned quantities are not limited to the quantities declared by the pattern.

</Column>

  </Column>

  <Column gap="0">

## Important Notes

<Column gap="0">

### Transfer Scope

Pattern P2P tunnels carry only processing jobs and their returned products. They do not carry ordinary network storage or AE channels.

### Jobs Are Not Split

Large jobs are not divided. For example, the full `10000 A -> 10000 B` job goes to one machine.

### Shared Machines

The Product Extraction Card and Strict mode both filter by item type. When machines are shared, prevent other processes from producing the same item type as the expected product early.

### Resetting Tasks

A reset permanently destroys ingredients that are still waiting inside an endpoint, so it requires confirmation. The input can reset only endpoints that are loaded and connected to its current subnet.

### Reopen This Guide

Hover over an item from this mod in an inventory and press AE2's guide key, `G`, to reopen this page.

</Column>
  </Column>
</Column>
