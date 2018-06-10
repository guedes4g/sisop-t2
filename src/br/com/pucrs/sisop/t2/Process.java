package br.com.pucrs.sisop.t2;

import java.util.LinkedList;
import java.util.List;

public class Process {
    public String id;
    public int pageSize;
    public int size;
    public List<Page> pages;

    public Process(String id, int size, int pageSize) {
        this.id = id;
        this.size = size;
        this.pages = new LinkedList<>();
        this.pageSize = pageSize;
    }

    public void increasePage(Page p){
        p.setProcess(this);

        this.pages.add(p);
    }

    public void increaseSize(int size){
        this.size += size;
    }

    public int sobraEspacoPaginas(){
        return (this.pages.size() * this.pageSize) - this.size;
    }

    public void terminate() {
        for (Page p : pages)
            p.setProcess(null);
    }
}
