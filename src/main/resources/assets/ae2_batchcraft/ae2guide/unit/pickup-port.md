---
navigation:
  parent: unit/index.md
  title: Pickup Port
  icon: pattern_p2p_unit_port_pickup
  position: 70
item_ids:
- ae2_batchcraft:pattern_p2p_unit_port_pickup
---

# Unit Port (Pickup)

<RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_pickup" />

Collects item entities from the block space in front of it and also accepts item or fluid products pushed by adjacent automation.

| Property | Value |
| --- | --- |
| Unit identity | Required |
| Operational Unit task | Required |
| Entity pickup | Items in the front block space |
| Passive return | Items and fluids supported by the target capability |

Picked resources pass through the Manager's active return rule. Only an amount that can be accepted by the return path is removed from the entity or source.

Choose this port when a process drops its result into the world. Use a Return Port for push-only automation without entity collection.
