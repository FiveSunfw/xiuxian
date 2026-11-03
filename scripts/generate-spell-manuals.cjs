module.exports=function({json,a}){
 const fs=require('fs');const zh={'spell.xiuxian.active':'正在主修','guide.xiuxian.spell_tab':'术修',
 'spell.xiuxian.selected':'当前术法：%s','spell.xiuxian.cast':'施展 %s','spell.xiuxian.need_page':'请打开当前主修法诀，达到层数后参悟对应术法页。',
 'spell.xiuxian.learned':'已参悟','spell.xiuxian.activate':'修炼此法诀','spell.xiuxian.study':'参悟此页',
 'spell.xiuxian.practice_page':'修炼篇 · 覆盖炼气一至%s层。主修后提高吐纳和回灵效率；术法页在一层、六层分别解锁，需要单独参悟。',
 'spell.xiuxian.reset_notice':'法诀随吐纳提升等级，角色升层额外加1级。转修另一部会清零等级与进度，已经参悟的书页保留。',
 'spell.xiuxian.keep_book':'修炼和参悟都保留原书，可继续翻阅。','spell.xiuxian.page_requirement':'炼气%s层解锁，需要主修本法诀。',
 'spell.xiuxian.controls':'%s切换本诀术法，%s施展。法诀等级提高术法威力。',
 'spell.xiuxian.rank':'法诀等级 %s · 进度 %s / %s','spell.xiuxian.rank_full':'法诀等级 %s · 圆满','spell.xiuxian.book_rank':'当前主修法诀等级 %s',
 'key.xiuxian.sword_cast':'施展当前剑诀／术法','key.xiuxian.sword_cycle':'切换当前剑诀／术法',
 'spell.xiuxian.effect_金_0':'金刃攻击24格内瞄准目标，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_木_0':'木刺攻击目标并短暂减速，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_水_0':'水箭攻击目标并缓速，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_火_0':'火弹攻击目标并短暂点燃，不引燃方块；基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_土_0':'碎岩攻击瞄准目标，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_冰_0':'冰锥攻击并短暂减速，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_雷_0':'雷矢以电弧瞬击目标，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_风_0':'风刃攻击并击退目标，基础伤害4，消耗8灵力。',
 'spell.xiuxian.effect_金_1':'目标周围4格内最多攻击5个目标，基础伤害6，消耗22灵力。',
 'spell.xiuxian.effect_木_1':'目标周围的荆棘攻击最多5个目标并减速，基础伤害6，消耗22灵力。',
 'spell.xiuxian.effect_水_1':'涌浪攻击目标附近最多5个目标并缓速，基础伤害6，消耗22灵力。',
 'spell.xiuxian.effect_火_1':'炎环攻击目标附近最多5个目标并点燃生物，不破坏方块，消耗22灵力。',
 'spell.xiuxian.effect_土_1':'岩甲提供短时伤害抗性。法诀等级延长持续时间，消耗22灵力。',
 'spell.xiuxian.effect_冰_1':'霜环攻击目标附近最多5个目标并减速，基础伤害6，消耗22灵力。',
 'spell.xiuxian.effect_雷_1':'电弧攻击目标附近最多5个目标，基础伤害6，消耗22灵力。',
 'spell.xiuxian.effect_风_1':'风障提供短时伤害吸收。法诀等级延长持续时间，消耗22灵力。',
 'guide.xiuxian.spell_title':'术修法诀与参悟',
 'guide.xiuxian.new_25_left':'术修法诀现在包含修炼篇和两页术法。右键打开书，点击修炼此法诀，书会保留。\n\n一层解锁第一术法，六层解锁第二术法。打开对应页点击参悟，不会自动学会。',
 'guide.xiuxian.new_25_right':'主修术修法诀时，Z切换本诀的已学术法，R施展。不要求手持法杖。\n\n法诀等级0至9，随吐纳独立成长，角色升层额外提升。每级默认增加8%威力，防护法术延长持续。',
 'guide.xiuxian.new_26_left':'转修另一部功法时，法诀等级与进度清零；重复选择当前功法不重置。\n\n学会的术法页保留，但只能使用当前主修法诀里的术法。六层页按角色境界解锁，与法诀等级分别计算。',
 'guide.xiuxian.new_26_right':'当前法诀等级与进度在K面板显示。\n\n境界卡层或九层圆满后，仍能吐纳补练法诀。修为不会继续溢出积累。\n\n基础进度需求300×下一等级，数值可配置。'
 };
 for(const lang of ['zh_cn','en_us']){const obj=JSON.parse(fs.readFileSync(`src/main/resources/${a}/lang/${lang}.json`,'utf8'));Object.assign(obj,zh);
 obj['guide.xiuxian.new_17_left']='凡人先采草，直接吃草或以药炉炼药液引气。\n\n功法决定主修路线。术修法诀自带两页术法，需要达到境界再参悟；剑修需要另外学习剑诀书。\n\n基础传承都能在工作台制作，查看JEI。';
 obj['guide.xiuxian.manuals']='功法与资源\n\n术修法诀右键打开书，选择修炼，再参悟一层和六层的术法页，书保留。\n\nK查看主修与法诀等级。转修清空法诀等级，已参悟页面保留。\n\n书底“术修”查看详细规则，“修行”查看灵根与剑修。';
 obj['guide.xiuxian.new_20_left']='术修功法右键打开书页，点击修炼此法诀后主修，书保留。\n\n修炼篇覆盖一至九层；术法页在一层、六层解锁，分别参悟。\n\nK功法页可切换已学传承。转修会清空法诀等级。';json(`${a}/lang/${lang}.json`,obj);}
};
