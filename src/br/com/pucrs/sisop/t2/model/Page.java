package br.com.pucrs.sisop.t2.model;

public class Page {
    private Process p;
    private char memory;

    public Page(char memory) {
        this.memory = memory;
    }

    public void setProcess(Process p) {
        this.p = p;
    }

    public boolean isFree() {
        return p == null;
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
}
