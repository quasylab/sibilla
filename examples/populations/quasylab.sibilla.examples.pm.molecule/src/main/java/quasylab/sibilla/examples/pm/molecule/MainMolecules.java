package quasylab.sibilla.examples.pm.molecule;

import quasylab.sibilla.core.simulator.SimulationEnvironment;

import java.util.ArrayList;
import java.util.Random;

public class MainMolecules {

    public static void main (String[] args) throws InterruptedException {

        /*
        MoleculeDefiniton def = new MoleculeDefiniton();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SimulationEnvironment.silent = false;
        final double lambda = 1;


        double increment = 0.245;
        double[] results = new double[11];

        for (int i = 0; i <= 12 ; i++) {
            final double percentage = increment;
            int occupancy = i;
            double p = simulator.reachability(0.01,0.01,0.005,def.createModel(lambda),
                    def.state(10,10,0,0),
                    s ->(s.getOccupancy(MoleculeDefiniton.NaPositive))>=occupancy);
            increment = increment + 0.0001;
            results[i] = p;
            System.out.println(""+occupancy+"->"+p+"****\n\n");

        }

        for( int i=0 ; i<=12 ; i++ ) {
            System.out.println(i+"->"+results[i]);
        }
        */





        // in realtà è inutile creare una dataset per poi creare un training Set
        // basta un metodo che crea direttamente il training set

        ArrayList<DatasetPoint> dataset = generateDataSet(10000);
        ArrayList<DatasetPoint> trainingSet = generateTrainingSet(dataset);
        //trainingSet.sort(DatasetPoint::compareTo);
        System.out.println("");
        for (DatasetPoint p: trainingSet) {
            System.out.println(p.getCSVString());
        }

    }


    public static ArrayList<DatasetPoint> generateTrainingSet(ArrayList<DatasetPoint> dataset) throws InterruptedException {

        int trainingSetSize = dataset.size();

        MoleculeDefiniton def = new MoleculeDefiniton();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SimulationEnvironment.silent = false;

        for(int i = 0; i < trainingSetSize; i++){

            double numberNa = dataset.get(i).datasetPointInput.get(0);
            double deadline = dataset.get(i).datasetPointInput.get(1);
            double lambda = 1.0;

            int occupancy = getRandomNumber(1,(int) numberNa/4);

            double p = simulator.reachability(0.01,0.01,deadline,def.createModel(lambda),
                    def.state(numberNa,numberNa,0,0),
                    s ->(s.getOccupancy(MoleculeDefiniton.NaPositive))>=1);

            dataset.get(i).datasetPointOutput.set(0,p);
        }

        return dataset;
    }

    public static ArrayList<DatasetPoint> generateDataSet(int size,double rangeMin, double rangeMax, double deadlineMin, double deadlineMax){

        ArrayList<DatasetPoint> dataset = new ArrayList<>();

        for(int i = 0; i < size; i++){
            double randomValueLambda = getRandomNumber(rangeMin,rangeMax);
            double randomValueDeadline = getRandomNumber(deadlineMin,deadlineMax);
            DatasetPoint data = new DatasetPoint();
            data.addSingleInput(randomValueLambda);
            data.addSingleInput(randomValueDeadline);
            data.addSingleOutput(0.0);
            dataset.add(data);
        }

        return dataset;
    }

    public static ArrayList<DatasetPoint> generateDataSet(int size){

        ArrayList<DatasetPoint> dataset = new ArrayList<>();

        for(int i = 0; i < size; i++){
            int randomValueLambda = getRandomNumber(5,15);
            double randomValueDeadline = getRandomNumber(0,0.002);
            DatasetPoint data = new DatasetPoint();
            data.addSingleInput(randomValueLambda);
            data.addSingleInput(randomValueDeadline);
            data.addSingleOutput(0.0);
            dataset.add(data);
        }

        return dataset;
    }

    public static double getRandomNumber(double min, double max) {
        Random r = new Random();
        double randomValue = min + (max - min) * r.nextDouble();
        return randomValue;
    }

    public static int getRandomNumber(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }


}
