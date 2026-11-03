const fs=require('fs');
module.exports=function(){
 const source=fs.readFileSync('src/main/java/dev/xiuxian/ManualDefinition.java','utf8');
 const base=[...source.matchAll(/(sword|spell)\("([a-z_]+)", "([^"]+)"(?:, (\d+))?/g)].map(m=>({id:m[2],name:m[3],coverage:m[1]==='sword'?Number(m[4]):9}));
 return base.flatMap(m=>{const title=m.name.replace('·炼气篇','').replace('·残篇','');return [{...m,family:m.id},{id:m.id+(m.coverage===9?'_fragment_3':'_complete'),name:title+(m.coverage===9?'·一至三层残篇':'·炼气篇'),coverage:m.coverage===9?3:9,family:m.id},{id:m.id+'_fragment_6',name:title+'·一至六层残篇',coverage:6,family:m.id}];});
};
