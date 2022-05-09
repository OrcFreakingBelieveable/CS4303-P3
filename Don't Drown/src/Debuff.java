import java.util.Random;

public enum Debuff {
    NONE("Feeling Typical", "No debuff"),
    OVERWORKED("Overworked", "Every platform has a token"),
    PANIC_PRONE("Panic Prone", "Stress will spike every few seconds"),
    STRESS_MOTIVATED("Stress Motivated", "Steering is sluggish when you're not stressed enough"),
    CANT_UNWIND("Can't Unwind", "Stress cannot reduce"),
    TUNNEL_VISION("Tunnel Vision", "Your vision is reduced, but you won't be stressed by the wave if you can't see it"),
    LACK_CONTRAST("Lacking Self-awareness",
            "The stress bar is hidden, and the drawing quality is not stress-dependent"),
            ;

    private static Random rand = new Random();

    public final String label;
    public final String description;

    Debuff(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public static Debuff random() {
        return Debuff.values()[rand.nextInt(Debuff.values().length)];
    }
}
