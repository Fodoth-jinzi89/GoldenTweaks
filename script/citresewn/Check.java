import net.fodoth.skina.goldentweaks.compat.citresewn.CITLazyCacheASM;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarFile;

public class Check extends ClassLoader {
    public static final Function<Object,Object> FACTORY = value -> value;
    public static class Cache {
        static int created;
        final Function<Object,Object> factory;
        public Cache(Function<Object,Object> factory) { this.factory = factory; created++; }
    }
    static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
    static void verify(ClassNode node) throws Exception {
        for (MethodNode method : node.methods)
            new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
    }
    public static void main(String[] args) throws Exception {
        // Verify the patch against the actual pack's fully merged ItemStack.
        ClassNode actual = read(Files.readAllBytes(Path.of(args[0])));
        boolean eager = actual.methods.stream().filter(m -> m.name.equals("<init>")).anyMatch(m -> { for (AbstractInsnNode n : m.instructions) if (n instanceof FieldInsnNode f && f.getOpcode() == Opcodes.PUTFIELD && f.name.startsWith("citresewn$cacheType")) return true; return false; });
        if (eager) CITLazyCacheASM.patch(actual);
        verify(actual);
        for (MethodNode method : actual.methods) {
            if (!method.name.equals("<init>")) continue;
            for (AbstractInsnNode instruction : method.instructions)
                if (instruction instanceof FieldInsnNode f && f.name.startsWith("citresewn$cacheType"))
                    throw new AssertionError("Constructor still allocates a CIT cache");
        }
        ClassNode harness = new ClassNode();
        harness.version = Opcodes.V21;
        harness.access = Opcodes.ACC_PUBLIC;
        harness.name = "LazyStack";
        harness.superName = "java/lang/Object";
        MethodNode init = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false));
        // Use the four original upstream mixins, not a hand-written lazy cache model.
        try (JarFile jar = new JarFile(args[1])) {
            for (String type : List.of("item", "enchantment", "armor", "elytra")) {
                String path = "shcm/shsupercm/fabric/citresewn/defaults/mixin/types/"+type+"/ItemStackMixin.class";
                ClassNode source = read(jar.getInputStream(jar.getJarEntry(path)).readAllBytes());
                for (FieldNode f : source.fields) harness.fields.add(f);
                for (MethodNode m : source.methods) {
                    if (m.name.equals("<init>")) {
                        AbstractInsnNode start = m.instructions.getFirst();
                        while (start.getOpcode()!=Opcodes.ALOAD) start=start.getNext();
                        AbstractInsnNode call=start.getNext();
                        while(call.getOpcode()<0)call=call.getNext();
                        m.instructions.remove(start);m.instructions.remove(call);
                        for(AbstractInsnNode n:m.instructions.toArray())
                            if(n.getOpcode()==Opcodes.RETURN)m.instructions.remove(n);
                        init.instructions.add(m.instructions);
                    } else if (m.name.startsWith("citresewn$getCache")) harness.methods.add(m);
                }
                for (MethodNode m : harness.methods)
                    for (AbstractInsnNode n : m.instructions)
                        if(n instanceof FieldInsnNode f) f.owner=harness.name;
            }
        }
        init.instructions.add(new InsnNode(Opcodes.RETURN));
        init.maxStack=5;init.maxLocals=1;harness.methods.add(init);
        for(AbstractInsnNode n:init.instructions)
            if(n instanceof FieldInsnNode f && f.getOpcode()==Opcodes.PUTFIELD)f.owner=harness.name;
        CITLazyCacheASM.patch(harness);
        // Isolate Minecraft/CIT runtime dependencies after applying the real patch.
        String desc="LCheck$Cache;";
        for(FieldNode f:harness.fields){f.desc=desc;f.signature=null;}
        for(MethodNode m:harness.methods){
            m.signature=null;
            if(!m.name.equals("<init>"))m.desc="()"+desc;
            for(AbstractInsnNode n:m.instructions.toArray()){
                if(n instanceof TypeInsnNode t && t.getOpcode()==Opcodes.NEW)t.desc="Check$Cache";
                if(n instanceof MethodInsnNode c && c.owner.contains("CITCache$"))c.owner="Check$Cache";
                if(n instanceof FieldInsnNode f){
                    if(f.getOpcode()==Opcodes.GETSTATIC){f.owner="Check";f.name="FACTORY";f.desc="Ljava/util/function/Function;";}
                    else f.desc=desc;
                }
                if(n instanceof InvokeDynamicInsnNode){
                    // Original bound factory is replaced with an identity function for the isolated check.
                    m.instructions.insertBefore(n,new InsnNode(Opcodes.POP));
                    m.instructions.set(n,new FieldInsnNode(Opcodes.GETSTATIC,"Check","FACTORY","Ljava/util/function/Function;"));
                }
            }
        }
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        harness.accept(writer);
        Class<?> stack=new Check().defineClass("LazyStack",writer.toByteArray(),0,writer.toByteArray().length);
        Object first=stack.getConstructor().newInstance(), second=stack.getConstructor().newInstance();
        if(Cache.created!=0)throw new AssertionError("Eager allocation");
        for(String type:List.of("Item","Enchantment","Armor","Elytra")){
            var getter=stack.getMethod("citresewn$getCacheType"+type);
            int before=Cache.created;
            Object cache=getter.invoke(first);
            if(Cache.created!=before+1 || getter.invoke(first)!=cache)throw new AssertionError("Cache not reused");
            if(getter.invoke(second)==cache)throw new AssertionError("Cache shared between stacks");
            Object token=new Object();
            if(((Cache)cache).factory.apply(token)!=token)throw new AssertionError("Factory changed");
        }
        if(Cache.created!=8)throw new AssertionError("Unexpected allocations");
        Object raced=stack.getConstructor().newInstance();
        var getter=stack.getMethod("citresewn$getCacheTypeItem");
        var start=new java.util.concurrent.CountDownLatch(1);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(8);
        var futures=new ArrayList<java.util.concurrent.Future<Object>>();
        try {
            for(int i=0;i<8;i++) futures.add(pool.submit(() -> { start.await();return getter.invoke(raced); }));
            start.countDown();
            Object cache=futures.getFirst().get();
            for(var future:futures)if(future.get()!=cache)throw new AssertionError("Concurrent cache identity changed");
            if(Cache.created!=9)throw new AssertionError("Concurrent duplicate allocation");
        } finally { pool.shutdownNow(); }
        try { CITLazyCacheASM.patch(new ClassNode());throw new AssertionError("Missing CIT should fail"); }
        catch(IllegalStateException expected){}
        System.out.println("PASS: real merged ItemStack verifies; 4 upstream initializers lazy; stable per-stack caches; factories preserved; no eager allocations");
    }
}

