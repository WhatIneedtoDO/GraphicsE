package men.brakh.graphicseditor.model.figure.impl;

import men.brakh.graphicseditor.config.Configuration;
import men.brakh.graphicseditor.controller.Controller;
import men.brakh.graphicseditor.model.Point;
import men.brakh.graphicseditor.model.PointType;
import men.brakh.graphicseditor.model.canvas.AbstractCanvas;
import men.brakh.graphicseditor.model.figure.AbstractLine;
import men.brakh.graphicseditor.model.figure.Figure;
import men.brakh.graphicseditor.model.figure.intf.Movable;
import men.brakh.graphicseditor.model.figure.intf.Resizable;
import men.brakh.graphicseditor.model.figure.intf.Selectable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StraightLine extends AbstractLine implements Resizable, Movable, Selectable {
    private Configuration config = Configuration.getInstance();


    public StraightLine(AbstractCanvas canvas, Point startPoint) {
        super(canvas, startPoint.copy());
        move(new Point(-config.getMinFigureWidth(), -config.getMinFigureHeight()));
        super.addPoint(startPoint);
    }

    @Override
    public void addPoint(Point point) {

    }
    HashMap<List<Figure>, ArrayList<Point>> clickedPoints = canvas.getClickedPoints();

    @Override
    public void draw() {
        if (points.size() < 2) {
            return;
        }

        if (clickedPoints.containsKey(canvas.getSelected())) {
            ArrayList<Point> lineP = canvas.getLineP();
            int i = lineP.size()-1;
            int number = points.size()-1;
            points.add(lineP.get(i));

            Point point1 = points.get(0);
            Point point2 = points.get(1);
            Point pointI = points.get(number);

            System.out.println("Point3 = " + pointI);
            System.out.println("Point2 = " + point2);
            System.out.println("Point1 = " + point1);

            canvas.withColorSaving(getBushColor(), getPenColor(), getPenWidth(), () -> {
                canvas.drawLine(point1, pointI);
                canvas.drawLine(point2, pointI);


                return null;
            });
            System.out.println("Points : " + points.size()+ "\n NewLinepoints : " + clickedPoints.size() + "\n numbers elements = " + points);
        } else {

            Point point1 = points.get(0);
            Point point2 = points.get(1);

            canvas.withColorSaving(getBushColor(), getPenColor(), getPenWidth(), () -> {
                canvas.drawLine(point1, point2);
                return null;
            });
        }


    }

    @Override
    public void resize(PointType pointType, Point fromPoint, Point toPoint) {
        super.resize(pointType, fromPoint, toPoint);
    }

    @Override
    public void move(Point deltaPoint) {
        super.move(deltaPoint);
    }

    @Override
    public void select() {
        super.select();
    }

    @Override
    public boolean deserialize(String text) {
        if (text.split(";").length != 7) {
            return false;
        } else {
            return super.deserialize(text);
        }
    }
}
