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

    public static final int fieldSize = 5;
    public static final int marksInLine = 4;
    public static final int cellSize = 60;
    public static final String humanMark = "X";
    public static final String compMark = "O";

    public static GridPane gameField;
    public static Label[][] labels = new Label[fieldSize][fieldSize];
    public static int[][] decisionArray = new int[fieldSize][fieldSize];
    public static Label labelClicked;
    public static Scene scene;
    public static boolean gameStopped = false;


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
                labels[i][j].setStyle("-fx-background-color: transparent");
                labels[i][j].setPrefWidth(cellSize);
                labels[i][j].setMinWidth(cellSize);
                labels[i][j].setPrefHeight(cellSize);
                labels[i][j].setMinHeight(cellSize);
                labels[i][j].setAlignment(Pos.CENTER);
                labels[i][j].setFont(Font.font("System", FontWeight.NORMAL, 40));

                // запускаем обработку нажатия мыши для каждого ярлыка
                labels[i][j].setOnMouseClicked(e -> {
                    if (gameStopped) return;
                    labelClicked = (Label) e.getSource();
                    if (labelClicked.getText().equals("")){
                        labelClicked.setText(humanMark);
                        if (checkWinner(humanMark)) {
                            scene.lookup("#win").setVisible(true);
                            gameStopped = true;
                            return;
                        }
                        // Здесь ходит компьютер
                        for (int k = 0; k < fieldSize; k++)
                            for (int l = 0; l < fieldSize; l++) {
                                decisionArray[k][l] = 0;
                                if (labels[k][l].getText().equals("")) {
                                    // проверка, может ли противник следующим ходом сюда походить и выиграть
                                    labels[k][l].setText(humanMark);
                                    if (checkWinner(humanMark)) decisionArray[k][l]+=30; // большой вес, но даже 3 будущих линии противника не первесят немедленный выигрыш
                                    // проверка, могу ли я закончить игру, походив в эту точку
                                    labels[k][l].setText(compMark);
                                    if (checkWinner(compMark)) decisionArray[k][l]+=100; // максимальный вес! надо ходить обязательно!
                                    // проверка, появятся ли 3- и 2-значные линии при ходе сюда

                                    labels[k][l].setText(""); // возврат пустого значения в проверяемую клетку
                                }
                            }

                        int currentDecision = 0;
                        int decisionX;
                        int decisionY;
                        do {
                            decisionX = (int) (Math.random() * fieldSize);
                            decisionY = (int) (Math.random() * fieldSize);
                        } while(!(labels[decisionX][decisionY].getText().equals("")));
                        for (int k = 0; k < fieldSize; k++)
                            for (int l = 0; l < fieldSize; l++) {
                                if (decisionArray[k][l]>currentDecision){
                                    currentDecision =decisionArray[k][l];
                                    decisionX = k;
                                    decisionY = l;
                                }
                            }
                        labels[decisionX][decisionY].setText(compMark);
                        if (checkWinner(compMark)) gameStopped = true;
                    }
                }); // конец обработки нажатия мыши

                gameField.add(labels[i][j], i, j);    // добавляем ярлык в табличный вид игр4ового поля
            }
    }

    private boolean checkWinner(String mark) {
        for (int i = 0; i < fieldSize; i++)
            for (int j = 0; j < fieldSize; j++) {
                if (labels[i][j].getText().equals(mark)){
                    // check down
                    if (j<=fieldSize-marksInLine)
                        if (checkLine(mark, i, j, 0, 1, marksInLine)) return true;
                    // check right
                    if (i<=fieldSize-marksInLine)
                        if (checkLine(mark, i, j, 1, 0, marksInLine)) return true;
                    // check diagonal \
                    if (i<=fieldSize-marksInLine && j<=fieldSize-marksInLine)
                        if (checkLine(mark, i, j, 1, 1, marksInLine)) return true;
                    if (i>=marksInLine && j<=fieldSize-marksInLine)
                        if (checkLine(mark, i, j, -1, 1, marksInLine)) return true;
                }
            }
        return false;
    }

    private boolean checkLine(String mark, int startX, int startY, int dx, int dy, int length) {
        int counter = 1;
        for (int i = 1; i < length; i++)
            if (labels[startX+dx*i][startY+dy*i].getText().equals(mark)) counter += 1;
        if (counter == length) return true;
        else return false;
    }

    public static void resetField(){
        for (int i = 0; i < fieldSize; i++)
            for (int j = 0; j < fieldSize; j++)
                Main.labels[i][j].setText("");
        scene.lookup("#win").setVisible(false);
        gameStopped = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
