const fs=require('fs');const herbs=require('./herbs.cjs');let count=0;
function read(file){return JSON.parse(fs.readFileSync(file,'utf8'));}
function walk(dir){for(const e of fs.readdirSync(dir,{withFileTypes:true})){const f=dir+'/'+e.name;if(e.isDirectory())walk(f);else if(f.endsWith('.json')){read(f);count++;}}}
walk('src/main/resources');
const java=fs.readFileSync('src/main/java/dev/xiuxian/HerbKind.java','utf8');const all=new Set;
for(const h of herbs){if(!java.includes('("'+h.id+'", "'+h.liquid+'")'))throw Error('Java herb definition missing '+h.id);const modifier=read('src/main/resources/data/xiuxian/forge/biome_modifier/'+h.id+'.json');if(modifier.biomes.length!==h.biomes.length)throw Error('Biome modifier mismatch');for(const b of h.biomes){if(all.has(b))throw Error('Duplicate biome '+b);all.add(b);}for(let age=0;age<3;age++){const f='src/main/resources/assets/xiuxian/textures/block/'+h.id+'_'+age+'.png';const data=fs.readFileSync(f);if(data.readUInt32BE(16)!==16||data.readUInt32BE(20)!==16)throw Error('Invalid sprite dimensions '+f);}const loot=read('src/main/resources/data/xiuxian/loot_tables/blocks/'+h.id+'.json');if(loot.pools[0].entries[0].name!=='xiuxian:'+h.id)throw Error('Wrong herb drop');}
if(herbs.length!==8||all.size!==53)throw Error('Expected 8 herbs and 53 biomes');
const zh=read('src/main/resources/assets/xiuxian/lang/zh_cn.json'),en=read('src/main/resources/assets/xiuxian/lang/en_us.json');if(JSON.stringify(Object.keys(zh).sort())!==JSON.stringify(Object.keys(en).sort()))throw Error('Language key mismatch');for(const h of herbs)for(const lang of [zh,en])for(const key of ['block.xiuxian.'+h.id,'item.xiuxian.'+h.liquid,'herb.xiuxian.'+h.id+'.habitat','herb.xiuxian.'+h.id+'.appearance'])if(!lang[key])throw Error('Missing '+key);
console.log('Passed: '+count+' JSON resources, 8 registered herb definitions, 24 sprites, 8 loot tables, 53 unique biome assignments and bilingual atlas content.');

const tier2=read('src/main/resources/data/xiuxian/recipes/spirit_stone_tier_2.json');
if(tier2.pattern.join('').split('S').length-1!==9||tier2.result.item!=='xiuxian:spirit_stone_tier_2')throw Error('Tier II stone must cost nine stones');
for(const id of ['scattered_spirit_stones','small_spirit_vein','medium_spirit_vein','large_spirit_vein']){const f=read('src/main/resources/data/xiuxian/worldgen/configured_feature/'+id+'.json');if(f.type!=='xiuxian:'+id)throw Error('Wrong vein feature type');}
for(const id of ['spirit_stone_ore','deepslate_spirit_stone_ore']){const loot=read('src/main/resources/data/xiuxian/loot_tables/blocks/'+id+'.json');if(loot.pools[0].entries[0].children[1].name!=='xiuxian:spirit_stone')throw Error('Ore must directly drop white spirit stones');}
console.log('Growth resources: nine-to-one compression, four vein configurations and direct ore drops verified.');
