package br.com.pucrs.sisop.t2;

import br.com.pucrs.sisop.t2.model.Page;
import br.com.pucrs.sisop.t2.model.Process;
import br.com.pucrs.sisop.t2.swapalgo.LRU;
import br.com.pucrs.sisop.t2.swapalgo.Random;
import br.com.pucrs.sisop.t2.swapalgo.SwapAlgorithm;

import java.util.HashMap;
import java.util.Map;

/*
 * Trabalho 2 - Sistemas Operacionais para Engenharia de Software (2018/01)
 * Professor: Avelino
 * Alunos: Felipe Guedes e Mateus Haas
 *
 * Descrição do problema: desenvolver uma solução que implemente uma simulação de pagefault de processos em uma sistema operacional
 *
 * Classe: essa classe implementa todos os métodos de ALOCAÇÃO, ACESSO, CRIAÇÃO e TÉRMINO de um processo e suas páginas.
 */

public class MemoryManager {
    private SwapAlgorithm swapAlgo;
    private int pageSize, memorySize, diskSize;

    private Map<String, Process> processes = new HashMap();

    private Page[] ram, disk;

    private int rounds = 0;

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
        this.pageSize = pageSize;
        this.memorySize = memorySize;
        this.diskSize = diskSize;

        this.ram = new Page[memorySize / pageSize];
        this.disk = new Page[diskSize / pageSize];

        this.createPages();

        this.swapAlgo = algo.equals("lru") ? new LRU(ram) : new Random(ram);
    }

    public synchronized String access(String process, int memoryPosition) {
        StringBuilder sb = new StringBuilder();
        Process p;

        //sum the rounds
        rounds++;

        if (processes.containsKey(process)) {
            p = processes.get(process);

            //Verifica se há erro de invasão de memória
            if (!p.hasSegmentationFault(memoryPosition)) {
                //Verifica se é possível acessar essa posição de memória
                if (p.canAccess(memoryPosition)) {
                    p.read(memoryPosition, rounds);

                    return "OK.";
                }

                //Isso significa que precisamos fazer SWAP
                else {
                    //Verificar se há espaço para SWAP
                    if (hasEnoughSpaceToSwap()) {
                        sb.append("OK. SWAP necessário. ");

                        sb.append(printRamAndDisk());

                        //Faz o SWAP
                        makePageAvailableInRam(p.getPage(memoryPosition));

                        sb.append(printRamAndDisk());

                        //finalize the reading
                        p.read(memoryPosition, rounds);
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

        return sb.toString();
    }

    public synchronized String create(String process, int size) {
        StringBuilder sb = new StringBuilder();
        Process p;

        //sum the rounds
        rounds++;

        if (!processes.containsKey(process)) {
            if (hasEnoughSpaceInRam(size)) {
                confirmProcessCreation(process, size);

                return "OK. Criou normal.";
            }
            else {
                if (hasEnoughSpaceToSwap(size)) {
                    sb.append("OK. Criou a partir de SWAP. ");

                    sb.append(printRamAndDisk());

                    createFreeSpaceInRam(size);
                    confirmProcessCreation(process, size);

                    sb.append(printRamAndDisk());
                }
                else
                    return "Não tem mais memória.";
            }
        }
        else
            return "Erro ao criar processo. Processo já existe.";

        return sb.toString();
    }

    public synchronized String terminate(String process) {
        Process p;

        //sum the rounds
        rounds++;

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

    public Process getProcess(String key) {
        if (processes.containsKey(key))
            return processes.get(key);

        return null;
    }

    public synchronized String expand(String process, int size) {
        int amountFree, amountNeededToAllocate;
        StringBuilder sb = new StringBuilder();
        Process p;

        //sum the rounds
        rounds++;

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
                        //print
                        sb.append("Espaço alocado com sucesso. Criou nova página a partir de SWAP | ");

                        //print before
                        sb.append(printRamAndDisk());

                        //Cria espaço livre na RAM
                        createFreeSpaceInRam(amountNeededToAllocate);

                        //Confirma a expansão
                        confirmExpansionInProcess(p, size, amountNeededToAllocate);

                        //print after
                        sb.append(printRamAndDisk());
                    }
                    else
                        return "Não tem mais memória.";
                }
            }
        }
        else
            return "Processo não existe.";

        return sb.toString();
    }

    private void createFreeSpaceInRam(int size) {
        //TBD: pegar espaços vazios do disco e invocar o swap
        int iterations = (int) Math.ceil(size * 1.0 / pageSize);

        for (;iterations > 0; iterations--)
            swapUsingAlgo();
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
            p.increasePage(getNextFreePageInRam(), rounds);
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
        return getFreeSpaceInRam() >= size;
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

    private void makePageAvailableInRam(Page page) {
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

    private void swapUsingAlgo() {
        Page pageRam = swapAlgo.getPage();
        Page freeInDisk = getNextFreePageInDisk();

        //pega a RAM e bota no livre em disco
        //pega o livre em disco e bota na RAM
        swap(pageRam, freeInDisk);
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

    private String printRamAndDisk() {
        StringBuilder sb = new StringBuilder();

        sb.append("R [");
        for (int i = 0; i < ram.length; i++)
            sb.append(ram[i] + " ");

        sb.append("] D [");

        for (int i = 0; i < disk.length; i++)
            sb.append(disk[i] + " ");

        sb.append("];");

        return sb.toString();
    }
}
