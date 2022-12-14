package men.brakh.graphicseditor.view.controls;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListCell;
import men.brakh.graphicseditor.config.Configuration;
import men.brakh.graphicseditor.model.Point;
import men.brakh.graphicseditor.model.PointType;
import men.brakh.graphicseditor.model.canvas.AbstractCanvas;
import men.brakh.graphicseditor.model.canvas.impl.JavaFXCanvas;
import men.brakh.graphicseditor.model.figure.*;
import men.brakh.graphicseditor.model.figure.intf.Movable;

public class FiguresListCell extends ListCell<String> {
    private final int canvasSize = 70;
    private final int padding = 5;

    private Configuration config = Configuration.getInstance();

    private Canvas canvasInCell = new Canvas(canvasSize + 10, canvasSize);
    private AbstractCanvas canvas = new JavaFXCanvas(canvasInCell);

    @Override
    public void updateItem(String name, boolean empty) {
        canvas.setBrushColor("#000");

        super.updateItem(name, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            FigureFactory.getFigure(name, canvas, new Point(padding, padding)).ifPresent(
                    figure -> {
                        if(figure instanceof Movable) {
                            ((Movable) figure).move(new Point(config.getMinFigureWidth(), config.getMinFigureHeight()));
                        }
                         else if (figure instanceof AbstractRectFigure) {
                            if(figure instanceof Movable) {
                                ((Movable) figure).move(new Point(0, padding));
                            }
                            figure.moveStartPoint(PointType.RB_VERTEX, new Point(padding, padding), new Point(canvasSize - padding, canvasSize - padding * 2));
                        } else if (figure instanceof AbstractLine) {
                            figure.moveStartPoint(PointType.POINT_NODE, new Point(padding, padding), new Point(canvasSize - padding, canvasSize - padding * 2));
                        }
                    }
            );

            canvas.redraw();
            setText(name);
            setGraphic(canvasInCell);
        }
    }
}
