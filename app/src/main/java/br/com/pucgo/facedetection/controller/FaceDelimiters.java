package br.com.pucgo.facedetection.controller;


import org.opencv.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.pucgo.facedetection.enumerator.FaceMovementEnum;

public class FaceDelimiters {

    public int age = 0;
    public int width = 0;
    private int height = 0;
    public int xpt = 0;
    public int ypt = 0;
    private int life = 0;

    private FaceMovementEnum state;
    private FaceMovementEnum lastState;
    private int alternations;
    private int faceStill;

    private int stills;
    private int lefts;
    private int rights;
    private int ups;
    private int downs;

    public Point eyeLeft1;
    public Point eyeLeft2;
    public Point eyeRight1;
    public Point eyeRight2;

    public int mouthTopline;
    public int mouthBotline;
    public Point mouthTopLeft;
    public Point mouthBotRight;
    public HashMap<String, Integer> emotion = new HashMap<>();

    public static final int FACE_MAX_MOVEMENT = 40;
    private final int FACE_ALTERNATION_THRESH = 2;
    private ArrayList<Point> facePoints = new ArrayList<>();

    public ArrayList<Point> getFacePoints() {
        return facePoints;
    }

    public FaceDelimiters(int age, int width, int height, int xpt, int ypt, int life) {

        this.age = age;
        this.width = width;
        this.height = height;
        this.xpt = xpt;
        this.ypt = ypt;
        this.life = life;

        updateEyes();
        updateMouth();

        this.state = FaceMovementEnum.OTHER;
        this.lastState = this.state;
        this.alternations = 0;
        this.faceStill = 0;

        this.stills = 0;
        this.lefts = 0;
        this.rights = 0;
        this.ups = 0;
        this.downs = 0;
        this.emotion.put("neutral", 0);
        this.emotion.put("anger", 0);
        this.emotion.put("happy", 0);
        this.emotion.put("disgust", 0);
        this.emotion.put("surprise", 0);
    }

    public void updateFace(int width, int height, int xpt, int ypt, String emotionUpdate) {
        facePoints.clear();
        FaceMovementEnum turnDir = getTurnDir(xpt, xpt, ypt, ypt, width, width, height, height);
        updateMoveState(turnDir);

        age = age + 1;
        this.width = width;
        this.height = height;
        this.xpt = xpt;
        this.ypt = ypt;
        life = 0;
        updateEyes();
        updateMouth();

        for (Map.Entry<String, Integer> entry : emotion.entrySet()) {
            if (entry.getKey().toLowerCase().equals(emotionUpdate.toLowerCase())) {
                emotion.put(entry.getKey(), entry.getValue() + 1);
            }
        }
    }

    private void updateEyes() {
        int eyeTopline = ypt + (height / 3);
        int eyeBotline = ypt + (height / 2);

        eyeLeft1 = new Point(xpt + (width / 5), eyeTopline);
        eyeLeft2 = new Point(xpt + ((width * 3) / 8), eyeBotline);
        eyeRight1 = new Point(xpt + ((width * 5) / 8), eyeTopline);
        eyeRight2 = new Point(xpt + ((width * 4) / 5), eyeBotline);

        facePoints.add(eyeLeft1);
        facePoints.add(eyeLeft2);
        facePoints.add(eyeRight1);
        facePoints.add(eyeRight2);
    }

    private void updateMouth() {
        mouthTopline = ypt + ((height * 2) / 3);
        mouthBotline = ypt + height;
        mouthTopLeft = new Point(xpt + width / 5, mouthTopline);
        mouthBotRight = new Point(xpt + (width * 4) / 5, mouthBotline);

        facePoints.add(mouthTopLeft);
        facePoints.add(mouthBotRight);
    }

    public boolean isShaking() {
        return alternations >= FACE_ALTERNATION_THRESH && ((state == FaceMovementEnum.LEFT) || (state == FaceMovementEnum.RIGHT));
    }

    public boolean isNodding() {
        return alternations >= FACE_ALTERNATION_THRESH && ((state == FaceMovementEnum.UP) || (state == FaceMovementEnum.DOWN));
    }

    public boolean isStill() {
        int FACE_STILL_THRESHOLD = 3;
        return (faceStill < FACE_STILL_THRESHOLD);
    }

    private void updateMoveState(FaceMovementEnum turnDir) {
        int FACE_LR_STATE_CHANGE_THRESH = 1;
        int FACE_UD_STATE_CHANGE_THRESH = 1;
        if (turnDir == FaceMovementEnum.OTHER) {
            faceStill += 1;
            state = FaceMovementEnum.OTHER;

        } else if (turnDir == FaceMovementEnum.STILL) {
            if (state != FaceMovementEnum.STILL) {
                lastState = state;
            } else {
                faceStill = 0;
            }
            state = FaceMovementEnum.STILL;
            stills += 1;
            int FACE_ALTERNATIONS_EXPIRE = 6;
            if (stills > FACE_ALTERNATIONS_EXPIRE) {
                alternations = 0;
                stills = 0;
            }

        } else if (turnDir == FaceMovementEnum.RIGHT) {
            faceStill += 1;
            if (state == FaceMovementEnum.OTHER) {
                rights += 1;
                if (rights > FACE_LR_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.RIGHT;
                }

            } else if (state == FaceMovementEnum.RIGHT) {
                rights += 1;

            } else if (state == FaceMovementEnum.LEFT) {
                rights += 1;
                if (rights > FACE_LR_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.RIGHT;
                    resetNonAltCounts();
                    alternations += 1;
                }

            } else if ((state == FaceMovementEnum.UP) || (state == FaceMovementEnum.DOWN)) {
                state = FaceMovementEnum.OTHER;
                resetCounts();

            } else if (state == FaceMovementEnum.STILL) {
                if (lastState == FaceMovementEnum.LEFT) {
                    alternations += 1;
                }
                state = FaceMovementEnum.RIGHT;
            }

        } else if (turnDir == FaceMovementEnum.LEFT) {
            faceStill += 1;
            if (state == FaceMovementEnum.OTHER) {
                lefts += 1;
                if (lefts > FACE_LR_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.LEFT;
                }

            } else if (state == FaceMovementEnum.RIGHT) {
                lefts += 1;
                if (lefts > FACE_LR_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.LEFT;
                    resetNonAltCounts();
                    alternations += 1;
                }

            } else if (state == FaceMovementEnum.LEFT) {
                lefts += 1;

            } else if ((state == FaceMovementEnum.UP) || (state == FaceMovementEnum.DOWN)) {
                state = FaceMovementEnum.OTHER;
                resetCounts();

            } else if (state == FaceMovementEnum.STILL) {
                if (lastState == FaceMovementEnum.RIGHT) {
                    alternations += 1;
                }
                state = FaceMovementEnum.LEFT;
            }

        } else if (turnDir == FaceMovementEnum.UP) {
            faceStill += 1;
            if (state == FaceMovementEnum.OTHER) {
                ups += 1;
                if (ups > FACE_UD_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.UP;
                }

            } else if (state == FaceMovementEnum.DOWN) {
                ups += 1;
                if (ups > FACE_UD_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.UP;
                    resetNonAltCounts();
                    alternations += 1;
                }

            } else if (state == FaceMovementEnum.UP) {
                ups += 1;

            } else if ((state == FaceMovementEnum.LEFT) || (state == FaceMovementEnum.RIGHT)) {
                state = FaceMovementEnum.OTHER;
                resetCounts();

            } else if (state == FaceMovementEnum.STILL) {
                if (lastState == FaceMovementEnum.DOWN) {
                    alternations += 1;
                }
                state = FaceMovementEnum.UP;
            }

        } else if (turnDir == FaceMovementEnum.DOWN) {
            faceStill += 1;
            if (state == FaceMovementEnum.OTHER) {
                downs += 1;
                if (downs > FACE_UD_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.DOWN;
                }

            } else if (state == FaceMovementEnum.UP) {
                downs += 1;
                if (downs > FACE_UD_STATE_CHANGE_THRESH) {
                    state = FaceMovementEnum.DOWN;
                    resetNonAltCounts();
                    alternations += 1;
                }

            } else if (state == FaceMovementEnum.DOWN) {
                downs += 1;

            } else if ((state == FaceMovementEnum.LEFT) || (state == FaceMovementEnum.RIGHT)) {
                state = FaceMovementEnum.OTHER;
                resetCounts();

            } else if (state == FaceMovementEnum.STILL) {
                if (lastState == FaceMovementEnum.UP) {
                    alternations += 1;
                }
                state = FaceMovementEnum.DOWN;
            }
        }
    }

    private void resetCounts() {
        stills = 0;
        rights = 0;
        lefts = 0;
        ups = 0;
        downs = 0;
        alternations = 0;
    }

    private void resetNonAltCounts() {
        stills = 0;
        rights = 0;
        lefts = 0;
        ups = 0;
        downs = 0;
    }

    private FaceMovementEnum getTurnDir(int oldXpt, int newXpt, int oldYpt, int newYpt, int oldWidth, int newWidth, int oldHeight, int newHeight) {
        int old_x = oldXpt + (oldWidth / 2);
        int new_x = newXpt + (newWidth / 2);
        int old_y = oldYpt + (oldHeight / 2);
        int new_y = newYpt + (newHeight / 2);

        FaceMovementEnum xdir = FaceMovementEnum.STILL;
        FaceMovementEnum ydir = FaceMovementEnum.STILL;

        int FACE_LR_MOVE_THRESH = 2;
        if (new_x - old_x > FACE_LR_MOVE_THRESH) {
            xdir = FaceMovementEnum.RIGHT;
        }

        if (new_x - old_x < -FACE_LR_MOVE_THRESH) {
            xdir = FaceMovementEnum.LEFT;
        }

        int FACE_UD_MOVE_THRESH = 1;
        if (new_y - old_y > FACE_UD_MOVE_THRESH) {
            ydir = FaceMovementEnum.DOWN;
        }

        if (new_y - old_y < -FACE_UD_MOVE_THRESH) {
            ydir = FaceMovementEnum.UP;
        }

        if (ydir == xdir) {
            return FaceMovementEnum.STILL;
        } else {
            if ((ydir != FaceMovementEnum.STILL) && (xdir != FaceMovementEnum.STILL)) {
                if ((Math.abs(new_x - old_x)) > (Math.abs(new_y - old_y) / 2)) {
                    return xdir;
                } else {
                    int FACE_ONE_DIMENSION_THRESH = 2;
                    if (((Math.abs(new_y - old_y)) - (Math.abs(new_x - old_x))) > FACE_ONE_DIMENSION_THRESH) {
                        return ydir;
                    } else {
                        return FaceMovementEnum.OTHER;
                    }
                }
            } else {
                if (xdir == FaceMovementEnum.STILL) {
                    return ydir;
                } else {
                    return xdir;
                }
            }
        }
    }

    public boolean isTooOld() {
//        int FACE_MAX_LIFE = 1;
        return life > 1;
    }

    public int updateLife() {
        life = life + 1;
        return life;
    }
}


