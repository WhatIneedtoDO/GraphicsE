package men.brakh.graphicseditor.model.figure;

import men.brakh.graphicseditor.config.Configuration;
import men.brakh.graphicseditor.model.Point;
import men.brakh.graphicseditor.model.PointType;
import men.brakh.graphicseditor.model.canvas.AbstractCanvas;
import men.brakh.graphicseditor.model.figure.intf.TextSerializible;

/**
 * Фигура, которую можно вписать в прямоугольник (По факту, на данный момени, все кроме линии)
 */
public abstract class AbstractRectFigure implements Figure, TextSerializible {
    private Configuration config = Configuration.getInstance();

    protected AbstractCanvas canvas;

    private String brushColor;
    private String penColor;
    private int penWidth;

    private int left; // Левая
    private int bottom; // нижняя точка

    private int right; // Правая
    private int top; // верхняя точка


    /**
     * Конструктор фигуры, которую можно вписать в прямоугольник
     * @param canvas Объект канваса
     * @param startPoint Начальные координаты
     */
    public AbstractRectFigure(AbstractCanvas canvas, Point startPoint) {
        this.canvas = canvas;
        this.left = startPoint.getX() - config.getMinFigureWidth();
        this.bottom = startPoint.getY();
        this.right = startPoint.getX() ;
        this.top = startPoint.getY() - config.getMinFigureHeight();

        this.brushColor = canvas.getBrushColor();
        this.penColor = canvas.getPenColor();
        this.penWidth = canvas.getPenWidth();

        if(canvas != null)
            canvas.addFigure(this);
    }


    /**
     * Копирование фигуры
     * Копия на канвас не добавляется автоматически
     */
    @Override
    public Figure copy() {
        AbstractRectFigure newFigure = null;
        try {
            newFigure = this.getClass().getConstructor(AbstractCanvas.class, Point.class)
                    .newInstance(this.canvas, this.getRightBottomPoint());

            newFigure.assign(this);

            canvas.removeFigure(newFigure);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newFigure;
    }

    /**
     * Скопировать все свойства у другой фигуры
     */
    @Override
    public void assign(Figure figure) {
        AbstractRectFigure rectFigure = (AbstractRectFigure) figure;
        this.left = rectFigure.left;
        this.right = rectFigure.right;
        this.bottom = rectFigure.bottom;
        this.top = rectFigure.top;

        this.canvas = rectFigure.canvas;

        this.brushColor = rectFigure.brushColor;
        this.penWidth = rectFigure.penWidth;
        this.penColor = rectFigure.penColor;
    }


    /**
     * Координаты левой нижней точки
     * @return Координаты левой нижней точки
     */
    protected Point getLeftTopPoint() {
        return new Point(left, top);
    }

    /**
     * Коордитаты правоый верхней точки
     * @return Коордитаты правоый верхней точки
     */

    protected Point getRightBottomPoint() {
        return new Point(right, bottom);
    }

    /**
     * Возвращает тип точки в пределах данной фигуры
     * @param point Точка
     * @return тип точки в пределах фигуры
     */
    @Override
    public PointType checkPoint(Point point) {
        if(!point.yInRange(top, bottom) || !point.xInRange(left, right)) {
            return PointType.UNKNOWN_POINT; // Точка вне фигуры
        }

        Point leftTop = new Point(left, top);
        Point rightTop = new Point(right, top);
        Point leftBottom = new Point(left, bottom);
        Point rightBottom = new Point(right, bottom);

        if(point.equals(leftTop)) {
            return PointType.LT_VERTEX;
        }

        if(point.equals(rightTop)) {
            return PointType.RT_VERTEX;
        }

        if(point.equals(leftBottom)) {
            return PointType.LB_VERTEX;
        }

        if(point.equals(rightBottom)) {
            return PointType.RB_VERTEX;
        }

        if(point.xEquals(left) && point.yInRange(top, bottom)) {
            return PointType.LEFT_SIDE;
        }

        if(point.xEquals(right) && point.yInRange(top, bottom)) {
            return PointType.RIGHT_SIDE;
        }

        if(point.yEquals(top) && point.xInRange(left, right)) {
            return PointType.TOP_SIDE;
        }

        if(point.yEquals(bottom) && point.xInRange(left, right)) {
            return PointType.BOTTOM_SIDE;
        }

        return PointType.POINT_INSIDE; // Остался только один вариант - точка внутри фигуры
    }

    /**
     * Изменение размера фигуры
     * @param pointType Тип точки, которую тянем ({@link PointType})
     * @param fromPoint Не используется. Используется только в {@link AbstractLine}
     * @param toPoint Точка, в которую переместили вершину
     */
    protected void resize(PointType pointType, Point fromPoint, Point toPoint) {
        int oldLeft = left;
        int oldTop = top;
        int oldRight = right;
        int oldBottom = bottom;

        switch (pointType) {
            case LT_VERTEX: // Левый верхний
                left = toPoint.getX();
                top = toPoint.getY();
                break;
            case RT_VERTEX: // Правый верхний
                right = toPoint.getX();
                top = toPoint.getY();
                break;
            case LB_VERTEX: // Левый нижний
                left = toPoint.getX();
                bottom = toPoint.getY();
                break;
            case RB_VERTEX: // Правый нижний
                right = toPoint.getX();
                bottom = toPoint.getY();
                break;
            case LEFT_SIDE:
                left = toPoint.getX();
                break;
            case RIGHT_SIDE:
                right = toPoint.getX();
                break;
            case TOP_SIDE:
                top = toPoint.getY();
                break;
            case BOTTOM_SIDE:
                bottom = toPoint.getY();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        normalzie(); // Выполняем нормализацию координат после перемещения

        if(right - left < config.getMinFigureWidth()) {
            left = oldLeft;
            right = oldRight;
        }

        if(bottom - top < config.getMinFigureHeight()) {
            top = oldTop;
            bottom = oldBottom;
        }

    }

    /**
     * Перемещение стартовой точки
     */
    @Override
    public void moveStartPoint(PointType pointType, Point fromPoint, Point toPoint) {
        resize(pointType, fromPoint, toPoint);
    }

    /**
     * Возвращает true, если текущая фигура находится внутри другой фигуры
     * @param rectFigure Другая фигура
     */
    @Override
    public boolean isInside(Figure rectFigure) {
        AbstractRectFigure figure = (AbstractRectFigure) rectFigure;

        return this.getLeftTopPoint().xInRange(figure.left, figure.right)
            && this.getRightBottomPoint().xInRange(figure.left, figure.right)
            && this.getLeftTopPoint().yInRange(figure.top, figure.bottom)
            && this.getRightBottomPoint().yInRange(figure.top, figure.bottom);
    }

    /**
     * Перемещение фигуры
     * @param deltaPoint delta перемещения
     */
    protected void move(Point deltaPoint) {
        left += deltaPoint.getX();
        right += deltaPoint.getX();
        bottom += deltaPoint.getY();
        top += deltaPoint.getY();
    }

    /**
     * Нормализация координат (Левая координата должна быть левее правой и т.д.)
     * @return true если нормализация была применена
     */
    private boolean normalzie() {
        boolean isNormalized = false;

        if(bottom + config.getPointAreaSize() < top) { // На канвасе ось У сверху вниз
            int tmp = top;
            top = bottom;
            bottom = tmp;
            isNormalized = true;
        }

        if(left > right + config.getPointAreaSize()) {
            int tmp = left;
            left = right;
            right = tmp;
            isNormalized = true;
        }

        return isNormalized;
    }

    /**
     * Выделение фигуры на полотне
     */
    protected void select() {
        int padding = config.getPointAreaSize();
        String color = config.getSelectionColor();


        canvas.withColorSaving(color, color, config.getSelectionPenWidth(), () ->
                {
                    canvas.drawRectangle(new Point(left - padding, bottom - padding), new Point(left + padding, bottom + padding));
                    canvas.drawRectangle(new Point(right - padding, bottom - padding), new Point(right + padding, bottom + padding));
                    canvas.drawRectangle(new Point(left - padding, top - padding), new Point(left + padding, top + padding));
                    canvas.drawRectangle(new Point(right - padding, top - padding), new Point(right + padding, top + padding));

                    String penBackup = penColor;
                    penColor = color;
                    canvas.drawStrokeRectangle(getLeftTopPoint(), getRightBottomPoint());
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
        // ClassName ; BRUSHCOLOR ; PENCOLOR; PENSIZE ; LEFT ; TOP ; RIGHT ; BOTTOM;
        return String.format("%s;%s;%s;%d;%d;%d;%d;%d\n",
                this.getClass().getSimpleName(), brushColor, penColor, penWidth, left, top, right, bottom);
    }

    /**
     * Десериализация из CSV
     * @param text csv
     */
    @Override
    public boolean deserialize(String text) {
        try {
            String[] rows = text.split(";");

            if(rows.length != 8)
                return false;

            brushColor = rows[1];
            penColor = rows[2];
            penWidth = Integer.parseInt(rows[3]);
            left = Integer.parseInt(rows[4]);
            top = Integer.parseInt(rows[5]);
            right = Integer.parseInt(rows[6]);
            bottom = Integer.parseInt(rows[7]);


            return true;
        } catch (Exception e) {
            canvas.removeFigure(this);
            return  false;
        }
    }

    /**
     * Отрисовка фигуры на полотне
     */
    @Override
    public abstract void draw();

    /* ГЕТТЕРЫ И СЕТТЕРЫ */

    @Override
    public String getPenColor(){
        return penColor;
    }

    @Override
    public String getBushColor() {
        return brushColor;
    }

    @Override
    public int getPenWidth(){
        return penWidth;
    }

    @Override
    public void setPenColor(String color) {
        penColor = color;
    }

    @Override
    public void setBrushColor(String color) {
        brushColor = color;
    }

    @Override
    public void setPenWidth(int width) {
        penWidth = width;
    }

    protected void setLeft(int left) {
        this.left = left;
    }

    protected void setBottom(int bottom) {
        this.bottom = bottom;
    }

    protected void setRight(int right) {
        this.right = right;
    }

    protected void setTop(int top) {
        this.top = top;
    }

}
