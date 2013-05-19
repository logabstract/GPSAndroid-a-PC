
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


public class Lienzo extends Canvas{
    int posX;
    int posY;
    int angulo;
    
    public Lienzo(int posX,int posY,int angulo) {
        super();
        this.posX=posX;
        this.posY=posY;
        this.angulo=angulo;
        setBackground(Color.WHITE);
        setBounds(0, 0, 500,500);
        setVisible(true);
                
    }
    public void dibujar(){
        repaint();
    }
    
    public void update(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//antialiasing
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dbi = bi.createGraphics();
        pintarOffScreen(g2dbi);
        g2d.drawImage(bi, 0, 0, this);
    }
    public void pintarOffScreen(Graphics2D g2d){
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//antialiasing
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
    public void iniciar(Graphics g){
        update(g);
        int j=-10;
        g.drawLine(250, 0, 250, 500);
        g.drawLine(0, 250, 500, 250);
        for(int i=0;i<500;i=i+25){
            g.drawLine(i, 246, i, 254);
            g.drawString(String.valueOf(j), i-5, 263);
            j++;
        }
        for(int i=0;i<500;i=i+25){
            g.drawLine(246, 0+i, 254, 0+i);
            g.drawString(String.valueOf(j), 235, i+5);
            j--;
        }
        g.setColor(Color.red);
        g.fillOval(250+(posX/4), 250-(posY/4), 5, 5);
    }
    @Override
    public void paint(Graphics g){
        iniciar(g);
    }
    
}
