param([string]$Jdk = 'C:\Program Files\BellSoft\LibericaJDK-21')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'build/maid-animation-check'
New-Item -ItemType Directory -Force $output | Out-Null
$maid = Get-ChildItem -LiteralPath (Join-Path $root 'libs/implementation') -Filter '*touhou_little_maid-*.jar' | Select-Object -First 1
$extra = Get-ChildItem -LiteralPath "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/io.github.llamalad7/mixinextras-neoforge/0.5.3" -Recurse -Filter 'mixinextras-neoforge-0.5.3.jar' | Select-Object -First 1
$asmRoot = "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/org.ow2.asm"
$asm = foreach ($module in @('asm', 'asm-tree')) {
    Get-ChildItem -LiteralPath (Join-Path $asmRoot $module) -Recurse -Filter "$module-9.8.jar" | Select-Object -First 1 -ExpandProperty FullName
}
$fastutil = Get-ChildItem -LiteralPath "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/8.5.12" -Recurse -Filter 'fastutil-8.5.12.jar' | Select-Object -First 1
$guava = Get-ChildItem -LiteralPath "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/com.google.guava/guava/33.0.0-jre" -Recurse -Filter 'guava-33.0.0-jre.jar' | Select-Object -First 1
$joml = Get-ChildItem -LiteralPath "$env:GRADLE_USER_HOME/caches/modules-2/files-2.1/org.joml/joml" -Recurse -Filter 'joml-*.jar' | Where-Object Name -NotLike '*sources*' | Select-Object -First 1
$classpath = (@((Join-Path $root 'build/classes/java/main'), $maid.FullName, $extra.FullName, $fastutil.FullName, $joml.FullName, $guava.FullName) + $asm) -join ';'
@'
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.DoubleValue;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.IValue;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.Vector3v;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GTAnimationConstants;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class Check {
    static int allocations;
    public static void main(String[] args) throws Exception {
        Operation<com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.TransitionKeyFrame> transitionFactory = a -> { allocations++;return new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.TransitionKeyFrame((double)a[0],(Vector3v)a[1],(Vector3v)a[2]); };
        Operation<Vector3v> vf=a->new Vector3v((IValue)a[0],(IValue)a[1],(IValue)a[2]);
        IValue[] axes={DoubleValue.ZERO,DoubleValue.ONE,GTAnimationConstants.reuse(-0.0,a->new DoubleValue((double)a[0]))};
        Vector3v[] fixed=new Vector3v[27];
        for(int i=0;i<27;i++)fixed[i]=GTAnimationConstants.reuseVector(axes[i/9],axes[i/3%3],axes[i%3],vf);
        int allocationStart=allocations;
        for(Vector3v begin:fixed)for(Vector3v post:fixed) {
            var shared=GTAnimationConstants.reuseTransition(0,begin,post,transitionFactory);
            for(int i=0;i<1000;i++)if(GTAnimationConstants.reuseTransition(0,begin,post,transitionFactory)!=shared)throw new AssertionError("Transition not shared");
            var upstream=new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.TransitionKeyFrame(0,begin,post);
            for(double t:new double[]{-1,0,0.5,0.99999,1,2,Double.NaN}) {
                var expected=upstream.getLerpPoint(null,t);var actual=shared.getLerpPoint(null,t);
                for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(expected.get(axis))!=Float.floatToRawIntBits(actual.get(axis)))throw new AssertionError("Transition evaluation changed");
                if(actual==shared.getLerpPoint(null,t))throw new AssertionError("Mutable evaluation shared");
            }
        }
        if(allocations-allocationStart!=729)throw new AssertionError("Transition allocation count");
        for(double duration:new double[]{-0.0,1,-1,Double.NaN,Double.POSITIVE_INFINITY}) {
            var a=GTAnimationConstants.reuseTransition(duration,fixed[0],fixed[0],transitionFactory);
            var b=GTAnimationConstants.reuseTransition(duration,fixed[0],fixed[0],transitionFactory);
            if(a!=b || Double.doubleToRawLongBits(a.getTotalTick())!=Double.doubleToRawLongBits(duration))throw new AssertionError("Duration sharing changed");
        }
        var unpooled=new Vector3v(DoubleValue.ZERO,DoubleValue.ZERO,DoubleValue.ZERO);
        if(GTAnimationConstants.reuseTransition(0,unpooled,fixed[0],transitionFactory)==GTAnimationConstants.reuseTransition(0,unpooled,fixed[0],transitionFactory))throw new AssertionError("Unpooled vector shared");
        String framePrefix="com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/";
        for(String name:new String[]{"BoneKeyFrame","TransitionKeyFrame"}) {
            ClassNode node=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream(framePrefix+name+".class")).accept(node,0);
            for(FieldNode field:node.fields)if((field.access&Opcodes.ACC_STATIC)==0 && (field.access&Opcodes.ACC_FINAL)==0)throw new AssertionError("Mutable frame field");
            for(MethodNode method:node.methods)if(!method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)if(insn.getOpcode()==Opcodes.PUTFIELD)throw new AssertionError("Frame mutation");
        }
        ClassNode easing=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream(framePrefix+"EasingType.class")).accept(easing,0);
        int transitionSites=0;
        for(MethodNode method:easing.methods)if(method.name.equals("buildTransitionKeyFrame") && (method.access&Opcodes.ACC_STATIC)!=0)for(AbstractInsnNode insn:method.instructions)
            if(insn instanceof MethodInsnNode m && m.owner.equals(framePrefix+"TransitionKeyFrame") && m.name.equals("<init>") && m.desc.equals("(DL"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;)V"))transitionSites++;
        if(transitionSites!=1)throw new AssertionError("Transition constructor site changed");
        Operation<com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.LinearKeyFrame> linearFactory=a->new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.LinearKeyFrame((double)a[0],(double)a[1],(Vector3v)a[2],(Vector3v)a[3],(Vector3v)a[4]);
        var linear=GTAnimationConstants.reuseLinear(2.5,3.75,fixed[0],fixed[13],fixed[26],linearFactory);
        for(int i=0;i<100000;i++)if(GTAnimationConstants.reuseLinear(2.5,3.75,fixed[0],fixed[13],fixed[26],linearFactory)!=linear)throw new AssertionError("Linear frame not shared");
        var linearOriginal=linearFactory.call(2.5,3.75,fixed[0],fixed[13],fixed[26]);
        for(double t:new double[]{-1,0,0.00001,0.25,0.5,0.99999,1,2,Double.NaN}) {
            var actual=linear.getLerpPoint(null,t);var expected=linearOriginal.getLerpPoint(null,t);
            for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(actual.get(axis))!=Float.floatToRawIntBits(expected.get(axis)))throw new AssertionError("Linear lerp bits");
            if(actual==linear.getLerpPoint(null,t))throw new AssertionError("Linear mutable result shared");
            var from=new org.joml.Vector3f(2,4,6);
            var a=linear.getTransitionPoint(null,new org.joml.Vector3f(from),t);
            var b=linearOriginal.getTransitionPoint(null,new org.joml.Vector3f(from),t);
            for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(a.get(axis))!=Float.floatToRawIntBits(b.get(axis)))throw new AssertionError("Linear transition bits");
        }
        double[] exactTimes={0,-0.0,1,-1,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.longBitsToDouble(0x7ff8000000000042L),Double.longBitsToDouble(0x7ff8000000000043L)};
        var timeFrames=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for(double start:exactTimes)for(double duration:exactTimes) {
            var f=GTAnimationConstants.reuseLinear(start,duration,fixed[0],fixed[0],fixed[0],linearFactory);
            if(!timeFrames.add(f) || Double.doubleToRawLongBits(f.getStartTick())!=Double.doubleToRawLongBits(start) || Double.doubleToRawLongBits(f.getTotalTick())!=Double.doubleToRawLongBits(duration))throw new AssertionError("Linear time bits lost");
        }
        if(GTAnimationConstants.reuseLinear(2.5,3.75,unpooled,fixed[13],fixed[26],linearFactory)==GTAnimationConstants.reuseLinear(2.5,3.75,unpooled,fixed[13],fixed[26],linearFactory))throw new AssertionError("Unrecognized vector pooled");
        var varied=GTAnimationConstants.reuseVector(new DoubleValue(0.125),new DoubleValue(0.25),new DoubleValue(0.5),vf);
        var variedFrame=GTAnimationConstants.reuseLinear(1,2,varied,varied,varied,linearFactory);
        if(variedFrame!=GTAnimationConstants.reuseLinear(1,2,varied,varied,varied,linearFactory))throw new AssertionError("Nontrivial vector not accepted");
        var workers=new java.util.ArrayList<Thread>();var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        for(int i=0;i<8;i++){var worker=new Thread(()->{try{for(int j=0;j<10000;j++)if(GTAnimationConstants.reuseLinear(2.5,3.75,fixed[0],fixed[13],fixed[26],linearFactory)!=linear)throw new AssertionError("Linear publication");}catch(Throwable e){failure.set(e);}});workers.add(worker);worker.start();}
        for(var worker:workers)worker.join();if(failure.get()!=null)throw new AssertionError(failure.get());
        var linearCacheField=GTAnimationConstants.class.getDeclaredField("LINEAR_FRAMES");linearCacheField.setAccessible(true);
        var linearCache=(java.util.Map<?,?>)linearCacheField.get(null);
        int linearLimit=net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.MAID_ANIMATION_LINEAR_CACHE_LIMIT;
        for(int i=0;i<linearLimit+10;i++)GTAnimationConstants.reuseLinear(i+1000,1,fixed[0],fixed[0],fixed[0],linearFactory);
        if(linearCache.size()!=linearLimit || GTAnimationConstants.reuseLinear(2.5,3.75,fixed[0],fixed[13],fixed[26],linearFactory)!=linear)throw new AssertionError("Linear cache cap");
        if(GTAnimationConstants.reuseLinear(-100,1,fixed[0],fixed[0],fixed[0],linearFactory)==GTAnimationConstants.reuseLinear(-100,1,fixed[0],fixed[0],fixed[0],linearFactory))throw new AssertionError("Full linear cache fallback");
        ClassNode linearNode=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream(framePrefix+"LinearKeyFrame.class")).accept(linearNode,0);
        for(FieldNode field:linearNode.fields)if((field.access&Opcodes.ACC_STATIC)==0 && (field.access&Opcodes.ACC_FINAL)==0)throw new AssertionError("Mutable linear field");
        for(MethodNode method:linearNode.methods)if(!method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)if(insn.getOpcode()==Opcodes.PUTFIELD)throw new AssertionError("Linear mutation");
        int linearSites=0;
        for(MethodNode method:easing.methods)if(method.name.equals("buildLinearKeyFrame"))for(AbstractInsnNode insn:method.instructions)
            if(insn instanceof MethodInsnNode m && m.owner.equals(framePrefix+"LinearKeyFrame") && m.name.equals("<init>") && m.desc.equals("(DDL"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;)V"))linearSites++;
        if(linearSites!=1)throw new AssertionError("Linear constructor site changed");
        System.out.println("PASS: 100k linear sharing, raw time bits/NaN payloads, actual lerp/transition, vector fallback, 8-thread publication, bounded pool, immutable fields and upstream constructor site");
        Operation<com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.CatmullRomKeyFrame> catFactory=a->new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.CatmullRomKeyFrame((double)a[0],(double)a[1],(Vector3v)a[2],(Vector3v)a[3],(Vector3v)a[4],(Vector3v)a[5],(Vector3v)a[6]);
        var cat=GTAnimationConstants.reuseCatmull(2.5,3.75,fixed[0],fixed[13],varied,fixed[26],fixed[1],catFactory);
        for(int i=0;i<100000;i++)if(GTAnimationConstants.reuseCatmull(2.5,3.75,fixed[0],fixed[13],varied,fixed[26],fixed[1],catFactory)!=cat)throw new AssertionError("Catmull not shared");
        var catOriginal=catFactory.call(2.5,3.75,fixed[0],fixed[13],varied,fixed[26],fixed[1]);
        for(double t:new double[]{-1,0,0.00001,0.25,0.5,0.99999,1,2,Double.NaN}) {
            var actual=cat.getLerpPoint(null,t);var expected=catOriginal.getLerpPoint(null,t);
            for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(actual.get(axis))!=Float.floatToRawIntBits(expected.get(axis)))throw new AssertionError("Catmull lerp bits");
            if(actual==cat.getLerpPoint(null,t))throw new AssertionError("Catmull mutable result shared");
            var a=cat.getTransitionPoint(null,new org.joml.Vector3f(2,4,6),t);
            var b=catOriginal.getTransitionPoint(null,new org.joml.Vector3f(2,4,6),t);
            for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(a.get(axis))!=Float.floatToRawIntBits(b.get(axis)))throw new AssertionError("Catmull transition bits");
        }
        var catTimes=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for(double start:exactTimes)for(double duration:exactTimes){
            var f=GTAnimationConstants.reuseCatmull(start,duration,fixed[0],fixed[0],fixed[0],fixed[0],fixed[0],catFactory);
            if(!catTimes.add(f) || Double.doubleToRawLongBits(f.getStartTick())!=Double.doubleToRawLongBits(start) || Double.doubleToRawLongBits(f.getTotalTick())!=Double.doubleToRawLongBits(duration))throw new AssertionError("Catmull time bits lost");
        }
        for(int slot=0;slot<5;slot++){
            Vector3v[] v={fixed[0],fixed[13],varied,fixed[26],fixed[1]};v[slot]=unpooled;
            if(GTAnimationConstants.reuseCatmull(2.5,3.75,v[0],v[1],v[2],v[3],v[4],catFactory)==GTAnimationConstants.reuseCatmull(2.5,3.75,v[0],v[1],v[2],v[3],v[4],catFactory))throw new AssertionError("Catmull unrecognized slot "+slot);
            v[slot]=fixed[2];
            if(GTAnimationConstants.reuseCatmull(2.5,3.75,v[0],v[1],v[2],v[3],v[4],catFactory)==cat)throw new AssertionError("Catmull key slot missing "+slot);
        }
        workers.clear();failure.set(null);
        for(int i=0;i<8;i++){var worker=new Thread(()->{try{for(int j=0;j<10000;j++)if(GTAnimationConstants.reuseCatmull(2.5,3.75,fixed[0],fixed[13],varied,fixed[26],fixed[1],catFactory)!=cat)throw new AssertionError("Catmull publication");}catch(Throwable e){failure.set(e);}});workers.add(worker);worker.start();}
        for(var worker:workers)worker.join();if(failure.get()!=null)throw new AssertionError(failure.get());
        var catCacheField=GTAnimationConstants.class.getDeclaredField("CATMULL_FRAMES");catCacheField.setAccessible(true);
        var catCache=(java.util.Map<?,?>)catCacheField.get(null);
        int catLimit=net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.MAID_ANIMATION_CATMULL_CACHE_LIMIT;
        for(int i=0;i<catLimit+10;i++)GTAnimationConstants.reuseCatmull(i+1000,1,fixed[0],fixed[0],fixed[0],fixed[0],fixed[0],catFactory);
        if(catCache.size()!=catLimit || GTAnimationConstants.reuseCatmull(2.5,3.75,fixed[0],fixed[13],varied,fixed[26],fixed[1],catFactory)!=cat)throw new AssertionError("Catmull cap");
        if(GTAnimationConstants.reuseCatmull(-100,1,fixed[0],fixed[0],fixed[0],fixed[0],fixed[0],catFactory)==GTAnimationConstants.reuseCatmull(-100,1,fixed[0],fixed[0],fixed[0],fixed[0],fixed[0],catFactory))throw new AssertionError("Catmull full fallback");
        ClassNode catNode=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream(framePrefix+"CatmullRomKeyFrame.class")).accept(catNode,0);
        for(FieldNode field:catNode.fields)if((field.access&Opcodes.ACC_STATIC)==0 && (field.access&Opcodes.ACC_FINAL)==0)throw new AssertionError("Mutable Catmull field");
        for(MethodNode method:catNode.methods)if(!method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)if(insn.getOpcode()==Opcodes.PUTFIELD)throw new AssertionError("Catmull mutation");
        int catSites=0;
        for(MethodNode method:easing.methods)if(method.name.equals("buildCatmullRomKeyFrame"))for(AbstractInsnNode insn:method.instructions)
            if(insn instanceof MethodInsnNode m && m.owner.equals(framePrefix+"CatmullRomKeyFrame") && m.name.equals("<init>") && m.desc.equals("(DDL"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;L"+framePrefix+"Vector3v;)V"))catSites++;
        if(catSites!=1)throw new AssertionError("Catmull constructor site changed");
        System.out.println("PASS: 100k Catmull sharing; all five vector key/fallback slots; raw time bits, real interpolation/transition, independent results, 8 threads, bounded pool and upstream immutability/site");
        var numericTransition=GTAnimationConstants.reuseTransition(2.5,varied,fixed[13],transitionFactory);
        for(int i=0;i<100000;i++)if(GTAnimationConstants.reuseTransition(2.5,varied,fixed[13],transitionFactory)!=numericTransition)throw new AssertionError("Numeric transition sharing");
        var numericOriginal=new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.TransitionKeyFrame(2.5,varied,fixed[13]);
        for(double t:new double[]{-1,0,0.5,0.99999,1,2,Double.NaN}) {
            var actual=numericTransition.getLerpPoint(null,t);var expected=numericOriginal.getLerpPoint(null,t);
            for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(actual.get(axis))!=Float.floatToRawIntBits(expected.get(axis)))throw new AssertionError("Numeric transition eval");
            if(actual==numericTransition.getLerpPoint(null,t))throw new AssertionError("Numeric transition mutable result");
        }
        var transitionTimes=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for(double duration:exactTimes){
            var f=GTAnimationConstants.reuseTransition(duration,varied,varied,transitionFactory);
            if(!transitionTimes.add(f) || Double.doubleToRawLongBits(f.getTotalTick())!=Double.doubleToRawLongBits(duration))throw new AssertionError("Transition duration bits");
        }
        if(GTAnimationConstants.reuseTransition(2.5,unpooled,fixed[13],transitionFactory)==GTAnimationConstants.reuseTransition(2.5,unpooled,fixed[13],transitionFactory) || GTAnimationConstants.reuseTransition(2.5,varied,unpooled,transitionFactory)==GTAnimationConstants.reuseTransition(2.5,varied,unpooled,transitionFactory))throw new AssertionError("Transition unrecognized vector fallback");
        if(GTAnimationConstants.reuseTransition(2.5,fixed[13],varied,transitionFactory)==numericTransition)throw new AssertionError("Transition point order");
        workers.clear();failure.set(null);
        for(int i=0;i<8;i++){var worker=new Thread(()->{try{for(int j=0;j<10000;j++)if(GTAnimationConstants.reuseTransition(2.5,varied,fixed[13],transitionFactory)!=numericTransition)throw new AssertionError("Transition publication");}catch(Throwable e){failure.set(e);}});workers.add(worker);worker.start();}
        for(var worker:workers)worker.join();if(failure.get()!=null)throw new AssertionError(failure.get());
        var transitionCacheField=GTAnimationConstants.class.getDeclaredField("NUMERIC_TRANSITIONS");transitionCacheField.setAccessible(true);
        var transitionCache=(java.util.Map<?,?>)transitionCacheField.get(null);
        int transitionLimit=net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.MAID_ANIMATION_TRANSITION_CACHE_LIMIT;
        for(int i=0;i<transitionLimit+10;i++)GTAnimationConstants.reuseTransition(i+1000,varied,varied,transitionFactory);
        if(transitionCache.size()!=transitionLimit || GTAnimationConstants.reuseTransition(2.5,varied,fixed[13],transitionFactory)!=numericTransition)throw new AssertionError("Transition cap");
        if(GTAnimationConstants.reuseTransition(-100,varied,varied,transitionFactory)==GTAnimationConstants.reuseTransition(-100,varied,varied,transitionFactory))throw new AssertionError("Transition full fallback");
        System.out.println("PASS: 100k numeric transition sharing, exact duration bits, real eval, point order, unrecognized vectors, 8 threads, bounded pool and full fallback");
        allocations=0;
        System.out.println("PASS: 729 fixed transitions shared over 729k requests; exact eval bits and independent results; signed-duration sharing/unpooled fallback; upstream immutable fields and constructor site");
        String sharedName=GTAnimationConstants.reuseBoneName(new String("LeftForeArm"));
        for(int i=0;i<100000;i++)if(GTAnimationConstants.reuseBoneName(new String("LeftForeArm"))!=sharedName)throw new AssertionError("Bone name not shared");
        for(String name:new String[]{"leftforearm","骨骼\u0000%s","","Tail37"})if(!GTAnimationConstants.reuseBoneName(new String(name)).equals(name))throw new AssertionError("Name changed");
        if(GTAnimationConstants.reuseBoneName(null)!=null)throw new AssertionError("Null name changed");
        ClassNode boneAnimation=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream("com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/BoneAnimation.class")).accept(boneAnimation,0);
        long constructors=boneAnimation.methods.stream().filter(m->m.name.equals("<init>") && m.desc.equals("(Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/List;)V")).count();
        if(constructors!=1)throw new AssertionError("Bone name constructor changed");
        System.out.println("PASS: 100k equal bone names shared; case/Unicode/empty/null preserved; upstream constructor verified");
        var emptyA=GTAnimationConstants.reuseEmptyFrames(new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.BoneKeyFrame[0]);
        var emptyB=GTAnimationConstants.reuseEmptyFrames(new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.BoneKeyFrame[0]);
        if(emptyA!=emptyB)throw new AssertionError("Empty frames not shared");
        var listA=it.unimi.dsi.fastutil.objects.ReferenceArrayList.wrap(emptyA);
        var listB=it.unimi.dsi.fastutil.objects.ReferenceArrayList.wrap(emptyB);
        var frame=new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.LinearKeyFrame(0,1,new Vector3v(DoubleValue.ZERO,DoubleValue.ZERO,DoubleValue.ZERO),null,null);
        listA.add(frame);
        if(listA.getFirst()!=frame || !listB.isEmpty() || emptyA.length!=0)throw new AssertionError("Empty channel mutation leaked");
        var nonempty=new com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.BoneKeyFrame[]{frame};
        if(GTAnimationConstants.reuseEmptyFrames(nonempty)!=nonempty)throw new AssertionError("Nonempty frames changed");
        Operation<DoubleValue> original = values -> { allocations++;return new DoubleValue((double)values[0]); };
        for(int i=0;i<100000;i++) {
            if(GTAnimationConstants.reuse(0.0,original)!=DoubleValue.ZERO)throw new AssertionError("Zero not shared");
            if(GTAnimationConstants.reuse(1.0,original)!=DoubleValue.ONE)throw new AssertionError("One not shared");
        }
        if(allocations!=0)throw new AssertionError("Redundant allocations");
        DoubleValue negativeZero=GTAnimationConstants.reuse(-0.0,original);
        if(Double.doubleToRawLongBits(negativeZero.value())!=Long.MIN_VALUE || negativeZero==DoubleValue.ZERO)throw new AssertionError("Signed zero lost");
        for(int i=0;i<100000;i++)if(GTAnimationConstants.reuse(-0.0,original)!=negativeZero)throw new AssertionError("Negative zero not shared");
        if(allocations!=0)throw new AssertionError("Negative zero allocated");
        double[] values={-1,0.5,Double.MIN_VALUE,Double.MAX_VALUE,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.longBitsToDouble(0x7ff8000000000042L),Double.longBitsToDouble(0x7ff8000000000043L)};
        for(double value:values) {
            DoubleValue result=GTAnimationConstants.reuse(value,original);
            if(Double.doubleToRawLongBits(result.value())!=Double.doubleToRawLongBits(value))throw new AssertionError("Changed bits");
            for(int i=0;i<10000;i++)if(GTAnimationConstants.reuse(value,original)!=result)throw new AssertionError("Nonzero constant not shared");
        }
        if(allocations!=values.length)throw new AssertionError("Fallback factory bypassed");
        Operation<Vector3v> vectorFactory = args2 -> { allocations++;return new Vector3v((IValue)args2[0],(IValue)args2[1],(IValue)args2[2]); };
        var xField=Vector3v.class.getDeclaredField("x");xField.setAccessible(true);
        var yField=Vector3v.class.getDeclaredField("y");yField.setAccessible(true);
        var zField=Vector3v.class.getDeclaredField("z");zField.setAccessible(true);
        java.util.Set<Vector3v> vectors=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        int before=allocations;
        IValue[] constants={DoubleValue.ZERO,DoubleValue.ONE,negativeZero};
        for(int bits=0;bits<27;bits++) {
            IValue x=constants[bits/9];
            IValue y=constants[bits/3%3];
            IValue z=constants[bits%3];
            Vector3v vector=GTAnimationConstants.reuseVector(x,y,z,vectorFactory);vectors.add(vector);
            if(xField.get(vector)!=x || yField.get(vector)!=y || zField.get(vector)!=z)throw new AssertionError("Vector axis changed");
            for(int i=0;i<10000;i++)if(GTAnimationConstants.reuseVector(x,y,z,vectorFactory)!=vector)throw new AssertionError("Vector not shared");
        }
        if(vectors.size()!=27 || allocations!=before)throw new AssertionError("Wrong vector cache");
        IValue dynamic = evaluator -> 0.0;
        for(IValue special:new IValue[]{new DoubleValue(-0.0),new DoubleValue(0),new DoubleValue(2),dynamic}) {
            for(int axis=0;axis<3;axis++) {
                IValue[] components={DoubleValue.ZERO,DoubleValue.ONE,DoubleValue.ZERO};components[axis]=special;
                Vector3v vector=GTAnimationConstants.reuseVector(components[0],components[1],components[2],vectorFactory);
                if(xField.get(vector)!=components[0] || yField.get(vector)!=components[1] || zField.get(vector)!=components[2])throw new AssertionError("Expression identity lost");
            }
        }
        if(allocations!=before+12)throw new AssertionError("Vector fallback bypassed");
        DoubleValue vx=GTAnimationConstants.reuse(-0.015882495790719986,original);
        DoubleValue vy=GTAnimationConstants.reuse(0.3216641843318939,original);
        Vector3v numeric=GTAnimationConstants.reuseVector(vx,vy,negativeZero,vectorFactory);
        before=allocations;
        for(int i=0;i<100000;i++)if(GTAnimationConstants.reuseVector(vx,vy,negativeZero,vectorFactory)!=numeric)
            throw new AssertionError("Numeric vector not shared");
        if(allocations!=before || xField.get(numeric)!=vx || yField.get(numeric)!=vy || zField.get(numeric)!=negativeZero)
            throw new AssertionError("Numeric axes/allocation changed");
        DoubleValue equalX=new DoubleValue(vx.value());
        Vector3v separate=GTAnimationConstants.reuseVector(equalX,vy,negativeZero,vectorFactory);
        if(separate==numeric || xField.get(separate)!=equalX)throw new AssertionError("Distinct axis identity lost");
        IValue subclass=new DoubleValue(2) {};
        if(GTAnimationConstants.reuseVector(subclass,vy,negativeZero,vectorFactory)==GTAnimationConstants.reuseVector(subclass,vy,negativeZero,vectorFactory))
            throw new AssertionError("Subclass was cached");
        if(GTAnimationConstants.reuseVector(dynamic,vy,negativeZero,vectorFactory)==GTAnimationConstants.reuseVector(dynamic,vy,negativeZero,vectorFactory))
            throw new AssertionError("Dynamic expression was cached");
        var evaluated=numeric.eval(null);
        if(Float.floatToRawIntBits(evaluated.x)!=(Float.floatToRawIntBits((float)vx.value()))
                || evaluated.y!=(float)vy.value() || Float.floatToRawIntBits(evaluated.z)!=Integer.MIN_VALUE)
            throw new AssertionError("Vector evaluation changed");
        evaluated.x=999;
        if(numeric.eval(null).x!=(float)vx.value())throw new AssertionError("Evaluation result shared");
        var vectorCacheField=GTAnimationConstants.class.getDeclaredField("NUMERIC_VECTORS");vectorCacheField.setAccessible(true);
        var vectorCache=(java.util.Map<?,?>)vectorCacheField.get(null);
        int vectorLimit=net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.MAID_ANIMATION_VECTOR_CACHE_LIMIT;
        var vectorPool=java.util.concurrent.Executors.newFixedThreadPool(8);
        var vectorFutures=new java.util.ArrayList<java.util.concurrent.Future<Vector3v>>();
        var vectorCount=new java.util.concurrent.atomic.AtomicInteger();
        DoubleValue threadX=new DoubleValue(0.123);
        Operation<Vector3v> concurrentVectorFactory = a -> {vectorCount.incrementAndGet();return new Vector3v((IValue)a[0],(IValue)a[1],(IValue)a[2]);};
        try {
            for(int i=0;i<64;i++)vectorFutures.add(vectorPool.submit(()->GTAnimationConstants.reuseVector(threadX,vy,negativeZero,concurrentVectorFactory)));
            Vector3v shared=vectorFutures.get(0).get();
            for(var future:vectorFutures)if(future.get()!=shared)throw new AssertionError("Concurrent vector sharing failed");
        } finally {vectorPool.shutdownNow();}
        if(vectorCount.get()!=1)throw new AssertionError("Concurrent duplicate vector");
        for(int i=0;i<vectorLimit+100;i++)GTAnimationConstants.reuseVector(new DoubleValue(i+5),vy,negativeZero,vectorFactory);
        if(vectorCache.size()!=vectorLimit || GTAnimationConstants.reuseVector(vx,vy,negativeZero,vectorFactory)!=numeric)
            throw new AssertionError("Vector capacity/existing entry lost");
        DoubleValue uncached=new DoubleValue(12345);
        if(GTAnimationConstants.reuseVector(uncached,vy,negativeZero,vectorFactory)==GTAnimationConstants.reuseVector(uncached,vy,negativeZero,vectorFactory))
            throw new AssertionError("Full vector pool did not fall back");
        var cacheField=GTAnimationConstants.class.getDeclaredField("CONSTANTS");cacheField.setAccessible(true);
        var cache=(it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<?>)cacheField.get(null);
        int limit=net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig.MAID_ANIMATION_CONSTANT_CACHE_LIMIT;
        var pool=java.util.concurrent.Executors.newFixedThreadPool(8);
        var futures=new java.util.ArrayList<java.util.concurrent.Future<DoubleValue>>();
        var count=new java.util.concurrent.atomic.AtomicInteger();
        Operation<DoubleValue> concurrentFactory = a -> {count.incrementAndGet();return new DoubleValue((double)a[0]);};
        try {
            for(int i=0;i<64;i++)futures.add(pool.submit(()->GTAnimationConstants.reuse(0.654321987,concurrentFactory)));
            DoubleValue shared=futures.get(0).get();
            for(var future:futures)if(future.get()!=shared)throw new AssertionError("Concurrent sharing failed");
        } finally {pool.shutdownNow();}
        if(count.get()!=1)throw new AssertionError("Concurrent duplicate allocation");
        for(int i=0;i<limit+100;i++) {
            double value=i+10.25;
            if(GTAnimationConstants.reuse(value,original).value()!=value)throw new AssertionError("Bounded cache changed value");
        }
        if(cache.size()!=limit)throw new AssertionError("Cache exceeds cap");
        DoubleValue kept=GTAnimationConstants.reuse(0.5,original);
        if(GTAnimationConstants.reuse(0.5,original)!=kept)throw new AssertionError("Full cache lost existing entry");
        before=allocations;
        if(GTAnimationConstants.reuse(999999.25,original)==GTAnimationConstants.reuse(999999.25,original) || allocations!=before+2)
            throw new AssertionError("Full cache did not use original factory");
        ClassNode scalar=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream("com/github/tartaricacid/touhoulittlemaid/geckolib3/core/molang/value/DoubleValue.class")).accept(scalar,0);
        for(FieldNode field:scalar.fields)if((field.access&Opcodes.ACC_STATIC)==0 && (field.access&Opcodes.ACC_FINAL)==0)
            throw new AssertionError("Upstream DoubleValue no longer immutable");
        for(MethodNode method:scalar.methods)if(!method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)
            if(insn.getOpcode()==Opcodes.PUTFIELD)throw new AssertionError("Upstream DoubleValue mutates");
        for(MethodNode method:scalar.methods)if(method.name.equals("equals") || method.name.equals("hashCode"))
            throw new AssertionError("DoubleValue no longer uses identity equality");
        ClassNode vectorClass=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream("com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/Vector3v.class")).accept(vectorClass,0);
        for(FieldNode field:vectorClass.fields)if((field.access&Opcodes.ACC_STATIC)==0 && (field.access&Opcodes.ACC_FINAL)==0)
            throw new AssertionError("Upstream Vector3v no longer immutable");
        for(MethodNode method:vectorClass.methods)if(!method.name.equals("<init>"))for(AbstractInsnNode insn:method.instructions)
            if(insn.getOpcode()==Opcodes.PUTFIELD)throw new AssertionError("Upstream Vector3v mutates");
        String prefix="com/github/tartaricacid/touhoulittlemaid/geckolib3/core/";
        ClassNode processor=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream(prefix+"keyframe/bone/BoneKeyFrameProcessor.class")).accept(processor,0);
        int wraps=0;
        for(MethodNode method:processor.methods)if(method.name.equals("process"))for(AbstractInsnNode insn:method.instructions)
            if(insn instanceof MethodInsnNode call && call.owner.equals("it/unimi/dsi/fastutil/objects/ReferenceArrayList") && call.name.equals("wrap") && call.desc.equals("([Ljava/lang/Object;)Lit/unimi/dsi/fastutil/objects/ReferenceArrayList;"))wraps++;
        if(wraps!=1)throw new AssertionError("Frame wrapping site changed");
        System.out.println("PASS: empty frame array shared; independent mutable wrappers and nonempty identity preserved; exact erased upstream wrap descriptor verified");
        for(String[] target:new String[][]{{"molang/MolangParser","getConstant","1","molang/value/DoubleValue"},{"keyframe/bone/RawBoneKeyFrame","getValue","2","molang/value/DoubleValue"},{"keyframe/bone/RawBoneKeyFrame","init","2","keyframe/bone/Vector3v"}}) {
            ClassNode node=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream(prefix+target[0]+".class")).accept(node,0);
            int sites=0;
            for(MethodNode method:node.methods)if(method.name.equals(target[1]))for(AbstractInsnNode n:method.instructions)
                if(n instanceof TypeInsnNode t && t.getOpcode()==Opcodes.NEW && t.desc.equals(prefix+target[3]))sites++;
            if(sites!=Integer.parseInt(target[2]))throw new AssertionError("Upstream allocation sites changed");
        }
        System.out.println("PASS: positive zero, negative zero and one shared without allocation; signed zero, NaN payload, infinities and other values preserved; all 3 upstream allocation sites verified");
        System.out.println("PASS: all 27 constant vectors shared; axes/expression identity and fallback preserved; 2 upstream vector sites verified");
        System.out.println("PASS: repeated nonzero constants and distinct NaN payloads shared exactly; 8-thread publication, bounded capacity, full-cache fallback and upstream immutability verified");
        System.out.println("PASS: nontrivial numeric vectors shared; axis identity, subclass/dynamic fallback, independent eval results, concurrency and bounded vector pool verified");
        var oldLinearResult=linear.getLerpPoint(null,0.5);
        GTAnimationConstants.clearNumericCaches();
        for(String name:new String[]{"CONSTANTS","NUMERIC_VECTORS","LINEAR_FRAMES","CATMULL_FRAMES","NUMERIC_TRANSITIONS"}) {
            var f=GTAnimationConstants.class.getDeclaredField(name);f.setAccessible(true);
            if(!((java.util.Map<?,?>)f.get(null)).isEmpty())throw new AssertionError("Reload retained "+name);
        }
        var identities=GTAnimationConstants.class.getDeclaredField("CONSTANT_VECTOR_IDENTITIES");identities.setAccessible(true);
        if(((java.util.Map<?,?>)identities.get(null)).size()!=27)throw new AssertionError("Fixed vector registry reset");
        var currentLinearResult=linear.getLerpPoint(null,0.5);
        for(int axis=0;axis<3;axis++)if(Float.floatToRawIntBits(oldLinearResult.get(axis))!=Float.floatToRawIntBits(currentLinearResult.get(axis)))throw new AssertionError("Old frame changed on reload");
        var newScalar=GTAnimationConstants.reuse(0.125,original);
        if(newScalar!=GTAnimationConstants.reuse(0.125,original))throw new AssertionError("Scalar did not refill");
        var newVector=GTAnimationConstants.reuseVector(newScalar,newScalar,newScalar,vf);
        if(newVector==varied || newVector!=GTAnimationConstants.reuseVector(newScalar,newScalar,newScalar,vf))throw new AssertionError("Vector refill");
        var newLinear=GTAnimationConstants.reuseLinear(2.5,3.75,fixed[0],fixed[13],fixed[26],linearFactory);
        if(newLinear==linear || newLinear!=GTAnimationConstants.reuseLinear(2.5,3.75,fixed[0],fixed[13],fixed[26],linearFactory))throw new AssertionError("Linear refill");
        var newCat=GTAnimationConstants.reuseCatmull(1,2,newVector,newVector,newVector,newVector,newVector,catFactory);
        if(newCat!=GTAnimationConstants.reuseCatmull(1,2,newVector,newVector,newVector,newVector,newVector,catFactory))throw new AssertionError("Catmull refill");
        var newTransition=GTAnimationConstants.reuseTransition(2.5,newVector,newVector,transitionFactory);
        if(newTransition!=GTAnimationConstants.reuseTransition(2.5,newVector,newVector,transitionFactory))throw new AssertionError("Transition refill");
        ClassNode loader=new ClassNode();new ClassReader(Check.class.getClassLoader().getResourceAsStream("com/github/tartaricacid/touhoulittlemaid/client/resource/GeckoModelLoader.class")).accept(loader,0);
        var reload=loader.methods.stream().filter(m->m.name.equals("reload") && m.desc.equals("()V") && (m.access&Opcodes.ACC_STATIC)!=0).findFirst().orElseThrow();
        var calls=new java.util.ArrayList<String>();
        for(AbstractInsnNode n:reload.instructions)if(n instanceof MethodInsnNode m)calls.add(m.name);
        if(!calls.equals(java.util.List.of("clearAllCache","loadDefaultAnimation")))throw new AssertionError("Upstream reload ordering changed");
        GTAnimationConstants.clearNumericCaches();GTAnimationConstants.clearNumericCaches();
        System.out.println("PASS: reload clears five pools and old identities, fixed registry survives, old frames unchanged, all pools refill; upstream reload ordering verified");
    }
}
'@ | Set-Content -LiteralPath (Join-Path $output 'Check.java') -Encoding utf8
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -cp $classpath -d $output (Join-Path $output 'Check.java')
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
& (Join-Path $Jdk 'bin/java.exe') -Xverify:all -cp "$output;$classpath" Check
if ($LASTEXITCODE -ne 0) { throw 'Animation constant check failed' }
