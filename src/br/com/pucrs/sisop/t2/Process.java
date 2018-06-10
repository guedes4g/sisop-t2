package br.com.pucrs.sisop.t2;

import java.util.ArrayList;
import java.util.List;

public class Process {
    public String id;
    public int pageSize;
    public int size;
    public List<Page> pages;

    public Process(String id, int size, int pageSize) {
        this.id = id;
        this.size = size;
        this.pages = new ArrayList<>();
        this.pageSize = pageSize;
    }

    public void increasePage(Page p){
        p.setProcess(this);

        this.pages.add(p);
    }

    public boolean hasSegmentationFault(int memoryPosition) {
        return memoryPosition >= size;
    }

    public boolean canAccess(int memoryPosition) {
        //pega a página e verifica se está na RAM
        return getPage(memoryPosition).isInRAM();
    }

    public Page getPage(int memoryPosition) {
        return pages.get(memoryPosition / pageSize);
    }

    public void increaseSize(int size){
        this.size += size;
    }

    public int getFreeSpaceInPages(){
        return (pages.size() * pageSize) - size;
    }

    public void terminate() {
        for (Page p : pages)
            p.setProcess(null);
    }
}
