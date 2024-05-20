package test;

import org.junit.Test;

import java.util.*;

public class TestSomething {
    @Test
    public void test(){
        String str = "1234095";
        System.out.println(Arrays.toString(str.split("")));
        // int[] ints = Arrays.stream(str.split("")).mapToInt(Integer::parseInt).toArray();
        // Arrays.stream(ints).forEach(System.out::println);

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.get(list.size() - 1);

        System.out.println(list);
    }

}
