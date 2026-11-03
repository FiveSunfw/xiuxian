const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const root = 'src/main/resources';
function json(file, value) { const dest = path.join(root, file); fs.mkdirSync(path.dirname(dest), { recursive: true }); fs.writeFileSync(dest, JSON.stringify(value, null, 2) + '\n'); }
const a = 'assets/xiuxian', d = 'data/xiuxian';
json(`${a}/lang/zh_cn.json`, {
  "block.xiuxian.qi_herb": "引气草",
  "block.xiuxian.herbal_furnace": "基础炼药炉",
  "item.xiuxian.qi_liquid": "引气药液",
  "key.xiuxian.status": "查看修炼状态",
  "key.categories.xiuxian": "修仙",
  "screen.xiuxian.status": "修炼状态",
  "screen.xiuxian.player": "道友 %s",
  "screen.xiuxian.qi_one": "境界 · 炼气一层",
  "screen.xiuxian.mortal": "境界 · 凡人",
  "message.xiuxian.already": "你已引气入体，无需重复服用入门药物。",
  "message.xiuxian.success": "引气入体成功！你已踏入炼气一层。",
  "message.xiuxian.failure": "灵气逸散，此次引气未成。可以再次尝试。",
  "guide.xiuxian.title": "引气入门",
  "guide.xiuxian.intro": "引气入门\n\n你是一名凡人。寻找引气草，直接吃下，或炼成药液服用，尝试引气入体。\n\n按 K 查看境界。\nJEI 中对药液按 R 查配方。\n\n本书遗失后可输入\n/xiuxian guide\n补领。",
  "guide.xiuxian.herbs": "一、寻找药草\n\n在平原和森林的新生成区块寻找引气草。\n\n它有青绿色茎叶，成熟时顶端开出浅蓝白色花。\n\n左键破坏整株药草，即可获得引气草。\n\n各生长阶段均可采集，可直接吃、种植或炼药。",
  "guide.xiuxian.furnace": "三、制作药炉\n\n工作台配方\n\n圆石　空位　圆石\n圆石　空位　圆石\n圆石　营火　圆石\n\n6 圆石＋1 营火\n\n药炉放置后右键打开。\n无需灵气和额外燃料。",
  "guide.xiuxian.brewing": "四、炼制药液\n\n用玻璃瓶对水使用，获得水瓶。\n\n药炉左槽放 1 株引气草。\n中槽放 1 个原版水瓶。\n右槽得到 1 瓶药液。\n\n炼制耗时 %s 秒。\n成品槽满时暂停，取走药液后继续。",
  "guide.xiuxian.cultivation": "五、引气入体\n\n对空长按，吃草或喝药。\n饱食时也可服用。\n\n当前成功率\n引气草 %s%%\n药液 %s%%\n\n失败只消耗本次物品。\n药液喝完返还空瓶。\n\n成功进入炼气一层。\n按 K 查看境界。",
  "message.xiuxian.guide_failed": "入门书发放失败，请稍后重试。",
  "guide.xiuxian.planting": "二、种植药草\n\n采到的引气草，也能作为种苗。\n\n对泥土或草方块右键，种下一株幼株。\n\n在光照充足的地方自然生长，成熟后花朵更明显。\n\n左键破坏获取药草。\n对空长按使用键食用。"
});
json(`${a}/lang/en_us.json`, {
  "block.xiuxian.qi_herb": "Qi Herb",
  "block.xiuxian.herbal_furnace": "Herbal Furnace",
  "item.xiuxian.qi_liquid": "Qi Initiation Elixir",
  "key.xiuxian.status": "Cultivation status",
  "key.categories.xiuxian": "Xiuxian",
  "screen.xiuxian.status": "Cultivation",
  "screen.xiuxian.player": "Cultivator %s",
  "screen.xiuxian.qi_one": "Qi Refining I",
  "screen.xiuxian.mortal": "Mortal",
  "message.xiuxian.already": "You have already completed initiation.",
  "message.xiuxian.success": "Qi initiation succeeded! Qi Refining I reached.",
  "message.xiuxian.failure": "Qi dissipated. You may try again.",
  "guide.xiuxian.title": "Qi Initiation Guide",
  "guide.xiuxian.intro": "Qi Initiation\n\nFind Qi Herbs. Eat them or brew an elixir to attempt initiation.\n\nK: view your status\nJEI R: view recipe\n\nReplace this book with\n/xiuxian guide",
  "guide.xiuxian.herbs": "1. Finding Herbs\n\nSearch new plains and forest chunks. Look for jade-green leaves and pale blue-white flowers.\n\nLeft-click to break and collect the whole plant at any growth stage.",
  "guide.xiuxian.furnace": "3. Herbal Furnace\n\nC  _  C\nC  _  C\nC  F  C\n\nC = Cobblestone\nF = Campfire\n_ = Empty\n\nRight-click to open.\nNo Qi or extra fuel.",
  "guide.xiuxian.brewing": "4. Brewing\n\nUse a glass bottle on water to fill it.\n\nLeft: 1 Qi Herb\nMiddle: 1 water bottle\nOutput: 1 elixir\n\nWait %s seconds.\nFull output pauses brewing.",
  "guide.xiuxian.cultivation": "5. Initiation\n\nHold use to eat or drink at full hunger.\n\nHerb chance: %s%%\nElixir chance: %s%%\n\nFailure consumes one item. Elixirs return a bottle.\n\nSuccess: Qi Refining I",
  "message.xiuxian.guide_failed": "Could not deliver the guide. Please try again.",
  "guide.xiuxian.planting": "2. Growing Herbs\n\nUse herbs on dirt or grass to plant. Provide plenty of light. Mature plants have larger flowers.\n\nLeft-click to harvest. Hold use in the air to eat."
});
const variants = {};
for(let age=0; age<3; age++) {
 variants[`age=${age}`]={model:`xiuxian:block/qi_herb_${age}`};
 json(`${a}/models/block/qi_herb_${age}.json`,{parent:'minecraft:block/cross',render_type:'minecraft:cutout',textures:{cross:`xiuxian:block/qi_herb_${age}`}});
}
json(`${a}/blockstates/qi_herb.json`,{variants});
json(`${a}/blockstates/herbal_furnace.json`,{variants:{'':{model:'xiuxian:block/herbal_furnace'}}});
json(`${a}/models/block/herbal_furnace.json`,{parent:'minecraft:block/cube_bottom_top',textures:{side:'xiuxian:block/furnace_side',top:'xiuxian:block/furnace_top',bottom:'minecraft:block/cobblestone'}});
json(`${a}/models/item/herbal_furnace.json`,{parent:'xiuxian:block/herbal_furnace'});
json(`${a}/models/item/qi_herb.json`,{parent:'minecraft:item/generated',textures:{layer0:'xiuxian:block/qi_herb_2'}});
for(const item of ['qi_liquid']) json(`${a}/models/item/${item}.json`,{parent:'minecraft:item/generated',textures:{layer0:`xiuxian:item/${item}`}});
for(const name of ['qi_herb','herbal_furnace']) json(`${d}/loot_tables/blocks/${name}.json`,{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:`xiuxian:${name}`}],conditions:[{condition:'minecraft:survives_explosion'}]}]});
json(`${d}/recipes/herbal_furnace.json`,{type:'minecraft:crafting_shaped',pattern:['C C','C C','CFC'],key:{C:{item:'minecraft:cobblestone'},F:{item:'minecraft:campfire'}},result:{item:'xiuxian:herbal_furnace'}});
json(`${d}/worldgen/configured_feature/qi_herb.json`,{type:'minecraft:random_patch',config:{tries:12,xz_spread:4,y_spread:2,feature:{feature:{type:'minecraft:simple_block',config:{to_place:{type:'minecraft:simple_state_provider',state:{Name:'xiuxian:qi_herb',Properties:{age:'2'}}}}},placement:[{type:'minecraft:block_predicate_filter',predicate:{type:'minecraft:all_of',predicates:[{type:'minecraft:matching_blocks',blocks:['minecraft:air']},{type:'minecraft:would_survive',state:{Name:'xiuxian:qi_herb',Properties:{age:'2'}}}]}}]}}});
json(`${d}/worldgen/placed_feature/qi_herb.json`,{feature:'xiuxian:qi_herb',placement:[{type:'minecraft:rarity_filter',chance:8},{type:'minecraft:in_square'},{type:'minecraft:heightmap',heightmap:'WORLD_SURFACE_WG'},{type:'minecraft:biome'}]});
json(`${d}/forge/biome_modifier/qi_herb.json`,{type:'forge:add_features',biomes:['minecraft:plains','minecraft:forest'],features:'xiuxian:qi_herb',step:'vegetal_decoration'});
json('data/minecraft/tags/blocks/mineable/pickaxe.json',{replace:false,values:['xiuxian:herbal_furnace']});
function crc(buf) { let c=0xffffffff; for(const b of buf) { c^=b; for(let i=0;i<8;i++) c=(c>>>1)^((c&1)?0xedb88320:0); } return (c^0xffffffff)>>>0; }
function chunk(type, data) { const t=Buffer.from(type), n=Buffer.alloc(4), c=Buffer.alloc(4); n.writeUInt32BE(data.length); c.writeUInt32BE(crc(Buffer.concat([t,data]))); return Buffer.concat([n,t,data,c]); }
function png(file, pixel, width=16, height=16) {
 const raw=Buffer.alloc(height*(1+width*4));
 for(let y=0;y<height;y++) for(let x=0;x<width;x++) { const rgba=pixel(x,y); rgba.forEach((v,i)=>raw[y*(1+width*4)+1+x*4+i]=v); }
 const hdr=Buffer.alloc(13); hdr.writeUInt32BE(width,0); hdr.writeUInt32BE(height,4); hdr[8]=8; hdr[9]=6;
 const out=path.join(root,a,'textures',file+'.png'); fs.mkdirSync(path.dirname(out),{recursive:true}); fs.writeFileSync(out,Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',hdr),chunk('IDAT',zlib.deflateSync(raw)),chunk('IEND',Buffer.alloc(0))]));
}
const clear=[0,0,0,0];
// All stages fill a normal flower's height; buds and petals distinguish maturity.
for(let age=0;age<3;age++) png(`block/qi_herb_${age}`,(x,y)=>{
 const leftLeaf=Math.abs(x-5)+Math.abs(y-10)<4 && x<=7;
 const rightLeaf=Math.abs(x-10)+Math.abs(y-8)<4 && x>=8;
 if(leftLeaf||rightLeaf) return (x+y)%3===0?[100,206,159,255]:[38,143,118,255];
 if((x===7||x===8)&&y>=4&&y<=14) return x===7?[39,108,93,255]:[91,173,129,255];
 if(age===0&&x>=6&&x<=9&&y>=2&&y<=4) return [121,191,165,255];
 if(age===1&&Math.abs(x-7.5)+Math.abs(y-3)<3) return [129,208,208,255];
 if(age===2){
  if((x===7||x===8)&&y>=1&&y<=6) return [190,235,241,255];
  if((y===3||y===4)&&x>=4&&x<=11) return [164,218,235,255];
  if((x===6||x===9)&&(y===2||y===5)) return [214,244,241,255];
 }
 return clear;
});
png('item/qi_liquid',(x,y)=>{
 if(y<3||y>14) return clear;
 if(y<6) return x>=6&&x<=9?[172,139,91,255]:clear;
 if(x<4||x>11) return clear;
 return y>8?[71,174,157,255]:[183,219,220,210];
});
png('block/furnace_side',(x,y)=> y>8&&y<14&&x>4&&x<11 ? [230,110+(x+y)%3*25,38,255] : [70+(x*y%4)*8,91+(x*y%4)*6,79,255]);
png('block/furnace_top',(x,y)=>Math.abs(x-7.5)+Math.abs(y-7.5)<8?[31,49,40,255]:[105+(x*y%3)*8,127,107,255]);
console.log('Generated data, language files, block models and 16x16 pixel textures.');


require('./generate-herbs.cjs')({json,png,a,d});

require('./generate-growth-resources.cjs')({json,png,a,d});
require('./generate-veins.cjs')({json,png,a,d});

require("./generate-alchemy.cjs")({json,png,a,d});
require('./generate-alchemy-content.cjs')({json,png,a,d});
require('./generate-foundation.cjs')({json,png,a,d});
require('./generate-sword-combat.cjs')({json,png,a,d});
require('./generate-sword-book-screen.cjs')({json,a});
require('./generate-spell-manuals.cjs')({json,a});
