module.exports=function({json,png,a,d}) {
 json(`${d}/recipes/spirit_copper_ingot.json`,{type:'minecraft:crafting_shapeless',ingredients:[{item:'minecraft:copper_ingot'},{item:'xiuxian:spirit_stone'}],result:{item:'xiuxian:spirit_copper_ingot'}});
 json(`${d}/recipes/herbal_furnace.json`,{type:'minecraft:crafting_shaped',pattern:['CSC','C C','CFC'],key:{C:{item:'minecraft:cobblestone'},S:{item:'xiuxian:spirit_stone'},F:{item:'minecraft:campfire'}},result:{item:'xiuxian:herbal_furnace'}});
 json(`${a}/models/item/spirit_copper_ingot.json`,{parent:'minecraft:item/generated',textures:{layer0:'xiuxian:item/spirit_copper_ingot'}});
 png('item/spirit_copper_ingot',(x,y)=>{
  if(y<4||y>12||x<2||x>13||x+y<8||x+y>24)return[0,0,0,0];
  if(Math.abs(x-y-1)<1 || (y===8&&x>7))return[236,247,250,255];
  if(y===4||x+y===8)return[255,193,133,255];
  return y>9?[155,72,43,255]:[224,126,76,255];
 });
 for(const lang of ['zh_cn','en_us']) {
  const obj=require('../src/main/resources/'+a+'/lang/'+lang+'.json');
  obj['item.xiuxian.spirit_copper_ingot']=lang==='zh_cn'?'灵铜锭':'Spirit Copper Ingot';json(`${a}/lang/${lang}.json`,obj);
 }
};
