import processing.core.PShape;
import processing.core.PVector;

public abstract class AbstractCollectable extends AbstractDrawable {

    public final PVector initPos;
    public boolean collected = false;

    protected AbstractCollectable(DontDrown sketch, PShape[][] tokens, float x, float y) {
        super(sketch, tokens);
        this.pos = new PVector(x, y);
        this.initPos = pos.copy();
    }

    protected void resetAC() {
        this.pos = initPos.copy();
        this.collected = false;
    }

    public void render() {
        if (!collected) {
           renderAD(); 
        }
    }

}
