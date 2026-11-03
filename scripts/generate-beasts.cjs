module.exports=function({json,png,a,d}){
 const beasts=[['spirit_hare','灵角兔',0xc3c6a5,['plains','sunflower_plains','forest','flower_forest'],8],['azure_fang_wolf','青牙狼',0x526b65,['forest','birch_forest','dark_forest','taiga','old_growth_pine_taiga'],6],['rockhide_boar','岩甲野猪',0x817462,['windswept_hills','windswept_forest','meadow','stony_peaks'],5],['marsh_viper','泽毒蝰',0x54774e,['swamp','mangrove_swamp'],5],['frostback_spider','霜背蛛',0x7796ac,['snowy_taiga','snowy_slopes','frozen_peaks','grove'],4],['windfeather_eagle','风翎鹰',0x829b8c,['windswept_hills','jagged_peaks','stony_peaks','meadow'],3],['thundercrest_lizard','雷纹蜥',0x645d83,['stony_peaks','windswept_gravelly_hills','badlands','wooded_badlands'],3],['rockhide_beast_king','岩甲兽王',0x5e584f,['windswept_hills','stony_peaks','dripstone_caves','lush_caves'],1],['jade_scale_python','碧鳞蟒',0x32766a,['swamp','mangrove_swamp'],1]];
 const materials={spirit_horn:'灵角',wolf_pelt:'狼皮',azure_fang:'完整青牙',rockhide_plate:'岩甲片',curved_tusk:'弯獠',viper_fang:'泽毒牙',venom_sac:'完整毒囊',frost_silk:'霜丝',frost_gland:'寒丝腺',wind_feather:'风翎',eagle_talon:'完整鹰爪',thunder_scale:'雷纹鳞',charged_spine:'蓄雷脊鳞',thick_rock_plate:'厚岩甲',king_tusk:'王獠',jade_scale:'碧鳞',python_sinew:'蟒筋',beast_marrow:'灵髓'};
 for(const[id,name,color,biomes,weight]of beasts){
  json(`${a}/models/item/${id}_spawn_egg.json`,{parent:'minecraft:item/template_spawn_egg'});
  json(`${d}/forge/biome_modifier/${id}.json`,{type:'forge:add_spawns',biomes:biomes.map(b=>'minecraft:'+b),spawners:{type:'xiuxian:'+id,weight,minCount:1,maxCount:id==='azure_fang_wolf'?2:1}});
  png('entity/'+id,(x,y)=>{const rgb=[color>>16,(color>>8)&255,color&255];if(y>=44&&y<=54&&x<6)return[138,215,224,255];if(y>=44&&y<=54&&x>=8&&x<13)return[189,218,134,255];if(y>=44&&y<=54&&x>=16&&x<25)return[226,215,175,255];if(y>=40)return[98+(x+y)%22,104+(x+y)%22,108+(x+y)%22,255];if(y>=31&&y<=33&&(x===7||x===11))return[192,231,163,255];if((x+y*2)%13===0)return rgb.map(v=>Math.min(255,v+45)).concat(255);return rgb.map(v=>Math.max(0,v+(x*3+y*7)%17-8)).concat(255);},64,64);
 }
 let i=0;for(const[id,name]of Object.entries(materials)){
  json(`${a}/models/item/${id}.json`,{parent:'minecraft:item/generated',textures:{layer0:'xiuxian:item/'+id}});
  const index=i++;png('item/'+id,(x,y)=>{const horn=/horn|fang|tusk|talon/.test(id),silk=/silk|sinew|feather/.test(id);if(horn){if(x<5||x>10||y<2||y>13||x+y<11)return[0,0,0,0];return[221,213,170,255];}if(silk){if(Math.abs(x-y)>2||x<2||x>13)return[0,0,0,0];return[153+index%60,205,215,255];}if(Math.abs(x-7.5)+Math.abs(y-7.5)>6)return[0,0,0,0];return[88+index*7%125,98+index*11%115,84+index*13%130,255];});
 }
 const fs=require('fs');for(const lang of ['zh_cn','en_us']){const obj=JSON.parse(fs.readFileSync(`src/main/resources/${a}/lang/${lang}.json`,'utf8'));for(const[id,name]of beasts){obj['entity.xiuxian.'+id]=lang==='zh_cn'?name:id.replaceAll('_',' ');obj['item.xiuxian.'+id+'_spawn_egg']=(lang==='zh_cn'?name+'刷怪蛋':id.replaceAll('_',' ')+' spawn egg');}for(const[id,name]of Object.entries(materials))obj['item.xiuxian.'+id]=lang==='zh_cn'?name:id.replaceAll('_',' ');Object.assign(obj,{'guide.xiuxian.beast_title':'一阶妖兽','guide.xiuxian.beast_tab':'妖兽',
 'guide.xiuxian.new_27_left':'一阶妖兽按物种分别对标炼气一至九层，不随玩家等级变化。和平难度不刷新。\n\n灵角兔（一层）：平原林缘，受惊逃跑，被打才反击。\n青牙狼（二层）：森林，一至两只，低伏后扑击。\n岩甲野猪（三层）：山麓，蓄力直冲，侧腹和冲撞后的硬直是机会。',
 'guide.xiuxian.new_27_right':'泽毒蝰（四层）：沼泽，毒击前有准备。清宁药液可解毒。\n\n霜背蛛（五层）：寒地低处洞口，吐丝减速后逼近。霜丝投射物可用近战斩断，到时消失。\n\n前摇会有亮纹与粒子提示，先观察再进攻。',
 'guide.xiuxian.new_28_left':'风翎鹰：高山露天，俯冲后落地收翅；离开领地后停止追击。\n\n雷纹蜥：裸岩、恶地，蓄力后朝前方扇面放电，绕后躲避。\n\n两者分别对标六层、七层。',
 'guide.xiuxian.new_28_right':'岩甲兽王：对标八层，山地地下与洞穴少量出现。\n\n冲撞后会有震地，离开落点或跳起躲避。硬直期与侧面能绕过岩甲防护。\n\n同种首领在64格内限制自然刷新，避免密集堆叠。',
 'guide.xiuxian.new_29_left':'碧鳞蟒：对标九层，沼泽近水陆地稀有出现。\n\n半血退守一次，水纹提示后再扑击。贴近深水时会潜入水中，在岸上则后撤。\n\n首领不会阻断基础修炼流程，可准备后挑战。',
 'guide.xiuxian.new_29_right':'妖兽掉落各自种属材料，如灵角、青牙、霜丝、风翎与雷纹鳞。\n\n兽王保底王獠，碧鳞蟒掉筋、鳞与灵髓。\n\n材料用途待后续设定，当前没有制作配方。创造模式有九种刷怪蛋可查找。'});json(`${a}/lang/${lang}.json`,obj);}
};
