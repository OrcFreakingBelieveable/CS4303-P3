import processing.core.PConstants;
import processing.core.PVector;

public class PlayerCharacter extends AbstractDrawable {

    public static final float PC_DIAMETER_DIV = 40f;
    private static final float PC_INCR_DIV = 100f;

    // horizontal movement
    private static final int PC_MIN_ACC_TIME = 10; // frames to max speed from stop
    private static final int PC_MAX_ACC_TIME = 25; // frames to max speed from stop
    private static final int PC_MIN_DEC_TIME = 5; // frames to stop from max speed
    private static final int PC_MAX_DEC_TIME = 20; // frames to stop from max speed
    private static final float PC_MAX_SPEED_MULT = 0.6f; // incr per frame
    private static final float PC_AIR_THRUST_MULT = 0.15f; // horizontal thrust multiplier when mid-air
    private static final float PC_AIR_FRICTION_FACTOR = 0.05f; // horizontal friction multiplier when mid-air

    // vertical movement
    private static final float PC_JUMP_IMPULSE = 12.5f;
    private static final float PC_RISING_GRAVITY = 0.5f;
    private static final float PC_FALLING_GRAVITY = 0.7f;
    private static final float PC_FALLING_DRAG_FACTOR = 0.025f; // vertical drag multiplier when falling
    private static final float PC_BOUNCE_MULT = 0.75f; // coefficient of restitution for horizontal collision
    private static final int PC_HANG_TIME_DEF = 3; // frames of (default) hang time
    private static final int PC_BOUNCE_REMEMBER = 5; // frames before landing on a platform for a jump to work
    private static final int PC_COYOTE_TIME = 2; // frames to jump after falling off the end of a platform

    // rendering
    public static final float PC_MIN_HUE = 280f;
    public static final float PC_MAX_HUE = 360f;
    public static final float PC_MIN_SAT = 0.2f;
    public static final float PC_MAX_SAT = 1f;
    public static final float PC_MIN_LIGHT = 0.9f;
    public static final float PC_MAX_LIGHT = 0.9f;
    public static final float PC_STROKE_ALPHA = 1f;
    public static final float PC_FILL_ALPHA = 0.85f;

    public final float incr; // movement increment
    public final float maxSpeed; // max horizontal speed, in incr per frame

    public PVector vel; // current velocity
    public float diameter;

    // vertical movement
    public Platform surface = null; // platform that the PC is on
    public FallState fallState = FallState.FALLING;
    public final int riseFrames; // time taken to reach peak of jump
    public final int fallFrames; // time taken to return to ground after peak
    public final int jumpFrames; // total time taken to return to ground after jump
    private final float jumpHeightIncr; // vertical jump height at peak, in incr
    public final float jumpRange; // max horizontal distance travelled in a jump, in pixels
    public final float jumpHeight; // vertical jump height at peak, in pixels

    // horizontal movement
    public final float minHorizontalThrust; // default thrust
    public final float maxHorizontalThrust; // default thrust
    public final float minHorizontalFriction; // default friction force
    public final float maxHorizontalFriction; // default friction force
    private SteerState steerState = SteerState.NEITHER;
    private MoveState moveState = MoveState.AT_REST;
    public boolean steerSinceLand = false;

    // force resolution
    private PVector resultant = new PVector();
    public static final float I_MASS = 1 / 15f; // inverse mass

    // frame counters
    private int jumpMemoryCounter = 0; // trying to jump just before hitting the ground
    private int hangCounter = 0; // peak of jump
    private int coyoteCounter = 0; // jumping just after leaving the edge of a platform

    public enum FallState {
        ON_SURFACE(0f),
        COYOTE_TIME(0f),
        RISING(PC_RISING_GRAVITY),
        HANG_TIME(0f),
        FALLING(PC_FALLING_GRAVITY);

        final float gravity;

        FallState(Float gravity) {
            this.gravity = gravity;
        }
    }

    public enum SteerState {
        LEFT(-1),
        RIGHT(1),
        NEITHER(0);

        public final int directionMult;

        SteerState(int directionMult) {
            this.directionMult = directionMult;
        }
    }

    public enum MoveState {
        AT_REST,
        ACCELERATING, // deceleration is acceleration against current velocity
        MAX_SPEED;
    }

    public SteerState getSteerState() {
        return steerState;
    }

    public MoveState getMoveState() {
        return moveState;
    }

    private int riseFrames() {
        // v = 0 = u + at -> t = - u/a
        // underestimating / ignoring drag
        float u = (PC_JUMP_IMPULSE - FallState.RISING.gravity) * I_MASS;
        float a = FallState.RISING.gravity * I_MASS;
        return (int) Math.floor(u / a);
    }

    private float jumpHeight() {
        // s = ut + 1/2at^2
        // u = initial vertical speed after jump
        // u = jumpImpulse * iWeight (incr per frame)
        // t = riseFrames (frames)
        // a = - (rising.gravity * iWeight) (underestimating / ignoring drag)
        // ... (incr per frame^2)
        float u = (PC_JUMP_IMPULSE - FallState.RISING.gravity) * I_MASS; // incr per frame
        return (float) ((u * riseFrames) +
                (0.5f * -(FallState.RISING.gravity * I_MASS) *
                        Math.pow(riseFrames, 2)));
    }

    private int fallFrames() {
        // s = ut + 1/2at^2
        // s = jumpHieght (incr)
        // u = 0 (incr per frame)
        // t = ? (frames)
        // a = falling.gravity * iWeight (underestimating / ignoring drag) (incr per
        // ... frame^2)
        return (int) Math.floor(Utils.solveQuadratic(-FallState.FALLING.gravity * I_MASS / 2, 0, jumpHeightIncr));
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
        this.minHorizontalFriction = (maxSpeed / PC_MAX_DEC_TIME);
        this.maxHorizontalFriction = (maxSpeed / PC_MIN_DEC_TIME);
        this.minHorizontalThrust = maxHorizontalFriction + (maxSpeed / PC_MAX_ACC_TIME);
        this.maxHorizontalThrust = minHorizontalFriction + (maxSpeed / PC_MIN_ACC_TIME);

        this.riseFrames = riseFrames();
        this.jumpHeightIncr = jumpHeight();
        this.jumpHeight = incr * jumpHeightIncr;
        this.fallFrames = fallFrames();
        this.jumpFrames = riseFrames + PC_HANG_TIME_DEF + fallFrames;
        this.jumpRange = jumpRange();
    }

    private void applyHorizontalDrag() {
        // horizontally deccelerate, dependent upon surface/air
        if (fallState.equals(FallState.ON_SURFACE)) {
            resultant.x += state.pcFriction * (vel.x < 0 ? 1 : -1);
        } else {
            resultant.x += state.pcFriction * (vel.x < 0 ? 1 : -1) * PC_AIR_FRICTION_FACTOR;
        }
    }

    private void applyHorizontalThrust() {
        if (Math.abs(vel.x) < maxSpeed || steerState.equals(vel.x < 0 ? SteerState.RIGHT : SteerState.LEFT)) {
            if (fallState.equals(FallState.ON_SURFACE)) {
                resultant.x += state.pcThrust * steerState.directionMult;
            } else {
                resultant.x += state.pcThrust * steerState.directionMult * PC_AIR_THRUST_MULT;
            }
        } else if (Math.abs(vel.x) >= maxSpeed && !steerState.equals(SteerState.NEITHER)) {
            vel.x = maxSpeed * steerState.directionMult;
            moveState = MoveState.MAX_SPEED;
        }
    }

    private void updateVelocity() {
        // apply gravity
        resultant.y += fallState.gravity;

        // jump if landed within 5 frames of pressing the jump button
        if (jumpMemoryCounter-- >= 0 && fallState.equals(FallState.ON_SURFACE)) {
            jump();
        }

        // check if at horizontal rest
        if (!moveState.equals(MoveState.AT_REST) && steerState.equals(SteerState.NEITHER)
                && ((fallState.equals(FallState.ON_SURFACE) && Math.abs(vel.x) < state.pcMinSpeed)
                        || (!fallState.equals(FallState.ON_SURFACE)
                                && Math.abs(vel.x) < state.pcMinSpeed * PC_AIR_FRICTION_FACTOR))) {
            vel.x = 0;
            moveState = MoveState.AT_REST;
        }

        // if at max horizontal speed, check if max speed should be maintained
        if (moveState.equals(MoveState.MAX_SPEED)) {
            if (vel.x > 0 && steerState.equals(SteerState.RIGHT)
                    || vel.x < 0 && steerState.equals(SteerState.LEFT)) {
                // do nothing
            } else {
                moveState = MoveState.ACCELERATING;
            }
        }

        // if not at rest or max speed, apply thrust from steering
        if (moveState.equals(MoveState.ACCELERATING)) {
            applyHorizontalThrust();
        }

        // if not at rest or max speed, apply friction force
        if (moveState.equals(MoveState.ACCELERATING)) {
            applyHorizontalDrag();
        }

        // calulate acceleration and velocity from resultant force
        PVector acc = resultant.mult(I_MASS).mult(incr);
        vel.add(acc);

        // check if peak of jump (i.e. start of hang time) reached, or finished
        if (fallState.equals(FallState.RISING) && vel.y >= 0) {
            fallState = FallState.HANG_TIME;
            hangCounter = 0;
            vel.y = 0;
        } else if (fallState.equals(FallState.HANG_TIME) && hangCounter++ >= PC_HANG_TIME_DEF) {
            // check if end of hang time reached
            fallState = FallState.FALLING;
        } else if (fallState.equals(FallState.FALLING)) {
            // apply drag if falling
            vel.y -= vel.y * PC_FALLING_DRAG_FACTOR;
        } else if (fallState.equals(FallState.COYOTE_TIME) && coyoteCounter-- == 0) {
            fallState = FallState.FALLING;
        }

        // reset resultant force
        resultant = new PVector();
    }

    public void integrate() {
        updateVelocity();
        pos.add(vel);
        if (sketch.level != null) {
            if (pos.x - diameter / 2 <= sketch.level.marginX) {
                pos.x = sketch.level.marginX + diameter / 2;
                vel.x = Math.abs(vel.x) * PC_BOUNCE_MULT;
            } else if (pos.x + diameter / 2 >= sketch.width) {
                pos.x = sketch.width - diameter / 2;
                vel.x = -Math.abs(vel.x) * PC_BOUNCE_MULT;
            }
        }
    }

    public void jump() {
        if (fallState.equals(PlayerCharacter.FallState.ON_SURFACE)
                || fallState.equals(PlayerCharacter.FallState.COYOTE_TIME)) {
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

    private void fall(boolean fall) {
        if (!fall) {
            // stop the player from sliding off the edge of a platform
            this.steer(SteerState.NEITHER);
            if (surface != null) {
                // turn away from nearest edge
                vel.x = Math.abs(vel.x * PC_BOUNCE_MULT) * (pos.x < surface.pos.x + surface.width / 2 ? 1 : -1);
            } else {
                // turn around
                vel.x = -vel.x * PC_BOUNCE_MULT;
            }
        } else {
            if (surface != null)
                surface.supportingPC = false;
            surface = null;
            fallState = FallState.COYOTE_TIME;
            coyoteCounter = PC_COYOTE_TIME;
        }
    }

    public void fall() {
        if (steerState.equals(SteerState.NEITHER)
                || vel.x < 0 && steerState.equals(SteerState.RIGHT)
                || vel.x > 0 && steerState.equals(SteerState.LEFT)) {
            fall(false);
        } else {
            fall(true);
        }
    }

    public void steer(SteerState direction) {
        if (!direction.equals(steerState)) {
            moveState = MoveState.ACCELERATING;
            this.steerState = direction;
        }
        if (!direction.equals(SteerState.NEITHER))
            steerSinceLand = true;
    }

    public void reset(float x, float y) {
        this.oldPos.x = x;
        this.oldPos.y = y;
        this.pos.x = x;
        this.pos.y = y;
        this.vel.x = 0;
        this.vel.y = 0;
        this.steer(SteerState.NEITHER);
        moveState = MoveState.AT_REST;
        this.steerSinceLand = true;
        this.fall(true);
        if (this.token != null) {
            this.generateToken();
        }
    }

    protected void generateToken() {
        sketch.colorModeHSB();
        int fillColour = sketch.color(state.stressHSBColour[0], state.stressHSBColour[1], state.stressHSBColour[2],
                PC_FILL_ALPHA);
        int strokeColour = sketch.color(state.stressHSBColour[0], state.stressHSBColour[1],
                state.stressHSBColour[2] - PC_MIN_LIGHT / 2, PC_STROKE_ALPHA);

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
