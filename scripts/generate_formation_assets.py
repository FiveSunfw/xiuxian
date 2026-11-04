from pathlib import Path
from PIL import Image,ImageDraw
import json
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
def save(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def tex(name,im):
 p=ROOT/f'assets/xiuxian/textures/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def item(id):save(f'assets/xiuxian/models/item/{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'xiuxian:item/{id}'}})
def ingredient(id):return {'item':id}
def recipe(id,pattern,keys,count=1):save(f'data/xiuxian/recipes/{id}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':keys,'result':{'item':f'xiuxian:{id}','count':count}})
def blockloot(id):save(f'data/xiuxian/loot_tables/blocks/{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'xiuxian:{id}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for id,color in [('formation_heart_stone','#c2ede2'),('gathering_formation_token','#5ccfc6'),('guardian_formation_token','#ebc568'),('assault_formation_token','#de6657')]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(7,0),(13,4),(12,12),(8,15),(3,12),(2,4)],fill='#394e50');d.polygon([(7,1),(12,5),(11,11),(8,14),(4,11),(3,5)],fill=color);d.line((5,5,10,5),fill='#f0e1b0');d.line((8,3,8,12),fill='#466c68',width=1)
 if id.startswith('gathering'):d.ellipse((5,6,10,11),outline='#ebffff')
 elif id.startswith('guardian'):d.rectangle((5,7,10,11),outline='#fff0ca')
 elif id.startswith('assault'):d.line((5,10,10,6),fill='#fff1c9',width=2)
 else:d.line((5,9,10,7),fill='#ffffff',width=2)
 tex('item/'+id,im);item(id)
for id,color in [('formation_plate_side','#606b68'),('formation_plate_top','#717e77'),('formation_plate_active','#538c87'),('formation_flag','#b89f6a')]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),fill=color)
 if 'top' in id or 'active' in id:
  line='#b2e6ce' if 'active' in id else '#b5bba0';d.ellipse((1,1,14,14),outline=line,width=1);d.rectangle((4,4,11,11),outline=line);d.line((1,8,14,8),fill=line);d.line((8,1,8,14),fill=line)
 elif id=='formation_flag':
  d.rectangle((1,1,14,14),outline='#614b2e');d.line((8,2,8,13),fill='#7a3b32',width=2);d.line((4,6,11,10),fill='#7a3b32');d.point((11,4),fill='#d4efda')
 else:
  for y in (2,7,12):d.line((0,y,15,y),fill='#45514f')
 tex('block/'+id,im)
def box(a,b,texture):return {'from':a,'to':b,'faces':{f:{'texture':'#'+texture} for f in ['up','down','north','south','east','west']}}
for active in (False,True):
 id='formation_plate_active' if active else 'formation_plate'
 e=box([0,0,0],[16,8,16],'side');e['faces']['up']={'texture':'#top'}
 save(f'assets/xiuxian/models/block/{id}.json',{'textures':{'particle':'xiuxian:block/formation_plate_side','side':'xiuxian:block/formation_plate_side','top':'xiuxian:block/'+('formation_plate_active' if active else 'formation_plate_top')},'elements':[e]})
save('assets/xiuxian/blockstates/formation_plate.json',{'variants':{'lit=false':{'model':'xiuxian:block/formation_plate'},'lit=true':{'model':'xiuxian:block/formation_plate_active'}}})
save('assets/xiuxian/models/item/formation_plate.json',{'parent':'xiuxian:block/formation_plate','display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}})
save('assets/xiuxian/models/block/formation_flag.json',{'textures':{'particle':'xiuxian:block/formation_flag','flag':'xiuxian:block/formation_flag','wood':'minecraft:block/stripped_oak_log'},'elements':[box([6,0,6],[10,2,10],'wood'),box([7,2,7],[9,16,9],'wood'),box([3,7,7],[13,15,8],'flag')]})
save('assets/xiuxian/blockstates/formation_flag.json',{'variants':{'':{'model':'xiuxian:block/formation_flag'}}})
save('assets/xiuxian/models/item/formation_flag.json',{'parent':'xiuxian:block/formation_flag','display':{'gui':{'rotation':[20,225,0],'translation':[0,0,0],'scale':[.8,.8,.8]}}})
blockloot('formation_plate');blockloot('formation_flag')
save('data/xiuxian/recipes/formation_heart_stone.json',{'type':'minecraft:crafting_shapeless','ingredients':[ingredient('xiuxian:spirit_stone_tier_2')]*4,'result':{'item':'xiuxian:formation_heart_stone'}})
recipe('formation_plate',['SCS','GTG','S S'],{'S':[ingredient('minecraft:smooth_stone'),ingredient('minecraft:deepslate_bricks')],'C':ingredient('xiuxian:spirit_cinnabar'),'G':ingredient('xiuxian:spirit_gold_ingot'),'T':ingredient('xiuxian:spirit_stone_tier_2')})
recipe('formation_flag',['PSP',' S ','GT '],{'P':ingredient('xiuxian:blank_talisman_paper'),'S':ingredient('minecraft:stick'),'G':ingredient('xiuxian:spirit_gold_ingot'),'T':ingredient('xiuxian:spirit_stone')},4)
for id,special in [('gathering_formation_token','minecraft:amethyst_shard'),('guardian_formation_token','minecraft:obsidian')]:recipe(id,['GAK','AHA','KAG'],{'G':ingredient('xiuxian:spirit_gold_ingot'),'A':ingredient(special),'K':ingredient('minecraft:gold_ingot'),'H':ingredient('xiuxian:formation_heart_stone')})
recipe('assault_formation_token',['Q Q','CSC','QHQ'],{'Q':ingredient('minecraft:quartz'),'C':ingredient('xiuxian:cinnabar'),'S':ingredient('xiuxian:spirit_gold_sword'),'H':ingredient('xiuxian:formation_heart_stone')})
save('data/xiuxian/damage_type/formation_assault.json',{'message_id':'formation_assault','scaling':'never','exhaustion':.1,'effects':'hurt'})
for tag in ['bypasses_armor','bypasses_cooldown']:save(f'data/minecraft/tags/damage_type/{tag}.json',{'replace':False,'values':['xiuxian:formation_assault']})
for tag,id in [('mineable/pickaxe','formation_plate'),('mineable/axe','formation_flag')]:
 path=f'data/minecraft/tags/blocks/{tag}.json';p=ROOT/path;data=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]};data['values']=list(dict.fromkeys(data['values']+['xiuxian:'+id]));save(path,data)
print('Formation assets generated')

# Spacious headless acceptance arena keeps large fields inside their test bounds.
import gzip,struct
def nstr(value):
 raw=value.encode('utf-8');return struct.pack('>H',len(raw))+raw
def tag(kind,name,payload):return bytes([kind])+nstr(name)+payload
arena=tag(3,'DataVersion',struct.pack('>i',3465))+tag(9,'size',bytes([3])+struct.pack('>iiii',3,49,17,49))+tag(9,'palette',bytes([10])+struct.pack('>i',1)+tag(8,'Name',nstr('minecraft:air'))+b'\x00')+tag(9,'blocks',bytes([10])+struct.pack('>i',0))+tag(9,'entities',bytes([10])+struct.pack('>i',0))+b'\x00'
path=ROOT/'data/xiuxian/structures/formation_arena.nbt';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(gzip.compress(bytes([10])+nstr('')+arena,mtime=0))
