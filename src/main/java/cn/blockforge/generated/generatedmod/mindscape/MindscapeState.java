package cn.blockforge.generated.generatedmod.mindscape;

public enum MindscapeState {
    NONE, UNSTABLE, MEDITATING, ENTERING, IN_MINDSCAPE, CHALLENGE_ACTIVE, COMPLETED, RETURNING;
    public static MindscapeState read(String value){
        try{return valueOf(value);}catch(IllegalArgumentException exception){return NONE;}
    }
}
