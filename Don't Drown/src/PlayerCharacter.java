import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;

public class PlayerCharacter {

    private static final float PC_DIAMETER_DIV = 40f;
    private static final float PC_INCR_DIV = 100f;
    private static final int PC_DEF_ACC_TIME = 5; // frames to max speed from stop
    private static final int PC_DEF_DEC_TIME = 6; // frames to stop from max speed
    private static final float PC_MAX_SPEED_MULT = 0.05f;
    private static final float PC_DRAG_FACTOR = 0.1f; // horizontal drag when mid-air
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

    private final DontDrown sketch;
    private final LevelState state;
    private final float incr; // movement increment
    private final float maxSpeed; // max horizontal speed

    public PVector pos; // position
    public PVector vel; // velocity
    public float diameter;

    // vertical movement
    public Platform surface = null; // platform that the PC is on
    public FallState fallState = FallState.FALLING;

    // horizontal movement
    public final float horizontalThrustDef; // default thrust
    public final float horizontalFrictionDef; // default friction force
    private SteerState steerState = SteerState.NEITHER;
    private boolean steerSinceLand = false;
    public boolean movingHorizontally = false;

    // force resolution
    private PVector resultant = new PVector();
    private float iWeight = 1 / 15f; // inverse weight
    private float jumpImpulseMult = 15f;

    // frame counters
    private int jumpMemoryCounter = 0; // trying to jump just before hitting the ground
    private int hangCounter = 0; // peak of jump
    private int coyoteCounter = 0; // jumping just after leaving the edge of a platform

    // rendering
    private int frameCounter = 0;
    private final int nFrames;
    private final int framesPerChange = 60;

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

    public PlayerCharacter(DontDrown sketch, LevelState state) {
        this.sketch = sketch;
        this.state = state;
        this.pos = new PVector(sketch.width / 2f, sketch.height / 2f);
        this.diameter = sketch.width / PC_DIAMETER_DIV;
        this.vel = new PVector();
        this.incr = sketch.width / PC_INCR_DIV;
        this.maxSpeed = incr * PC_MAX_SPEED_MULT;
        this.horizontalThrustDef = (float) ((2 * maxSpeed) / Math.pow(PC_DEF_ACC_TIME, 2)) / iWeight;
        this.horizontalFrictionDef = (float) ((2 * maxSpeed) / Math.pow(PC_DEF_DEC_TIME, 2)) / iWeight;
        this.nFrames = sketch.pcTokens.length;
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
                        resultant.x = -state.pcThrust();
                    } else {
                        vel.x = -maxSpeed;
                    }
                    break;
                case RIGHT:
                    // accelerate right if not at max speed
                    if (vel.x <= maxSpeed) {
                        resultant.x = state.pcThrust();
                    } else {
                        vel.x = maxSpeed;
                    }
                    break;
                case NEITHER:
                    // horizontally deccelerate if on a surface
                    if (fallState.equals(FallState.ON_SURFACE)) {
                        resultant.x = -(vel.x / Math.abs(vel.x)) * state.pcFriction();
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
            resultant.y = -jumpImpulseMult;
            jumpMemoryCounter = 0;
            surface = null;
        } else {
            jumpMemoryCounter = PC_BOUNCE_REMEMBER;
        }
    }

    public void land(Platform upon) {
        fallState = FallState.ON_SURFACE;
        surface = upon;
        vel.y = 0f;
        pos.y = upon.pos.y - diameter / 2f;
        if (steerState.equals(SteerState.NEITHER)) {
            steerSinceLand = false;
        }
    }

    public void fall() {
        if (!steerSinceLand) {
            // stop the player from sliding off the edge of a platform when they land
            vel.x = -vel.x;
        } else {
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

    public void render() {
        frameCounter = (frameCounter + 1) % (nFrames * framesPerChange);
        PImage token = sketch.pcTokens[frameCounter / framesPerChange].copy();
        token.loadPixels(); // will be grayscale
        sketch.colorMode(PConstants.HSB, 360f, 1f, 1f);
        float[] colour = state.pcHSBColour();
        for (int i = 0; i < token.height * token.width; i++) {
            int color = token.pixels[i];
            if (((color >> 24) & 0xFF) > 0) {
                // not a transparent pixel
                token.pixels[i] = PImage.blendColor(color, sketch.color(colour[0], colour[1], colour[2]),
                        PConstants.DARKEST);
            }
        }
        token.updatePixels();
        sketch.imageMode(PConstants.CENTER);
        sketch.image(token, pos.x, pos.y);
        sketch.colorMode(PConstants.RGB, 255, 255, 255);
    }
}
