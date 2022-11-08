package men.brakh.graphicseditor.model;

import men.brakh.graphicseditor.config.Configuration;

import java.util.ArrayList;
import java.util.List;


public class Point {
    /* КОНФИГ */
    private Configuration config = Configuration.getInstance();

    private int x;
    private int y;
    private List<Integer> pointX = new ArrayList<Integer>();

    public List<Integer> getPointX() {
        return pointX;
    }

    public void setPointX(List<Integer> pointX) {
        this.pointX = pointX;
    }

    private List<Integer> pointY = new ArrayList<Integer>();

    public List<Integer> getPointY() {
        return pointY;
    }

    public void setPointY(List<Integer> pointY) {
        this.pointY = pointY;
    }






    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point copy() {
        return new Point(x, y);
    }

    public void assign(Point point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    public Point add(Point point) {
        this.x += point.getX();
        this.y += point.getY();
        return this;
    }

    /**
     * Вычисляет разницу между текущей точкой и переданной
     * @param point Вторая точка
     * @return CurrPoint - Point
     */
    public Point delta(Point point) {
        return new Point(x - point.x, y - point.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;
        return xEquals(point.x) &&
                yEquals(point.y);
    }

    /**
     * Возвращает true если x объекта равен (с учетом погрешности {@link Configuration#pointAreaSize}) передаваемому значению
     * @param x Х, с которым необходимо сравнить
     * @return true if this.x ~ x
     */
    public boolean xEquals(int x) {
        return Math.abs(this.x - x) <= config.getPointAreaSize();
    }

    /**
     * Возвращает true если y объекта равен (с учетом погрешности {@link Configuration#pointAreaSize}) передаваемому значению
     * @param y Y, с которым необходимо сравнить
     * @return true if this.y ~ y
     */
    public boolean yEquals(int y) {
        return Math.abs(this.y - y) <= config.getPointAreaSize();
    }

    /**
     * Возвращает true если y находятся в диапазоне от y1 до y2
     * @param y1 нижняя граница диапазона
     * @param y2 верхняя граница диапазона
     * @return true if y1 < y < y2
     */
    public boolean yInRange(int y1, int y2) {
        return (y > y1 - config.getPointAreaSize()) && (y < y2 + config.getPointAreaSize());
    }

    /**
     * Возвращает true если x находятся в диапазоне от x1 до x2
     * @param x1 нижняя граница диапазона
     * @param x2 верхняя граница диапазона
     * @return true if x1 < x < x2
     */
    public boolean xInRange(int x1, int x2) {
        return (x > x1 - config.getPointAreaSize()) && (x < x2 + config.getPointAreaSize());
    }


}
