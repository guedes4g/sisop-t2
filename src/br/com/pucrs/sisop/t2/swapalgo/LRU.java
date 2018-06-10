package br.com.pucrs.sisop.t2.swapalgo;

import br.com.pucrs.sisop.t2.model.Page;

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
