package br.com.pucrs.sisop.t2.swapalgo;

import br.com.pucrs.sisop.t2.model.Page;

/*
 * Trabalho 2 - Sistemas Operacionais para Engenharia de Software (2018/01)
 * Professor: Avelino
 * Alunos: Felipe Guedes e Mateus Haas
 *
 * Descrição do problema: desenvolver uma solução que implemente uma simulação de pagefault de processos em uma sistema operacional
 *
 * Classe: essa classe implementa o algoritmo de SWAP "aleatório", onde pesquisa em todas as páginas e seleciona uma aleatoriamente.
 */

public class Random implements SwapAlgorithm {
    private Page[] ram;

    public Random(Page[] ram) {
        this.ram = ram;
    }

    @Override
    public Page getPage() {
        java.util.Random r = new java.util.Random();
        int i = r.nextInt(ram.length);
        //Pega baseado no índice e soma
        return ram[ i ];
    }
}
