from pathlib import Path
import json
from PIL import Image,ImageDraw
root=Path('src/main/resources')
def save(path,data):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def texture(path,image):
 p=root/f'assets/xiuxian/textures/{path}.png';p.parent.mkdir(parents=True,exist_ok=True);image.save(p)
def model(id):save(f'assets/xiuxian/models/item/{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'xiuxian:item/{id}'}})
def ingredient(id):return {'item':id}
def shapeless(id,items,count=1):save(f'data/xiuxian/recipes/{id}.json',{'type':'minecraft:crafting_shapeless','ingredients':[ingredient(i) for i in items],'result':{'item':f'xiuxian:{id}','count':count}})
def shaped(id,pattern,keys):save(f'data/xiuxian/recipes/{id}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':keys,'result':{'item':f'xiuxian:{id}'}})
ids=['fire_talisman','ice_talisman','thunder_talisman','armor_talisman','healing_talisman','swiftness_talisman','rockbreaking_talisman','heavenly_eye_talisman','waterward_talisman']
colors=['#b83520','#326b9b','#8054ae','#91622b','#429247','#6d8a3f','#785548','#6c57a4','#398f95']
for index,(id,color) in enumerate(zip(ids,colors)):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((4,0,11,15),fill='#8a6130');d.rectangle((5,1,10,14),fill='#edcf72');d.line((5,2,10,2),fill=color);d.line((7,3,8,12),fill=color,width=1)
 for j in range(4):
  y=4+j*2;dx=2 if (index>>j)&1 else -2;d.line((7,y,7+dx,y+1),fill=color);d.point((9 if (index+j)%2 else 5,y),fill=color)
 texture('item/'+id,im);model(id)
for id,color in [('cinnabar','#b33c36'),('spirit_cinnabar','#f35d73'),('spirit_stone_dust','#c6f1de')]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(2,12),(5,7),(7,8),(10,4),(13,10),(14,13),(3,14)],fill=color);d.line((4,11,10,6),fill='#f4d8be');d.point((9,11),fill='#fff1dd');texture('item/'+id,im);model(id)
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((3,1,12,14),fill='#a37e45');d.rectangle((4,2,11,13),fill='#ecdca2');d.line((5,4,10,4),fill='#d0b982');texture('item/blank_talisman_paper',im);model('blank_talisman_paper')
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.line((3,13,11,5),fill='#543d27',width=3);d.line((4,12,10,6),fill='#b99b61');d.line((10,5,12,3),fill='#d6b55e',width=3);d.polygon([(11,3),(14,0),(15,1),(13,5)],fill='#9eb3a4');texture('item/wolf_hair_brush',im);model('wolf_hair_brush')
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),fill='#777b7a')
for y in range(16):
 for x in range(16):
  if (x*7+y*11)%9<3:d.point((x,y),fill='#626767')
for x,y in [(3,4),(9,2),(11,10),(4,12),(8,7)]:d.rectangle((x,y,x+2,y+1),fill='#832b2c');d.point((x,y),fill='#d16453')
texture('block/cinnabar_ore',im)
save('assets/xiuxian/models/block/cinnabar_ore.json',{'parent':'minecraft:block/cube_all','textures':{'all':'xiuxian:block/cinnabar_ore'}})
save('assets/xiuxian/models/item/cinnabar_ore.json',{'parent':'xiuxian:block/cinnabar_ore'})
save('assets/xiuxian/blockstates/cinnabar_ore.json',{'variants':{'':{'model':'xiuxian:block/cinnabar_ore'}}})
save('data/xiuxian/loot_tables/blocks/cinnabar_ore.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'xiuxian:cinnabar_ore','conditions':[{'condition':'minecraft:match_tool','predicate':{'enchantments':[{'enchantment':'minecraft:silk_touch','levels':{'min':1}}]}}]},{'type':'minecraft:item','name':'xiuxian:cinnabar'}]}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
# Wooden table with visible legs, paper and a raised brush, all texture regions at native 16px.
for name,base in [('talisman_table_wood','#735333'),('talisman_table_top','#977446')]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),fill=base)
 for y in (3,7,11,15):d.line((0,y,15,y),fill='#513c28')
 if name.endswith('top'):
  d.rectangle((4,2,10,13),fill='#e4cb89');d.line((7,3,7,11),fill='#a13b2d');d.line((5,6,9,8),fill='#a13b2d');d.line((12,3,13,12),fill='#4f3828')
 texture('block/'+name,im)
tex={'wood':'xiuxian:block/talisman_table_wood','top':'xiuxian:block/talisman_table_top','particle':'xiuxian:block/talisman_table_wood','paper':'xiuxian:item/blank_talisman_paper','brush':'xiuxian:item/wolf_hair_brush'}
def box(start,end,t):return {'from':start,'to':end,'faces':{side:{'texture':'#'+(t if side=='up' else 'wood')} for side in ['up','down','north','south','east','west']}}
elements=[box([0,11,0],[16,14,16],'top')]
for x,z in [(1,1),(12,1),(1,12),(12,12)]:elements.append(box([x,0,z],[x+3,11,z+3],'wood'))
elements +=[box([4,14,2],[10,14.2,13],'paper'),box([12,14,3],[13,14.7,12],'brush')]
save('assets/xiuxian/models/block/talisman_table.json',{'textures':tex,'elements':elements})
save('assets/xiuxian/models/item/talisman_table.json',{'parent':'xiuxian:block/talisman_table','display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}})
save('assets/xiuxian/blockstates/talisman_table.json',{'variants':{f'facing={direction}':{'model':'xiuxian:block/talisman_table','y':angle} for direction,angle in [('north',0),('east',90),('south',180),('west',270)]}})
save('data/xiuxian/loot_tables/blocks/talisman_table.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'xiuxian:talisman_table'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
shapeless('spirit_stone_dust',['xiuxian:spirit_stone'],9)
shapeless('blank_talisman_paper',['minecraft:paper']*3+['xiuxian:spirit_stone_dust'],3)
shapeless('spirit_cinnabar',['xiuxian:cinnabar','xiuxian:spirit_stone_dust'])
shaped('wolf_hair_brush',[' H ',' C ','SB '],{'H':ingredient('xiuxian:wolf_brush_hair'),'C':ingredient('minecraft:copper_ingot'),'S':ingredient('xiuxian:spirit_stone'),'B':ingredient('minecraft:bamboo')})
shaped('talisman_table',['WWW','PSP',' C '],{'W':{'tag':'minecraft:wooden_slabs'},'P':ingredient('xiuxian:blank_talisman_paper'),'S':ingredient('xiuxian:spirit_stone'),'C':ingredient('minecraft:crafting_table')})
save('data/xiuxian/worldgen/configured_feature/cinnabar_ore.json',{'type':'xiuxian:cinnabar_ore','config':{}})
save('data/xiuxian/worldgen/placed_feature/cinnabar_ore.json',{'feature':'xiuxian:cinnabar_ore','placement':[{'type':'minecraft:count','count':4},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':-32},'max_inclusive':{'absolute':64}}},{'type':'minecraft:biome'}]})
save('data/xiuxian/forge/biome_modifier/cinnabar_ore.json',{'type':'forge:add_features','biomes':'#minecraft:is_overworld','features':'xiuxian:cinnabar_ore','step':'underground_ores'})
for tag,id in [('mineable/pickaxe','cinnabar_ore'),('needs_stone_tool','cinnabar_ore'),('mineable/axe','talisman_table')]:
 p=root/f'data/minecraft/tags/blocks/{tag}.json';data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v=f'xiuxian:{id}';data['values']=list(dict.fromkeys(data['values']+[v]));save(f'data/minecraft/tags/blocks/{tag}.json',data)
for id,color in [('talisman_shield','#dab85b'),('talisman_swiftness','#c9d889'),('talisman_heavenly_eye','#c3a3ed')]:
 im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im);d.ellipse((2,2,15,15),outline=color,width=2);d.line((9,4,9,13),fill=color,width=2);d.line((5,8,13,8),fill=color);texture('mob_effect/'+id,im)
print('Talisman assets generated')
