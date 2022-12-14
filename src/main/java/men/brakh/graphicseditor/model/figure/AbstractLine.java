package men.brakh.graphicseditor.model.figure;

import men.brakh.graphicseditor.config.Configuration;
import men.brakh.graphicseditor.controller.Controller;
import men.brakh.graphicseditor.model.Point;
import men.brakh.graphicseditor.model.PointType;
import men.brakh.graphicseditor.model.canvas.AbstractCanvas;
import men.brakh.graphicseditor.model.figure.intf.TextSerializible;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class AbstractLine implements Figure, TextSerializible {
    private Configuration config = Configuration.getInstance();

    private String penColor;
    private int penWidth;

    protected AbstractCanvas canvas;

    protected List<Point> points = new ArrayList<>();

    public AbstractLine(AbstractCanvas canvas, Point startPoint) {
        this.canvas = canvas;
        points.add(startPoint);

        this.penColor = canvas.getPenColor();
        this.penWidth = canvas.getPenWidth();

        if(canvas != null)
            canvas.addFigure(this);
    }


    @Override
    public Figure copy() {
        AbstractLine newLine = null;
        try {
            newLine = this.getClass().getConstructor(AbstractCanvas.class, Point.class)
                    .newInstance(this.canvas, this.points.get(0).copy());

            newLine.assign(this);

            canvas.removeFigure(newLine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newLine;

    }

    @Override
    public void assign(Figure figure) {
        AbstractLine line = (AbstractLine) figure;

        this.canvas = line.canvas;
        this.penColor = line.penColor;
        this.penWidth = line.penWidth;

        List<Point> newPoints = new ArrayList<>();
        line.points.forEach(point -> newPoints.add(point.copy()));



        this.points = newPoints;
    }


    /**
     * Добавление вершины линии
     * @param point Координата вершины
     */
    public void addPoint(Point point) {
        points.add(point);

    }

    /**
     * Тип точки
     * @param point Точка
     * @return Тип точки
     */
    @Override
    public PointType checkPoint(Point point) {
        for (Point linePoint : points) {
            if(linePoint.equals(point)) {
                return PointType.POINT_NODE;
            }
        }

        if(points.size() < 2) return PointType.UNKNOWN_POINT;

        for(int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i-1);
            Point p2 = points.get(i);

            int dx1 = p2.getX() - p1.getX();
            int dy1 = p2.getY() - p1.getY();

            int dx = point.getX() - p1.getX();
            int dy = point.getY() - p1.getY();


            int S = dx1 * dy - dx * dy1;


            double ab = Math.sqrt(dx1 * dx1 + dy1 * dy1);

            double h = (double) S / ab;

            if (Math.abs(h) < config.getPointAreaSize()) {
                return PointType.POINT_INSIDE;
            }
        }
        return PointType.UNKNOWN_POINT;
    }


    /**
     * Перемещение стартовой точки
     */
    @Override
    public void moveStartPoint(PointType pointType, Point fromPoint, Point toPoint) {
        resize(pointType, fromPoint, toPoint);
    }

    /**
     * Получение точки по координатам
     * @param needPoint координаты
     */
    private Optional<Point> getPoint(Point needPoint) {
        for(Point point : points) {
            if(point.equals(needPoint)) {
                return Optional.of(point);
            }
        }

        return Optional.empty();
    }

    /**
     * Перемещние точки линии
     * @param pointType Не используется. Используется в {@link AbstractRectFigure}
     * @param fromPoint Координата точки линии
     * @param toPoint Координата, куда надо переместить эту точку
     */
    protected void resize(PointType pointType, Point fromPoint, Point toPoint) {
        getPoint(fromPoint).ifPresent(
                point -> {
                    point.assign(toPoint);
                }
        );
    }

    /**
     * Перемещение линии
     */
    protected void move(Point deltaPoint) {
        for(Point point : points) {
            point.add(deltaPoint);
        }
    }

    /**
     * Возвращает true, если текущая фигура находится внутри другой фигуры
     * @param rectFigure Другая фигура
     */
    @Override
    public boolean isInside(Figure rectFigure) {
        AbstractRectFigure figure = (AbstractRectFigure) rectFigure;

        final boolean[] inside = {true};

        points.forEach(
                point -> {
                    if(!point.xInRange(figure.getLeftTopPoint().getX(), figure.getRightBottomPoint().getX())) {
                        inside[0] = false;
                    }

                    if(!point.yInRange(figure.getLeftTopPoint().getY(), figure.getRightBottomPoint().getY())) {
                        inside[0] = false;
                    }
                }
        );

        return inside[0];
    }


    /**
     * Выделение линии
     */
    protected void select() {
        int padding = config.getPointAreaSize();
        String color = config.getSelectionColor();
        
        canvas.withColorSaving(color, color, config.getSelectionPenWidth(), () -> {
                    for(Point point : points) {
                        int x = point.getX();
                        int y = point.getY();
                        canvas.drawRectangle(new Point(x - padding, y - padding), new Point(x + padding, y + padding));
                    }

                    String penBackup = penColor;
                    penColor = color;
                    draw();
                    penColor = penBackup;
                    return null;
                }
        );
    }

    /**
     * Сериализация в CSV
     */
    @Override
    public String serialize() {
        // ClassName ; PENCOLOR; PENSIZE ; x0 ; y0 ; ... ; xN ; yN
        StringBuilder row =  new StringBuilder(String.format("%s;%s;%d",
                this.getClass().getSimpleName(), penColor, penWidth));
        points.forEach(
                point -> row.append(String.format(";%d;%d", point.getX(), point.getY()))
        );
        row.append("\n");

        return row.toString();
    }

    /**
     * Десериализация из CSV
     * @param text csv
     */
    @Override
    public boolean deserialize(String text) {
        try {
            String[] rows = text.split(";");

            if (rows.length < 7 || rows.length % 2 == 0)
                return false;

            penColor = rows[1];
            penWidth = Integer.parseInt(rows[2]);
            this.points.clear();

            for(int i = 3; i < rows.length; i = i + 2) {
                Point point = new Point(Integer.parseInt(rows[i]), Integer.parseInt(rows[i + 1]));
                points.add(point);
            }
            return true;
        } catch (Exception e) {
            System.out.println("???");
            canvas.removeFigure(this);
            return  false;
        }
    }


    /**
     * Ортрисовка
     */
    @Override
    public abstract void draw();

    /*
     * ГЕТТЕРЫ И СЕТТЕРЫ
     */

    @Override
    public String getPenColor() {
        return penColor;
    }

    @Override
    public String getBushColor() {
        return null;
    }

    @Override
    public int getPenWidth() {
        return penWidth;
    }

    @Override
    public void setPenColor(String color) {
        penColor = color;
    }

    @Override
    public void setBrushColor(String color) {
        // NOTHING
    }

    @Override
    public void setPenWidth(int width) {
        penWidth = width;
    }



}
