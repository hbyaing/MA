package cn.blockforge.generated.generatedmod.client.firecoil;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
public final class SupportBuildProbe {
 static void run(Path out)throws Exception {
  String name=SupportBounds.class.getName();byte[] bytes;
  try(var z=new java.util.zip.ZipFile("E:/五行仙途/deliverables/task-20261016-root-balance/wuxingxiantu-0.2-20261016-root-balance.jar")){
   bytes=z.getInputStream(z.getEntry(name.replace('.','/')+".class")).readAllBytes();
  }
  final byte[] code=bytes;
  var loader=new ClassLoader(SupportBounds.class.getClassLoader()){
   @Override protected Class<?> loadClass(String n,boolean resolve)throws ClassNotFoundException {
    if(!n.equals(name))return super.loadClass(n,resolve);
    var c=findLoadedClass(n);if(c==null)c=defineClass(n,code,0,code.length);if(resolve)resolveClass(c);return c;
   }
  };
  Class<?>[] types={loader.loadClass(name),SupportBounds.class};Object[] instances=new Object[2];Method[] methods=new Method[2];Field[] data=new Field[2],grids=new Field[2];
  for(int i=0;i<2;i++){
   var c=types[i].getDeclaredConstructor();c.setAccessible(true);instances[i]=c.newInstance();
   methods[i]=types[i].getDeclaredMethod("build",Recipe.class,float.class,boolean.class,int.class,float[].class,float[].class,float[].class);methods[i].setAccessible(true);
   data[i]=types[i].getDeclaredField("data");data[i].setAccessible(true);grids[i]=types[i].getDeclaredField("pointGrid");grids[i].setAccessible(true);
  }
  var recipe=Client.recipe(5,5);float[] origin={0,0,0,1.2f,0,0,0,0},axis={0,1,0,.04f,0,0,0,0},side={1,0,0,0,0,0,0,0};
  for(int sample=0;sample<40;sample++){
   for(int i=0;i<2;i++)methods[i].invoke(instances[i],recipe,sample*2.713f,sample%2==0,sample%2,origin,axis,side);
   if(!Arrays.equals((float[])data[0].get(instances[0]),(float[])data[1].get(instances[1]))||!Arrays.equals((byte[])grids[0].get(instances[0]),(byte[])grids[1].get(instances[1])))throw new AssertionError("CPU support output changed at "+sample);
  }
  var bean=(com.sun.management.ThreadMXBean)java.lang.management.ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);long id=Thread.currentThread().getId();
  var rows=new ArrayList<Map<String,Object>>();
  for(int i=0;i<2;i++){
   for(int j=0;j<80;j++)methods[i].invoke(instances[i],recipe,j*.071f,true,0,origin,axis,side);
   long allocated=bean.getThreadAllocatedBytes(id),start=System.nanoTime();
   for(int j=0;j<200;j++)methods[i].invoke(instances[i],recipe,j*.071f,true,0,origin,axis,side);
   long elapsed=System.nanoTime()-start,allocation=bean.getThreadAllocatedBytes(id)-allocated;
   rows.add(Map.of("variant",i==0?"installed":"optimized","meanCpuMs",elapsed/200e6,"allocatedBytesPerBuild",allocation/200D));
  }
  Files.writeString(out.resolve("support-cpu.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"exactBodyAndWeaponCases",40,"samples",200,"scope","CPU support builder only, reflective invocation for both variants; not game FPS or a GC pause measurement","results",rows)));
 }
}
