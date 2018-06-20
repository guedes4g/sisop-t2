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
        int i = r.nextInt(ram.length);
        System.out.println(i);
        //Pega baseado no índice e soma
        return ram[ i ];
    }
}
