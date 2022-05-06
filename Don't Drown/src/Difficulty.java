public enum Difficulty {
    EASY(1f, true, 0.6f, 3),
    MEDIUM(1.5f, false, 0.4f, 3),
    HARD(2f, false, 0.2f, 3),
    VERY_HARD(2.5f, false, 0.1f, 3),
    ; 
    
    public final float heightMult; 
    public final boolean hasGround; 
    public final float verticality; 
    public final int betweenRedHerrings; 

    Difficulty(float heightMult, boolean hasGround, float verticality, int betweenRedHerrings) {
        this.heightMult = heightMult;
        this.hasGround = hasGround;
        this.verticality = verticality;
        this.betweenRedHerrings = betweenRedHerrings; 
    }
}
