import java.util.ArrayList;
import java.util.Comparator;

public class CollisionDetector {

    private final DDSketch sketch;

    public CollisionDetector(DDSketch sketch) {
        this.sketch = sketch;
    }

    public void detectCollisions() {
        if (sketch.pc.fallState.equals(PlayerCharacter.FallState.FALLING)) {
            ArrayList<Platform> sortedPlatforms = new ArrayList<>(sketch.platforms);
            sortedPlatforms.sort(new Comparator<Platform>() {

                @Override
                public int compare(Platform o1, Platform o2) {
                    return Math.round(o1.pos.x - o2.pos.x);
                }

            });

            for (Platform platform : sortedPlatforms) {
                if (platform.pos.x > sketch.pc.pos.x + sketch.pc.diameter / 2) {
                    // platform too far right
                    // cut off search
                    break;
                } else if (platform.pos.x + platform.width > sketch.pc.pos.x - sketch.pc.diameter / 2
                        && platform.pos.y <= sketch.pc.pos.y + sketch.pc.diameter / 2
                        && platform.pos.y > sketch.pc.pos.y) {
                    // collision
                    sketch.pc.land(platform.pos.y);
                } else {
                    // platform too far left, up, or down
                    // continue search
                    continue;
                }
            }
        }
    }

}
