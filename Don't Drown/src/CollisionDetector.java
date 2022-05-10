import java.util.ArrayList;
import java.util.Comparator;

import processing.core.PConstants;
import processing.core.PVector;

public class CollisionDetector {

    private final DontDrown sketch;

    public PVector pcOldPos;
    public ArrayList<Platform> sortedPlatforms;
    public ArrayList<Token> sortedTokens;

    public CollisionDetector(DontDrown sketch) {
        this.sketch = sketch;
        pcOldPos = sketch.pc.pos.copy();
    }

    /** To be called at the start of a level */
    public void sortLists() {
        sortedPlatforms = new ArrayList<>(sketch.level.platforms);
        sortedPlatforms.sort(new Comparator<Platform>() {

            @Override
            public int compare(Platform o1, Platform o2) {
                return Math.round(o2.pos.y - o1.pos.y);
            }

        });

        sortedTokens = new ArrayList<>(sketch.level.tokens);
        sortedTokens.sort(new Comparator<Token>() {

            @Override
            public int compare(Token o1, Token o2) {
                return Math.round(o2.pos.y - o1.pos.y);
            }

        });
    }

    /* Finds the horizontal position of the PC when it was at a given height */
    private float getXAtYOverlap(PlayerCharacter pc, PVector dir, float otherY) {
        if (dir.y != 0) {
            if (dir.x < 0) {
                return Math.min(pc.pos.x,
                        pcOldPos.x + (dir.x * ((otherY - (pcOldPos.y + PlayerCharacter.diameter / 2)) / dir.y)));
            } else {
                return Math.max(pc.pos.x,
                        pcOldPos.x + (dir.x * ((otherY - (pcOldPos.y + PlayerCharacter.diameter / 2)) / dir.y)));
            }
        } else {
            return -1;
            // ignore x
        }
    }

    private void detectPlatformCollisions(PlayerCharacter pc, PVector dir) {
        if (pc.fallState.equals(PlayerCharacter.FallState.FALLING)
                || pc.fallState.equals(PlayerCharacter.FallState.DROPPING)) {
            for (Platform platform : sortedPlatforms) {
                if (platform.pos.y < pcOldPos.y) {
                    // platform too high
                    // cut off search
                    break;
                } else if (platform.pos.y > (pc.pos.y + PlayerCharacter.radius)) {
                    // platform too low
                    // continue search
                } else if (pc.fallState.equals(PlayerCharacter.FallState.DROPPING) && platform.equals(pc.surface)) {
                    // ignore the platform that the PC is falling through
                    // continue search
                } else {
                    // platform vertically between oldPos and pos + PlayerCharacter.radius
                    // check horizontal overlap to confirm collision
                    float xAtYOverlap = getXAtYOverlap(pc, dir, platform.pos.y);
                    if (xAtYOverlap >= platform.pos.x && xAtYOverlap <= platform.pos.x + platform.width
                            || pc.pos.x >= platform.pos.x && pc.pos.x <= platform.pos.x + platform.width
                            || pcOldPos.x >= platform.pos.x && pcOldPos.x <= platform.pos.x + platform.width) {
                        pc.land(platform);
                        break;
                    }
                }

            }
        } else if (pc.fallState.equals(PlayerCharacter.FallState.ON_SURFACE)
                && (pc.pos.x < pc.surface.pos.x
                        || pc.pos.x > pc.surface.pos.x + pc.surface.width)) {
            pc.fall();
        }
    }

    private void detectTokenCollisions(PlayerCharacter pc, PVector dir) {
        float collisionRange = PlayerCharacter.radius + Token.height / 2;
        for (Token token : sortedTokens) {
            if (token.pos.y + Token.height / 2 < Math.min(pcOldPos.y, pc.pos.y) - PlayerCharacter.radius) {
                // token too high
                // cut off search
                break;
            } else if (token.collected) {
                // token already collected
                // continue search
            } else if (token.pos.y - Token.height / 2 > Math.max(pcOldPos.y, pc.pos.y) + PlayerCharacter.radius) {
                // token too low
                // continue search
            } else {
                if (PVector.dist(pcOldPos, token.pos) <= collisionRange
                        || PVector.dist(pc.pos, token.pos) <= collisionRange) {
                    sketch.levelState.collectToken(token);
                    break;
                } else {
                    // the angle between the pc's path and the token's centre, from the pc's old position
                    float theta = Math.abs(dir.heading() - PVector.angleBetween(pcOldPos, token.pos));

                    float hyp = PVector.dist(pcOldPos, token.pos); 

                    // the shortest distance from the pc's path to the centre of the token
                    float x = (float) (hyp * Math.sin(theta)); 

                    // the distance along the pc's path to the closest point to the token
                    float y = (float) (hyp * Math.cos(theta)); 
                    
                    if (theta <= PConstants.HALF_PI && x <= collisionRange && y <= PVector.dist(pcOldPos, pc.pos)) {
                        sketch.levelState.collectToken(token);
                        break;
                    }
                }
            }
        }
    }

    public void detectCollisions() {
        PlayerCharacter pc = sketch.pc;
        PVector dir = (pc.pos.copy().sub(pcOldPos)).normalize(); // i.e. bearing of (pc.vel from last frame + panning)
        detectPlatformCollisions(pc, dir);
        detectTokenCollisions(pc, dir);
        pcOldPos = pc.pos.copy();

        // check level end conditions
        if (pc.surface != null && pc.surface.equals(sketch.level.highestPlatform)) {
            sketch.endLevel(true);
        } else if (pc.pos.y > sketch.risingWave.pos.y) {
            sketch.endLevel(false);
        }
    }

}
