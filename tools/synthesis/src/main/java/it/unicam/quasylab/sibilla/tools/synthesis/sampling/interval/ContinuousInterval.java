package it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval;


public class ContinuousInterval extends AbstractInterval {

    public ContinuousInterval(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }

    public ContinuousInterval(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }

    @Override
    public void scale(double scaleFactor) {
        double newLength = this.length() * scaleFactor;
        double newLowerBound = this.center() - newLength/2;
        double newUpperBound = this.center() + newLength/2;
        this.lowerBound = newLowerBound;
        this.upperBound = newUpperBound;
    }

    @Override
    public double getRandomValue() {
        return super.rand.nextDouble() * (upperBound - lowerBound) + lowerBound;
    }

    @Override
    public void changeCenter(double newCenter) {
        double newLowerBound = newCenter - this.length()/2;
        double newUpperBound = newCenter + this.length()/2;
        this.lowerBound = newLowerBound;
        this.upperBound = newUpperBound;
    }

    @Override
    public boolean contains(double value) {
        return value >= this.lowerBound && value<= this.upperBound;
    }

    @Override
    public double getClosestValueTo(double value) {
        if(value < this.lowerBound)
            return this.lowerBound;
        return Math.min(value, this.upperBound);
    }

    @Override
    public Interval getDeepCopy() {
        return new ContinuousInterval(this.id,this.lowerBound,this.upperBound);
    }



    @Override
    public String toString() {
        return "Continuous "+super.toString();
    }

}
