package br.com.pucrs.sisop.t2;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMode {
    int numberOfThreads;
    double terminateProbability, addMemoryProbability;
    ArrayList<RandomThread> threadGroup;
    MemoryManager mm;

    public RandomMode(double terminateProbability, double addMemoryProbability, int numberOfThreads,  MemoryManager mm){
        this.addMemoryProbability = addMemoryProbability;
        this.terminateProbability = terminateProbability;
        this.numberOfThreads = numberOfThreads;
        this.mm = mm;
        threadGroup = generateRandomThreads();
    }

    public void run (){
        for(int i = 0; i < this.threadGroup.size(); i++){
            this.threadGroup.get(i).run();
        }
        for(int j = 0; j < this.threadGroup.size(); j++){
            try {
                this.threadGroup.get(j).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<RandomThread> generateRandomThreads() {
        ArrayList<RandomThread> aux = new ArrayList<>();
        for(int i  = 0; i < this.numberOfThreads ; i++){
            int finalI = i;
            String processName = ("p"+ finalI);
            aux.add(new RandomThread(processName, mm, terminateProbability, addMemoryProbability));
        }
        return aux;
    }
}
