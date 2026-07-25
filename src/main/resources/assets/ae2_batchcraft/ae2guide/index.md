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
---

# AE2 BatchCraft

<Row gap="16">
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_input" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_output" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_energy" scale="3" />
  <ItemImage id="ae2_batchcraft:component_placer" scale="3" />
</Row>

AE2 BatchCraft provides one-to-many P2P distribution channels for AE2 processing patterns.

- A standard Pattern Provider still stores patterns and connects to the main network.
- The Pattern P2P Tunnel input receives crafting jobs from it and distributes those jobs among outputs on the same frequency in round-robin order.
- Each output can connect to one machine. It supplies ingredients to the machine through the sides declared by the pattern and returns products to the Pattern Provider.

Hover over any item from this mod in an inventory and hold AE2's guide key, `G`, to reopen this page.

<br/>

## Added Features and Items

- Pattern P2P Tunnel (Input): receives jobs from a standard Pattern Provider and distributes them among outputs on the same frequency.
- Pattern P2P Tunnel (Output): receives jobs from the input, inserts ingredients into a machine, and returns machine products to the input.
- Pattern P2P Tunnel (Energy): powers its AE subnet first, then distributes remaining FE among machines connected to every configured output in that subnet.
- AE Component Placer: places a selected point, line, or plane of cables and cable-attached parts using AE network storage or its nine local material slots.
- Configurable ingredient input sides: in a standard AE2 Pattern Encoding Terminal, switch to processing-pattern mode, hover over an input ingredient, and press `Ctrl + Middle Mouse Button` to configure its input side. Only Pattern P2P Tunnel outputs use this extra direction data; standard Pattern Providers ignore it.

<br/>

## Important Notes

- The Pattern P2P Tunnel input and outputs must be on the same AE2 subnet, and their chunks must be loaded.
- Frequency `0000` means that an endpoint is not configured. Inputs and outputs do not operate without a configured frequency.
- The energy tunnel has no frequency. It supplies every output with a frequency other than `0000` on its current AE subnet, regardless of which frequency each output uses.
- The tunnel only transports crafting jobs and returned products. It does not carry ordinary network storage or channels.
- Large jobs are not divided. For example, one machine receives the complete input for `10000 A -> 10000 B`; the quantities are not split into smaller jobs.
- Strict mode validates only the types of returned items, not whether the machine recipe actually completed. When machines are shared, prevent other processes from producing the same item type as the primary product early.

<br/>

## Item Comparison

| | Pattern P2P Tunnel (Input) | Pattern P2P Tunnel (Output) |
| --- | --- | --- |
| I/O | Input endpoint | Output endpoint |
| AE channels | Uses `1` | Uses none |
| Connects to | Output face of a standard Pattern Provider | Processing machine |
| Main role | Receives and distributes jobs; collects returned products | Inserts ingredients into machines; receives machine products |
| Frequency setup | Generates a frequency and writes it to a Memory Card | Reads the input frequency from a Memory Card |
| Return mode | Global setting for the frequency | Can follow the input or use a local setting |

<br/>

## Pattern P2P Tunnel (Input)

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_input" />

The input is the only job entry point for a group of tunnels. Install it on an AE subnet cable and point it toward the output face of a standard Pattern Provider on the main network. The standard Pattern Provider recognizes the input as a processing machine.

### Configuration

- P2P frequency: use an AE2 Memory Card to `Shift + Right-click` the input. This generates a frequency and writes it to the card. Then right-click each output with that Memory Card.
- Product return mode: right-click the input with an empty hand to open its configuration screen. The selected mode is synchronized to every output on the same frequency that has "Synchronize input settings" enabled.

### Distribution Rules

The input resumes round-robin distribution after the last output that successfully accepted a job. An output is skipped if it is offline, its chunk is not loaded, it has ingredients waiting to be retried, its strict batch is processing a different pattern, or it has reached the `64`-job limit.

If an output cannot accept any ingredients, the input tries the next output. Once any ingredient has entered a machine, the job is not reassigned, preventing one crafting operation from being split across multiple machines.

<br/>

## Pattern P2P Tunnel (Output)

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_output" />

An output is both a job exit and a product entry. Point its front face toward the machine when installing it. If an ingredient has no configured input side, it is inserted only through the face where the output connects to the machine.

An output uses no AE channels, but it must share an AE subnet grid and a nonzero P2P frequency with the input, and its chunk must be loaded.

### Configuration

Right-click the output with an empty hand to open its configuration screen.

- Synchronize input settings: when enabled, the output continuously follows the input's product return mode. The screen displays the synchronized mode, but it cannot be changed from the output.
- Local product return mode: after synchronization is disabled, a mode can be selected for this output alone without affecting the input or other outputs.

An active strict batch continues to use the rules in effect when its first job was accepted. Configuration changes apply to later batches.

<br/>

## Product Return Modes

| Behavior | Unblocked | Strict |
| --- | --- | --- |
| No active crafting job | Allows all products | Allows all products |
| Active crafting job | Allows all products | Allows only product and byproduct types declared by the pattern in the active batch |
| Returned quantity | Unlimited | Unlimited |
| Concurrent jobs for the same pattern | Does not wait for products or create a strict batch | Accumulates up to `64` jobs for the same pattern |
| Job for a different pattern | May still be accepted | Waits for the active batch to complete |
| Best suited for | Fully isolated machine outputs and maximum throughput | Shared return paths where products must not be mixed |

### Unblocked

Unblocked mode does not filter returned contents according to crafting jobs and does not wait for job products to return. It imposes the fewest restrictions and is suitable when every machine or output pipe is already isolated from unrelated products.

### Strict

Strict mode creates a product-type allowlist while a batch is active. The allowlist contains every output declared by the processing pattern, so both primary products and byproducts can return independently from any machine output slot.

Strict mode does not limit the quantity returned at once. For example, if a pattern declares `3 A` and a machine returns `8 A`, all `8 A` pass through. Batch completion uses only the accumulated quantity of the primary product as its completion signal; byproducts do not consume the target quantity or end the batch early.

When the accumulated primary product reaches the quantity required by the active same-pattern batch, the batch ends and a different pattern may be accepted. With no active batch, strict mode does not block unrelated products already present in the machine.

<br/>

## Configuring Ingredient Input Sides in Pattern Encoding

In a standard AE2 Pattern Encoding Terminal, switch to processing-pattern mode, hover over an input ingredient, and press `Ctrl + Middle Mouse Button` to open the input-side selector.

- Automatic: uses only the face where the Pattern P2P Tunnel output connects to the machine.
- East, south, west, north, top, or bottom: uses an absolute world direction. Only the selected face is attempted; insertion does not fall back to the connected face.
- Left/right hints: the direction screen labels the two horizontal directions to the player's left and right when the GUI opens. Turning afterward does not change those labels for the current screen.

Direction data is stored in the processing pattern, but only Pattern P2P Tunnel outputs use it. Patterns with direction data can still be placed in standard Pattern Providers and used normally; standard Pattern Providers ignore the extra data. Missing, invalid, or unreadable direction data is treated as automatic.

<br/>

## Pattern P2P Tunnel (Energy)

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_energy" />

Install the energy tunnel on an AE subnet cable with its front face toward an FE source. It uses no AE channel, stores no P2P frequency, and does not interact with Memory Cards or crafting jobs.

Right-click the channel with an empty hand to open its configuration. Passive receive is the default and only accepts FE pushed by external blocks. Active pull keeps accepting pushed FE and also pulls from the adjacent source; its pull interval adapts from five ticks down to one when demand remains and energy is available.

External machines may push FE into the channel in either mode. In active mode, pulling begins at a five-tick interval and accelerates only while its subnet or connected machines still need energy and the source can provide more. It has no internal energy buffer.

Received energy is applied in this order:

1. The AE subnet containing the energy tunnel.
2. Machines in front of every Pattern P2P Tunnel output on that subnet whose frequency is not `0000`.

All eligible outputs participate regardless of their actual frequency. Energy is divided evenly, and shares rejected by one machine are offered to other machines. Outputs on another AE grid, unloaded outputs, and outputs with frequency `0000` are ignored.

<br/>

## AE Component Placer

<RecipeFor id="ae2_batchcraft:component_placer" />

The placer uses the same network binding, wireless range, and power rules as an AE2 wireless terminal. Bind it through an AE2 Security Station and charge it before using network materials. It can still use materials stored in its nine local slots while disconnected from the network.

### Selecting an Area

- `Shift + Right-click` a block to select the first corner, then right-click the opposite corner.
- Starting a new selection resets its translation to `X: 0, Y: +1, Z: 0`, placing the target one block above the selected blocks by default.
- The selection may be one point, one line, or one plane. Three-dimensional volumes are rejected.
- Each axis may contain at most `16` blocks, and a plane may contain at most `16x16` targets.
- The X, Y, and Z controls in the placer screen translate the complete selection by up to `16` blocks in either direction without resizing it.

### Configuration and Placement

- Put a cable into the cable marker slot. This is a ghost slot: it records the exact cable type and color without consuming the sample. Dense cables cannot be selected.
- Put a cable-attached part into the part marker slot. Both marker slots are ghost slots and do not consume their samples.
- The placer screen shows the player inventory. Move cables or parts into the nine local material slots as needed.
- Choose the absolute direction that every marked part will face.
- Left-click the four-color frequency square to reset it to `0000`. Right-click it with an AE2 Memory Card to load the P2P frequency stored on the card.
- The nine local slots accept usable AE cables and cable-attached parts.
- Press **Place** to run the operation. Non-air targets are skipped before any material is extracted.
- For every target, the placer extracts the selected cable and part from the AE network first, then falls back to the nine local slots. If either item is unavailable, that target is skipped and any partial extraction is refunded.
- Every newly placed P2P part receives the frequency shown by the placer. Frequency `0000` leaves P2P parts unconfigured; non-P2P parts ignore the frequency.

<br/>
<br/>
<br/>
<br/>
