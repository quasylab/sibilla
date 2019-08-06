package quasylab.sibilla.core.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
    private String type, session;
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

    public SimulationView(SimulationSession<S> session, SimulationManager<S> simManager) {
        this.simManager = simManager;
        this.simulationLength = session.getExpectedTasks();
        this.session = session.toString();
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
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(commonView(), c);
        c.anchor = GridBagConstraints.SOUTH;
        c.gridx = 0;
        c.gridy = 0;
        switch(type){
            case "quasylab.sibilla.core.simulator.ThreadSimulationManager": panel.add(threadView(), c); break;
            case "quasylab.sibilla.core.simulator.NetworkSimulationManager": panel.add(networkView(), c); break;
            case "quasylab.sibilla.core.simulator.SequentialSimulationManager": panel.add(sequentialView(), c); break;
            default: break;
        }
        return panel;
    }

    private JPanel commonView(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        progressBar = new JProgressBar(0, simulationLength);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(800,30));
        JPanel progressPanel = new JPanel();
        progressPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        progressPanel.add(new JLabel("Execution progress: "));
        progressPanel.add(progressBar);
        panel.add(progressPanel, c);
        simManager.addPropertyChangeListener("progress"+session, this::progressStatus);
        JPanel queuePanel = new JPanel();
        waitingTasks = new JLabel("Tasks in queue: 0");
        queuePanel.add(waitingTasks);
        queuePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 1;
        panel.add(queuePanel, c);
        simManager.addPropertyChangeListener("waitingTasks"+session, this::taskQueueStatus);
        return panel;
    }

    private JPanel sequentialView(){
        JPanel panel = new JPanel();
        JLabel simulationManager = new JLabel("Sequential Simulation Manager");
        simulationManager.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        panel.add(simulationManager);
        return panel;
    }

    private JPanel networkView(){
        simManager.addPropertyChangeListener("servers"+session, this::serverView);
        simManager.addPropertyChangeListener("servers", this::serverView);
        simManager.addPropertyChangeListener("end"+session, evt -> {
            serverData.values().forEach(x->x.append("Simulation Completed."));
        });
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JLabel simulationManager = new JLabel("Network Simulation Manager");
        simulationManager.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(simulationManager,c);
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0,0,0,0);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Simulation Log:"), c);
        c.gridx = 0;
        c.gridy = 2;
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        tabbedPane.setPreferredSize(new Dimension(1200, 500));
        panel.add(tabbedPane, c);
        return panel;

    }

    private JPanel threadView(){
        simManager.addPropertyChangeListener("runtime"+session, this::threadRuntime);
        simManager.addPropertyChangeListener("threads"+session, this::threadCount);
        simManager.addPropertyChangeListener("end"+session, this::endThreadSimulation);
        JPanel panel = new JPanel(new GridBagLayout());
        //panel.setPreferredSize(new Dimension(1200,600));
        GridBagConstraints c = new GridBagConstraints();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JLabel simulationManager = new JLabel("Multithreading Simulation Manager");
        simulationManager.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10,10,10,10);
        panel.add(simulationManager, c);
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0,0,0,0);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Simulation Log:"), c);
        c.anchor = GridBagConstraints.SOUTH;
        c.gridx = 0;
        c.gridy = 2;
        JScrollPane content = new JScrollPane(threadDetail);
        DefaultCaret caret = (DefaultCaret)threadDetail.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        content.setPreferredSize(new Dimension(1000,400));
        panel.add(content, c);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(10,0,10,10);
        panel.add(threadCount, c);
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

    private void endThreadSimulation(PropertyChangeEvent evt){
        String[] str = evt.getNewValue().toString().split(";");
        threadCount.setText("Running Tasks: 0");
        threadDetail.append("Simulation Completed.\n\n\nSimulation statistics:\n");
        threadDetail.append("Concurrent tasks: "+str[0]+"\nPool size: "+str[1]+"\nAverage runtime: "+str[2]+"ns\nMaximum runtime: "+str[3]+"ns\nMinimum runtime: "+str[4]+"ns\n");
    }


    private void serverView(PropertyChangeEvent evt){
        ServerState server = (ServerState) evt.getNewValue();
        String serverName = null;
        try{
        serverName = server.getServer().getSocket().getInetAddress().getHostAddress()+":"+server.getServer().getSocket().getPort();
        }catch(NullPointerException e){
            System.out.println("debug");
        }
            int index;
            if((index = tabbedPane.indexOfTab(serverName)) == -1){
                JScrollPane scrollPane = new JScrollPane(serverDetail(serverName, server));
                //scrollPane.setPreferredSize(new Dimension(1000,400));
                tabbedPane.addTab(serverName, scrollPane);
            }
            else
                serverDetail(serverName, server);
        }


}