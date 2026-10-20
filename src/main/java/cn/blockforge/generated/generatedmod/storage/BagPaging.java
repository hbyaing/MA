package cn.blockforge.generated.generatedmod.storage;
import java.util.*;
import java.util.function.Predicate;
/** 筛选页面只保存原存储索引，绝不移动或删除原始物品。 */
public final class BagPaging {
 public record Page(int number,int pages,int[] indices){}
 public static <T> Page scroll(List<T> items,Predicate<T> accepts,int requested,int columns,int visibleRows){List<Integer> selected=new ArrayList<>();for(int i=0;i<items.size();i++)if(accepts.test(items.get(i)))selected.add(i);int rows=selected.size()/columns+1,max=Math.max(0,rows-visibleRows),row=Math.max(0,Math.min(max,requested));int[] indices=new int[columns*visibleRows];Arrays.fill(indices,-1);for(int i=0;i<indices.length&&row*columns+i<selected.size();i++)indices[i]=selected.get(row*columns+i);return new Page(row,max+1,indices);}
 public static <T> Page select(List<T> items,Predicate<T> accepts,int requested,int size){List<Integer> selected=new ArrayList<>();for(int i=0;i<items.size();i++)if(accepts.test(items.get(i)))selected.add(i);int pages=Math.max(1,selected.size()/size+1),page=Math.max(0,Math.min(pages-1,requested));int[] indices=new int[size];Arrays.fill(indices,-1);for(int i=0;i<size&&page*size+i<selected.size();i++)indices[i]=selected.get(page*size+i);return new Page(page,pages,indices);}
}
