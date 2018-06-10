package br.com.pucrs.sisop.t2;

import java.util.HashMap;
import java.util.Map;

public class MemoryManager {
    private Algorithm algo;
    private int pageSize, memorySize, diskSize;

    private Map<String, Process> processes = new HashMap();

    private Page[] ram;
    private Page[] disk;

    public int getPageSize() {
        return pageSize;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public MemoryManager(String algo, int pageSize, int memorySize, int diskSize) {
        this.algo = algo.equals("lru") ? Algorithm.LRU : Algorithm.Random;

        this.pageSize = pageSize;
        this.memorySize = memorySize;
        this.diskSize = diskSize;

        this.ram = new Page[memorySize / pageSize];
        this.disk = new Page[diskSize / pageSize];

        this.createPages();
    }

    public void access(String process, int memoryPosition) {
        Process p;

        if (processes.containsKey(process)) {
            p = processes.get(process);

            //Verifica se há erro de invasão de memória
            if (!p.hasSegmentationFault(memoryPosition)) {
                //Verifica se é possível acessar essa posição de memória
                if (p.canAccess(memoryPosition)) {
                    System.out.print("OK.");
                }
                else {
                    //Isso significa que precisamos fazer SWAP
                    System.out.print("SWAP necessário.");

                    //Verificar se há espaço para SWAP
                }
            }
            else
                System.out.print("Segmentation fault.");
        }
        else
            System.out.print("Processo não existe.");
    }

    public void create(String process, int size) {
        Process p;

        if (!processes.containsKey(process))
            if (hasEnoughSpace(size)) {
                //Cria o processo
                p = new Process(process, size, this.pageSize);
                processes.put(process, p);

                //Vincula as páginas
                grantPages(p, size);

                System.out.print("OK.");
            }
            else
                System.out.print("Erro ao criar processo. Processo já existe.");
    }

    public void terminate(String process) {
        Process p;

        if (processes.containsKey(process)) {
            //finalizo o processo
            p = processes.get(process);
            p.terminate();

            //retiro do mapa
            processes.remove(process);

            System.out.print("OK.");
        }
        else
            System.out.print("Processo não existe.");
    }

    public void expand(String process, int size) {
        Process p;
        int amountFree;

        if (processes.containsKey(process)) {
            p = processes.get(process);

            amountFree = p.getFreeSpaceInPages();

            //Tenta verificar se o processo possui páginas com espaço livre (o suficiente para permitir o increase)
            if (amountFree >= size) {
                System.out.print("Espaço alocado com sucesso. Não foi necessário criar página (tinha espaço sobrando).");
                p.increaseSize(size);
            }
            else {
                //Caso não tenha "o suficiente", verifica se há página para ser alocada
                //"Tamanho real que quero alocar" - "quanto já posso alocar com minhas páginas"
                if (hasEnoughSpace(size - amountFree)) {
                    //Em caso afirmativo, concede o total de páginas necessárias para o processo
                    grantPages(p, size - amountFree);
                    p.increaseSize(size);

                    //print
                    System.out.print("Espaço alocado com sucesso. Nova página.");
                }
                else {
                    //TBD:
                }
            }
        }
        else
            System.out.print("Processo não existe.");
    }

    private void grantPages(Process p, int size) {
        int iterations = (int) Math.ceil(size * 1.0 / pageSize);

        for (;iterations > 0; iterations--)
            p.increasePage(getNextFreePage());
    }

    private Page getNextFreePage() {
        for (int i = 0; i < ram.length; i++)
            if (ram[i].isFree())
                return ram[i];

        return null;
    }

    private boolean hasEnoughSpace(int size) {
        int free = 0;

        for (int i = 0; i < ram.length; i++) {
            if (ram[i].isFree()) {
                free += pageSize;

                if (free >= size)
                    return true;
            }
        }

        return false;
    }

    private void createPages() {
        for (int i = 0; i < ram.length; i++)
            ram[i] = new Page(pageSize, 'R');

        for (int i = 0; i < disk.length; i++)
            disk[i] = new Page(pageSize, 'D');
    }
}
