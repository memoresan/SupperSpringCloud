package Queue;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class ProQueue {
    static Comparator<Integer> cmp = new Comparator<Integer>() {
        public int compare(Integer e1, Integer e2) {
            return e2 - e1;
        }
    };
    public static void main(String[] agrs){
        //不用比较器，默认升序排列
        Queue<Integer> q = new PriorityQueue<>((x,y)-> {return Long.compare(y,x);});
        q.add(3);
        q.add(2);
        q.add(4);
        while(!q.isEmpty())
        {
            System.out.print(q.poll()+" ");
        }

    }
}
