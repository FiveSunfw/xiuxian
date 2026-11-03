package dev.xiuxian;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.*;

public class BeastRenderer extends MobRenderer<SpiritBeast,BeastRenderer.Model> {
    private final ResourceLocation texture;
    public BeastRenderer(EntityRendererProvider.Context context,BeastKind kind){super(context,new Model(kind),kind.width*.45f);texture=new ResourceLocation(Xiuxian.ID,"textures/entity/"+kind.id+".png");}
    @Override protected void scale(SpiritBeast beast,com.mojang.blaze3d.vertex.PoseStack pose,float partial){if(beast.kind==BeastKind.KING)pose.scale(1.6f,1.6f,1.6f);}
    @Override public ResourceLocation getTextureLocation(SpiritBeast beast){return texture;}
    @Override protected float getWhiteOverlayProgress(SpiritBeast beast,float partial){return beast.phase()==1?.5f:0;}
    static class Model extends EntityModel<SpiritBeast> {
        final ModelPart root,head;
        final BeastKind kind;
        static void box(PartDefinition root,String name,float x,float y,float z,int w,int h,int d){root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(name.startsWith("fang")?0:name.equals("horn")?8:name.startsWith("tusk")?16:0,name.startsWith("fang")||name.equals("horn")||name.startsWith("tusk")?44:name.equals("armor")?40:name.equals("head")?24:0).addBox(-w/2f,-h/2f,-d/2f,w,h,d),PartPose.offset(x,y,z));}
        static LayerDefinition layer(BeastKind kind){
            MeshDefinition mesh=new MeshDefinition();var root=mesh.getRoot();
            boolean snake=kind==BeastKind.VIPER||kind==BeastKind.PYTHON;
            int bodyW=kind==BeastKind.KING?14:kind==BeastKind.PYTHON?12:kind==BeastKind.VIPER?5:kind==BeastKind.RABBIT?6:10;
            int bodyH=snake?4:kind==BeastKind.SPIDER?5:8;
            int bodyD=snake?14:kind==BeastKind.EAGLE?7:12;
            box(root,"body",0,18,0,bodyW,bodyH,bodyD);box(root,"head",0,kind==BeastKind.PYTHON?9:snake?17:kind==BeastKind.RABBIT?14:15,-bodyD/2f-3,kind==BeastKind.RABBIT?5:7,6,6);
            if(kind==BeastKind.PYTHON)box(root,"neck",0,14,-9,8,10,7);
            if(snake){box(root,"tail",0,20,bodyD/2f+5,5,3,10);}
            else if(kind==BeastKind.EAGLE){box(root,"left_wing",10,16,0,14,1,6);box(root,"right_wing",-10,16,0,14,1,6);box(root,"beak",0,16,-9,3,2,4);box(root,"tail",0,18,7,6,1,7);}
            else{
                int pairs=kind==BeastKind.SPIDER?4:2;
                for(int side=-1;side<=1;side+=2)for(int i=0;i<pairs;i++)box(root,"leg_"+side+"_"+i,side*(kind==BeastKind.SPIDER?9:bodyW/2f-1),kind==BeastKind.SPIDER?21:22,-4+i*(pairs==4?3:8),kind==BeastKind.SPIDER?10:3,kind==BeastKind.SPIDER?2:5,3);
            }
            if(kind==BeastKind.RABBIT){box(root,"ear_l",2,8,-8,2,9,2);box(root,"ear_r",-2,8,-8,2,9,2);box(root,"horn",0,9,-10,1,5,1);}
            if(kind==BeastKind.BOAR||kind==BeastKind.KING){box(root,"armor",0,12,0,bodyW+2,3,11);box(root,"tusk_l",4,16,-10,2,5,2);box(root,"tusk_r",-4,16,-10,2,5,2);}
            if(kind==BeastKind.WOLF){box(root,"ear_l",3,10,-8,2,4,2);box(root,"ear_r",-3,10,-8,2,4,2);box(root,"tail",0,15,9,3,3,7);box(root,"fang_l",2,18,-10,1,3,1);box(root,"fang_r",-2,18,-10,1,3,1);}
            if(kind==BeastKind.LIZARD)for(int i=0;i<4;i++)box(root,"crest_"+i,0,12,-3+i*3,2,5,2);
            return LayerDefinition.create(mesh,64,64);
        }
        Model(BeastKind kind){this.kind=kind;root=layer(kind).bakeRoot();head=root.getChild("head");}
        @Override public void setupAnim(SpiritBeast beast,float walk,float amount,float age,float yaw,float pitch){
            head.yRot=yaw*.0174533f;head.xRot=pitch*.0174533f;
            if(kind==BeastKind.EAGLE){root.getChild("left_wing").zRot=beast.phase()==2?-.9f:(float)Math.sin(age*.5)*.6f;root.getChild("right_wing").zRot=beast.phase()==2?.9f:-(float)Math.sin(age*.5)*.6f;}
            else if(kind==BeastKind.VIPER||kind==BeastKind.PYTHON)root.getChild("tail").yRot=(float)Math.sin(walk*.7)*amount;
            else for(int side=-1;side<=1;side+=2)for(int i=0;i<(kind==BeastKind.SPIDER?4:2);i++)root.getChild("leg_"+side+"_"+i).xRot=(float)Math.cos(walk*.8+i*Math.PI+side)*amount*.7f;
        }
        @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,float r,float g,float b,float alpha){root.render(pose,buffer,light,overlay,r,g,b,alpha);}
    }
}
