package XOGame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int fieldSize = 5;
    private static final int marksInLine = 4;
    private static final int cellSize = 60;
    private static final String humanMark = "X";
    private static final String compMark = "O";

    private static GridPane gameField;
    private static Label[][] labels = new Label[fieldSize][fieldSize];
    private static int[][] decisionArray = new int[fieldSize][fieldSize];
    private static Label labelClicked;
    private static Scene scene;
    private static boolean gameStopped = false;
    private static int decisionX; // координаты, в которые будет ходить компьютер
    private static int decisionY;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("xofield.fxml"));
        primaryStage.setTitle("XO Game");
        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        gameField = (GridPane) scene.lookup("#GameField");

        // Заполняе игровое поле ярлыками (пока пустыми)
        for (int i = 0; i < fieldSize; i++)
            for (int j = 0; j < fieldSize; j++) {
                labels[i][j] = new Label("");
                setLabel(i,j);  // становка необходимых полей текстового ярлыка

                // запускаем обработку нажатия мыши для каждого ярлыка
                labels[i][j].setOnMouseClicked(e -> {
                    if (gameStopped) return; // если игра окончена, не обрабатываем клики мышью
                    labelClicked = (Label) e.getSource();   // узнаем на каком именно ярлыке нажата клавиша мыши
                    if (labelClicked.getText().equals("")){ // если клетка еще не занята, делаем ход
                        labelClicked.setText(humanMark);    // ставим в клетку метку игрока
                        if (checkWinner(humanMark, marksInLine)) {       // если этот ход привел к победе, то
                            scene.lookup("#win").setVisible(true);  // сообщаем о победе
                            gameStopped = true;                             // прекращаем игру
                            return;
                        }
                        else if (checkDraw()) {
                            scene.lookup("#draw").setVisible(true);
                            return;
                        }
                        // Здесь ходит компьютер
                        for (int k = 0; k < fieldSize; k++)
                            for (int l = 0; l < fieldSize; l++) {
                                decisionArray[k][l] = 0;  // обнуляем вес важности хода  вданную клетку
                                if (labels[k][l].getText().equals("")) {
                                    // проверка, может ли противник следующим ходом сюда походить и выиграть
                                    labels[k][l].setText(humanMark);
                                    if (checkWinner(humanMark,marksInLine)) decisionArray[k][l]+=30; // большой вес, но даже 3 будущих линии противника не первесят немедленный выигрыш
                                    // может ли человек ходом сюда составить последовательность на 1 короче победной?
                                    if (checkWinner(humanMark,marksInLine-1)) decisionArray[k][l]+=10;
                                    // проверка, могу ли я закончить игру, походив в эту точку
                                    labels[k][l].setText(compMark);
                                    if (checkWinner(compMark,marksInLine)) decisionArray[k][l]+=100; // максимальный вес! надо ходить обязательно!
                                    // если я могу составить линию на 1 короче победной
                                    if (checkWinner(compMark,marksInLine-1)) decisionArray[k][l]+=9;
                                    labels[k][l].setText(""); // возврат пустого значения в проверяемую клетку
                                }
                            }

                        int currentDecision = 0;
                        do {
                            decisionX = (int) (Math.random() * fieldSize); // если на основании весов решение не появится,
                            decisionY = (int) (Math.random() * fieldSize); // сделай ход в случайное место
                        } while(!(labels[decisionX][decisionY].getText().equals(""))); // обновляем координаты пока не попадем в пустую клетку
                        for (int k = 0; k < fieldSize; k++)
                            for (int l = 0; l < fieldSize; l++) {
                                if (decisionArray[k][l]>currentDecision){ // если вес текущей клетки больше максимального найденного до сих пор
                                    currentDecision =decisionArray[k][l]; // сохрани ее вес
                                    decisionX = k;                        // и запомни координаты хода
                                    decisionY = l;
                                }
                            }
                        labels[decisionX][decisionY].setText(compMark);  // устанавливаем в выбранную клетку маркер компьютера
                        if (checkWinner(compMark, marksInLine)) { // проверяем, не привел ли ход к победе компьютера
                            gameStopped = true;
                            scene.lookup("#compwin").setVisible(true);  // сообщаем о победе
                        }
                        else if (checkDraw()) {
                            scene.lookup("#draw").setVisible(true);
                        }

                    }
                }); // конец обработки нажатия мыши

                gameField.add(labels[i][j], i, j);    // добавляем ярлык в табличный вид игрового поля
            }
    }

    private boolean checkDraw() {
        for (int i = 0; i < fieldSize; i++)
            for (int j = 0; j < fieldSize; j++)
                if (labels[i][j].getText().equals("")) return false;
        return true;
    }

    private void setLabel(int i, int j) {
        labels[i][j].setStyle("-fx-background-color: transparent");
        labels[i][j].setPrefWidth(cellSize);
        labels[i][j].setMinWidth(cellSize);
        labels[i][j].setPrefHeight(cellSize);
        labels[i][j].setMinHeight(cellSize);
        labels[i][j].setAlignment(Pos.CENTER);
        labels[i][j].setFont(Font.font("System", FontWeight.NORMAL, 40));
    }

    public static void resetField(){
        for (int i = 0; i < fieldSize; i++)
            for (int j = 0; j < fieldSize; j++)
                Main.labels[i][j].setText("");
        scene.lookup("#win").setVisible(false);
        scene.lookup("#compwin").setVisible(false);
        scene.lookup("#draw").setVisible(false);
        gameStopped = false;
    }

    private boolean checkWinner(String mark, int lengthToWin) {
        for (int i = 0; i < fieldSize; i++)
            for (int j = 0; j < fieldSize; j++) {
                if (labels[i][j].getText().equals(mark)){
                    // check down
                    if (j<=fieldSize-lengthToWin)
                        if (checkLine(mark, i, j, 0, 1, lengthToWin)) return true;
                    // check right
                    if (i<=fieldSize-lengthToWin)
                        if (checkLine(mark, i, j, 1, 0, lengthToWin)) return true;
                    // check diagonal \
                    if (i<=fieldSize-lengthToWin && j<=fieldSize-lengthToWin)
                        if (checkLine(mark, i, j, 1, 1, lengthToWin)) return true;
                    if (i>=lengthToWin && j<=fieldSize-lengthToWin)
                        if (checkLine(mark, i, j, -1, 1, lengthToWin)) return true;
                }
            }
        return false;
    }

    private boolean checkLine(String mark, int startX, int startY, int dx, int dy, int length) {
        int counter = 1;
        for (int i = 1; i < length; i++)
            if (labels[startX+dx*i][startY+dy*i].getText().equals(mark)) counter += 1;
        return (counter == length);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
