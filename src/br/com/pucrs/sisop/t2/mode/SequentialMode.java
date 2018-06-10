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
        String line, buffer = "", process;
        String[] blocks;
        int parameter = 0;

        try {
            while ((line = br.readLine()) != null) {
                blocks = line.split(" ");

                //parse parameters
                process = blocks[1];

                if (blocks.length > 2)
                    parameter = Integer.parseInt(blocks[2]);

                switch (blocks[0].toUpperCase()) {
                    case "C":
                        buffer = mm.create(process, parameter);
                        break;

                    case "A":
                        buffer = mm.access(process, parameter);
                        break;

                    case "M":
                        buffer = mm.expand(process, parameter);
                        break;

                    case "T":
                        buffer = mm.terminate(process);
                        break;
                }

                System.out.println("["+line+"] - " + buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
