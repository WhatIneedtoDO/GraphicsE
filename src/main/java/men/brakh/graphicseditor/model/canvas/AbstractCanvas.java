package men.brakh.graphicseditor.model.canvas;

import men.brakh.graphicseditor.model.Point;
import men.brakh.graphicseditor.model.PointType;
import men.brakh.graphicseditor.model.figure.AbstractRectFigure;
import men.brakh.graphicseditor.model.figure.Figure;
import men.brakh.graphicseditor.model.figure.intf.Selectable;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Абстракция поверх различных реализаций канваса (от swing, javafx и тд)
 */
public abstract class AbstractCanvas {
    private List<Figure> figures = new ArrayList<>();

    /**
     * Множество выделенных фигур. Hashcode && Equals у фигур стандартные, все сравнения - по ссылкам
     */

    private Set<Figure> selectedFigures = new HashSet<>();
    protected HashMap<List <Figure>, ArrayList<Point>> clickedPoints = new HashMap<>();

    public synchronized void addToList (Figure figure, Point point ) {
        ArrayList<Point> lineP = clickedPoints.get(figure);
        if (lineP == null) {
            lineP = new ArrayList<Point>();
            lineP.add(point);
            System.out.println("lineP values : " + lineP);
            clickedPoints.put(getSelected(), lineP);
            System.out.println("hashmap add values : " + clickedPoints);
        } else {
            if (!lineP.contains(point)) lineP.add(point);
        }
        System.out.println("hashmap contains : " + clickedPoints);
    }



    public void setClickedPoints(HashMap<List<Figure>, ArrayList<Point>> clickedPoints) {
        this.clickedPoints = clickedPoints;
    }
    public HashMap<List<Figure>, ArrayList<Point>> getClickedPoints() {
        return clickedPoints;
    }


    /**
     * Выполнение метода с сохранением цвета полотна
     * (Применяется цвет фигуры, после чего возвращается предыдущие цвета полотна)
     * @param brushColor Цвет кисти
     * @param penColor Цвет линий
     * @param penWidth Размер линий
     * @param func Функция, которую надо выполнить
     */
    public void withColorSaving(String brushColor, String penColor, int penWidth, Callable<Void> func) {
        String brushColorBackup = getBrushColor();
        String penColorBackup = getPenColor();
        int penWidthBackup = getPenWidth();

        setBrushColor(brushColor);
        setPenColor(penColor);
        setPenWidth(penWidth);


        try {
            func.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setBrushColor(brushColorBackup);
        setPenColor(penColorBackup);
        setPenWidth(penWidthBackup);
    }

    /**
     * Очистка полотна
     */
    public abstract void clear();

    /**
     * Перерисовка полотна
     */
    public void redraw() {
        clear();

        figures.forEach(Figure::draw); // Отрисовываем каждую фигуру
        selectedFigures.forEach(
                figure -> {
                    if(figure instanceof Selectable) {
                        ((Selectable) figure).select();
                    }
                }
        ); // Выделяем фигуры, которые нужно
    }

    /**
     * Меняет курсор на канвасе
     * @param point Точка, на которую наведен курсор
     */
    public abstract void changeCursor(Point point);

    /**
     * Добавление фигуры на полотно
     * @param figure объект фигуры
     * @return добавленная фигура
     */
    public Figure addFigure(Figure figure) {
        figures.add(figure);
        figure.draw();

        return figure;
    }

    /**
     * Удаление фигуры с полотна
     * @param figure Объект фигуры
     */
    public void removeFigure(Figure figure) {
        figures.remove(figure);
        redraw();
    }

    public void removeAll(List<Figure> removedFigures) {
        figures.removeAll(removedFigures);
        redraw();
    }

    public void removeAll() {
        figures.clear();
        redraw();
    }

    /**
     * Метод возвращает фигуру, расположенную в точке
     * @param point Точка
     * @return Опционал фигуры (Если в точке нет фигуры - опционал пустой)
     */
    public Optional<Figure> getFigureAtPoint(Point point) {
        for(int i = figures.size() - 1; i >= 0; i--) {
            Figure figure = figures.get(i);
            PointType type = figure.checkPoint(point);
            if(type != PointType.UNKNOWN_POINT) {
                return Optional.of(figure);
            }
        }


        return Optional.empty();
    }

    /*
     * Выделение
     */

    /**
     * Выделение фигуры на полотне
     * @param figure Объект фигуры
     */
    public void select(Figure figure) {
        if(figure instanceof Selectable) {
            selectedFigures.add(figure);
            ((Selectable) figure).select();
        }
    }

    /**
     * Выделение фигур на полотне
     * @param figures Коллекция фигур
     */
    public void selectAll(Collection<Figure> figures) {
        selectedFigures.addAll(figures);

        figures.forEach(
                figure -> {
                    if(figure instanceof Selectable) {
                        ((Selectable) figure).select();
                    }
                }
        );
    }

    /**
     * Возвращает список фигур внутри прямоугольника
     */
    public List<Figure> getFiguresInside(AbstractRectFigure figure) {
        List<Figure> figuresInside = new ArrayList<>();

        figures.forEach(
                currFigure -> {
                    if(currFigure.isInside(figure)) {
                        figuresInside.add(currFigure);
                    }
                }
        );

        return figuresInside;
    }

    /**
     * Проверяет, выделена ли фигура
     * @param figure объект фигуры
     * @return true если фигура выделена
     */
    public boolean isSelected(Figure figure) {
        return selectedFigures.contains(figure);
    }

    /**
     * Снимает выделение фигуры
     * @param figure Объект фигуры
     */
    public void unSelect(Figure figure) {
        selectedFigures.remove(figure);
        redraw(); // После очистки перерисовываем
    }

    /**
     * Снимает выделение ВСЕХ фигур
     */
    public void unSelectAll() {
        selectedFigures.clear();
        redraw(); // После очистки перерисовываем
    }

    public List<Figure> getSelected() {
        return new ArrayList<>(selectedFigures);
    }

    public List<Figure> getAllFigures() {
        return figures;
    }

    /*
     * РИСОВАНИЕ
     */

    /**
     * Отрисовка линии из точки1 в точку2
     * @param point1 Точка 1
     * @param point2 Точка 2
     */
    public abstract void drawLine(Point point1, Point point2);

    /**
     * Отрисовка прямоугольника
     * @param leftTop Левая верхняя точка
     * @param rightBottom Правая нижняя точка
     */
    public abstract void drawRectangle(Point leftTop, Point rightBottom);

    /**
     * Отрисовка полого прямоугольника
     * @param leftTop Левая верхняя точка
     * @param rightBottom Правая нижняя точка
     */
    public abstract void drawStrokeRectangle(Point leftTop, Point rightBottom);

    /*
     * ПОЛЯ САМОГО КАНВАСА
     */

    public abstract String getBrushColor();
    public abstract void setBrushColor(String brushColor);
    public abstract String getPenColor();
    public abstract void setPenColor(String penColor);
    public abstract int getPenWidth();
    public abstract void setPenWidth(int borderSize);
}
