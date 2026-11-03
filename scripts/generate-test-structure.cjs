// Empty 5x5x5 GameTest structure, encoded as compressed NBT.
const fs=require('fs'),zlib=require('zlib');
const name=s=>{const b=Buffer.from(s);const n=Buffer.alloc(2);n.writeUInt16BE(b.length);return Buffer.concat([n,b]);};
const int=n=>{const b=Buffer.alloc(4);b.writeInt32BE(n);return b;};
const tag=(type,key,data)=>Buffer.concat([Buffer.from([type]),name(key),data]);
const list=(key,type,entries)=>tag(9,key,Buffer.concat([Buffer.from([type]),int(entries.length),...entries]));
const nbt=Buffer.concat([Buffer.from([10,0,0]),tag(3,'DataVersion',int(3465)),list('size',3,[int(5),int(5),int(5)]),list('palette',10,[Buffer.concat([tag(8,'Name',name('minecraft:air')),Buffer.from([0])])]),list('blocks',10,[]),list('entities',10,[]),Buffer.from([0])]);
fs.mkdirSync('src/main/resources/data/xiuxian/structures',{recursive:true});fs.writeFileSync('src/main/resources/data/xiuxian/structures/empty.nbt',zlib.gzipSync(nbt));
