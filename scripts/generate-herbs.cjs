const herbs=require('./herbs.cjs');
module.exports=function({json,png,a,d}){
 const zh=require('../src/main/resources/assets/xiuxian/lang/zh_cn.json');
 const en=require('../src/main/resources/assets/xiuxian/lang/en_us.json');
 for(let index=0;index<herbs.length;index++){
  const h=herbs[index];
  zh['block.xiuxian.'+h.id]=h.name;en['block.xiuxian.'+h.id]=h.en;
  zh['item.xiuxian.'+h.liquid]=h.elixir;en['item.xiuxian.'+h.liquid]=h.en+' Elixir';
  for(const [key,z,e] of [['environment',h.environment,h.environmentEn],['habitat',h.habitat,h.habitatEn],['appearance',h.appearance,h.appearanceEn]]){zh['herb.xiuxian.'+h.id+'.'+key]=z;en['herb.xiuxian.'+h.id+'.'+key]=e;}
  const variants={};
  for(let age=0;age<3;age++){
   variants['age='+age]={model:`xiuxian:block/${h.id}_${age}`};
   json(`${a}/models/block/${h.id}_${age}.json`,{parent:'minecraft:block/cross',render_type:'minecraft:cutout',textures:{cross:`xiuxian:block/${h.id}_${age}`}});
   if(index>0)png(`block/${h.id}_${age}`,(x,y)=>{
    const c=h.color.map(v=>Math.max(0,v-(2-age)*15)); const bloom=[...c,255],stem=[40,113,80,255],leaf=[57,143,102,255],clear=[0,0,0,0];
    switch(index){
     case 1: if(y>=3&&y<=7&&Math.abs(x-7.5)<=6-(y===3?2:0))return (y===4||y===6)?[134,207,132,255]:bloom;if(x>=7&&x<=8&&y>=7&&y<=14)return[116,88,48,255];break;
     case 2: if(y>=2&&y<=14&&(Math.abs(x-7.5)<=1||x===Math.round(7.5+(y-3)*0.45)||x===Math.round(7.5-(y-3)*0.45)))return y<7?bloom:[215,237,246,255];break;
     case 3: if(Math.abs(x-7.5)+Math.abs(y-4)<4)return bloom;if(y>=10&&y<=14&&Math.abs(x-7.5)<7-(y-10))return (x+y)%3===0?[151,172,135,255]:[96,123,105,255];if(x===8&&y>=7&&y<=13)return stem;break;
     case 4: if(y>=1&&y<=14&&(x===7||x===8||x===Math.round(7+(14-y)*0.45)||x===Math.round(8-(14-y)*0.45)))return y<6?[255,195,65,255]:bloom;break;
     case 5: if(y>=2&&y<=8&&(x===7+(y%2?2:-1)||x===8))return bloom;if(y>=9&&y<=14&&Math.abs(x-7.5)<=14-y)return leaf;if(x===8&&y>=6&&y<=14)return stem;break;
     case 6: if(y>=2&&y<=14&&(x===7+Math.round(Math.sin(y/3)*3)||x===8+Math.round(Math.sin(y/3)*3)))return bloom;if(y>=6&&y<=12&&x===3+Math.round((y-6)/2))return[40,123,139,255];break;
     case 7: if((Math.abs(x-4)+Math.abs(y-12)<4||Math.abs(x-9)+Math.abs(y-11)<4||Math.abs(x-12)+Math.abs(y-13)<3)&&y<=14)return (x*3+y)%5===0?bloom:[49,88,86,255];break;
    }
    return clear;
   });
  }
  json(`${a}/blockstates/${h.id}.json`,{variants});
  json(`${a}/models/item/${h.id}.json`,{parent:'minecraft:item/generated',textures:{layer0:`xiuxian:block/${h.id}_2`}});
  json(`${a}/models/item/${h.liquid}.json`,{parent:'minecraft:item/generated',textures:{layer0:`xiuxian:item/${h.liquid}`}});
  png(`item/${h.liquid}`,(x,y)=>{if(y<3||y>14)return[0,0,0,0];if(y<6)return x>=6&&x<=9?[172,139,91,255]:[0,0,0,0];if(x<4||x>11)return[0,0,0,0];return y>8?[...h.color,255]:[183,219,220,210];});
  json(`${d}/loot_tables/blocks/${h.id}.json`,{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:'xiuxian:'+h.id}],conditions:[{condition:'minecraft:survives_explosion'}]}]});
  json(`${d}/worldgen/configured_feature/${h.id}.json`,{type:'minecraft:random_patch',config:{tries:index===7?24:12,xz_spread:4,y_spread:index===7?3:2,feature:{feature:{type:'minecraft:simple_block',config:{to_place:{type:'minecraft:simple_state_provider',state:{Name:'xiuxian:'+h.id,Properties:{age:'2'}}}}},placement:[{type:'minecraft:block_predicate_filter',predicate:{type:'minecraft:all_of',predicates:[{type:'minecraft:matching_blocks',blocks:index===2?['minecraft:air','minecraft:snow']:['minecraft:air']},{type:'minecraft:would_survive',state:{Name:'xiuxian:'+h.id,Properties:{age:'2'}}}]}}]}}});
  const placement=index===7?[{type:'minecraft:count',count:16},{type:'minecraft:in_square'},{type:'minecraft:height_range',height:{type:'minecraft:uniform',min_inclusive:{absolute:-48},max_inclusive:{absolute:48}}},{type:'minecraft:biome'}]:[{type:'minecraft:rarity_filter',chance:6},{type:'minecraft:in_square'},{type:'minecraft:heightmap',heightmap:'WORLD_SURFACE_WG'},{type:'minecraft:biome'}];
  json(`${d}/worldgen/placed_feature/${h.id}.json`,{feature:'xiuxian:'+h.id,placement});
  json(`${d}/forge/biome_modifier/${h.id}.json`,{type:'forge:add_features',biomes:h.biomes.map(b=>'minecraft:'+b),features:'xiuxian:'+h.id,step:'vegetal_decoration'});
 }
 Object.assign(zh,{'guide.xiuxian.herbs':'八方灵草\n\n主世界有八类入门草药，产地与外观不同，入门效果相同。\n\n左键破坏整株获取。每种都可食用、种植，并炼成对应药液。','guide.xiuxian.species':'%s\n\n产地\n%s\n\n辨识\n%s','guide.xiuxian.contents':'引气入门','guide.xiuxian.atlas':'八方草木图鉴','guide.xiuxian.index':'目录','guide.xiuxian.habitat':'生境','guide.xiuxian.appearance':'辨识','guide.xiuxian.uses':'用途','guide.xiuxian.common_use':'左键破坏整株获取。可直接食用，也可种植或与水瓶炼成对应药液。','guide.xiuxian.plant_title':'采集与种植','guide.xiuxian.furnace_title':'基础炼药炉','guide.xiuxian.brew_title':'炼制药液','guide.xiuxian.cultivate_title':'引气入体','guide.xiuxian.overview':'八方水土，各育灵草。\n\n点击目录或药草图样查看产地与辨识图。\n\n八种药草的入门成功率相同，均能通过药炉制成各自药液。','guide.xiuxian.planting':'采集与种植\n\n左键破坏药草即可获得整株。\n\n所有药草均可种在泥土或草方块上，也支持各自产地的砂石、雪块或苔土。\n\n幽光苔在洞穴暗处也能生长。\n\n对空长按使用键食用。'});
 Object.assign(en,{'guide.xiuxian.herbs':'Eight Beginner Herbs\n\nEach habitat has a distinct herb. All share the same initiation chance.\n\nBreak to collect. Eat, plant, or brew each into its own elixir.','guide.xiuxian.species':'%s\n\nHabitat\n%s\n\nAppearance\n%s','guide.xiuxian.contents':'Qi Initiation','guide.xiuxian.atlas':'Herbal Atlas','guide.xiuxian.index':'Contents','guide.xiuxian.habitat':'Habitat','guide.xiuxian.appearance':'Appearance','guide.xiuxian.uses':'Uses','guide.xiuxian.common_use':'Break to collect. Eat, plant, or brew with a water bottle into its matching elixir.','guide.xiuxian.plant_title':'Gather & plant','guide.xiuxian.furnace_title':'Herbal Furnace','guide.xiuxian.brew_title':'Brewing','guide.xiuxian.cultivate_title':'Qi initiation','guide.xiuxian.overview':'Eight habitats nurture eight herbs.\n\nSelect an entry or herb illustration to inspect it.\n\nAll herbs share the same initiation chance and brew into matching elixirs.','guide.xiuxian.planting':'Gather & Plant\n\nBreak herbs to collect them.\n\nAll grow on dirt and grass. Each also accepts its native sand, rock, snow or mossy soil.\n\nGlow Moss needs no daylight. Hold use in the air to eat.'});
 Object.assign(zh,{'screen.xiuxian.progress_locked':'尚未引气入体','screen.xiuxian.progress_full':'修为圆满','screen.xiuxian.progress':'修为 %s / %s','screen.xiuxian.health':'生命值 %s','screen.xiuxian.attack':'攻击力 %s','screen.xiuxian.breathing':'自动吐纳 +%s 修为/秒','screen.xiuxian.breathing_paused':'吐纳暂停','message.xiuxian.level_up':'修为精进，已达到炼气 %s 层！'});
 Object.assign(en,{'screen.xiuxian.progress_locked':'Not initiated','screen.xiuxian.progress_full':'Qi Refining complete','screen.xiuxian.progress':'Cultivation %s / %s','screen.xiuxian.health':'Health %s','screen.xiuxian.attack':'Attack damage %s','screen.xiuxian.breathing':'Automatic cultivation +%s/s','screen.xiuxian.breathing_paused':'Cultivation paused','message.xiuxian.level_up':'You reached Qi Refining level %s!'});
 const numerals=['一','二','三','四','五','六','七','八','九'];
 for(let i=1;i<=9;i++){zh['realm.xiuxian.qi_'+i]='炼气'+numerals[i-1]+'层'+(i===9?' · 圆满':'');en['realm.xiuxian.qi_'+i]='Qi Refining '+i+(i===9?' - Complete':'');}
 zh['guide.xiuxian.cultivation']='五、引气入体与修炼\n\n吃草成功率 %s%%，药液 %s%%。\n\n入门后自动吐纳。按 K 查看修为进度、实际生命和攻击。\n\n修为满时自动升层，九层圆满。\n\n每层的生命和攻击提升由世界配置决定。';
 en['guide.xiuxian.cultivation']='Cultivation\n\nHerb chance: %s%%\nElixir chance: %s%%\n\nAfter initiation, cultivate automatically. K shows progress and real health and damage.\n\nA full bar grants the next level. Level 9 completes Qi Refining.';
 json(`${a}/lang/zh_cn.json`,zh);json(`${a}/lang/en_us.json`,en);
 png('gui/guide_book',(x,y)=>{
  if(x<9||x>410||y<8||y>260)return[0,0,0,0];
  if(y>250)return y%3===0?[109,75,42,255]:[178,151,106,255];
  if((x>=19&&x<=201||x>=218&&x<=400)&&y>=17&&y<=247){const edge=Math.min(y-17,247-y,x<210?x-19:x-218,x<210?201-x:400-x);const grain=(x*17+y*31)%7;const shade=edge<3?19:0;return[230+grain-shade,213+grain-shade,174+grain-shade,255];}
  if(x>=202&&x<=217)return[116+Math.abs(209-x)*5,78+Math.abs(209-x)*4,47+Math.abs(209-x)*3,255];
  return (x+y)%5===0?[113,74,43,255]:[77,48,32,255];
 },420,270);
};
