package men.brakh.graphicseditor.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;


import men.brakh.graphicseditor.config.Configuration;
import men.brakh.graphicseditor.model.Mode;
import men.brakh.graphicseditor.model.Point;
import men.brakh.graphicseditor.model.PointType;
import men.brakh.graphicseditor.model.canvas.AbstractCanvas;
import men.brakh.graphicseditor.model.canvas.impl.JavaFXCanvas;
import men.brakh.graphicseditor.model.changes.ChangeType;
import men.brakh.graphicseditor.model.changes.ChangesStack;
import men.brakh.graphicseditor.model.figure.AbstractLine;
import men.brakh.graphicseditor.model.figure.Figure;
import men.brakh.graphicseditor.model.figure.FigureFactory;
import men.brakh.graphicseditor.model.figure.impl.Rectangle;
import men.brakh.graphicseditor.model.figure.intf.Movable;
import men.brakh.graphicseditor.model.figure.intf.Resizable;
import men.brakh.graphicseditor.model.figure.serializer.CsvFigureSerializer;
import men.brakh.graphicseditor.model.figure.serializer.FigureSerializer;
import men.brakh.graphicseditor.view.controls.FiguresListCell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static men.brakh.graphicseditor.model.Mode.MODE_CREATE;
import static men.brakh.graphicseditor.model.PointType.POINT_INSIDE;

public class Controller {
    private final KeyCombination undoKc = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    private final KeyCombination redoKc = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);


    @FXML
    private MenuItem menuRedo;

    @FXML
    private MenuItem menuUndo;

    @FXML
    private Canvas canvasMain;

    @FXML
    private ListView<String> lwFigures;


    @FXML
    private ColorPicker cpPen;

    @FXML
    private Label lblPenWidth;

    @FXML
    private Slider sliderPenWidth;

    private Rectangle selection;


    private Configuration config = Configuration.getInstance();

    private AbstractCanvas canvas;


    private Mode mode; // ?????????????? ?????????? ????????????

    private Point prevPoint; // ???????????????????? ???????????????????????????? ??????????

    private Figure currentFigure; // ?????????????? ????????????

    private PointType currPointType; // ?????????????? ?????? ??????????

    private ChangesStack changesStack; // ???????? ??????????????????


    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        canvas = new JavaFXCanvas(canvasMain);
        mode = Mode.MODE_VIEW;

        List<String> figuresNames = FigureFactory.getFiguresNames();

        figuresNames.add(0, config.getFigureNoneName());
        ObservableList<String> items = FXCollections.observableArrayList(figuresNames);
        lwFigures.setItems(items);
        lwFigures.setCellFactory(param -> new FiguresListCell());
        lwFigures.getSelectionModel().selectFirst();

        cpPen.setValue(Color.BLACK);


        changesStack = new ChangesStack(canvas, config.getChangesStackSize());
    }

    /**
     * ?????????????????? ???????????? ?? ?????????????????????? color picker
     */
    private void selectWithColorUpdating(Figure figure) {
        canvas.select(figure);
        if (canvas.getSelected().size() == 1) {
            if (figure.getBushColor() != null)
                cpPen.setValue(Color.web(figure.getPenColor()));
            lblPenWidth.setText(Integer.toString(figure.getPenWidth()));
            sliderPenWidth.setValue(figure.getPenWidth());
        }
    }

    /**
     * ???????????? ?????????????????? ?????????????????????? color picker
     */
    private void unselectAllWithColorUpdating() {
        canvas.unSelectAll();

        cpPen.setValue(Color.web(canvas.getPenColor()));
        lblPenWidth.setText(Integer.toString(canvas.getPenWidth()));
        sliderPenWidth.setValue(canvas.getPenWidth());
    }


    @FXML
    void canvasOnMouseDragged(MouseEvent event) {
        Point clickedPoint = new Point(event.getX(), event.getY());
        switch (mode) {
            case MODE_VIEW:
                break;
            case MODE_RESIZE:
                if (currentFigure instanceof Resizable) {
                    ((Resizable) currentFigure).resize(currPointType, prevPoint, clickedPoint);
                }
                break;
            case MODE_MOVE:
                List<Figure> selectedFigures = canvas.getSelected();

                selectedFigures.forEach(figure -> {
                    if (figure instanceof Movable) {
                        ((Movable) figure).move(clickedPoint.delta(prevPoint));
                    }
                });
                break;
            case MODE_SELECTION:
                currentFigure = selection;
            case MODE_CREATE:
                PointType pointType = currentFigure.checkPoint(prevPoint);

                // ???????? ?????? ???????????????? ?????? ?????????? ?????????????????? => ??????????????????, ?????????? - ?????????????????? ????????????????????
                switch (pointType) {
                    case LT_VERTEX:
                    case RT_VERTEX:
                    case LB_VERTEX:
                    case RB_VERTEX:
                        currPointType = pointType;
                        break;
                }
                currentFigure.moveStartPoint(currPointType, prevPoint, clickedPoint);
                break;

        }

        prevPoint = clickedPoint; // ?????????????????? ???????????????????? ????????????????????
        canvas.redraw();
    }

    /**
     * ?????????????????? ???????? ???? ?????????????? ????????????????????????
     */
    private void blankAreaClick(Point clickedPoint) {
        // ?????????????? ???????????????????????? ??????????????????
        unselectAllWithColorUpdating();

        String selectedFigureName = lwFigures.getSelectionModel().getSelectedItem();

        if (selectedFigureName.equals(config.getFigureNoneName())) { // ???????? ???????????????? ???? ???????? - ???????? ????????????????
            prevPoint = clickedPoint;
            mode = Mode.MODE_SELECTION;
            selection = new Rectangle(canvas, clickedPoint);
            selection.setBrushColor("#fff0");
            currPointType = selection.checkPoint(clickedPoint);
            return;
        }


        // ?????????????? ????????????
        FigureFactory.getFigure(selectedFigureName, canvas, clickedPoint).ifPresent(
                createdFigure -> {
                    prevPoint = clickedPoint; // ?????????????????? ???????????????????? ????????????????????
                    currPointType = createdFigure.checkPoint(clickedPoint); // ?????? ?????????? ?????? ????????????????
                    mode = MODE_CREATE; // ???????????? ?????????? ???? ???????????????? ????????????
                    currentFigure = createdFigure;
                }
        );

    }

    @FXML
    void onKeyPressed(KeyEvent event) {
        if (undoKc.match(event)) {
            menuUndoClicked(event);
            return;
        } else if (redoKc.match(event)) {
            menuRedoClicked(event);
            return;
        }
        switch (event.getCode()) {
            case DELETE:
                List<Figure> selectedFigure = canvas.getSelected();
                selectedFigure.forEach(figure -> changesStack.add(ChangeType.CHANGE_REMOVE, figure));
                menuUndo.setDisable(false);
                canvas.removeAll(selectedFigure);
                unselectAllWithColorUpdating();
                break;
        }

    }

    @FXML
    void canvasOnMouseMoved(MouseEvent event) {
        canvas.changeCursor(new Point(event.getX(), event.getY()));
    }

    /**
     * ?????????????????? ?????????? ???? ????????????
     */
    private void figureClick(Point clickedPoint, Figure clickedFigure) {
        currentFigure = clickedFigure; // ?????????????? ???????????? - ??????????????
        prevPoint = clickedPoint; // ?????????????? ???????????????????? - ?????? ????????????????????

        if (canvas.isSelected(clickedFigure)) { // ???????????? ????????????????
            PointType pointType = clickedFigure.checkPoint(clickedPoint);

            if (pointType == POINT_INSIDE) {
                mode = Mode.MODE_MOVE; // ?????????? ???????????? -> ????????????????????
                List<Figure> selectedFigures = canvas.getSelected();
                selectedFigures.forEach(figure -> changesStack.add(ChangeType.CHANGE_MOVE, figure));
                menuUndo.setDisable(false);
            } else {
                mode = Mode.MODE_RESIZE; // ???? ?????????? -> ????????????????
                changesStack.add(ChangeType.CHANGE_RESIZE, clickedFigure);
                menuUndo.setDisable(false);
            }
            currPointType = pointType;

        } else { // ???????????? ???? ???????????????? -> ???????? ????????????????
            unselectAllWithColorUpdating();
            selectWithColorUpdating(clickedFigure);
        }
    }

    @FXML
    public void canvasDoubleClick(MouseEvent event) {

        if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
            mode = MODE_CREATE;
            Point clickedPoint = new Point(event.getX(), event.getY());
            PointType check = currentFigure.checkPoint(clickedPoint);

            if (check.equals(POINT_INSIDE)) {

                canvas.addToList(currentFigure,clickedPoint);

            }
            menuUndo.setDisable(false);
        }
        canvas.getSelected().forEach(figure -> changesStack.add(ChangeType.CHANGE_MOVE, figure));
        canvas.redraw();
    }


    @FXML
        // ???????????? ????????????
    void canvasOnMousePressed(MouseEvent event) {
        switch (event.getButton()) {
            case PRIMARY:
                Point clickedPoint = new Point(event.getX(), event.getY());
                Optional<Figure> clickedFigureOptional = canvas.getFigureAtPoint(clickedPoint);

                boolean isFigureNone = lwFigures.getSelectionModel().getSelectedItem().equals(config.getFigureNoneName());

                if (!clickedFigureOptional.isPresent() || !isFigureNone) { // ???????????? ?? ???????? ?????????? ?????? ???? ???????????????????? => ??????????????????
                    blankAreaClick(clickedPoint);
                } else {
                    figureClick(clickedPoint, clickedFigureOptional.get());
                }
                break;
            case MIDDLE:
                break;
            case SECONDARY:
                break;
        }
    }


    @FXML
        // ?????? ????????????????
    void canvasOnMouseReleased(MouseEvent event) {
        List<Figure> selectedFigures;

        switch (mode) {
            case MODE_CREATE:
                changesStack.add(ChangeType.CHANGE_ADD, currentFigure);
                menuUndo.setDisable(false);
                break;
            case MODE_SELECTION:
                selectedFigures = canvas.getFiguresInside(selection);
                selectedFigures.remove(selection);
                canvas.selectAll(selectedFigures);
                canvas.removeFigure(selection);
                break;
        }

        mode = Mode.MODE_VIEW; // ?????????? ?????????????????? ???????????? - ?????????????????????????????? ?????????? ??????????????????

        canvas.redraw();
    }


    @FXML
    void penColorSelected(ActionEvent event) {
        List<Figure> selected = canvas.getSelected();
        String color = cpPen.getValue().toString();
        if (selected.size() == 0) {
            canvas.setPenColor(color);
        } else { // ???????? ???????? ???????????????????? ????????????, ???? ???????????? ???????? ?????? ???????? ???????? ???????????????????? ??????????
            selected.forEach(figure -> {
                changesStack.add(ChangeType.CHANGE_RECOLOR, figure);
                menuUndo.setDisable(false);
                figure.setPenColor(color);
            });
            canvas.redraw();
        }
    }

    @FXML
    void sliderPenWidthChanged(Event event) {
        List<Figure> selected = canvas.getSelected();
        int penWidth = (int) Math.round(sliderPenWidth.getValue());
        if (selected.size() == 0) {
            canvas.setPenWidth(penWidth);
        } else {
            selected.forEach(figure -> {
                changesStack.add(ChangeType.CHANGE_RECOLOR, figure);
                menuUndo.setDisable(false);
                figure.setPenWidth(penWidth);
            });
            canvas.redraw();
        }
        lblPenWidth.setText(Integer.toString(penWidth));
    }


    // ????????
    @FXML
    void menuNewClicked(Event event) {
        changesStack.clear();
        canvas.removeAll();
    }

    @FXML
    void menuRedoClicked(Event event) {
        changesStack.redo();
        unselectAllWithColorUpdating();

        if (changesStack.redoEmpty()) {
            menuRedo.setDisable(true);
        }

        menuUndo.setDisable(false);

    }

    @FXML
    void menuUndoClicked(Event event) {
        changesStack.undo();
        unselectAllWithColorUpdating();

        if (changesStack.undoEmpty()) {
            menuUndo.setDisable(true);
        }

        menuRedo.setDisable(false);
    }

    private void alert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText("ERROR:");
        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }

    @FXML
    void menuSaveAsClicked(Event event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save this painting");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("BRAKH files (*.brakh)", "*.brakh");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            String filepath = file.getPath();
            if (!file.getName().endsWith(".brakh")) {
                filepath += ".brakh";
            }
            List<Figure> figures = canvas.getAllFigures();

            FigureSerializer serializer = new CsvFigureSerializer();
            String csv = serializer.serialize(figures);

            try {
                Files.write(Paths.get(filepath), csv.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                alert(e);
            }
        }
    }

    @FXML
    void menuOpenClicked(Event event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open this painting");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("BRAKH files (*.brakh)", "*.brakh");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                List<String> rows = Files.readAllLines(Paths.get(file.getPath()));

                FigureSerializer serializer = new CsvFigureSerializer();

                List<Figure> figures = serializer.deserialize(canvas, rows);

                if (figures.size() == 0) {
                    alert(new RuntimeException("Invalid or empty file"));
                } else {
                    canvas.removeAll();
                    figures.forEach(
                            figure -> canvas.addFigure(figure)
                    );

                }


            } catch (IOException e) {
                alert(new RuntimeException("Invalid or empty file"));
            }
        }
    }
}
