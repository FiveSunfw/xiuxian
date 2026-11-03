module.exports=function({json,png,a,d}){
 const fs=require('fs');
 const manuals=require('./manual-catalog.cjs')();
 for(const m of manuals){
  json(`${a}/models/item/${m.id}.json`,{parent:'minecraft:item/generated',textures:{layer0:`xiuxian:item/manuals/${m.id}`}});
  json(`${d}/recipes/copy_manual_${m.id}.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:`xiuxian:${m.id}`},{item:'xiuxian:blank_manual'},{item:'minecraft:ink_sac'}],result:{item:`xiuxian:${m.id}`,count:2}});
 }
 json(`${d}/recipes/blank_manual.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:'minecraft:paper'},{item:'minecraft:paper'},{item:'minecraft:paper'},{item:'minecraft:leather'}],result:{item:'xiuxian:blank_manual'}});
 json(`${d}/recipes/root_testing_stone.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:'xiuxian:spirit_stone'},{item:'minecraft:copper_ingot'},{item:'minecraft:glass'}],result:{item:'xiuxian:root_testing_stone'}});
 json(`${a}/models/item/blank_manual.json`,{parent:'minecraft:item/generated',textures:{layer0:'minecraft:item/book'}});
 json(`${a}/models/item/root_testing_stone.json`,{parent:'minecraft:item/generated',textures:{layer0:'xiuxian:item/spirit_stone_tier_2'}});
 const basic={basic_sword:'minecraft:iron_ingot',metal_origin:'minecraft:iron_ingot',greenwood:'minecraft:oak_sapling',dark_water:'minecraft:kelp',scarlet_fire:'minecraft:charcoal',thick_earth:'minecraft:clay_ball',frost:'minecraft:snowball',thunder:'minecraft:lightning_rod',wind:'minecraft:feather'};
 for(const [id,item] of Object.entries(basic))json(`${d}/recipes/manual_${id}.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:'xiuxian:blank_manual'},{item:'xiuxian:spirit_stone'},{item:'minecraft:ink_sac'},{item}],result:{item:`xiuxian:${id}`}});
 const zh={'item.xiuxian.blank_manual':'空白书册','item.xiuxian.root_testing_stone':'测灵石','screen.xiuxian.manuals':'灵根与主修功法','screen.xiuxian.mana':'灵力 %s / %s','screen.xiuxian.manual_gate':'本层已满，需要覆盖下一层的功法','screen.xiuxian.no_manuals':'尚未学习新功法，可在工作台编写基础篇','message.xiuxian.roots':'灵根 %s · %s','roots.xiuxian.grade_0':'下品','roots.xiuxian.grade_1':'中品','roots.xiuxian.grade_2':'上品','message.xiuxian.manual_root_mismatch':'此功法需要匹配全部属性灵根。','message.xiuxian.manual_active':'当前主修 %s'};
 const en={'item.xiuxian.blank_manual':'Blank Manual','item.xiuxian.root_testing_stone':'Root Testing Stone','screen.xiuxian.manuals':'Roots and Active Manual','screen.xiuxian.mana':'Spiritual Energy %s / %s','screen.xiuxian.manual_gate':'Layer full: learn a manual covering the next layer','screen.xiuxian.no_manuals':'Craft a basic manual to begin','message.xiuxian.roots':'Roots %s / %s','roots.xiuxian.grade_0':'Low grade','roots.xiuxian.grade_1':'Middle grade','roots.xiuxian.grade_2':'High grade','message.xiuxian.manual_root_mismatch':'This manual requires all matching elemental roots.','message.xiuxian.manual_active':'Active manual: %s'};
 for(const lang of ['zh_cn','en_us']){const obj=JSON.parse(fs.readFileSync(`src/main/resources/${a}/lang/${lang}.json`,'utf8'));Object.assign(obj,lang==='zh_cn'?zh:en);for(const m of manuals)obj[`item.xiuxian.${m.id}`]=lang==='zh_cn'?m.name:m.id.replaceAll('_',' ');
 obj['guide.xiuxian.manuals']=lang==='zh_cn'?'功法与灵根\n\n灵根固定不变，只影响修炼速度，术修需要匹配属性，剑修不限属性。\n\n空白书册由3纸与1皮革制作。基础功法用书册、灵石、墨囊与对应材料编写，查看JEI。\n\n右键学习并主修，K面板进入功法页可切换已学功法。一次主修一部。\n\n无功法仅能修满一层；残篇按覆盖层数停止积累。九层修满才圆满，尚未开放筑基。':'Roots and Manuals\n\nRoots are permanent and affect cultivation speed. Sword manuals accept all roots; spell manuals require matching elements.\n\nCraft blank manuals with three paper and leather. See JEI for basic manual recipes.\n\nUse to learn and activate. Select known manuals from the K panel. Coverage gates promotion; the ninth layer must reach full progress.';
 json(`${a}/lang/${lang}.json`,obj);}
};
