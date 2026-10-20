// Adapted from hbyaing/xiantu r115 (ff253fa), retaining the original vein algorithm.
package cn.blockforge.generated.generatedmod.world;
import java.util.*;
import java.util.function.Function;

/** 最大连通洞壁的广度优先顺序，截取前缀后仍保持连通。 */
public final class ConnectedVeinShape {
    public static <T> List<T> largest(Collection<T> cells, Function<T,List<T>> neighbors, Comparator<T> order) {
        Set<T> remaining=new HashSet<>(cells);List<T> largest=new ArrayList<>();
        // Sort seed candidates once rather than rescanning the entire remaining set
        // for every isolated wall fragment. Component order remains deterministic.
        List<T> seeds=new ArrayList<>(remaining);seeds.sort(order);
        for(T first:seeds) {
            if(!remaining.remove(first))continue;
            ArrayDeque<T> queue=new ArrayDeque<>();queue.add(first);
            List<T> component=new ArrayList<>();
            while(!queue.isEmpty()) {T p=queue.removeFirst();component.add(p);for(T n:neighbors.apply(p))if(remaining.remove(n))queue.addLast(n);}
            if(component.size()>largest.size())largest=component;
        }
        return largest;
    }
}
