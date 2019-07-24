package quasylab.sibilla.core.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.border.Border;

public class SimulationView<S> {
    private JFrame frame = new JFrame("Sibilla");
    private SimulationManager<S> simManager;
    private String type;
    private int simulationLength;
    private JProgressBar progressBar;
    private JTabbedPane tabbedPane;
    private JLabel waitingTasks;
    private Map<Socket,JTextArea> serverData = new HashMap<>();
    /*
     * private static SimulationView instance;
     * 
     * public static SimulationView getSimulationView(){ if(instance == null)
     * instance = new SimulationView(); return instance; }
     */

    public SimulationView(int iterations, SimulationManager<S> simManager) {
        this.simManager = simManager;
        this.simulationLength = iterations;
        type = simManager.getClass().getName();
        frame.setContentPane(createView(type));
        frame.getContentPane().setPreferredSize(new Dimension(1300, 700));
        frame.pack();
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
        return new JPanel();
    }

    private JTextArea serverDetail(Socket socket, ServerState state){
        JTextArea text = serverData.get(socket);
        if(text == null){
            JTextArea newData = new JTextArea();
            //newData.setPreferredSize(new Dimension(410, 50));
            newData.append(formatServerState(state));
            serverData.put(socket, newData);
            text = newData;
        }else{
            text.append(formatServerState(state));
        }
        return text;
    }

    private String formatServerState(ServerState state) {
        return state.toString() + "\n";
    }

    private void progressStatus(PropertyChangeEvent evt) {
        int progress = simulationLength - (int) evt.getNewValue();
        progressBar.setValue(progress);
    }

    private void taskQueueStatus(PropertyChangeEvent evt){
        int waitingTasks = (int) evt.getNewValue();
        this.waitingTasks.setText("Tasks in queue: "+waitingTasks);
    }

    @SuppressWarnings("unchecked")
    private void serverView(PropertyChangeEvent evt){
        Map<Socket, ServerState> servers = (Map<Socket, ServerState>) evt.getNewValue();
        for(Map.Entry<Socket, ServerState> entry : servers.entrySet()){
            int index;
            if((index = tabbedPane.indexOfTab(entry.getKey().toString())) == -1){
                tabbedPane.addTab(entry.getKey().toString(), new JScrollPane(serverDetail(entry.getKey(), entry.getValue())));
                frame.pack();
            }
            else
                tabbedPane.setComponentAt(index, new JScrollPane(serverDetail(entry.getKey(), entry.getValue())));
        }
    }


}