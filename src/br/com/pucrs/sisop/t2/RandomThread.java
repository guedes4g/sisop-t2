package br.com.pucrs.sisop.t2;

import java.util.concurrent.ThreadLocalRandom;

public class RandomThread extends Thread {
    private String processName;
    private MemoryManager mm;
    private double probabilityTerminate, probabilityAllocate;
    private boolean terminated;

    public RandomThread(String processName, MemoryManager mm, double probabilityTerminate, double probabilityAllocate){
        this.processName = processName;
        this.probabilityTerminate = probabilityTerminate;
        this.probabilityAllocate = probabilityAllocate;
        this.mm = mm;
        this.terminated = false;
        mm.create(this.processName, generateRandomProcessSize());
    }

    @Override
    public void run()
    {
        while(!this.terminated) {
            StringBuilder sb = new StringBuilder();

            sb.append("["+ this.processName +"]");
            sb.append(mm.access(this.processName, generateRandomMemoryAccess()) + '\n');
            sb.append("["+ this.processName +"]");
            
            if (generateRandomProbability() < probabilityTerminate) {
                sb.append(mm.expand(processName, generateRandomProcessSize()));
                this.terminated = true;
            } else if (generateRandomProbability() < probabilityAllocate) {
                sb.append(mm.terminate(processName));
            }

            System.out.println(sb.toString());
        }
    }
    private double generateRandomProbability(){
        return ThreadLocalRandom.current().nextDouble(0, 100) ;
    }

    private int generateRandomProcessSize(){
        return ThreadLocalRandom.current().nextInt(1, this.mm.getPageSize()+ 1);
    }
    private int generateRandomMemoryAccess(){
        return ThreadLocalRandom.current().nextInt(0, this.mm.getMemorySize());
    }

}
