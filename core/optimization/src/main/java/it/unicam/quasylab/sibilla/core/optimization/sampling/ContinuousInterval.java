package it.unicam.quasylab.sibilla.core.optimization.sampling;


public class ContinuousInterval extends AbstractInterval{


    public ContinuousInterval(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }

    public ContinuousInterval(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }

    @Override
    public void scale(double scaleFactor) {
        double newLength = this.length() * scaleFactor;
        super.lowerBound = this.center() - newLength/2;
        super.upperBound = this.center() + newLength/2;
    }

    @Override
    public double getRandomValue() {
        return super.rand.nextDouble() * (upperBound - lowerBound) + lowerBound;
    }

    @Override
    public void changeCenter(double newCenter) {
        this.lowerBound = newCenter - this.length()/2;
        this.upperBound = newCenter + this.length()/2;
    }

    @Override
    public boolean contains(double value) {
        return value >= this.lowerBound && value<= this.upperBound;
    }

    @Override
    public double getIntervalValueClosestTo(double value) {
        if(value < this.lowerBound)
            return this.lowerBound;
        return Math.min(value, this.upperBound);
    }

    @Override
    public Interval getDeepCopy() {
        return new ContinuousInterval(this.id,this.lowerBound,this.upperBound);
    }

}
