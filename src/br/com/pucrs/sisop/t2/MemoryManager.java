package br.com.pucrs.sisop.t2;

import br.com.pucrs.sisop.t2.swapalgo.LRU;
import br.com.pucrs.sisop.t2.swapalgo.Random;
import br.com.pucrs.sisop.t2.swapalgo.SwapAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class MemoryManager {
    private SwapAlgorithm swapAlgo;
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
        this.swapAlgo = algo.equals("lru") ? new LRU() : new Random(ram);

        this.pageSize = pageSize;
        this.memorySize = memorySize;
        this.diskSize = diskSize;

        this.ram = new Page[memorySize / pageSize];
        this.disk = new Page[diskSize / pageSize];

        this.createPages();
    }

    public String access(String process, int memoryPosition) {
        Process p;

        if (processes.containsKey(process)) {
            p = processes.get(process);

            //Verifica se há erro de invasão de memória
            if (!p.hasSegmentationFault(memoryPosition)) {
                //Verifica se é possível acessar essa posição de memória
                if (p.canAccess(memoryPosition))
                    return "OK.";

                //Isso significa que precisamos fazer SWAP
                else {
                    //Verificar se há espaço para SWAP
                    if (hasEnoughSpaceToSwap()) {
                        //Faz o SWAP
                        swap(p.getPage(memoryPosition));

                        return "OK. SWAP necessário.";
                    }
                    else
                        return "SWAP necessário. Não há espaço para SWAP. Disco cheio.";
                }
            }
            else
                return "Segmentation fault.";
        }
        else
            return "Processo não existe.";
    }

    public String create(String process, int size) {
        Process p;

        if (!processes.containsKey(process)) {
            if (hasEnoughSpaceInRam(size)) {
                confirmProcessCreation(process, size);

                return "OK. Criou normal.";
            }
            else {
                if (hasEnoughSpaceToSwap(size)) {
                    createFreeSpaceInRam(size);

                    confirmProcessCreation(process, size);

                    return "OK. Criou a partir de SWAP.";
                }
                else
                    return "Não tem mais memória.";
            }
        }
        else
            return "Erro ao criar processo. Processo já existe.";
    }

    public String terminate(String process) {
        Process p;

        if (processes.containsKey(process)) {
            //finalizo o processo
            p = processes.get(process);
            p.terminate();

            //retiro do mapa
            processes.remove(process);

            return "OK.";
        }
        else
            return "Processo não existe.";
    }

    public String expand(String process, int size) {
        Process p;
        int amountFree, amountNeededToAllocate;

        if (processes.containsKey(process)) {
            p = processes.get(process);

            amountFree = p.getFreeSpaceInPages();

            //Tenta verificar se o processo possui páginas com espaço livre (o suficiente para permitir o increase)
            if (amountFree >= size) {
                p.increaseSize(size);

                return "Espaço alocado com sucesso. Não foi necessário criar página (tinha espaço sobrando).";
            }
            else {
                amountNeededToAllocate = size - amountFree;

                //Caso não tenha "o suficiente", verifica se há página para ser alocada
                //"Tamanho real que quero alocar" - "quanto já posso alocar com minhas páginas"
                if (hasEnoughSpaceInRam(amountNeededToAllocate)) {
                    //Apenas Confirma a expansão
                    confirmExpansionInProcess(p, size, amountNeededToAllocate);

                    //print
                    return "Espaço alocado com sucesso. Criou direto nova página.";
                }
                else {
                    if (hasEnoughSpaceToSwap(amountNeededToAllocate)) {
                        //Cria espaço livre na RAM
                        createFreeSpaceInRam(amountNeededToAllocate);

                        //Confirma a expansão
                        confirmExpansionInProcess(p, size, amountNeededToAllocate);

                        //print
                        return "Espaço alocado com sucesso. Criou nova página a partir de SWAP.";
                    }
                    else
                        return "Não tem mais memória.";
                }
            }
        }
        else
            return "Processo não existe.";
    }

    private void createFreeSpaceInRam(int size) {
        //TBD: pegar espaços vazios do disco e invocar o swap
        int iterations = (int) Math.ceil(size * 1.0 / pageSize);

        for (;iterations > 0; iterations--)
            swap(getNextFreePageInDisk());
    }

    private void confirmProcessCreation(String process, int size) {
        //Cria o processo
        Process p = new Process(process, size, this.pageSize);
        processes.put(process, p);

        //Vincula as páginas
        grantPages(p, size);
    }

    private void confirmExpansionInProcess(Process p, int realSize, int pageAllocationSize) {
        //Em caso afirmativo, concede o total de páginas necessárias para o processo
        grantPages(p, pageAllocationSize);
        p.increaseSize(realSize);
    }

    private void grantPages(Process p, int size) {
        int iterations = (int) Math.ceil(size * 1.0 / pageSize);

        for (;iterations > 0; iterations--)
            p.increasePage(getNextFreePageInRam());
    }

    private Page getNextFreePageInRam() {
        return getNextFreePage(ram);
    }

    private Page getNextFreePageInDisk() {
        return getNextFreePage(disk);
    }

    private Page getNextFreePage(Page[] list) {
        for (int i = 0; i < list.length; i++)
            if (list[i].isFree())
                return list[i];

        return null;
    }

    private boolean hasEnoughSpaceInRam(int size) {
        int free = 0;

        //Check for free space in RAM
        for (int i = 0; i < ram.length; i++) {
            if (ram[i].isFree()) {
                free += pageSize;

                if (free >= size)
                    return true;
            }
        }

        return false;
    }

    private int getFreeSpaceInRam() {
        return getFreeSpace(ram);
    }

    private int getFreeSpaceInDisk() {
        return getFreeSpace(disk);
    }

    private int getFreeSpace(Page[] list) {
        int free = 0;

        for (int i = 0; i < list.length; i++) {
            if (list[i].isFree())
                free += pageSize;
        }

        return free;
    }

    private void createPages() {
        for (int i = 0; i < ram.length; i++)
            ram[i] = new Page('R');

        for (int i = 0; i < disk.length; i++)
            disk[i] = new Page('D');
    }

    private boolean hasEnoughSpaceToSwap(int amountNeededToAllocate) {
        int free = getFreeSpaceInDisk() + getFreeSpaceInRam();

        return free >= amountNeededToAllocate;
    }

    private boolean hasEnoughSpaceToSwap() {
        return hasEnoughSpaceToSwap(pageSize);
    }

    private void swap(Page page) {
        //Primeiro, tenta realizar um SWAP "óbvio". Ou seja, tenho posição livre em RAM.
        if (hasEnoughSpaceInRam(pageSize))
            //swap on free RAM
            swapInFreeRam(page);

        //Segundo, usa o algoritmo "inteligente"
        else
            swapUsingAlgo(page);
    }

    private void swapUsingAlgo(Page pageDisk) {
        Page pageRam = swapAlgo.getPage();
        Page freeInDisk = getNextFreePageInDisk();

        //pega a RAM e bota no livre em disco
        //pega o que queremos no disco e bota na RAM
        //pega o antigo livre em disco e bota no antigo do que queremos
        swap(pageRam, pageDisk, freeInDisk);
    }

    private void swapInFreeRam(Page page) {
        //Pega a primeira posição na RAM que não está mais sendo usada
        Page free = getNextFreePageInRam();

        //Faz a troca pelo disco (não precisa levar em consideração o espaço vazio)
        swap(free, page);
    }

    private void swap(Page pRam, Page pDiskWanted, Page pDiskFree) {
        //Troca o livre pelo wanted
        int indexFree = indexOfDisk(pDiskFree);
        int indexWanted = indexOfDisk(pDiskWanted);

        disk[indexFree] = pDiskWanted;
        disk[indexWanted] = pDiskFree;

        swap(pRam, pDiskWanted);
    }

    private void swap(Page pRam, Page pDisk) {
        //get index
        int indexRam = indexOfRam(pRam);
        int indexDisk = indexOfDisk(pDisk);

        disk[indexDisk] = pRam;
        ram[indexRam] = pDisk;

        //update the process cache
        pRam.swap();
        pDisk.swap();
    }

    private int indexOfDisk(Page p) {
        return indexOfList(disk, p);
    }

    private int indexOfRam(Page p) {
        return indexOfList(ram, p);
    }

    private int indexOfList(Page[] list, Page p) {
        for (int i = 0; i < list.length; i++)
            if (list[i] == p)
                return i;

        return -1;
    }
}
