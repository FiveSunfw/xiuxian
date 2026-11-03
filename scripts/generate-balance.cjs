module.exports=function({json,a}){
 const fs=require('fs');for(const lang of ['zh_cn','en_us']){const obj=JSON.parse(fs.readFileSync(`src/main/resources/${a}/lang/${lang}.json`,'utf8'));
 for(const key of Object.keys(obj))if(key.startsWith('spell.xiuxian.effect_'))obj[key]=obj[key].replace('基础伤害4','默认基础8，另加境界成长').replace('基础伤害6','默认基础12，另加境界成长');
 Object.assign(obj,{'guide.xiuxian.new_18_right':'同根数的灵根品级倍率：下品默认0.6，中品1，上品1.8。上品是下品3倍。\n\n同品双灵根仍较快，超过两种逐渐减慢。资质影响吐纳速度，不限制最终境界，也不改变突破概率。',
 'guide.xiuxian.new_26_right':'功法品质默认倍率：黄下1.25，黄中1.75，黄上2.5，玄下3.5。\n\n飞剑折算剑及伤害附魔的40%，保留自身属性。术法基础加强，并随境界、练度、品质成长。\n\n剑修术修均可炼丹。炼器也按通用生产规划，符箓待定。'});json(`${a}/lang/${lang}.json`,obj);}
};
