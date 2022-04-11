import processing.core.PVector;

public class PlayerCharacter {

    private static final float PC_DIAMETER_DIV = 40f;
    private static final float PC_INCR_DIV = 100f;
    private static final float PC_MAX_SPEED_MULT = 0.1f;
    private static final float PC_BASE_FORCE = 1f; // base value of forces
    private static final float PC_DRAG_FACTOR = 0.2f; // horizontal drag when mid-air
    private static final float PC_BOUNCE_MULT = 0.5f; // coefficient of restitution for horizontal collision
    private static final int PC_HANG_TIME_DEF = 2; // frames
    private static final int PC_BOUNCE_REMEMBER = 5; // frames

    private final DDSketch sketch;
    private final float incr; // movement increment
    private final float maxSpeed; // max horizontal speed

    public PVector pos; // position
    public PVector vel; // velocity
    private PVector resultant = new PVector();
    public float horizontalThrustMult = 1f;
    public float horizontalDragMult = 0.8f;
    private float jumpImpulseMult = 15f;
    private int jumpMemoryCounter = 0;
    public float diameter;

    public float iWeight = 1 / 15f; // inverse weight
    public FallState fallState = FallState.FALLING;
    private SteerState steerState = SteerState.NEITHER;
    public boolean movingHorizontally = false;
    private int hangCounter = 0;

    public enum FallState {
        ON_SURFACE(0f),
        RISING(0.5f),
        HANG_TIME(0f),
        FALLING(0.7f);

        final float gravity;

        FallState(Float gravity) {
            this.gravity = gravity;
        }
    }

    public enum SteerState {
        LEFT,
        RIGHT,
        NEITHER;
    }

    public SteerState getSteerState() {
        return steerState;
    }

    public PlayerCharacter(DDSketch sketch) {
        this.sketch = sketch;
        this.pos = new PVector(sketch.width / 2f, sketch.height / 2f);
        this.diameter = sketch.width / PC_DIAMETER_DIV;
        this.vel = new PVector();
        this.incr = sketch.width / PC_INCR_DIV;
        maxSpeed = incr * PC_MAX_SPEED_MULT;
    }

    private void updateVelocity() {
        // apply gravity
        resultant.y += fallState.gravity;

        // check if at horizontal rest
        if (steerState.equals(SteerState.NEITHER) && Math.abs(vel.x) < horizontalDragMult * PC_BASE_FORCE / 2) {
            vel.x = 0;
            movingHorizontally = false;
        }

        /// apply horizontal steering
        if (movingHorizontally) {
            switch (steerState) {
                case LEFT:
                    // accelerate left if not at max speed
                    if (vel.x >= -maxSpeed) {
                        resultant.x = -horizontalThrustMult * PC_BASE_FORCE;
                    }
                    break;
                case RIGHT:
                    // accelerate right if not at max speed
                    if (vel.x <= maxSpeed) {
                        resultant.x = horizontalThrustMult * PC_BASE_FORCE;
                    }
                    break;
                case NEITHER:
                    // horizontally deccelerate if on a surface
                    if (fallState.equals(FallState.ON_SURFACE)) {
                        resultant.x = -(vel.x / Math.abs(vel.x)) * horizontalDragMult * PC_BASE_FORCE;
                    } else {
                        resultant.x = -(vel.x / Math.abs(vel.x)) * PC_DRAG_FACTOR * PC_BASE_FORCE;
                    }
                    break;
            }
        }

        // attempt to jump
        if (jumpMemoryCounter-- >= 0 && fallState.equals(FallState.ON_SURFACE)) {
            jump();
        }

        // calulate acceleration and velocity
        PVector acc = resultant.mult(iWeight);
        vel.add(acc);

        // check if peak of jump (i.e. start of hang time) reached
        if (fallState.equals(FallState.RISING) && vel.y >= 0) {
            fallState = FallState.HANG_TIME;
            hangCounter = 0;
            vel.y = 0;
        } else if (fallState.equals(FallState.HANG_TIME) && hangCounter++ >= PC_HANG_TIME_DEF) {
            // check if end of hang time reached
            fallState = FallState.FALLING;
        }

        // reset resultant force
        resultant = new PVector();
    }

    public void integrate() {
        updateVelocity();
        pos.add(vel.copy().mult(incr));
        if (pos.x - diameter / 2 <= 0) {
            pos.x = 0 + diameter / 2;
            vel.x = Math.abs(vel.x) * PC_BOUNCE_MULT;
        } else if (pos.x + diameter / 2 >= sketch.width) {
            pos.x = sketch.width - diameter / 2;
            vel.x = -Math.abs(vel.x) * PC_BOUNCE_MULT;
        }

        if (!fallState.equals(FallState.ON_SURFACE) && pos.y + diameter / 2 >= sketch.height) {
            land(sketch.height);
        }
    }

    public void jump() {
        if (fallState.equals(PlayerCharacter.FallState.ON_SURFACE)) {
            fallState = FallState.RISING;
            resultant.y = -jumpImpulseMult * PC_BASE_FORCE;
            jumpMemoryCounter = 0;
        } else {
            jumpMemoryCounter = PC_BOUNCE_REMEMBER;
        }
    }

    public void land(float y) {
        fallState = FallState.ON_SURFACE;
        vel.y = 0f;
        pos.y = y - diameter / 2f;
    }

    public void steer(SteerState direction) {
        this.steerState = direction;
        if (direction != SteerState.NEITHER) {
            movingHorizontally = true;
        } // else leave as it was
    }

    public void render() {
        sketch.fill(0xFFFFFFFF);
        sketch.circle(pos.x, pos.y, diameter);
    }
}
