package it.unicam.quasylab.sibilla.shell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ShellStreamInterceptor {
    ByteArrayOutputStream baos;
    PrintStream old;

    public ShellStreamInterceptor(){
        // Create a stream to hold the output
        this.baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        // IMPORTANT: Save the old System.out!
        old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);
    }

    public String getConsoleStream(){
        // Put things back
        System.out.flush();
        System.out.close();
        System.setOut(this.old);
        String console_until_now = this.baos.toString();
        //System.out.close();
        return console_until_now;
    }
}
