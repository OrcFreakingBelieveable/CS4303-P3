public enum Debuff {
    NONE("Feeling Typical"),
    OVERWORKED("Overworked"),
    PANIC_PRONE("Panic Prone"),
    STRESS_MOTIVATED("Stress Motivated"),
    CANT_UNWIND("Can't Unwind"),
    TUNNEL_VISION("Tunnel Vision"),
    LACK_CONTRAST("Lacking Self-awareness"),
    ;

    public final String label;

    Debuff(String label) {
        this.label = label;
    }
}
