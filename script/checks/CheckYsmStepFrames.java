import java.util.*;import java.util.concurrent.*;import java.lang.reflect.*;import java.nio.file.*;import java.util.jar.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.joml.Vector3f;
import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmStepFrames;
public class CheckYsmStepFrames {
 static Constructor<?> scalar,vector,frame;static Class<?> expr;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Object vec(float x)throws Exception{return vector.newInstance(scalar.newInstance(x),scalar.newInstance(2f),scalar.newInstance(3f));}
 static Object make(float duration,float x)throws Exception{return frame.newInstance(duration,vec(x),vec(x+1));}
 public static void main(String[]a)throws Exception{
  String p="com.elfmcys.yesstevemodel.";expr=Class.forName(p+"o0oOo00o0oOOOOOo00o0O0oo");Class<?> vc=Class.forName(p+"O0o00O0oO000O0oOOO0OoOoo"),fc=Class.forName(p+"O0o0o00O00oOOoOo0OOo0O0o");scalar=Class.forName(p+"o000OOo0ooo00o0OOOOOOO0O").getConstructor(float.class);vector=vc.getConstructor(expr,expr,expr);frame=fc.getConstructor(float.class,vc,vc);
  Object first=GTYsmStepFrames.share(make(2,3));for(int i=0;i<100000;i++)ok(GTYsmStepFrames.share(make(2,3))==first,"sharing");
  Method eval=Arrays.stream(fc.getMethods()).filter(m->m.getReturnType()==Vector3f.class&&m.getParameterCount()==2).findFirst().orElseThrow();
  Object original=make(2,3);for(float t:new float[]{0f,.5f,.99999f,1f}){Vector3f x=(Vector3f)eval.invoke(original,null,t),y=(Vector3f)eval.invoke(first,null,t);ok(x.equals(y),"interpolation "+t);y.x=999;ok(((Vector3f)eval.invoke(first,null,t)).equals(x),"result isolation");}
  ok(GTYsmStepFrames.share(make(0f,3))!=GTYsmStepFrames.share(make(-0f,3)),"duration signed zero");
  ok(GTYsmStepFrames.share(make(2,3))!=GTYsmStepFrames.share(make(3,3)),"duration included");
  for(int i=0;i<2;i++){Object[] vs={vec(1),vec(2)};Object dynamic=Proxy.newProxyInstance(expr.getClassLoader(),new Class<?>[]{expr},(o,m,v)->7f);vs[i]=vector.newInstance(dynamic,scalar.newInstance(2f),scalar.newInstance(3f));Object f=frame.newInstance(2f,vs[0],vs[1]);ok(GTYsmStepFrames.share(f)==f,"dynamic vector slot "+i);}
  for(float x:new float[]{Float.NaN,Float.intBitsToFloat(0x7fc00123)}){Object f=make(x,3);ok(GTYsmStepFrames.share(f)==f,"NaN bypass");}
  for(int slot=0;slot<2;slot++){Object[] vs={vec(1),vec(2)};Object f1=frame.newInstance(2f,vs[0],vs[1]);vs[slot]=vec(99);Object f2=frame.newInstance(2f,vs[0],vs[1]);ok(GTYsmStepFrames.share(f1)!=GTYsmStepFrames.share(f2),"key includes slot "+slot);}
  Method initial=Arrays.stream(fc.getMethods()).filter(m->m.getReturnType()==Vector3f.class&&m.getParameterCount()==1).findFirst().orElseThrow();Vector3f initialValue=(Vector3f)initial.invoke(first,new Object[]{null});ok(initialValue.equals(new Vector3f(3,2,3)),"initial value");initialValue.x=999;ok(((Vector3f)initial.invoke(first,new Object[]{null})).x==3,"initial isolation");
  ok(((Vector3f)eval.invoke(first,null,.99999f)).x==3,"step before endpoint");ok(((Vector3f)eval.invoke(first,null,1f)).x==4,"step endpoint");
  for(Class<?> c=fc;c!=Object.class;c=c.getSuperclass())for(var f:c.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers()))ok(Modifier.isFinal(f.getModifiers()),"immutable hierarchy");
  var pool=Executors.newFixedThreadPool(8);var jobs=new ArrayList<Future<?>>();for(int i=0;i<8;i++)jobs.add(pool.submit(()->{try{for(int k=0;k<5000;k++)ok(GTYsmStepFrames.share(make(2,3))==first,"concurrent");}catch(Exception e){throw new RuntimeException(e);}}));for(var job:jobs)job.get();pool.shutdown();
  for(int i=0;i<10000;i++)GTYsmStepFrames.share(make(i+100,3));var field=GTYsmStepFrames.class.getDeclaredField("FRAMES");field.setAccessible(true);ok(((Map<?,?>)field.get(null)).size()==4096,"cap");Object f=make(999999,3);ok(GTYsmStepFrames.share(f)==f,"full fallback");ok(GTYsmStepFrames.share(null)==null,"null");
  var mixin=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(a[1]))).accept(mixin,0);var hook=mixin.methods.stream().filter(m->m.name.equals("gt$shareStepFrame")).findFirst().orElseThrow();var ann=hook.visibleAnnotations.stream().filter(n->n.desc.endsWith("/Inject;")).findFirst().orElseThrow();String selector=null;for(int i=0;i<ann.values.size();i+=2)if(ann.values.get(i).equals("method"))selector=(String)((List<?>)ann.values.get(i+1)).getFirst();
  try(var jar=new JarFile(a[0])){var target=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("com/elfmcys/yesstevemodel/OO0o0ooO0oOoOO00o0O0OOo0.class"))).accept(target,0);String s=selector;var m=target.methods.stream().filter(n->(n.name+n.desc).equals(s)).findFirst().orElseThrow();int sites=0;for(var n:m.instructions)if(n instanceof TypeInsnNode type&&type.getOpcode()==Opcodes.NEW&&type.desc.equals(fc.getName().replace('.','/')))sites++;ok(sites==1,"compiled exact selector single NEW");}
  System.out.println("PASS: real YSM100k step sharing/interpolation/fresh results, signed duration, both key slots/dynamic vector slots, NaN bypass, immutable hierarchy, 8threads,4096cap,compiled selector/single upstream NEW");
 }
}
