package RunwayRedeclarationTool.View;

import RunwayRedeclarationTool.Exceptions.AttributeNotAssignedException;
import RunwayRedeclarationTool.Models.ObstaclePosition;
import RunwayRedeclarationTool.Models.RunwayParameters;
import RunwayRedeclarationTool.Models.VirtualRunway;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public abstract class RunwayView extends javafx.scene.canvas.Canvas {
    protected VirtualRunway runway;
    protected int TORA, ASDA, TODA, LDA;

    protected boolean leftRunway;
    protected int leftSpace;

    protected ObstaclePosition obstaclePosition;

    protected GraphicsContext gc;

    public RunwayView(VirtualRunway runway, ObstaclePosition obstaclePosition) {
        this.runway = runway;
        this.obstaclePosition = obstaclePosition;

        RunwayParameters p = runway.getOrigParams();
        this.TORA = p.getTORA();
        this.ASDA = p.getASDA();
        this.TODA = p.getTODA();
        this.LDA = p.getLDA();

        if (Integer.parseInt(runway.getDesignator().substring(0, 2)) <= 18) {   // left virtual runway
            leftRunway = true;
            leftSpace = 60;
        } else {
            leftRunway = false;
            leftSpace = Math.max(TODA, ASDA) - TORA;    // the largest out of clearway and stopway
        }

        gc = getGraphicsContext2D();

        widthProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                draw();
                drawObstacle();
            }
        });
        heightProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                draw();
                drawObstacle();
            }
        });
        draw();
    }

    protected abstract void draw();

    public abstract void drawObstacle();

    protected void drawStopway(int y) {
        int stopway = ASDA - TORA;

        if (stopway != 0) {
            if (leftRunway) {
                drawMeasuringLine(TORA + 60, stopway, y, "stopway");
            } else {
                drawMeasuringLine(leftSpace - stopway, stopway, y, "stopway");
            }
        }
    }

    protected void drawClearway(int y) {
        int clearway = TODA - TORA;

        if (clearway != 0) {
            if (leftRunway) {
                drawMeasuringLine(TORA + 60, clearway, y, "clearway");
            } else {
                drawMeasuringLine(0, clearway, y, "clearway");
            }
        }
    }

    protected void drawDesignators(int y) {
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Consolas", 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        String num = runway.getDesignator().substring(0, 2);
        String designator1 = num + "\n" + runway.getDesignator().substring(2, runway.getDesignator().length());

        int oppositeNum = Integer.parseInt(num) + 18;
        if (oppositeNum > 36) {
            oppositeNum -= 36;
        }
        String designator2 = String.format("%02d", oppositeNum);

        String letter = runway.getDesignator().substring(2, runway.getDesignator().length());
        if (letter.equals("L")) {
            designator2 += "\nR";
        } else if (letter.equals("R")) {
            designator2 += "\nL";
        } else {
            designator2 += "\n" + letter;
        }

        if (Integer.parseInt(runway.getDesignator().substring(0, 2)) <= 18) {
            gc.fillText(designator1, scale_x(TORA / 7 + leftSpace), scale_y(y));
            gc.fillText(designator2, scale_x(TORA * 6 / 7 + leftSpace), scale_y(y));
        } else {
            gc.fillText(designator2, scale_x(TORA / 7 + leftSpace), scale_y(y));
            gc.fillText(designator1, scale_x(TORA * 6 / 7 + leftSpace), scale_y(y));
        }
    }

    protected void drawTakeOffLandingDirection() {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(scale_y(0.5));
        gc.setFont(Font.font("Consolas", 14));

        scaledStrokeLine(60, 20, 560, 20);
        if (!leftRunway) {
            scaledStrokeLine(60, 20, 100, 16);
            scaledStrokeLine(60, 20, 100, 24);
        } else {
            scaledStrokeLine(560, 20, 520, 16);
            scaledStrokeLine(560, 20, 520, 24);
        }
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Take-off/landing direction", scale_x(60), scale_y(10));
    }

    protected void drawBrokenDownDistances(int oLength) throws AttributeNotAssignedException {
        RunwayParameters recalcParams = runway.getRecalcParams();

        int rTORA = recalcParams.getTORA();
        int rLDA = recalcParams.getLDA();
        // maybe use distFromRightTSH() instead of recalculated values?
        int slopecalc = recalcParams.getSlopeCalculation();
        int displacedThs = TORA - LDA; //original values


        if (obstaclePosition.getDistLeftTSH() < obstaclePosition.getDistRightTSH()) {
            if (obstaclePosition.getDistLeftTSH() > 0) {
                drawMeasuringLine(leftSpace, obstaclePosition.getDistLeftTSH(), 200, Integer.toString(obstaclePosition.getDistLeftTSH()) + "m");
            }
            drawMeasuringLine(obstaclePosition.getDistLeftTSH() + leftSpace, oLength, 200, "Obstacle");
            int endObstacle = leftSpace + obstaclePosition.getDistLeftTSH() + oLength;

            if (leftRunway) {
                // __|=|__->______
                drawMeasuringLine(endObstacle, 300, 200, "blast protection");
                drawMeasuringLine(endObstacle + 300, rTORA, 200, "TORA " + rTORA + "m");

                drawMeasuringLine(endObstacle, slopecalc + 60, 210, "slope offset");         // length = slopecalc+60 which is the strip end. Maybe show that in text too?
                drawMeasuringLine(endObstacle + slopecalc + 60, rLDA, 210, "LDA " + rLDA + "m");
            } else {
                // __|=|__<-______
                drawMeasuringLine(endObstacle, slopecalc + 60, 200, "slope offset");
                drawMeasuringLine(endObstacle + slopecalc + 60, rTORA, 200, "TORA " + rTORA + "m");

                drawMeasuringLine(endObstacle, 300, 210, "RESA + strip end");
                drawMeasuringLine(endObstacle + 300, rLDA, 210, "LDA " + rLDA + "m");
            }
        } else {
            int startObstacle = leftSpace + obstaclePosition.getDistLeftTSH();

            if (leftRunway) {
                // ______->__|=|__
                drawMeasuringLine(60, rTORA, 200, "TORA " + rTORA + "m");
                drawMeasuringLine(60 + rTORA, slopecalc + 60, 200, "slope offset");

                drawMeasuringLine(60 + displacedThs, rLDA, 210, "LDA " + rLDA + "m");
                drawMeasuringLine(60 + displacedThs + rLDA, 300, 210, "strip end + RESA");
            } else {
                // ______<-__|=|__
                drawMeasuringLine(leftSpace, rTORA, 200, "TORA " + rTORA + "m");
                drawMeasuringLine(leftSpace + rTORA, 300, 200, "blast protection");

                drawMeasuringLine(leftSpace, rLDA, 210, "LDA " + rLDA + "m");
                drawMeasuringLine(leftSpace + rLDA, slopecalc + 60, 210, "slope offset");
            }

            drawMeasuringLine(startObstacle, oLength, 200, "Obstacle");
            if (obstaclePosition.getDistRightTSH() > 0) {
                drawMeasuringLine(startObstacle + oLength, obstaclePosition.getDistRightTSH(), 200, obstaclePosition.getDistRightTSH() + "m");
            }
        }
    }

    protected void drawDisplacedThreshold(int y) {
        int displacedTsh = TORA - LDA;

        if (displacedTsh > 0) {
            if (leftRunway) {
                drawMeasuringLine(60, displacedTsh, y, "displaced TSH");
            } else {
                drawMeasuringLine(Math.max(TODA, ASDA) - displacedTsh, displacedTsh, y, "displaced TSH");
            }
        }
    }

    protected void drawMapScale() {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(scale_y(0.5));
        gc.setFont(Font.font("Consolas", 16));
        scaledStrokeLine(60, 290, 560, 290);
        scaledStrokeLine(60, 292, 60, 288);
        scaledStrokeLine(560, 292, 560, 288);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("500m", scale_x(60), scale_y(285));
    }

    protected void drawMeasuringLine(int x, int length, int y, String text) {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(scale_y(0.5));
        gc.setFont(Font.font("Consolas", 14));
        gc.setTextAlign(TextAlignment.CENTER);

        scaledStrokeLine(x, y, x + length, y);
        scaledStrokeLine(x, y + 2, x, y - 2);
        scaledStrokeLine(x + length, y + 2, x + length, y - 2);
        gc.fillText(text, scale_x(x + length / 2), scale_y(y - 5));
    }

    protected double scale_x(double length) {
        return length / (TODA + 60) * getWidth();
    }

    protected double scale_y(double length) {
        return length / 300 * getHeight();
    }

    protected void scaledStrokeLine(double x1, double y1, double x2, double y2) {
        gc.strokeLine(scale_x(x1), scale_y(y1), scale_x(x2), scale_y(y2));
    }

    protected void scaledFillRect(double x, double y, double w, double h) {
        gc.fillRect(scale_x(x), scale_y(y), scale_x(w), scale_y(h));
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

}