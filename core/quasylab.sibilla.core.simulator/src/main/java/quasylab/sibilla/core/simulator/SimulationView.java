package quasylab.sibilla.core.simulator;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
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

    public SimulationView(SimulationSession<S> session, SimulationManager<S> simManager) {
        this.simManager = simManager;
        this.simulationLength = session.getExpectedTasks();
        this.session = session.toString();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {                   
            e.printStackTrace();
        }
        type = simManager.getClass().getName();
        frame.setContentPane(createView(type));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private JPanel createView(String type){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 0.3;
        c.fill = GridBagConstraints.BOTH;
        panel.add(commonView(), c);
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.7;
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
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        progressBar = new JProgressBar(0, simulationLength);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        JPanel progressPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        progressPanel.add(new JLabel("Execution progress: "), c);
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1.0;
        progressPanel.add(progressBar, c);
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1.0;
        panel.add(progressPanel, c);
        simManager.addPropertyChangeListener("progress"+session, this::progressStatus);
        JPanel queuePanel = new JPanel(new GridBagLayout());
        waitingTasks = new JLabel("Tasks in queue: 0");
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.WEST;
        queuePanel.add(waitingTasks, c);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 1;
        panel.add(queuePanel, c);
        simManager.addPropertyChangeListener("waitingTasks"+session, this::taskQueueStatus);
        return panel;
    }

    private JPanel sequentialView(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTH;
        JLabel simulationManager = new JLabel("Sequential Simulation Manager");
        simulationManager.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        panel.add(simulationManager, c);
        return panel;
    }

    private JPanel networkView(){
        simManager.addPropertyChangeListener("servers"+session, this::serverView);
        simManager.addPropertyChangeListener("end"+session, evt -> {
            serverData.values().forEach(x->x.append("Simulation Completed."));
        });
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
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
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        panel.add(tabbedPane, c);
        return panel;

    }

    private JPanel threadView(){
        simManager.addPropertyChangeListener("runtime"+session, this::threadRuntime);
        simManager.addPropertyChangeListener("threads"+session, this::threadCount);
        simManager.addPropertyChangeListener("end"+session, this::endThreadSimulation);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,2,true));
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
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        JScrollPane content = new JScrollPane();
        content.setViewportView(threadDetail);
        content.setPreferredSize(panel.getSize());
        DefaultCaret caret = (DefaultCaret)threadDetail.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(content, c);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        c.weighty = 0.0;
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

    private JTextArea serverDetail(String serverName, String state){
        JTextArea text = serverData.get(serverName);
        if(text == null){
            JTextArea newData = new JTextArea();
            newData.setEditable(false);
            DefaultCaret caret = (DefaultCaret)newData.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            newData.append(state+"\n");
            serverData.put(serverName, newData);
            text = newData;
        }else{
            text.append(state+"\n");
        }
        return text;
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
        String[] data = (String[]) evt.getNewValue();
        String serverState = data[1];
        String serverName = data[0];
        if(tabbedPane.indexOfTab(serverName) == -1){
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView(serverDetail(serverName, serverState));
            scrollPane.setPreferredSize(tabbedPane.getSize());
            tabbedPane.addTab(serverName, scrollPane);
        }
        else
            serverDetail(serverName, serverState);
        }


}