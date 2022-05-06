public enum Debuff {
    STRESS_MOTIVATED("Stress Motivated"),
    TUNNEL_VISION("Tunnel Vision"),
    PANIC_PRONE("Panic Prone"),
    LACK_CONTRAST("Lacking Self-awareness"),
    OVERWORKED("Overworked"),
    CANT_UNWIND("Can't Unwind"),
    NONE("Feeling Typical");

    public final String label;

    Debuff(String label) {
        this.label = label;
    }
}
