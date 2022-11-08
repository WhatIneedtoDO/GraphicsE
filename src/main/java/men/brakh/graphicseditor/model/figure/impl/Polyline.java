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
import java.util.List;
import java.util.Optional;

public class Polyline extends AbstractLine implements Resizable, Movable, Selectable {

    private Configuration config = Configuration.getInstance();
    private Polyline polyline;

    public Polyline(AbstractCanvas canvas,Point clickedPoint){
        super(canvas,clickedPoint.copy());
        move(new Point(-config.getMinFigureWidth(), -config.getMinFigureHeight()));
        super.addPoint(clickedPoint);

    }
    @Override
    public void draw() {

       /* polyline = this.getClass().getConstructor(AbstractCanvas.class, Point.class)
                .newInstance(this.canvas, this.points.get(0).copy());
        polyline.assign(this);*/
        Point point1 = points.get(0);
        Point point2 = points.get(1);
        Point point3 = points.get(3);

        canvas.withColorSaving(getBushColor(), getPenColor(), getPenWidth(), () -> {
            canvas.drawLine(point1, point3);
            return null;
        });

    }

    @Override
    public void move(Point deltaPoint) {

    }

    @Override
    public void resize(PointType pointType, Point fromPoint, Point toPoint) {

    }

    @Override
    public void select() {

    }
}

