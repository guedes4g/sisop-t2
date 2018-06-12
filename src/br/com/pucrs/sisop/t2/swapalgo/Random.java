package br.com.pucrs.sisop.t2.swapalgo;

import br.com.pucrs.sisop.t2.model.Page;

public class Random implements SwapAlgorithm {
    private Page[] ram;

    public Random(Page[] ram) {
        this.ram = ram;
    }

    @Override
    public Page getPage() {
        java.util.Random r = new java.util.Random();

        //Pega baseado no índice e soma
        return ram[r.nextInt(ram.length) -1];
    }
}
