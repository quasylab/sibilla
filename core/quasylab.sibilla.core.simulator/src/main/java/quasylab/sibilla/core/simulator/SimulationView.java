package quasylab.sibilla.core.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

public class SimulationView<S> {
    private JFrame frame = new JFrame("Sibilla");
    private SimulationManager<S> simManager;
    private String type;
    private int simulationLength;
    private JProgressBar progressBar;
    private JTabbedPane tabbedPane;
    private JLabel waitingTasks;
    private Map<String, JTextArea> serverData = new HashMap<>();
    private JTextArea threadDetail = new JTextArea();
    private JLabel threadCount = new JLabel("Running Tasks: 0");
    /*
     * private static SimulationView instance;
     * 
     * public static SimulationView getSimulationView(){ if(instance == null)
     * instance = new SimulationView(); return instance; }
     */

    public SimulationView(int iterations, SimulationManager<S> simManager) {
        this.simManager = simManager;
        this.simulationLength = iterations;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {                   
            e.printStackTrace();
        }
        type = simManager.getClass().getName();
        frame.setContentPane(createView(type));
        frame.getContentPane().setPreferredSize(new Dimension(1300, 700));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private JPanel createView(String type){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.RED));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(commonView(), c);
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        switch(type){
            case "quasylab.sibilla.core.simulator.ThreadSimulationManager": panel.add(threadView(), c); break;
            case "quasylab.sibilla.core.simulator.NetworkSimulationManager": panel.add(networkView(), c); break;
            case "quasylab.sibilla.core.simulator.SequentialSimulationManager": break;
            default: break;
        }
        return panel;
    }

    private JPanel commonView(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        progressBar = new JProgressBar(0, simulationLength);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        panel.add(progressBar);
        simManager.addPropertyChangeListener("progress", this::progressStatus);
        waitingTasks = new JLabel("Tasks in queue: 0");
        c.gridx = 1;
        c.gridy = 0;
        panel.add(waitingTasks);
        simManager.addPropertyChangeListener("waitingTasks", this::taskQueueStatus);
        return panel;
    }

    private JPanel networkView(){
        simManager.addPropertyChangeListener("servers", this::serverView);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        tabbedPane.setPreferredSize(new Dimension(1200, 600));
        panel.add(tabbedPane, c);
        return panel;

    }

    private JPanel threadView(){
        simManager.addPropertyChangeListener("runtime", this::threadRuntime);
        simManager.addPropertyChangeListener("threads", this::threadCount);
        simManager.addPropertyChangeListener("end", evt -> {
            threadCount.setText("Running Tasks: 0");
            threadDetail.append("Simulation Completed.\n");
        });
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(1200,600));
        GridBagConstraints c = new GridBagConstraints();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(threadCount, c);
        c.gridx = 0;
        c.gridy = 1;
        JScrollPane content = new JScrollPane(threadDetail);
        DefaultCaret caret = (DefaultCaret)threadDetail.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        content.setPreferredSize(new Dimension(1000,400));
        panel.add(content, c);
        return panel;
    }


    private void threadRuntime(PropertyChangeEvent evt){
        long elapsedTime = (long) evt.getNewValue();
        threadDetail.append("Task terminated in: " + elapsedTime + "ns\n");
    }

    private void threadCount(PropertyChangeEvent evt){
        int count = (int) evt.getNewValue();
        threadCount.setText("Running tasks: "+count);
    }

    private JTextArea serverDetail(String serverName, ServerState state){
        JTextArea text = serverData.get(serverName);
        if(text == null){
            JTextArea newData = new JTextArea();
            newData.setEditable(false);
            DefaultCaret caret = (DefaultCaret)newData.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            newData.append(formatServerState(state));
            serverData.put(serverName, newData);
            text = newData;
        }else{
            text.append(formatServerState(state));
        }
        return text;
    }

    private String formatServerState(ServerState state) {
        String formattedString = "";
        formattedString = formattedString.concat(state.toString() + "\n");
        return formattedString;
    }

    private void progressStatus(PropertyChangeEvent evt) {
        int progress = simulationLength - (int) evt.getNewValue();
        progressBar.setValue(progress);
    }

    private void taskQueueStatus(PropertyChangeEvent evt){
        int waitingTasks = (int) evt.getNewValue();
        this.waitingTasks.setText("Tasks in queue: "+waitingTasks);
    }


    private void serverView(PropertyChangeEvent evt){
        ServerState server = (ServerState) evt.getNewValue();
        String serverName = null;
        try{
        serverName = server.getServer().getInetAddress().getHostAddress()+":"+server.getServer().getPort();
        }catch(NullPointerException e){
            System.out.println("debug");
        }
            int index;
            if((index = tabbedPane.indexOfTab(serverName)) == -1){
                tabbedPane.addTab(serverName, new JScrollPane(serverDetail(serverName, server)));
                //frame.pack();
            }
            else
                tabbedPane.setComponentAt(index, new JScrollPane(serverDetail(serverName, server)));
        }


}