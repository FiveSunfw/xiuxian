module.exports=function({json,png,a,d}) {
 const ids=['spirit_vein_core','depleted_spirit_ore','depleted_deepslate_spirit_ore'];
 for(const id of ids) {
  json(`${a}/blockstates/${id}.json`,{variants:{'':{model:'xiuxian:block/'+id}}});
  json(`${a}/models/block/${id}.json`,{parent:'minecraft:block/cube_all',textures:{all:'xiuxian:block/'+id}});
  json(`${a}/models/item/${id}.json`,{parent:'xiuxian:block/'+id});
  json(`${d}/loot_tables/blocks/${id}.json`,{type:'minecraft:block',pools:[]});
  png('block/'+id,(x,y)=>{const star=Math.abs(x-7.5)+Math.abs(y-7.5)<4||((x===7||x===8)&&y>1&&y<14)||((y===7||y===8)&&x>1&&x<14);const n=(x*7+y*11)%20;return star?(id===ids[0]?[240,249,255,255]:[69,77,82,255]):id.includes('deepslate')?[48+n,51+n,57+n,255]:[100+n,107+n,114+n,255];});
 }
 for(const name of ['mineable/pickaxe','needs_stone_tool']) {const path=`data/minecraft/tags/blocks/${name}.json`; const tag=require('../src/main/resources/'+path);tag.values.push(...ids.map(id=>'xiuxian:'+id));json(path,tag);}
 for(const lang of ['zh_cn','en_us']) {
  const obj=require('../src/main/resources/'+a+'/lang/'+lang+'.json');
  ids.forEach((id,i)=>obj['block.xiuxian.'+id]=lang==='zh_cn'?['灵脉核心','枯竭灵石矿','深层枯竭灵石矿'][i]:['Spirit Vein Core','Depleted Spirit Ore','Depleted Deepslate Spirit Ore'][i]);
  obj['guide.xiuxian.stones']=lang==='zh_cn'?'灵石与灵脉\n\n地下灵脉的星纹核心向地表散发灵气。换位置查看 K 面板吐纳速度，越接近正上方越快，可据此寻脉。\n\n石镐以上采矿掉白色灵石，留下暗纹枯竭矿。保留核心和枯竭矿，加载累计默认30分钟后再生。\n\n挖掉枯竭矿或精准采集会终止该处再生。破坏核心会失去加速与再生。\n\n灵石对空气右键吸收，9个合成二阶。':'Spirit Veins\n\nCompare your K-panel cultivation rate while moving to locate underground cores. The strongest rate lies above the core.\n\nMining leaves depleted ore. Keep the core and depleted blocks to regenerate after 30 loaded minutes by default.\n\nSilk Touch or removing depleted ore ends regeneration at that spot. Breaking the core ends its aura and regeneration.\n\nUse stones in the air. Nine craft a tier II stone.';
  json(`${a}/lang/${lang}.json`,obj);
 }
};
