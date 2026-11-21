import java.util.*;import java.util.concurrent.*;import java.lang.reflect.*;import java.nio.file.*;import java.util.jar.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
import net.fodoth.skina.goldentweaks.compat.yesstevemodel.GTYsmVectors;
import net.fodoth.skina.goldentweaks.mixin.fix.yesstevemodel.AnimationValuesMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Vector3f;
public class CheckYsmVectors {
 static void ok(boolean b,String s){if(!b)throw new AssertionError(s);}
 static Constructor<?> scalar,vector;static Object make(float x,float y,float z)throws Exception {return vector.newInstance(scalar.newInstance(x),scalar.newInstance(y),scalar.newInstance(z));}
 public static void main(String[]a)throws Exception {
  String pkg="com.elfmcys.yesstevemodel.";Class<?> expr=Class.forName(pkg+"o0oOo00o0oOOOOOo00o0O0oo");Class<?> vc=Class.forName(pkg+"O0o00O0oO000O0oOOO0OoOoo");Class<?> sc=Class.forName(pkg+"o000OOo0ooo00o0OOOOOOO0O");scalar=sc.getConstructor(float.class);vector=vc.getConstructor(expr,expr,expr);
  Object first=GTYsmVectors.share(make(1,2,3));for(int i=0;i<100000;i++)ok(GTYsmVectors.share(make(1,2,3))==first,"sharing");
  Method eval=Arrays.stream(vc.getMethods()).filter(m->m.getReturnType()==Vector3f.class).findFirst().orElseThrow();
  Vector3f one=(Vector3f)eval.invoke(first,new Object[]{null});one.x=999;Vector3f two=(Vector3f)eval.invoke(first,new Object[]{null});ok(one!=two&&two.equals(new Vector3f(1,2,3)),"evaluation results isolated");
  ok(GTYsmVectors.share(make(0,2,3))!=GTYsmVectors.share(make(-0f,2,3)),"signed zero");
  for(float x:new float[]{Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.MIN_VALUE,Float.MAX_VALUE}) {Object v=GTYsmVectors.share(make(x,2,3));Vector3f f=(Vector3f)eval.invoke(v,new Object[]{null});ok(Float.floatToRawIntBits(f.x)==Float.floatToRawIntBits(x),"raw bits");}
  Object dynamic=Proxy.newProxyInstance(expr.getClassLoader(),new Class<?>[]{expr},(p,m,v)->m.getReturnType()==float.class?7f:null);
  for(int i=0;i<3;i++){Object[] xyz={scalar.newInstance(1f),scalar.newInstance(2f),scalar.newInstance(3f)};xyz[i]=dynamic;Object v=vector.newInstance(xyz);ok(GTYsmVectors.share(v)==v,"dynamic fallback slot "+i);}
  Class<?> bc=Class.forName(pkg+"oOO0000OO00oO0000OooO0O0");Object builder=bc.getConstructor().newInstance();
  Field left=bc.getDeclaredField("O0OO0O0o00o0o00oOoO0o0oO"),right=bc.getDeclaredField("oo000ooO0O00oo0OO0ooO000");left.setAccessible(true);right.setAccessible(true);
  left.set(builder,make(1,2,3));right.set(builder,make(1,2,3));GTYsmVectors.compact(builder);ok(left.get(builder)==first&&right.get(builder)==first,"both builder slots");GTYsmVectors.compact(builder);ok(left.get(builder)==first,"idempotence");
  left.set(builder,null);GTYsmVectors.compact(builder);ok(left.get(builder)==null,"null safe");
  for(var f:vc.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers()))ok(Modifier.isFinal(f.getModifiers())&&f.getType()==expr,"immutable triple");
  var pool=Executors.newFixedThreadPool(8);var tasks=new ArrayList<Future<?>>();for(int t=0;t<8;t++)tasks.add(pool.submit(()->{try{for(int i=0;i<5000;i++)ok(GTYsmVectors.share(make(1,2,3))==first,"parallel");}catch(Exception e){throw new RuntimeException(e);}}));for(var f:tasks)f.get();pool.shutdown();
  for(int i=0;i<20000;i++)GTYsmVectors.share(make(i+100,2,3));var field=GTYsmVectors.class.getDeclaredField("VECTORS");field.setAccessible(true);ok(((Map<?,?>)field.get(null)).size()==16384,"bound");Object fallback=make(999999,2,3);ok(GTYsmVectors.share(fallback)==fallback,"full fallback");
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(a[1]))).accept(node,0);
  var hook=node.methods.stream().filter(m->m.name.equals("gt$shareNumericVectors")).findFirst().orElseThrow();var inject=hook.visibleAnnotations.stream().filter(x->x.desc.endsWith("/Inject;")).findFirst().orElseThrow();String selector=null;
  for(int i=0;i<inject.values.size();i+=2)if(inject.values.get(i).equals("method"))selector=(String)((List<?>)inject.values.get(i+1)).getFirst();
  try(var jar=new JarFile(a[0])){var target=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(bc.getName().replace('.','/')+".class"))).accept(target,0);String select=selector;var method=target.methods.stream().filter(m->(m.name+m.desc).equals(select)).findFirst().orElseThrow();int news=0;for(var n:method.instructions)if(n instanceof TypeInsnNode type&&type.getOpcode()==Opcodes.NEW&&type.desc.equals(vc.getName().replace('.','/')))news++;ok(news==2,"upstream two vectors before return hook");}
  System.out.println("PASS: actual YSM 100k triples, fresh mutable eval results, raw bits, every dynamic slot, both builder slots, idempotence/null, 8 threads, cap/fallback, compiled selector + two upstream NEW sites");
 }
}

