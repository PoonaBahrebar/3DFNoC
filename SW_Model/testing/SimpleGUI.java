package testing;

import ipCore.TrafficGenerator;
import mesh.ClusteredMesh;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleGUI extends JFrame {

    private int runState;
    JPanel panel;
    private JLabel processLabel;
    private JLabel clusterLabel;
    private JLabel radixLabel;
    private JLabel probLabel;
    private JLabel trafficLabel;
    private JLabel adaptiveLabel;
    private JLabel exitLabel;
    private JButton button;


    public SimpleGUI(){

        this.runState = 0;

        panel= new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        setTitle("SiMoNoC3D");
        processLabel = new JLabel("Simulation is running. Do not close screen!");
        clusterLabel = new JLabel("Cluster size:");
        radixLabel = new JLabel("Radix: ");
        trafficLabel = new JLabel("Traffic pattern: ");
        adaptiveLabel = new JLabel("Routing: XYZ");
        probLabel = new JLabel("Prob: " );
        panel.add(processLabel);
        panel.add(clusterLabel);
        panel.add(radixLabel);
        panel.add(trafficLabel);
        panel.add(adaptiveLabel);
        panel.add(probLabel);


        button = new JButton("Stop simulation");
        panel.add(button);

        event f = new event();
        button.addActionListener(f);

        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 175);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        setVisible(true);
    }

    public void updateRadix(int radix){
        this.radixLabel.setText("Radix: " + radix);
    }

    public void updateTraffic(int trafficPattern){
        if(trafficPattern == TrafficGenerator.UNIFORM){
            this.trafficLabel.setText("Traffic pattern: UNIFORM");
        }else if (trafficPattern == TrafficGenerator.HOTSPOT){
            this.trafficLabel.setText("Traffic pattern: HOTSPOT");
        } else if(trafficPattern == TrafficGenerator.RENT){
            this.trafficLabel.setText("Traffic pattern: RENTIAN");
        }
    }

    public void updateAdaptive(boolean adaptive){
        if(adaptive)
            this.adaptiveLabel.setText("Routing: Minimal Adaptive");
    }

    public void updateProb(int prob){
        this.probLabel.setText("Prob: " + prob+ "/1000");
    }

    public void setClusterLabel(int sizeX, int sizeY, int sizeZ){
        clusterLabel.setText("Cluster size: " + sizeX + "x" + sizeY + "x" + sizeZ);
    }

    public void finish(){
        this.processLabel.setText("Simulation ended correctly.");
        button.setText("Exit");
        runState = 1;
        exitLabel = new JLabel("Screen will close in 10");
        panel.add(exitLabel);
        for(int i = 0; i < 10; i++){
            exitLabel.setText("Screen will close in " + (10-i));
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex){
                System.out.println("Error: " +ex);
            }
        }
        System.exit(0);

    }


    public class event implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent f) {
            if(runState == 0) {
                processLabel.setText("Stopped process");
                button.setText("Exit");
                runState++;
            } else {
                System.exit(0);
            }

        }
    }

}
