import processing.core.PConstants;
import processing.core.PVector;

public class PlayerCharacter extends AbstractDrawable {

    private static final float PC_DIAMETER_DIV = 40f;
    private static final float PC_INCR_DIV = 100f;
    private static final int PC_DEF_ACC_TIME = 5; // frames to max speed from stop
    private static final int PC_DEF_DEC_TIME = 6; // frames to stop from max speed
    private static final float PC_MAX_SPEED_MULT = 0.05f;
    private static final float PC_DRAG_FACTOR = 0.1f; // horizontal drag when mid-air
    private static final float PC_JUMP_IMPULSE = 15f;
    private static final float PC_BOUNCE_MULT = 0.75f; // coefficient of restitution for horizontal collision
    private static final int PC_HANG_TIME_DEF = 2; // frames; default hang time
    private static final int PC_BOUNCE_REMEMBER = 5; // frames; frames before touching the ground for a jump to work
    private static final int PC_COYOTE_TIME = 5; // frames to jump after end of platform

    public static final float PC_MIN_HUE = 280f;
    public static final float PC_MAX_HUE = 360f;
    public static final float PC_MIN_SAT = 0.2f;
    public static final float PC_MAX_SAT = 1f;
    public static final float PC_MIN_LIGHT = 0.9f;
    public static final float PC_MAX_LIGHT = 0.9f;

    public final float incr; // movement increment
    private final float maxSpeed; // max horizontal speed, in incr per frame

    public PVector vel; // velocity, in incr per frame
    public float diameter;

    // vertical movement
    public Platform surface = null; // platform that the PC is on
    public FallState fallState = FallState.FALLING;
    public final int riseFrames; // time taken to reach peak of jump
    public final int fallFrames; // time taken to return to ground after peak
    public final int jumpFrames; // total time taken to return to ground after jump
    private final float jumpRangeIncr; // max horizontal distance travelled in a jump, in incr
    private final float jumpHeightIncr; // vertical jump height at peak, in incr
    public final float jumpRange; // max horizontal distance travelled in a jump, in pixels
    public final float jumpHeight; // vertical jump height at peak, in pixels

    // horizontal movement
    public final float horizontalThrustDef; // default thrust
    public final float horizontalFrictionDef; // default friction force
    private SteerState steerState = SteerState.NEITHER;
    public boolean steerSinceLand = false;
    public boolean movingHorizontally = false;

    // force resolution
    private PVector resultant = new PVector();
    private float iWeight = 1 / 15f; // inverse weight

    // frame counters
    private int jumpMemoryCounter = 0; // trying to jump just before hitting the ground
    private int hangCounter = 0; // peak of jump
    private int coyoteCounter = 0; // jumping just after leaving the edge of a platform

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

    private int riseFrames() {
        // v = 0 = u + at -> t = - u/a
        // underestimating / ignoring drag
        float u = (PC_JUMP_IMPULSE - FallState.RISING.gravity) * iWeight;
        float a = FallState.RISING.gravity * iWeight;
        return (int) Math.floor(u / a);
    }

    private float jumpHeight() {
        // s = ut + 1/2at^2
        // u = initial vertical speed after jump
        // u = jumpImpulse * iWeight (incr per frame)
        // t = riseFrames (frames)
        // a = - (rising.gravity * iWeight) (underestimating / ignoring drag)
        // ... (incr per frame^2)
        float u = (PC_JUMP_IMPULSE - FallState.RISING.gravity) * iWeight; // incr per frame
        return (float) ((u * riseFrames) +
                (0.5f * -(FallState.RISING.gravity * iWeight) *
                        Math.pow(riseFrames, 2)));
    }

    private int fallFrames() {
        // s = ut + 1/2at^2
        // s = jumpHieght (incr)
        // u = 0 (incr per frame)
        // t = ? (frames)
        // a = falling.gravity * iWeight (underestimating / ignoring drag) (incr per
        // ... frame^2)
        return (int) Math.floor(Utils.solveQuadratic(-FallState.FALLING.gravity * iWeight / 2, 0, jumpHeightIncr));
    }

    private float jumpRange() {
        // s = ut
        // constant horizontal velocity
        return maxSpeed * jumpFrames;
    }

    public PlayerCharacter(DontDrown sketch) {
        super(sketch);

        this.pos = new PVector(sketch.width / 2f, sketch.height / 2f);
        this.oldPos = pos.copy();

        this.diameter = sketch.width / PC_DIAMETER_DIV;
        this.vel = new PVector();

        this.incr = sketch.width / PC_INCR_DIV;
        this.maxSpeed = incr * PC_MAX_SPEED_MULT;
        this.horizontalThrustDef = (float) ((2 * maxSpeed) / Math.pow(PC_DEF_ACC_TIME, 2)) / iWeight;
        this.horizontalFrictionDef = (float) ((2 * maxSpeed) / Math.pow(PC_DEF_DEC_TIME, 2)) / iWeight;

        this.riseFrames = riseFrames();
        this.jumpHeightIncr = jumpHeight();
        this.jumpHeight = incr * jumpHeightIncr;
        this.fallFrames = fallFrames();
        this.jumpFrames = riseFrames + PC_HANG_TIME_DEF + fallFrames;
        this.jumpRangeIncr = jumpRange();
        this.jumpRange = jumpRangeIncr * incr;
        System.out.println("");
    }

    private void updateVelocity() {
        // apply gravity
        resultant.y += fallState.gravity;

        // check if at horizontal rest
        if (fallState.equals(FallState.ON_SURFACE) && steerState.equals(SteerState.NEITHER)
                && Math.abs(vel.x) < horizontalFrictionDef / 2) {
            vel.x = 0;
            movingHorizontally = false;
        }

        /// apply horizontal steering
        if (movingHorizontally) {
            switch (steerState) {
                case LEFT:
                    // accelerate left if not at max speed
                    if (vel.x >= -maxSpeed) {
                        resultant.x = -state.pcThrust;
                    } else {
                        vel.x = -maxSpeed;
                    }
                    break;
                case RIGHT:
                    // accelerate right if not at max speed
                    if (vel.x <= maxSpeed) {
                        resultant.x = state.pcThrust;
                    } else {
                        vel.x = maxSpeed;
                    }
                    break;
                case NEITHER:
                    // horizontally deccelerate, dependent upon surface/air
                    if (fallState.equals(FallState.ON_SURFACE)) {
                        resultant.x = -(vel.x / Math.abs(vel.x)) * state.pcFriction;
                    } else {
                        resultant.x = -(vel.x / Math.abs(vel.x)) * PC_DRAG_FACTOR;
                    }
                    break;
            }
        }

        // attempt to jump
        if (jumpMemoryCounter-- >= 0 && fallState.equals(FallState.ON_SURFACE)) {
            jump();
        }

        coyoteCounter--;

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
        if (pos.x == Float.NaN || pos.x < 0 || pos.x > sketch.width) {
            pos.x = oldPos.x; // TODO find out why this happens
        }
        if (pos.x - diameter / 2 <= 0) {
            pos.x = 0 + diameter / 2;
            vel.x = Math.abs(vel.x) * PC_BOUNCE_MULT;
        } else if (pos.x + diameter / 2 >= sketch.width) {
            pos.x = sketch.width - diameter / 2;
            vel.x = -Math.abs(vel.x) * PC_BOUNCE_MULT;
        }
    }

    public void jump() {
        if (fallState.equals(PlayerCharacter.FallState.ON_SURFACE) || coyoteCounter >= 0) {
            fallState = FallState.RISING;
            resultant.y = -PC_JUMP_IMPULSE;
            jumpMemoryCounter = 0;
            if (surface != null)
                surface.supportingPC = false;
            surface = null;
        } else {
            jumpMemoryCounter = PC_BOUNCE_REMEMBER;
        }
    }

    public void land(Platform upon) {
        if (upon != null) {
            fallState = FallState.ON_SURFACE;
            surface = upon;
            surface.supportingPC = true;
            vel.y = 0f;
            pos.y = upon.pos.y - diameter / 2f;
            if (steerState.equals(SteerState.NEITHER)) {
                steerSinceLand = false;
            }
        }
    }

    public void fall() {
        if (!steerSinceLand) {
            // stop the player from sliding off the edge of a platform when they land
            vel.x = -vel.x;
        } else {
            if (surface != null)
                surface.supportingPC = false;
            surface = null;
            fallState = FallState.FALLING;
            coyoteCounter = PC_COYOTE_TIME;
        }
    }

    public void steer(SteerState direction) {
        this.steerState = direction;
        if (direction != SteerState.NEITHER) {
            movingHorizontally = true;
            steerSinceLand = true;
        } // else leave as it was
    }

    public void reset(float x, float y) {
        this.oldPos.x = x;
        this.oldPos.y = y;
        this.pos.x = x;
        this.pos.y = y;
        this.vel.x = 0;
        this.vel.y = 0;
        this.steer(SteerState.NEITHER);
        this.movingHorizontally = false;
        this.steerSinceLand = true;
        this.fall();
        if (this.token != null) {
            this.generateToken();
        }
    }

    protected void generateToken() {
        sketch.colorMode(PConstants.HSB, 360f, 1f, 1f);
        int fillColour = sketch.color(state.stressHSBColour[0], state.stressHSBColour[1], state.stressHSBColour[2]);
        int strokeColour = sketch.color(state.stressHSBColour[0], state.stressHSBColour[1],
                state.stressHSBColour[2] - PC_MIN_LIGHT / 2);

        token = sketch.handDraw(PConstants.ELLIPSE, strokeColour, fillColour, 20, 0, 0, diameter, diameter);
        token.translate(pos.x, pos.y);
    }

    public void render() {
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        if (token == null || frameCounter++ % state.framesPerResketch == 0
                || Math.abs(state.stress - state.oldStress) > 5) {
            generateToken();
        } else if (oldPos.x != pos.x || oldPos.y != pos.y) {
            PVector movement = pos.copy().sub(oldPos);
            token.translate(movement.x, movement.y);
        }

        oldPos = pos.copy();
        sketch.shape(token);

    }
}
