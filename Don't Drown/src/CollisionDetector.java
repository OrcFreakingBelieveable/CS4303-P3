import java.util.ArrayList;
import java.util.Comparator;

import processing.core.PVector;

public class CollisionDetector {

    private final DontDrown sketch;

    public CollisionDetector(DontDrown sketch) {
        this.sketch = sketch;
    }

    public void detectCollisions() {
        PlayerCharacter pc = sketch.pc;
        if (pc.fallState.equals(PlayerCharacter.FallState.FALLING)) {
            ArrayList<Platform> sortedPlatforms = new ArrayList<>(sketch.level.platforms);
            sortedPlatforms.sort(new Comparator<Platform>() {

                @Override
                public int compare(Platform o1, Platform o2) {
                    return Math.round(o1.pos.x - o2.pos.x);
                }

            });

            PVector dir = (pc.pos.copy().sub(pc.oldPos)).normalize();
            for (Platform platform : sortedPlatforms) {
                float x;

                if (dir.y != 0) {
                    if (dir.x < 0) {
                        x = Math.min(pc.pos.x,
                                pc.oldPos.x + (dir.x * ((platform.pos.y - (pc.oldPos.y + pc.diameter / 2)) / dir.y)));
                    } else {
                        x = Math.max(pc.pos.x,
                                pc.oldPos.x + (dir.x * ((platform.pos.y - (pc.oldPos.y + pc.diameter / 2)) / dir.y)));
                    }
                } else {
                    x = -1;
                    // ignore x
                }

                if (platform.pos.x > x && platform.pos.x > pc.pos.x) {
                    // platform too far right
                    // cut off search
                    break;
                } else if (platform.pos.x + platform.width > x
                        && platform.pos.y <= pc.pos.y
                        && platform.pos.y >= pc.oldPos.y
                        || platform.pos.x + platform.width > pc.pos.x
                                && platform.pos.y <= pc.pos.y + pc.diameter / 1.5
                                && platform.pos.y > pc.pos.y) {
                    // collision
                    pc.land(platform);
                } else {
                    // platform too far left, up, or down
                    // continue search
                }
            }
        } else if (pc.fallState.equals(PlayerCharacter.FallState.ON_SURFACE)
                && (pc.pos.x < pc.surface.pos.x
                        || pc.pos.x > pc.surface.pos.x + pc.surface.width)) {
            pc.fall();
        }
    }

}
