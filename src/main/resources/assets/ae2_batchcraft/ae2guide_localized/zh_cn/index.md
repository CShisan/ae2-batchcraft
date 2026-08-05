---
navigation:
  title: AE2 批量工艺
  icon: pattern_p2p_tunnel_input
  position: 1000
item_ids:
- ae2_batchcraft:pattern_p2p_tunnel_input
- ae2_batchcraft:pattern_p2p_tunnel_output
- ae2_batchcraft:pattern_p2p_tunnel_energy
- ae2_batchcraft:component_placer
- ae2_batchcraft:product_extraction_card
- ae2_batchcraft:pattern_p2p_unit_manager
- ae2_batchcraft:white_pattern_p2p_unit_manager
- ae2_batchcraft:light_gray_pattern_p2p_unit_manager
- ae2_batchcraft:gray_pattern_p2p_unit_manager
- ae2_batchcraft:black_pattern_p2p_unit_manager
- ae2_batchcraft:lime_pattern_p2p_unit_manager
- ae2_batchcraft:yellow_pattern_p2p_unit_manager
- ae2_batchcraft:orange_pattern_p2p_unit_manager
- ae2_batchcraft:brown_pattern_p2p_unit_manager
- ae2_batchcraft:red_pattern_p2p_unit_manager
- ae2_batchcraft:pink_pattern_p2p_unit_manager
- ae2_batchcraft:magenta_pattern_p2p_unit_manager
- ae2_batchcraft:purple_pattern_p2p_unit_manager
- ae2_batchcraft:blue_pattern_p2p_unit_manager
- ae2_batchcraft:light_blue_pattern_p2p_unit_manager
- ae2_batchcraft:cyan_pattern_p2p_unit_manager
- ae2_batchcraft:green_pattern_p2p_unit_manager
- ae2_batchcraft:pattern_p2p_unit_port_drop
- ae2_batchcraft:pattern_p2p_unit_port_pickup
- ae2_batchcraft:pattern_p2p_unit_port_place
- ae2_batchcraft:pattern_p2p_unit_port_break
- ae2_batchcraft:pattern_p2p_unit_port_transfer
- ae2_batchcraft:pattern_p2p_unit_port_return
- ae2_batchcraft:pattern_p2p_unit_port_extract
- ae2_batchcraft:pattern_p2p_unit_port_redstone
- ae2_batchcraft:pattern_p2p_unit_port_energy
---

<Column gap="24">
  <Column gap="0">

# AE2 批量工艺

<Row gap="16">
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_input" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_output" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_tunnel_energy" scale="3" />
  <ItemImage id="ae2_batchcraft:component_placer" scale="3" />
  <ItemImage id="ae2_batchcraft:product_extraction_card" scale="3" />
</Row>

将一个样板供应器的加工任务轮询分配给多台机器。主端连接样板供应器，从端连接机器；任务完成后，产物沿原路返回样板供应器。

  </Column>

  <Column gap="0">

## 先完成这一套

按以下顺序搭建，即可让多个相同机器并行加工。

<Column gap="0">
### 1. 安装主端

在 AE 子网线缆上安装**样板 P2P 通道（输入）**，让它的正面朝向主网中样板供应器的输出面。

### 2. 安装从端

在每台加工机器前安装一个**样板 P2P 通道（输出）**，让从端正面朝向机器。

### 3. 配对频率

用 AE2 记忆卡潜行右击主端，生成并写入频率；再用这张卡右击每个从端。

### 4. 开始合成

将处理样板放入样板供应器，发起合成任务。

主端会依次将任务交给可用从端。主端占用 `1` 个 AE 频道；从端不占频道。

</Column>

### 开始前检查

<Column gap="0">

#### 网络与区块

主端与从端必须位于同一个 AE 子网，且所在区块已加载。

#### 频率

频率 `0000` 表示未配置。未配置频率时，端点不会工作。

</Column>

  </Column>

  <Column gap="0">

## 样板 P2P 通道（输入）

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_input" />

<Column gap="0">
### 作用

接收样板供应器的加工任务，轮询分配给同频率从端，并收回产物。

### 使用方法

安装在子网线缆上，正面连接样板供应器输出面；用记忆卡潜行右击它生成频率。

### 配置

空手右击主端，选择产物返回模式。选择会同步给同频率、启用“同步主端配置”的从端。若任务因产物未返回而卡住，可点击“重置任务状态”；确认后会重置同频率下所有已加载的从端与单元管理器。

### 工作结果

每次任务只交给一台机器，不会按数量拆分。离线、区块未加载、忙碌或无法输入材料的从端会被跳过。

</Column>

  </Column>

  <Column gap="0">

## 样板 P2P 通道（输出）

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_output" />

<Column gap="0">

### 作用

将材料送入前方机器，并把机器产物送回主端。

### 使用方法

让从端正面朝向机器，再用已写入主端频率的记忆卡右击从端。未指定材料输入面时，材料从从端连接机器的这一面输入。

### 配置

空手右击从端。默认同步主端的产物返回模式；关闭同步后，可仅为当前从端选择本地模式。若当前任务无法完成，可点击“重置任务状态”并确认，只清除这个从端的任务。

### 工作结果

从端不占 AE 频道。一个从端最多累计 `64` 个任务；严格模式中正在处理其他样板时，它会等待当前批次结束。

</Column>

  </Column>

  <Column gap="0">

## 样板 P2P 通道（能量）

<RecipeFor id="ae2_batchcraft:pattern_p2p_tunnel_energy" />

<Column gap="0">

### 作用

为所在 AE 子网供电，再将剩余 FE 尽量平均提供给已配置从端前方的机器。

### 使用方法

安装在 AE 子网线缆上，正面朝向 FE 能源方块。它不需要频率、不占 AE 频道，也不参与合成任务。

### 配置

空手右击后选择一种工作模式。

#### 被动接收

仅接受相邻方块主动推送的 FE。

#### 主动拉取

接受相邻方块推送的 FE，同时主动从正面的能源方块拉取 FE。

### 工作结果

同一子网内、频率不是 `0000` 的从端前方机器会获得供电；有活动任务的单元能量端口也会参与供电。不区分这些从端或单元管理器的具体频率。

</Column>

  </Column>

  <Column gap="0">

## 样板 P2P 单元

<Row gap="12">
  <ItemImage id="ae2_batchcraft:pattern_p2p_unit_manager" scale="3" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_unit_port_transfer" scale="2" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_unit_port_return" scale="2" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_unit_port_extract" scale="2" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_unit_port_redstone" scale="2" />
  <ItemImage id="ae2_batchcraft:pattern_p2p_unit_port_energy" scale="2" />
</Row>

一个单元由一个**管理器**和若干**功能端口**组成。管理器接收主端分配的完整加工任务，各端口只在这个任务进行期间执行材料传输、世界交互、产物返回、红石输出或供电。

单元管理器与普通从端一起参与主端的轮询分配，但一个管理器同一时间只处理一个任务。管理器和端口不会额外占用 AE 频道。

<Column gap="0">

### 1. 安装管理器

将**样板P2P单元（管理）**作为线缆中心安装到 AE 子网中。目标线缆位置不能已有中心线缆、线缆部件或遮板；管理器自身会连接六个方向的线缆。

### 2. 配对主端频率

先用记忆卡潜行右击样板 P2P 主端，再用这张卡右击管理器。管理器由此加入该主端的任务分配组。

### 3. 绑定功能端口

用记忆卡潜行右击管理器，将管理器身份写入卡中；再用这张卡右击属于该单元的每个端口。

端口和管理器必须位于同一个 AE 子网。不同管理器即使使用相同 P2P 频率，也不会共用端口。

### 4. 设置材料输出形式

在样板编码终端的处理样板模式中，将鼠标移到输入材料上，按 `Ctrl + 鼠标中键`，然后选择输出形式。

**普通**交给传输端口；**掉落**交给掉落端口；**放置**交给放置端口。

### 5. 配置单元行为

空手右击管理器打开配置。默认同步主端的产物返回、破坏回收和红石设置；关闭“同步主端配置”后，可为该管理器保存独立设置。若当前任务无法完成，可点击“重置任务状态”并确认，只清除这个单元的任务。

</Column>

### 单元端口

<Column gap="0">

#### 传输端口

接收输出形式为“普通”的材料，并将物品或流体输入端口正面连接的机器、容器或储罐。

#### 掉落端口

接收输出形式为“掉落”的物品，并将它们作为物品实体投放到端口前方。

#### 放置端口

接收输出形式为“放置”的方块物品或流体，并将其放置到端口前方的世界中。其他类型的材料不能使用此形式。

#### 返回端口

接收相邻设备或管线推入的物品与流体，并将其作为当前任务的产物返回主端。

#### 提取端口

任务进行期间，主动从端口正面连接的机器提取产物。该端口只采用提取间隔和数量，不受输入端提取开关影响。启用“同步主端配置”的单元管理器继承输入端数值；关闭同步后可在管理器中单独设置。材料仍在下发时也可以提取；全部材料下发完成前任务不会结束。配置的提取间隔不会被缩短；连续空提取会增加额外退避，额外退避上限为 20 tick，存储能力变化时会提前唤醒。

#### 拾取端口

拾取端口前方一格内的物品实体并返回主端；它也能像返回端口一样接收相邻设备主动推入的产物。

#### 破坏端口

破坏或吸取端口前方的方块与流体。开启“破坏回收”时，掉落物直接返回；关闭时，物品掉落会生成在世界中。源流体始终直接返回。

#### 红石端口

任务进行期间输出红石信号。可在管理器中设置强度，并选择单次触发、周期脉冲或持续输出。

#### 能量端口

任务进行期间，接收同一 AE 子网中样板 P2P 能量通道分配的 FE，并提供给端口正面的设备。没有活动任务时不供电。

</Column>

### 管理器颜色

管理器有福鲁伊克斯和 `16` 种染色版本。颜色遵循 AE 线缆的连接规则，用于控制它能与哪些颜色的线缆相连；端口归属仍由记忆卡保存的管理器身份决定。

### 单元物品配方

<RecipeFor id="ae2_batchcraft:pattern_p2p_unit_manager" />

<Column gap="16">
  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_transfer" />
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_return" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_extract" />
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_pickup" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_drop" />
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_place" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_break" />
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_redstone" />
  </Row>

  <Row gap="12">
    <RecipeFor id="ae2_batchcraft:pattern_p2p_unit_port_energy" />
  </Row>
</Column>

  </Column>

  <Column gap="0">

## 产物提取卡

<RecipeFor id="ae2_batchcraft:product_extraction_card" />

<Column gap="0">
### 作用

让样板供应器主动从相邻机器提取经过筛选的产物。

### 使用方法

将卡安装到样板供应器的升级槽。打开样板供应器界面后，点击左侧工具栏中的“产物提取设置”。下游端点的提取功能在样板 P2P 输入端独立配置。

### 配置

设置活跃提取时的间隔和每次最大数量；在标记槽放入物品，再选择筛选模式。配置的提取间隔不会被缩短；连续空提取会增加额外退避，额外退避上限为 20 tick，存储能力变化时会提前唤醒。

#### 黑名单

不提取标记的物品。

#### 白名单

只提取标记的物品。

### 工作结果

每个间隔最多提取设定数量，默认每 `20` tick 提取最多 `64` 个物品。筛选规则也会限制样板供应器接收的返回产物。

</Column>

  </Column>

  <Column gap="0">

## AE部件放置器

<RecipeFor id="ae2_batchcraft:component_placer" />

<Column gap="0">

### 作用

在选定的点、线或平面中批量放置 AE 线缆和线缆部件。

### 1. 选择范围

潜行右击方块设为第一个点，再右击另一位置完成范围。

### 2. 选择部件

打开放置器界面，标记线缆和可附着部件，再选择部件朝向。标记槽只记录类型，不消耗样品。

### 3. 设置频率

需要 P2P 频率时，用记忆卡右击四色频率方块加载频率。

### 4. 开始放置

点击“放置”。材料优先从已绑定的 AE 网络取得，不足时使用本地 9 格材料槽；被占用的位置会跳过。

### 范围限制

仅支持点、线或平面，单轴最大 `16` 格、平面最大 `16x16`。

</Column>

  </Column>

  <Column gap="0">

## 进阶设置

<Column gap="0">

### 设置材料输入面

在 AE2 样板编码终端中切换到处理样板模式，将鼠标移到输入材料上，按 `Ctrl + 鼠标中键`。

#### 自动方向

从端只通过连接机器的那一面输入材料。

#### 指定方向

从所选的世界方向输入，不会改用连接面。方向数据保存在处理样板中，只有样板 P2P 从端会使用；普通样板供应器会忽略它。

### 选择产物返回模式

根据机器的产物输出方式选择模式。

#### 无阻塞

不筛选返回产物，也不等待上次任务的产物。适合每台机器的产物输出已经完全隔离时使用。

#### 严格

当前批次进行时，只允许处理样板声明的产物和副产物返回；同一从端会等待当前批次完成后再接收不同样板的任务。适合多种产物共用返回路径时使用。

严格模式只检查返回物品的类型，不验证机器是否真正完成了配方；返回数量不受样板产出数量限制。

</Column>

  </Column>

  <Column gap="0">

## 值得注意

<Column gap="0">

### 传输范围

该通道只传递加工任务与加工产物，不传递普通网络库存或 AE 频道。

### 任务不会拆分

大型任务不会自动拆分。例如 `10000 A -> 10000 B` 会完整交给一台机器。

### 共享机器

产物提取卡和严格模式都依赖物品类型筛选。共享机器时，请避免其他流程提前产出与目标产物相同的物品。

### 重置任务

重置会永久销毁端点内部尚未下发的材料，因此必须在确认界面中再次确认。主端只能重置当前已加载且仍连接在同一子网中的从端和单元管理器。

### 再次打开指南

在物品栏中指向本模组物品并按住 AE2 指南键 `G`，可再次打开本页。

</Column>
  </Column>
</Column>
