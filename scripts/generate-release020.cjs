module.exports=function({json,a}){
 const fs=require('fs');const zh={
 'manual.xiuxian.missing_page':'此残篇缺少这一层的技法页，需要寻找更完整的同部功法。',
 'manual.xiuxian.sword_controls':'主手持原版剑。%s切换，%s施展，%s归剑。',
 'spell.xiuxian.practice_page':'修炼篇 · 覆盖炼气一至%s层。一本功法同时提供修炼与战斗，两页技法分别在一层、六层参悟。残篇可能缺页。',
 'spell.xiuxian.reset_notice':'功法独立成长，角色升层额外加级。换另一部清零；补全同部残篇保留等级和已参悟页。',
 'spell.xiuxian.keep_book':'学习、主修和参悟都保留原书，可赠送抄录。',
 'spell.xiuxian.controls':'%s切换本诀术法，%s施展；功法等级增强威力。',
 'message.xiuxian.no_sword_art':'打开当前剑修功法，参悟其中的剑诀页。',
 'guide.xiuxian.new_17_left':'先采草或炼药引气，再测灵并寻找功法。\n\n剑修与术修都只需一本功法：修炼篇负责成长，技法页负责战斗。右键打开，点击主修、参悟。书会保留。\n\n基础完整功法可制作，探索宝箱可找更好的传承。',
 'guide.xiuxian.new_20_left':'功法有完整炼气篇、一至三层残篇、一至六层残篇。完整度与品质独立。\n\n残篇修满覆盖层数后卡住，未覆盖的技法页也无法参悟。\n\n补全同一部功法保留练度，换另一部清零。',
 'guide.xiuxian.new_20_right':'地牢、矿井、沙漠神殿、丛林神庙、沉船、遗迹、要塞、林地府邸、古城等探索宝箱均可能有功法。\n\n各流派完整篇与残篇都可能出现，残篇更常见。\n\n书册＋墨囊＋原书可抄录。基础完整篇制作查看JEI。',
 'guide.xiuxian.new_21_left':'右键剑修功法，点击修炼此法诀。翻到剑诀页，达到一层或六层后点击参悟。\n\n一本书就能修炼和攻击，归剑自动作为通用操作可用。\n\n主手持任意原版剑，不必另找剑诀书。',
 'guide.xiuxian.new_21_right':'Z切换可用剑诀，R施展，V归剑，可改绑。\n\n飞剑采用主手剑外观，原物品保留。命中或碰撞后返回。\n\n功法独立升级增强剑击，九层后也可补练。',
 'guide.xiuxian.new_22_left':'基础养剑：驭剑／横扫。\n清风：驭剑／踏锋步。\n磐石：驭剑／护身。\n长河：驭剑／连锋。\n\n斜线前为一层页，后为六层页。',
 'guide.xiuxian.new_22_right':'破锋：贯虹／破甲。\n回澜：驭剑／回锋。\n孤锋：驭剑／蓄势。\n流光：驭剑／分光。\n\n一至三层残篇没有六层页。归剑不占书页。',
 'guide.xiuxian.new_23_left':'剑修与术修共用功法等级规则：随吐纳独立成长，角色升层额外加级。\n\n等级强化剑击伤害和护身持续时间。\n\n换不同功法清零；完整篇和同部残篇互换保留练度。',
 'guide.xiuxian.new_23_right':'原有独立剑诀书与已学剑诀作为旧存档兼容保留，已从常规制作和创造栏入口下架。\n\n新角色只需主修剑经、参悟书页，旧知识不会被删除。\n\n技能冷却不会因转修刷新。',
 'guide.xiuxian.new_25_left':'术修功法内有修炼篇和两页术法。右键打开，点击主修，一层和六层分别参悟对应术法。\n\n剑修也采用同样的书页流程。\n\n所有学习都保留原书。',
 'guide.xiuxian.manuals':'功法与战斗\n\n剑修、术修都用一本功法修炼并参悟战斗技法。\n\n一层、六层解锁书页，残篇按覆盖范围限制。\n\nK查看等级；换不同功法清零，补全同部保留。\n\n书底“修行”与“术修”有详细说明。'
 };
 for(const lang of ['zh_cn','en_us']){const obj=JSON.parse(fs.readFileSync(`src/main/resources/${a}/lang/${lang}.json`,'utf8'));Object.assign(obj,zh);json(`${a}/lang/${lang}.json`,obj);}
};
