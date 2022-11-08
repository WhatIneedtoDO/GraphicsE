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

    HashMap<List<Figure>, Point> newLinepoints = canvas.getClickedPoints();
    @Override
    public void draw() {
        if (points.size() < 2) {
            return;
        }
      if (newLinepoints.containsKey(canvas.getSelected())){
          Point point3 = newLinepoints.get(canvas.getSelected());
          Point point1 = points.get(0);
          Point point2 = points.get(1);
          System.out.println("contains need key? ::: " + newLinepoints.containsKey(canvas.getSelected()) + "::::" + "\n get values from key : "
                  + point3);
          canvas.withColorSaving(getBushColor(), getPenColor(), getPenWidth(), () -> {
              canvas.drawLine(point1, point3);
              canvas.drawLine(point3,point2);

              return null;
          });

      }

        Point point1 = points.get(0);
        Point point2 = points.get(1);

        canvas.withColorSaving(getBushColor(), getPenColor(), getPenWidth(), () -> {
            canvas.drawLine(point1, point2);
            return null;
        });



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
