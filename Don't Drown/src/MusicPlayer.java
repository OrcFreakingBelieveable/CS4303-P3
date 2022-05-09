
import ddf.minim.AudioOutput;
import ddf.minim.Minim;

public class MusicPlayer {

    private static class Note {
        public final int durationMult;
        public final String pitchName;

        public Note(String pitchName) {
            durationMult = 1;
            this.pitchName = pitchName;
        }

        public Note(String pitchName, int durationMult) {
            this.durationMult = durationMult;
            this.pitchName = pitchName;
        }
    }

    private static final float BPM = 100; // beats per minute
    private static final float MSPB = 60000f / BPM; // millis per beat

    private final DontDrown sketch;

    public final Minim minim;
    public final AudioOutput output;

    public int noteIndex = -1;
    public long nextPlayMillis = -1;

    /* The tune of the alphabet song and Twinkle Twinkle Little Star */
    private static final Note[] notes = new Note[] {
            new Note("C"),
            new Note("C"),
            new Note("G"),
            new Note("G"),
            new Note("A"),
            new Note("A"),
            new Note("G", 2),

            new Note("F"),
            new Note("F"),
            new Note("E"),
            new Note("E"),
            new Note("D"),
            new Note("D"),
            new Note("C", 2),

            new Note("G"),
            new Note("G"),
            new Note("F"),
            new Note("E"),
            new Note("E"),
            new Note("D", 2),

            new Note("G"),
            new Note("G"),
            new Note("F"),
            new Note("E"),
            new Note("E"),
            new Note("D", 2),

            new Note("C"),
            new Note("C"),
            new Note("G"),
            new Note("G"),
            new Note("A"),
            new Note("A"),
            new Note("G", 2),

            new Note("F"),
            new Note("F"),
            new Note("E"),
            new Note("E"),
            new Note("D"),
            new Note("D"),
            new Note("C", 2),
    };

    public MusicPlayer(DontDrown sketch) {
        this.sketch = sketch;
        minim = new Minim(sketch);
        output = minim.getLineOut();
        output.setTempo(BPM);
    }

    public void playMusic() {
        long now = System.currentTimeMillis();
        if (now >= nextPlayMillis) {
            noteIndex = ++noteIndex % notes.length;
            Note note = notes[noteIndex];
            float duration = sketch.levelState.getNoteDuration() * note.durationMult;
            nextPlayMillis = now + (long) (MSPB * duration);

            output.playNote(0, duration, note.pitchName);
        }
    }
}
