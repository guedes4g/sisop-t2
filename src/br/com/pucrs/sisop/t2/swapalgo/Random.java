package br.com.pucrs.sisop.t2.swapalgo;

import br.com.pucrs.sisop.t2.Page;

public class Random implements SwapAlgorithm {
    private int lastIndexUsed = 0;
    private Page[] ram;

    public Random(Page[] ram) {
        this.ram = ram;
    }

    @Override
    public Page getPage() {
        //Pega baseado no índice e soma
        return ram[lastIndexUsed++];
    }
}
