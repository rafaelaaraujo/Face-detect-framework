package br.com.facedetection.enumerator;

/**
 * Created by rafaela on 12/09/2016.
 */
public enum EmotionEnum {
    NEUTRAL(0),
    ANGER(1),
    HAPPY(2),
    SURPRISE(3);

    private final int value;

    public int getValue() {
        return value;
    }

    EmotionEnum(int i) {
        value = i;
    }

    public static EmotionEnum getEnumFromValue(int value){
        for (EmotionEnum emotionEnum : EmotionEnum.values()) {
            if(emotionEnum.getValue() == value){
                return emotionEnum;
            }
        }

        return null;
    }
}