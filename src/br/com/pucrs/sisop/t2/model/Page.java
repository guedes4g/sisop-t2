package br.com.pucrs.sisop.t2.model;

public class Page {
    private Process p;
    private char memory;
    private int order;

    private int lastTimeAccessed = 0;

    public Page(char memory) {
        this.memory = memory;
    }

    public void setProcess(Process p) {
        this.p = p;
    }

    public boolean isFree() {
        return p == null;
    }

    public void clean() {
        this.p = null;
        this.order = 0;
    }

    public Process getProcess() {
        return p;
    }

    public boolean isInRAM() {
        return memory == 'R';
    }

    public void swap() {
        if (memory == 'R')
            memory = 'D';
        else
            memory = 'R';
    }

    public String toString() {
        return isFree() ? "00" : p.toString() + "("+order+")";
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getLastTimeAccessed() {
        return lastTimeAccessed;
    }

    public void setLastTimeAccessed(int time) {
        lastTimeAccessed = time;
    }
}
