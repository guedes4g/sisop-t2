package br.com.pucrs.sisop.t2;

public class Page {
    private int size;
    private Process p;

    public Page(int size) {
        this.size = size;
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
}
