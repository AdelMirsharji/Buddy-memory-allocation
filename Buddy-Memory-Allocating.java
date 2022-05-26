import java.util.*;
import java.util.Random;

class Buddy {
    static class Pair {
        int lower_bound, upper_bound;
        Pair(int upper_bound, int lower_bound){
            this.lower_bound = upper_bound;
            this.upper_bound = lower_bound;
        }
    }
    int size;
    ArrayList<Pair>[] free_sections;
    HashMap<Integer, Integer> hash_map;

    Buddy(int s){
        size = s;
        hash_map = new HashMap<>();
        int x = (int)Math.ceil(Math.log(s) / Math.log(2));
        free_sections = new ArrayList[x + 1];
        for (int i = 0; i <= x; i++)
            free_sections[i] = new ArrayList<>();
        free_sections[x].add(new Pair(0, size - 1));
    }

    int allocate(int s){
        int x = (int)Math.ceil(Math.log(s) / Math.log(2));
        int i;
        Pair temp;
        if (free_sections[x].size() > 0) {
            temp = free_sections[x].remove(0);
            System.out.println("Memory from " + temp.lower_bound + " to " + temp.upper_bound + " allocated");
            hash_map.put(temp.lower_bound, temp.upper_bound - temp.lower_bound + 1);
            return temp.lower_bound;
        }
        for (i = x + 1; i < free_sections.length; i++) {
            if (free_sections[i].size() == 0)
                continue;
            break;
        }
        if (i == free_sections.length) {
            System.out.println("Failed to allocate memory");
            return -1;
        }
        temp = free_sections[i].remove(0);
        i--;

        for (; i >= x; i--) {
            Pair newPair = new Pair(temp.lower_bound, temp.lower_bound + (temp.upper_bound - temp.lower_bound) / 2);
            Pair newPair2 = new Pair(temp.lower_bound + (temp.upper_bound - temp.lower_bound + 1) / 2, temp.upper_bound);
            free_sections[i].add(newPair);
            free_sections[i].add(newPair2);
            temp = free_sections[i].remove(0);
        }
        System.out.println("Memory from " + temp.lower_bound + " to " + temp.upper_bound + " allocated");
        hash_map.put(temp.lower_bound, temp.upper_bound - temp.lower_bound + 1);
        return temp.lower_bound;
    }

    void deallocate(int s){
        if (!hash_map.containsKey(s)) {
            System.out.println("Invalid free request");
            return;
        }

        int x = (int)Math.ceil(Math.log(hash_map.get(s)) / Math.log(2));
        int i, buddyNumber, buddyAddress;
        free_sections[x].add(new Pair(s, s + (int) Math.pow(2, x) - 1));
        System.out.println("Memory block from " + s + " to " + (s + (int)Math.pow(2, x) - 1) + " freed");
        buddyNumber = s / hash_map.get(s);

        if (buddyNumber % 2 != 0) {
            buddyAddress = s - (int)Math.pow(2, x);
        }
        else {
            buddyAddress = s + (int)Math.pow(2, x);
        }

        for (i = 0; i < free_sections[x].size(); i++) {
            if (free_sections[x].get(i).lower_bound == buddyAddress) {
                if (buddyNumber % 2 == 0) {
                    free_sections[x + 1].add(new Pair(s, s + 2 * ((int) Math.pow(2, x)) - 1));
                    System.out.println("Coalescing of blocks starting at " + s + " and " + buddyAddress + " was done");
                }
                else {
                    free_sections[x + 1].add(new Pair(buddyAddress,
                            buddyAddress + 2 * ((int) Math.pow(2, x))
                                    - 1));
                    System.out.println("Coalescing of blocks starting at " + buddyAddress + " and " + s + " was done");
                }
                free_sections[x].remove(i);
                free_sections[x].remove(free_sections[x].size() - 1);
                break;
            }
        }
        hash_map.remove(s);
    }

    static class Process extends Buddy implements Runnable{
        int needed_size;
        int free_size;
        Process(int total_size,int needed_size) {
            super(total_size);
            this.needed_size = needed_size;
        }

        @Override
        public void run() {
            Random rand = new Random();
            int p = rand.nextInt(11);
            p = p + 5;
            p = p * 1000;
            free_size = allocate(needed_size);
            try {Thread.sleep(p);} catch(Exception e){e.printStackTrace();}
            deallocate(free_size);
        }
    }

    public static void main(String[] args) {
        int initialMemory = 1024;
        Process pro1 = new Process(initialMemory,16);
        Process pro2 = new Process(initialMemory,16);
        Process pro3 = new Process(initialMemory,32);
        Process pro4 = new Process(initialMemory,64);
        Thread t1 = new Thread(pro1);
        Thread t2 = new Thread(pro2);
        Thread t3 = new Thread(pro3);
        Thread t4 = new Thread(pro4);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}