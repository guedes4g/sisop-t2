package br.com.pucrs.sisop.t2.mode;

import br.com.pucrs.sisop.t2.MemoryManager;

import java.io.BufferedReader;
import java.io.IOException;

public class SequentialMode implements Runnable {
    private BufferedReader br;
    private MemoryManager mm;

    public SequentialMode(BufferedReader br, MemoryManager mm) {
        this.br = br;
        this.mm = mm;
    }

    public void run() {
        String line;

        try {
            while ((line = br.readLine()) != null) {
                String[] blocks = line.split(" ");
                String ret = "";

                //0 -> command type
                //1 -> process name
                //2 -> memory size
                switch (blocks[0].toUpperCase()) {
                    case "C":
                        ret = mm.create(blocks[1], Integer.parseInt(blocks[2]));
                        break;

                    case "A":
                        ret = mm.access(blocks[1], Integer.parseInt(blocks[2]));
                        break;

                    case "M":
                        ret = mm.expand(blocks[1], Integer.parseInt(blocks[2]));
                        break;

                    case "T":
                        ret = mm.terminate(blocks[1]);
                        break;
                }

                System.out.println("["+line+"] - " + ret);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
