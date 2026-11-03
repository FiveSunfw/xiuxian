module.exports=function({json,png,a,d}) {
 const ids={healing_liquid:'回春药液',cleansing_liquid:'清宁药液',warming_liquid:'温养药液',healing_pill:'回春丹',cleansing_pill:'清灵丹',nourishing_pill:'养气丹',qi_gathering_formula:'聚气丹丹方',healing_formula:'回春丹丹方',cleansing_formula:'清灵丹丹方',nourishing_formula:'养气丹丹方',pill_residue:'丹渣'};
 for(const [id,name] of Object.entries(ids)){
  json(`${a}/models/item/${id}.json`,{parent:'minecraft:item/generated',textures:{layer0:'xiuxian:item/'+id}});
  const color=id.startsWith('healing')?[204,62,64]:id.startsWith('cleansing')?[153,219,246]:[76,173,115];
  png('item/'+id,(x,y)=>{
   if(id.endsWith('formula')){if(x<3||x>12||y<1||y>14)return[0,0,0,0];return y===1||y===14?[122,77,40,255]:(x===6&&y>4&&y<11)||(y%3===0&&x>5&&x<11)?color.concat(255):[231,212,159,255];}
   if(id==='pill_residue')return y>7&&Math.abs(x-8)<(y-6)&&y<14?[89+(x*y)%28,72,54,255]:[0,0,0,0];
   if(id.endsWith('liquid')){if(y<3)return x>5&&x<10?[129,83,44,255]:[0,0,0,0];const inside=y<7?x>5&&x<10:Math.hypot(x-7.5,(y-10)*.9)<5;if(!inside)return[0,0,0,0];return y<8||x===4?[208,239,239,255]:color.concat(255);}
   if(Math.hypot(x-7.5,y-7.5)>5.8)return[0,0,0,0];return x+y<12?[244,233,183,255]:color.map(v=>Math.max(0,v-((x+y)%4)*9)).concat(255);
  });
 }
 json(`${d}/recipes/basic_alchemy_furnace.json`,{type:'minecraft:crafting_shaped',pattern:['CSC','CFC','CCC'],key:{C:{item:'xiuxian:spirit_copper_ingot'},S:{item:'xiuxian:spirit_stone'},F:{item:'minecraft:furnace'}},result:{item:'xiuxian:basic_alchemy_furnace'}});
 json(`${d}/recipes/qi_gathering_formula.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:'minecraft:paper'},{item:'minecraft:ink_sac'},{item:'xiuxian:spirit_stone'}],result:{item:'xiuxian:qi_gathering_formula'}});
 for(const type of ['qi_gathering','healing','cleansing','nourishing'])json(`${d}/recipes/copy_${type}_formula.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:`xiuxian:${type}_formula`},{item:'minecraft:paper'},{item:'minecraft:ink_sac'}],result:{item:`xiuxian:${type}_formula`,count:2}});
 png('block/alchemy_copper',(x,y)=>Math.abs(x-y)<1?[236,245,249,255]:[177+(x*7+y*3)%45,92+(x+y)%24,48,255]);
 const faces={};for(const dir of ['north','south','east','west','up','down'])faces[dir]={texture:'#all'};
 const elements=[];const box=(from,to)=>elements.push({from,to,faces});
 box([2,4,2],[14,11,14]);box([1,6,1],[15,9,15]);box([3,11,3],[13,13,13]);box([5,13,5],[11,14,11]);box([7,14,7],[9,16,9]);box([2,0,2],[5,4,5]);box([11,0,2],[14,4,5]);box([6,0,11],[10,4,14]);
 json(`${a}/models/block/basic_alchemy_furnace.json`,{parent:'minecraft:block/block',textures:{all:'xiuxian:block/alchemy_copper',particle:'xiuxian:block/alchemy_copper'},elements});
 json(`${a}/models/item/basic_alchemy_furnace.json`,{parent:'xiuxian:block/basic_alchemy_furnace'});json(`${a}/blockstates/basic_alchemy_furnace.json`,{variants:{'':{model:'xiuxian:block/basic_alchemy_furnace'}}});
 json(`${d}/loot_tables/blocks/basic_alchemy_furnace.json`,{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:'xiuxian:basic_alchemy_furnace'}],conditions:[{condition:'minecraft:survives_explosion'}]}]});
 const pick='data/minecraft/tags/blocks/mineable/pickaxe.json';const tag=require('../src/main/resources/'+pick);tag.values.push('xiuxian:basic_alchemy_furnace');json(pick,tag);
 for(const lang of ['zh_cn','en_us']){const obj=require('../src/main/resources/'+a+'/lang/'+lang+'.json');for(const [id,name] of Object.entries(ids))obj['item.xiuxian.'+id]=lang==='zh_cn'?name:id.replaceAll('_',' ');Object.assign(obj,lang==='zh_cn'?{'block.xiuxian.basic_alchemy_furnace':'基础炼丹炉','screen.xiuxian.start_alchemy':'开始炼丹','screen.xiuxian.alchemy_chance':'成功率 %s%%','screen.xiuxian.alchemy_needs':'主药×%s 辅药×%s 灵石×%s','effect.xiuxian.warm_nourishment':'温养','effect.xiuxian.qi_nourishment':'养气','effect.xiuxian.poison_immunity':'抗毒'}:{'block.xiuxian.basic_alchemy_furnace':'Basic Alchemy Furnace','screen.xiuxian.start_alchemy':'Start Alchemy','screen.xiuxian.alchemy_chance':'Success %s%%','screen.xiuxian.alchemy_needs':'Main x%s Aux x%s Stone x%s','effect.xiuxian.warm_nourishment':'Warm Nourishment','effect.xiuxian.qi_nourishment':'Qi Nourishment','effect.xiuxian.poison_immunity':'Poison Immunity'});json(`${a}/lang/${lang}.json`,obj);}
 for(const id of ['warm_nourishment','qi_nourishment','poison_immunity'])png('mob_effect/'+id,(x,y)=>Math.abs(x-7.5)+Math.abs(y-7.5)<6?[130,204,166,255]:[0,0,0,0]);
 const stoneFaces={};for(const dir of ['north','south','east','west','up','down'])stoneFaces[dir]={texture:'#stone'};
 const walls=[];const wall=(from,to,faces=stoneFaces)=>walls.push({from,to,faces});
 wall([0,0,0],[16,4,16]);wall([0,4,0],[3,16,16]);wall([13,4,0],[16,16,16]);wall([3,4,0],[13,16,3]);wall([3,4,13],[13,16,16]);
 wall([3,11,3],[13,12,13],{up:{texture:'#liquid'}});
 json(a+'/models/block/herbal_furnace.json',{parent:'minecraft:block/block',textures:{stone:'minecraft:block/cobblestone',liquid:'xiuxian:block/herbal_liquid',particle:'minecraft:block/cobblestone'},elements:walls});
 png('block/herbal_liquid',(x,y)=>(x+y)%4?[67,156,74,255]:[135,211,87,255]);
 for(const lang of ['zh_cn','en_us']){
  const obj=require('../src/main/resources/'+a+'/lang/'+lang+'.json');
  obj['jei.xiuxian.alchemy']=lang==='zh_cn'?'30秒；丹方保留；可能产出丹渣':'30s; formula retained; may yield residue';
  obj['effect.xiuxian.herbal_recovery']=lang==='zh_cn'?'回春':'Herbal Recovery';
  obj['guide.xiuxian.pills']=lang==='zh_cn'?'丹方与炼丹\n\n铜锭＋灵石合成灵铜锭。7灵铜锭、1灵石、中心熔炉合成基础丹炉。\n\n聚气丹丹方用纸、墨囊、灵石合成，其他丹方探索地牢和废弃矿井。丹方加纸与墨囊抄录。\n\n放入丹方，按JEI备齐主药、辅药与灵石，加1煤或木炭后点击开始。30秒后得到丹药或丹渣，丹方保留。炼气配基础炉成功率默认60%。':'Alchemy\n\nCraft spirit copper from copper and spirit stone. Build a furnace using seven spirit copper ingots, a furnace and a spirit stone.\n\nCraft the Qi Gathering formula with paper, ink and a stone. Find others in dungeon or mineshaft chests. Copy with paper and ink.\n\nInsert formula and ingredients shown in JEI, plus coal or charcoal. Start a 30 second batch. Base success is 60%; failure yields residue.';
  obj['guide.xiuxian.brewing']=lang==='zh_cn'?'药液炼制\n\n药炉放主药、水瓶与可选辅药，10秒熬制。\n\n无辅药时炼对应引气药液。\n青木芝＋小麦：回春药液。\n凝霜草＋泽兰草：清宁药液。\n引气草＋青木芝：温养药液。\n\n药液返还空瓶。聚气丹在基础炼丹炉炼制，具体材料查看JEI。':'Herbal Brewing\n\nInsert herb, water bottle and optional auxiliary ingredient. Brewing takes 10 seconds.\n\nWithout auxiliary: initiation elixir.\nGreenwood + wheat: healing.\nFrost + marsh: cleansing.\nQi herb + greenwood: warming.\n\nPills require an alchemy furnace. See JEI for materials.';
  json(a+'/lang/'+lang+'.json',obj);
 }

};
