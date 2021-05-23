import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Chess extends JButton {
    private Integer positionX, positionY;

    Chess(Integer x, Integer y) {
        this.positionX = x;
        this.positionY = y;
        this.setFont(new Font("Arial", Font.PLAIN, 40));
    }

    public void setPiece(String piece) {
        this.setText(piece);
        this.setEnabled(false);
    }

    public void resetPiece() {
        this.setText("");
        this.setEnabled(true);
    }

}
