import java.util.*;import java.util.concurrent.*;import java.lang.reflect.*;import java.nio.file.*;import java.util.jar.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmConstants;
public class CheckYsm {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[]a)throws Exception {
  Class<?> c=Class.forName("com.elfmcys.yesstevemodel.o000OOo0ooo00o0OOOOOOO0O");var ctor=c.getConstructor(float.class);var number=c.getMethod("oOo0OO0O0o000OO0O000oo0o");
  Object first=GTYsmConstants.share(ctor.newInstance(12.5f));for(int i=0;i<100000;i++)ok(GTYsmConstants.share(ctor.newInstance(12.5f))==first,"sharing");
  for(float f:new float[]{0f,-0f,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.MIN_VALUE,Float.MAX_VALUE}) {
   Object v=GTYsmConstants.share(ctor.newInstance(f));ok(Float.floatToRawIntBits((float)number.invoke(v))==Float.floatToRawIntBits(f),"raw bits");
  }
  ok(GTYsmConstants.share(ctor.newInstance(0f))!=GTYsmConstants.share(ctor.newInstance(-0f)),"signed zero");
  Object nan=GTYsmConstants.share(ctor.newInstance(Float.NaN));ok((float)number.invoke(nan)==0f,"upstream NaN normalization");
  Object dynamic=new Object();ok(GTYsmConstants.share(dynamic)==dynamic&&GTYsmConstants.share(null)==null,"fallback");
  var pool=Executors.newFixedThreadPool(8);var tasks=new ArrayList<Future<?>>();for(int t=0;t<8;t++)tasks.add(pool.submit(()->{try{for(int i=0;i<10000;i++)ok(GTYsmConstants.share(ctor.newInstance(12.5f))==first,"concurrent");}catch(Exception e){throw new RuntimeException(e);}}));for(var task:tasks)task.get();pool.shutdown();
  for(int i=0;i<50000;i++)GTYsmConstants.share(ctor.newInstance((float)(i+100)));
  var field=GTYsmConstants.class.getDeclaredField("CONSTANTS");field.setAccessible(true);ok(((Map<?,?>)field.get(null)).size()==32768,"bounded");
  Object uncached=ctor.newInstance(999999f);ok(GTYsmConstants.share(uncached)==uncached,"full fallback");
  for(var f:c.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers()))ok(f.getType()==float.class&&Modifier.isFinal(f.getModifiers()),"immutable scalar");
  try(var jar=new JarFile(a[0])) {
   for(String mixin:new String[]{"NumericParserMixin","AnimationValuesMixin"}) {
    var cn=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(a[1],mixin+".class"))).accept(cn,0);
    var ann=cn.invisibleAnnotations.stream().filter(x->x.desc.endsWith("/Mixin;")).findFirst().orElseThrow();String target=null;
    for(int i=0;i<ann.values.size();i+=2)if(ann.values.get(i).equals("targets"))target=(String)((List<?>)ann.values.get(i+1)).getFirst();
    var targetNode=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(target.replace('.','/')+".class"))).accept(targetNode,0);
    var handler=cn.methods.stream().filter(m->m.name.equals("gt$shareConstant")).findFirst().orElseThrow();var inj=handler.visibleAnnotations.stream().filter(x->x.desc.endsWith("/Inject;")).findFirst().orElseThrow();String selector=null;
    for(int i=0;i<inj.values.size();i+=2)if(inj.values.get(i).equals("method"))selector=(String)((List<?>)inj.values.get(i+1)).getFirst();
    String select=selector;ok(targetNode.methods.stream().filter(m->(m.name+m.desc).equals(select)).count()==1,"one exact selector "+mixin);
   }
  }
  System.out.println("PASS: actual YSM immutable scalar, 100k sharing, signed-zero/infinities/NaN behavior, dynamic fallback, 8 threads, 32768 cap, two compiled exact target descriptors");
 }
}
