module.exports=function({json,png,a,d}){
 const fs=require('fs');const arts=[['drive','驭剑诀','iron_ingot'],['recall','归剑诀','string'],['pierce','贯虹诀','arrow'],['sweep','横扫诀','flint'],['combo','连锋诀','feather'],['armor_break','破甲诀','iron_pickaxe'],['guard','护身剑诀','shield'],['intercept','截击诀','flint_and_steel'],['return_strike','回锋诀','fishing_rod'],['shadows','分光诀','amethyst_shard'],['charge','蓄势诀','blaze_powder'],['step','踏锋步','rabbit_foot']];
 for(let i=0;i<arts.length;i++){const[id,name,catalyst]=arts[i];
  json(`${a}/models/item/sword_art_${id}.json`,{parent:'minecraft:item/generated',textures:{layer0:`xiuxian:item/sword_arts/${id}`}});
  png('item/sword_arts/'+id,(x,y)=>{if(x<3||x>13||y<1||y>14)return[0,0,0,0];if(x<5)return[69,83,91,255];if(x===12&&y<6)return[224,207,155,255];if(x+y===17&&y>5||x===8&&y>7&&y<12)return[213,231,238,255];if(y===13&&x>5&&x<6+i%6)return[196,161,89,255];return[48+i*3,76+i*2,95+i*3,255];});
 }
 const zh={
 'key.xiuxian.sword_cast':'施展当前剑诀','key.xiuxian.sword_cycle':'切换已学剑诀','key.xiuxian.sword_recall':'归剑',
 'message.xiuxian.art_learned':'已学习剑诀：%s。按 Z 切换，R 施展。','message.xiuxian.art_selected':'当前剑诀：%s','message.xiuxian.art_cast':'施展 %s','message.xiuxian.no_sword_art':'请先学习剑诀书。','message.xiuxian.need_sword_manual':'需要主修剑修功法，并在主手持剑。','message.xiuxian.art_cooldown':'剑诀尚未恢复。','message.xiuxian.no_sword_target':'请瞄准24格内可见的目标。','message.xiuxian.step_ground':'踏锋步需要站在地面上。','message.xiuxian.no_projectile':'附近没有可以截击的来袭投射物。','message.xiuxian.not_enough_mana':'灵力不足。','message.xiuxian.sword_charged':'蓄势完成，下一次飞剑攻击获得强化。',
 'guide.xiuxian.cultivation_tab':'修行','guide.xiuxian.details':'修行要点','guide.xiuxian.journey_title':'修行路线','guide.xiuxian.roots_title':'灵根与资质','guide.xiuxian.energy_title':'修为与灵力','guide.xiuxian.manual_title':'功法与篇章','guide.xiuxian.sword_start_title':'剑修入门','guide.xiuxian.sword_attack_title':'进攻剑诀','guide.xiuxian.sword_defense_title':'防守与身法','guide.xiuxian.sword_styles_title':'剑经战斗风格',
 'guide.xiuxian.new_17_left':'凡人先采草，可直接吃草或以药炉炼引气药液。引气成功进入炼气一层。\n\n测灵、编写基础功法，再分别学习法诀。功法决定路线和成长，法诀提供战斗操作。',
 'guide.xiuxian.new_17_right':'按K查看进度与灵力，进入功法页切换主修。\n\n书底部“资源”进入炼丹、灵食与灵脉，“修行”进入本组新章节。\n\n炼气九层需要修满。筑基冲关尚未开放。',
 'guide.xiuxian.new_18_left':'金木水火土冰雷风中生成1至5种不重复灵根，分下中上品。\n\n固定资质会保存，死亡、重登或测灵都不重抽。\n\n测灵石：灵石＋铜锭＋玻璃。',
 'guide.xiuxian.new_18_right':'同品双灵根修炼最快，超过两种逐渐变慢。资质只影响修为积累，不限制最终境界。\n\n剑修不限属性；术修功法需要拥有列出的全部属性。具体倍率仍为试玩值。',
 'guide.xiuxian.new_19_left':'修为用于境界进度，灵力用于驱剑与剑诀。攻击消耗灵力，不扣修为。\n\n一层灵力上限100，之后每层加20。战斗回灵较慢，脱战较快，主修功法提高恢复。',
 'guide.xiuxian.new_19_right':'没有功法时一层修满即停。残篇按覆盖层数卡住，不存溢出。\n\n九层也要继续修至圆满。卡层或圆满时，灵石与聚气丹不消耗。\n\n功法、药食与灵脉共同影响吐纳。',
 'guide.xiuxian.new_20_left':'空白书册：3纸＋1皮革。\n\n基础功法：书册＋墨囊＋灵石＋对应材料，全部可查JEI。\n\n右键首次学习消耗一本；已经学会时只切换。K功法页可重选已学功法。',
 'guide.xiuxian.new_20_right':'剑经用铁锭；金铁锭、木橡树苗、水海带、火木炭、土黏土、冰雪球、雷避雷针、风羽毛。\n\n书＋空白书册＋墨囊可抄录。进阶书暂在地牢和矿井宝箱。流光残篇仅覆盖三层。',
 'guide.xiuxian.new_21_left':'先学基础养剑诀并主修，再制作和学习驭剑诀、归剑诀。\n\n剑诀书：空白书册＋墨囊＋灵石＋专用材料。驭剑用铁锭，归剑用线，其他查看JEI。\n\n功法与剑诀必须分别学习。',
 'guide.xiuxian.new_21_right':'主手持任意原版剑。Z切换剑诀，R施展，V归剑，均可改绑。\n\n瞄准24格内目标驱剑。投影使用主手剑外观，命中或碰墙后返回。\n\n切走剑、转修术修或超时会结束驱剑。',
 'guide.xiuxian.new_22_left':'驭剑：单体飞剑攻击。\n贯虹：高速突刺，单次更强。\n横扫：近身前方扇面，最多5目标。\n连锋：最多3次追击。\n破甲：降低目标护甲3秒。\n分光：剑影追击，最多3次较低伤害。',
 'guide.xiuxian.new_22_right':'归剑：召回，不伤沿途目标。\n回锋：回程最多追加一次攻击，需要先有飞剑。\n蓄势：准备1.5秒，受击打断；5秒内下一次飞剑强化。\n\n施展消耗灵力并有独立冷却，切换功法或剑诀不会清除冷却。',
 'guide.xiuxian.new_23_left':'护身：飞剑环身5秒，减轻来袭伤害；持续耗灵，不能同时远攻。\n\n截击：主动清除6格内一枚可见来袭投射物。\n\n踏锋：地面短距离冲步，受到方块碰撞限制，无法持续飞行。',
 'guide.xiuxian.new_23_right':'默认耗灵：驭剑6、归剑1、贯虹12、横扫14、连锋24、破甲18、护身8、截击10、回锋12、分光24、蓄势10、踏锋8。\n\n护身另每秒耗4。数值为试玩值。更换主修不恢复灵力。',
 'guide.xiuxian.new_24_left':'基础养剑：均衡。\n清风：飞剑更快，单次伤害降低。\n磐石：飞剑稍慢，护身减伤更强。\n长河：降低耗灵，伤害略低。\n\n这些风格只在主修该剑经时生效。',
 'guide.xiuxian.new_24_right':'破锋：攻击附短时破甲。\n回澜：召回更省灵力，回锋更强。\n孤锋：单体更强，横扫较弱。\n流光：速度更快，耗灵较高。\n\n炼气能驱剑战斗，御剑飞行留待筑基。',
 'guide.xiuxian.furnace':'三、制作药炉\n\n圆石　灵石　圆石\n圆石　空位　圆石\n圆石　营火　圆石\n\n6圆石＋1营火＋1灵石。\n\n药炉可由凡人制作和使用。',
 'guide.xiuxian.cultivation':'引气与修炼\n\n吃草成功率%s%%，药液%s%%。\n\n引气进入一层，自动吐纳；没有功法时一层修满即停。功法需覆盖目标层数。\n\n九层需要修满才圆满。\n\nK查看状态，书底“修行”查看最新规则。'};
 for(const lang of ['zh_cn','en_us']){const obj=JSON.parse(fs.readFileSync(`src/main/resources/${a}/lang/${lang}.json`,'utf8'));for(const [key,value]of Object.entries(zh))obj[key]=value;for(const[id,name]of arts)obj['item.xiuxian.sword_art_'+id]=lang==='zh_cn'?name+'·剑诀书':id.replaceAll('_',' ')+' sword art';json(`${a}/lang/${lang}.json`,obj);}
};
