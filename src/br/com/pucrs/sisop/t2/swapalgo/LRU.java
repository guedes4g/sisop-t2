package br.com.pucrs.sisop.t2.swapalgo;

import br.com.pucrs.sisop.t2.model.Page;

/*
 * Trabalho 2 - Sistemas Operacionais para Engenharia de Software (2018/01)
 * Professor: Avelino
 * Alunos: Felipe Guedes e Mateus Haas
 *
 * Descrição do problema: desenvolver uma solução que implemente uma simulação de pagefault de processos em uma sistema operacional
 *
 * Classe: essa classe implementa o algoritmo de SWAP "Least Recently Used", onde pesquisa em todas as páginas o último "round" de execução desta. O programa deve percorrer toda a lista e selecionar a página que teve o acesso mais antigo.
 */

public class LRU implements SwapAlgorithm {
    private Page[] ram;

    public LRU(Page[] ram) {
        this.ram = ram;
    }

    @Override
    public Page getPage() {
        int minSize = Integer.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < ram.length; i++) {
            if (ram[i].getLastTimeAccessed() < minSize) {
                index = i;
                minSize = ram[i].getLastTimeAccessed();
            }
        }

        return ram[index];
    }
}
