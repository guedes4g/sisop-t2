package br.com.pucrs.sisop.t2;

import java.io.*;
import java.util.Scanner;

public class Program {
    private static final String FILE_PATH_DEBUG = "./examples/e1.txt";
    private static final boolean DEBUG = true;

    private static String mode;
    private static MemoryManager mm;

    public static void main(String[] args) {
        System.out.println("Digite o path do arquivo de entrada: ");

        //get file input
        String file = DEBUG ? FILE_PATH_DEBUG : readFileFromTerminal();

        //parse
        parseFile(file);

        //finish
        System.out.println("Ended successfully.");
    }

    private static void parseFile(String filePath) {
        File f = new File(filePath);
        FileReader fr = null;

        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            if (br.ready()) {
                instanciateMemoryManager(br);

                if (isSequentialMode())
                    runSequentialMode(br);
                else
                    runRandomMode();
            }

            br.close();
            fr.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void instanciateMemoryManager(BufferedReader br) throws IOException {
        mode = br.readLine().toLowerCase();

        String algo = br.readLine().toLowerCase();
        int pageSize = Integer.parseInt(br.readLine());
        int memorySize = Integer.parseInt(br.readLine());
        int diskSize = Integer.parseInt(br.readLine());

        mm = new MemoryManager(algo, pageSize, memorySize, diskSize);
    }

    private static void runSequentialMode(BufferedReader br) throws IOException {
        String line;

        while ((line = br.readLine()) != null) {
            String[] blocks = line.split(" ");

            System.out.print("["+line+"] - ");

            //0 -> command type
            //1 -> process name
            //2 -> memory size
            switch (blocks[0].toUpperCase()) {
                case "C":
                    mm.create(blocks[1], Integer.parseInt(blocks[2]));
                    break;

                case "A":
                    mm.access(blocks[1], Integer.parseInt(blocks[2]));
                    break;

                case "M":
                    mm.expand(blocks[1], Integer.parseInt(blocks[2]));
                    break;

                case "T":
                    mm.terminate(blocks[1]);
                    break;
            }

            System.out.println("");
        }
    }

    private static void runRandomMode() {
        //TBD
    }

    private static String readFileFromTerminal() {
        Scanner s = new Scanner(System.in);
        return s.nextLine();
    }

    private static boolean isSequentialMode() {
        return mode.equals("sequencial") || mode.equals("s") || mode.equals("0");
    }
}