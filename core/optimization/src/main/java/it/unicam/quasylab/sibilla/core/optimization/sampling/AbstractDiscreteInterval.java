package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.util.List;

public abstract class AbstractDiscreteInterval extends AbstractInterval{

    protected List<Double> sequenceOfElement;

    public AbstractDiscreteInterval(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }

    public AbstractDiscreteInterval(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Returns the number of elements in this discrete interval
     * @return elements in the discrete interval
     */
    public int size(){
        return sequenceOfElement.size();
    }

    @Override
    public void scale(double scaleRatio){
        sequenceOfElement = sequenceOfElement.stream().map(element -> element*scaleRatio).toList();
        relocatesBounds();
    }

    @Override
    public double getRandomValue(){
        return  sequenceOfElement.get(rand.nextInt(this.sequenceOfElement.size()));
    }

    @Override
    public void changeCenter(double newCenter){
        sequenceOfElement = sequenceOfElement.stream().map(element -> element+newCenter).toList();
        relocatesBounds();
    }

    protected void relocatesBounds(){
        super.lowerBound = sequenceOfElement.get(0);
        super.upperBound = sequenceOfElement.get(sequenceOfElement.size()-1);
    }

    @Override
    public boolean contains(double value) {
        return this.sequenceOfElement.contains(value);
    }

    public List<Double> getSequenceOfElement() {
        return sequenceOfElement;
    }

    @Override
    public String toString() {
        String rtn = "Interval id=" + super.id +"\n [";
        if(sequenceOfElement.size()>0){
            if(sequenceOfElement.size()<=10){
                for (double d: sequenceOfElement) {
                    rtn += "    "+d;
                }
            }else{
                rtn +=  "    " + sequenceOfElement.get(0) +
                        "    " + sequenceOfElement.get(1) +
                        "  ...  "+
                        "    " + sequenceOfElement.get(sequenceOfElement.size()-2) +
                        "    " + sequenceOfElement.get(sequenceOfElement.size()-1);
            }
        }
        rtn += "    ]";
        return rtn;
    }

}
