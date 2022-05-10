import processing.core.PShape;
import processing.core.PVector;

/**
 * An extension of AbstractDrawable for objects that can be collected during a level. 
 * 
 * Would have been used for powerups if they were implemented. 
 */
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
