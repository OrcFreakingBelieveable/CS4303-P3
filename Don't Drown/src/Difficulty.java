import java.util.Random;

public enum Difficulty {
    EASY(1.5f, true, 0.6f, 3, 14),
    MEDIUM(2f, false, 0.4f, 3, 12),
    HARD(2.5f, false, 0.2f, 3, 12),
    VERY_HARD(3f, false, 0.1f, 3, 12),
    ;

    private static Random rand = new Random();

    public final float heightMult;
    public final boolean hasGround;
    public final float verticality;
    public final int betweenRedHerrings;
    public final int waveRiseTime; // seconds to reach the top of the viewport

    Difficulty(float heightMult, boolean hasGround, float verticality, int betweenRedHerrings, int waveRiseTime) {
        this.heightMult = heightMult;
        this.hasGround = hasGround;
        this.verticality = verticality;
        this.betweenRedHerrings = betweenRedHerrings;
        this.waveRiseTime = waveRiseTime;
    }

    public static Difficulty random() {
        return Difficulty.values()[rand.nextInt(Difficulty.values().length)];
    }
}
