---
navigation:
  parent: unit/index.md
  title: 拾取端口
  icon: pattern_p2p_unit_port_pickup
  position: 70
item_ids:
- ae2_batchcraft:pattern_p2p_unit_port_pickup
---

# 单元端口（拾取）

<RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_pickup" />

收集端口正面方块空间的物品实体，也接受相邻自动化主动推入的物品或流体产物。

| 属性 | 值 |
| --- | --- |
| 单元身份 | 必须绑定 |
| 可运行单元任务 | 必须存在 |
| 实体拾取 | 正面方块空间中的物品 |
| 被动返回 | 目标能力支持的物品与流体 |

拾取资源会经过管理器当前返回规则。只会从实体或来源中移除返回路径能够接收的数量。

处理流程会把结果掉落到世界时使用拾取端口；只有自动化推入、不需收集实体时使用返回端口。
