package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.util.Signal;

import java.util.LinkedList;

public class NewSlidingWindow {
//
//    final double from;
//    final double to;
//
//    final double size;
//
//
//    public NewSlidingWindow(double from, double to) {
//        this.from = from;
//        this.to = to;
//        this.size = to-from;
//    }
//
//    public Signal apply(Signal signal){
//        Signal z = new Signal();
//        LinkedList<Sample<Double>> w = new LinkedList<>();
//        Signal y = signal.trimmedSignal(from);
//        LinkedList<Sample<Double>> samples = y.getValues();
//
//        int sampleIndex = 0;
//        enqueueToTheWindow(w,samples.get(sampleIndex++));
//
//        while((w.getFirst().getTime() + this.size) < signal.last().getTime()){
//            add(z,w,samples.get(sampleIndex));
//            sampleIndex++;
//        }
//
//        if(isFull(w)){
//            Sample<Double> doubleSample = w.removeFirst();
//            z.add(doubleSample.getTime()-from, doubleSample.getValue());
//        }
//
//        manageLastSamplesInWindow(z,w,signal.getEnd());
//
//        z.setEnd(samples.getLast().getTime());
//        return z;
//    }
//
//    private void manageLastSamplesInWindow(Signal z, LinkedList<Sample<Double>> w, double end) {
//        // False when w is empty or the window go over the end of the signal
//       while ( !w.isEmpty() && (end - w.getFirst().getTime()) >= size){
//           Sample<Double> dequeuedSample = w.removeFirst();
//           z.add( dequeuedSample.getTime() -from, dequeuedSample.getValue() );
//
//           //Riaggiungere l'elemento se la distanza con quello subito dopo è minore con la distanza
//           // tra la fine della finestra e quello successivo
//       }
//    }
//
//    private boolean isFull(LinkedList<Sample<Double>> w) {
//        if (!w.isEmpty())
//            return (w.getLast().getTime()-w.getFirst().getTime()) == size;
//         else
//            return false;
//    }
//
//    private void add(Signal z, LinkedList<Sample<Double>> w, Sample<Double> sample) {
//        // true if w is not empty and sample.t is out of the window
//        while ( !w.isEmpty() && ( sample.getTime() - w.getFirst().getTime() > size) ) {
//            // de-enqueuing the element make w.getFirst() bigger
//            Sample<Double> dequeuedSample = w.removeFirst();
//            z.add( dequeuedSample.getTime() - from, dequeuedSample.getValue() );
//        }
//        // add to win
//        enqueueToTheWindow(w, sample);
//    }
//
//    private void enqueueToTheWindow(LinkedList<Sample<Double>> w, Sample<Double> sample) {
//
//        double time = sample.getTime();
//        double value = sample.getValue();
//
//        // true if w is not empty and the last value of the value is <= to the current value
//        while (!w.isEmpty() && w.getLast().getValue() <= value) {
//            // remove the last and set te time at the time of the last removed
//            time = w.removeLast().getTime();
//        }
//        // add to the window the value removing all the smallest ones
//        w.add(new Sample<>(time, value));
//    }
//
//

}
