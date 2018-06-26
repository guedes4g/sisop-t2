package br.com.pucrs.sisop.t2;

import br.com.pucrs.sisop.t2.mode.RandomMode;
import br.com.pucrs.sisop.t2.mode.SequentialMode;

import java.io.*;
import java.util.Scanner;

/*
 * Trabalho 2 - Sistemas Operacionais para Engenharia de Software (2018/01)
 * Professor: Avelino
 * Alunos: Felipe Guedes e Mateus Haas
 *
 * Descrição do problema: desenvolver uma solução que implemente uma simulação de pagefault de processos em uma sistema operacional
 *
 * Classe: é o "coração" do programa, de forma que realiza uma leitura inicial no arquivo de entrada e decide:
 *      1. qual modo de execução
 *      2. qual algoritmo de SWAP o programa deve executar
 */

public class Program {
    private static final String FILE_PATH_DEBUG = "./examples/e1.txt";
    private static final boolean DEBUG = true;

    //Random mode variables
    private static final int NUMBER_OF_THREADS = 10;
    private static final double TERMINATE_PROBABILITY = 10;
    private static final double EXPAND_PROBABILITY = 3;

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
        FileReader fr;
        Runnable programMode;

        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            if (br.ready()) {
                //initiate the Memory Manager (based on input file)
                instanciateMemoryManager(br);

                //decide which program we will run
                programMode = isSequentialMode() ? getSequentialMode(br) : getRandomMode();

                //and then, finally, run it
                programMode.run();
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

    private static Runnable getSequentialMode(BufferedReader br) throws IOException {
        return new SequentialMode(br, mm);
    }

    private static Runnable getRandomMode() {
        return new RandomMode(TERMINATE_PROBABILITY, EXPAND_PROBABILITY, NUMBER_OF_THREADS, mm);
    }

    private static String readFileFromTerminal() {
        Scanner s = new Scanner(System.in);
        return s.nextLine();
    }

    private static boolean isSequentialMode() {
        return mode.equals("sequencial") || mode.equals("s") || mode.equals("0");
    }
}