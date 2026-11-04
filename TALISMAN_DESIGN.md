# 符箓系统实现说明（0.6.0）

玩家玩法、配方及数值见 README“符箓”；实际验证见 VALIDATION。本模块沿用玩家持久化 NBT 与 CultivationFoundation 灵力接口，不新增 Capability。

## 数据与接口

- TalismanKind 是菜单、服务端绘制与 JEI 共用定义，记录引子、消耗、成功率。九符物品 ID 见 enum，普通/强化通过物品 NBT `Enhanced` 布尔值区分。普通与强化均在创造页可取，JEI 注册 NBT 子类型。
- TalismanTableEntity 保存6槽库存、Energy、NextDrawTick。Energy 单位等同画符耗蓝；每灵石补100，仅缺能时补充。TalismanMenu 各自保存供能模式，默认自身灵力；按钮0画符、1自身灵力、2台内能量，复用原版菜单网络。
- TalismanProjectile 使用同步的符箓物品记录种类与品质，沿用原版投掷物拥有者保存/同步。三种攻击共用实体类型，粒子与伤害按种类分发，最长100 tick。
- 天眼通过 SpiritVeins 索引查找指定球形范围内最近且区块已加载的有效核心，不触发区块加载。
- 神行采用固定 UUID 修饰符，移动乘1.4、Forge 跨步高度加0.5；效果结束移除修饰符。玄甲用独立效果与剩余护盾记录，Forge受伤事件记录本来源消耗，移除时保留其他来源黄心。

## 材料 ID

`cinnabar_ore`、`cinnabar`、`spirit_stone_dust`、`blank_talisman_paper`、`spirit_cinnabar`、`wolf_hair_brush`、`talisman_table`、`wolf_brush_hair`，命名空间均为 `xiuxian`。朱砂矿单次矿簇目标随机4～8，每区块尝试4次，实际矿数受地形影响。矿石可替换石头与深板岩矿物替换标签，石镐及以上，时运不增加掉落。

## 结算与保护

绘制先检查距离、境界、台子冷却、材料、成品空间与供能，再在服务端线程一次扣除和判定。随机结果可通过包内入口固定以做GameTest。输出槽 setItem 接受同步写入，玩家插入由槽过滤，避免重现炼器成品同步问题。药液返瓶进操作者背包，满时掉落脚边。破坏台子掉落库存，台内能量不掉落，无漏斗制符。

破岩严格只允许石头、深板岩、圆石、凝灰岩、泥土、沙砾；检查硬度、方块实体、玩家权限、Forge破坏事件及已加载区块。空目标不耗符。攻击排除施法者、其驯养动物及禁止友伤目标；落雷仅伤敌对生物。闪电是视觉实体，伤害独立结算，无真实爆炸和地面火。

资源生成脚本：scripts/generate_talisman_assets.py，可重建材料、符纹、矿石、桌子、效果图标、配方与世界生成JSON。中英文文本、图文引导和JEI集成随源码维护。
