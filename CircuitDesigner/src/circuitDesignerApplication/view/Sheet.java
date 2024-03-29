package circuitDesignerApplication.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import circuitDesignerApplication.Main;
import circuitDesignerApplication.controller.Mouse;
import circuitDesignerApplication.model.Component;
import circuitDesignerApplication.model.Conexion;
import circuitDesignerApplication.model.Element;
import circuitDesignerApplication.model.NodoComponent;
import circuitDesignerApplication.model.Wire;

/*** 
 * Creacion de hoja donde se dibujan los componentes y los cables 
 * @author jimejose
 *
 */
public class Sheet extends JPanel {
	private Component component = null;
	private Wire wire = null;
	
	
    private Graphics2D g2d;
    private int npoint = 1;
    private int[] xl = new int[50];
    private int[] yl = new int[50];
    
    private int gridSize = 23;
    private boolean grid = true;
    
    
    private Point mouseLoc = new Point(20, 20);
    
    private NodoComponent startingNode = null;
    private NodoComponent endingNode = null;
    
    private static Sheet ref;
    
    /***
     * Add un punto en la hoja
     */
    public void addPoint() {
        try {
        	xl[npoint - 1] = Mouse.getLocX();
            yl[npoint - 1] = Mouse.getLocY();
            npoint++;
            xl[npoint - 1] = Mouse.getLocX();
            yl[npoint - 1] = Mouse.getLocY();
        } catch (Exception e) {
        	System.out.print(e);
        }
    }
    /***
     * Quitar el punto de la hoja
     */
    public void deletePoint() {
        if (npoint != 1) {
            npoint--;
            xl[npoint - 1] = Mouse.getLocX();
            yl[npoint - 1] = Mouse.getLocY();
        } else {
            startingNode = null;
        }
    }

    public void resetLine() {
        npoint = 1;
    }
    
    /***
     * Constructor
     * @param widht
     * @param height
     */
    
    public Sheet(int widht, int height) {
    	ref=this;
    	
    	Timer timer = new Timer(100, new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		repaint(); 
        		}
        	});
        timer.start();
        
        Element element=new Element(100, 20);
        this.setPreferredSize(new Dimension(widht,widht));
        
        this.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent event) {
        		Mouse.setClickLocation(event.getPoint());
        		System.out.println(Mouse.getselectedComponent());
        		checkClick();
        		}
        	});
        
        
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseLoc.x = e.getX();
                mouseLoc.y = e.getY();
                Point point = e.getPoint();
                ////Check
                Main.getWindow().getMousepos().changeMousePosition(mouseLoc.x, mouseLoc.y);
                if ( ((Element) Element.drawing.getHead().getData()).underMouse(point)) {
                	((Element) Element.drawing.getHead().getData()).moveComponent(point);
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                if (Mouse.getState() == Mouse.State.placingComponent) {
                    if (component!= null) {
                    	component.setMovX(e.getX() - Mouse.getClickX());
                    	component.setMovY(e.getY() - Mouse.getClickY());
                    }
                }
                
                xl[npoint - 1] = e.getX();
                yl[npoint - 1] = e.getY();

                mouseLoc.setLocation(e.getX(), e.getY());
                Mouse.setMouseLocation(e.getX(), e.getY());
                Main.getWindow().getMousepos().changeMousePosition(mouseLoc.x, mouseLoc.y);
                }
            });
        
        this.setBackground(Color.white);
        this.setBorder(BorderFactory.createLineBorder(Color.blue));
        
        }
        /***
         * Verificando si agarra el componente
         */
        public void checkClick() {
            if (Mouse.getState() == Mouse.State.placingComponent) {
                return;
            }
            for (Component i : Component.getDraw()) {
                if(i==null) {
                	return;
                }
                if (i.getShape().intersects(new Rectangle(mouseLoc.x, mouseLoc.y, 2, 2))) {
                    i.onClick();
                }
            }
        }
        /***
         * Seleccionando el componente en la hoja
         * @param e
         */
        public void component(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) && Mouse.getState() == Mouse.State.placingComponent) {
                Mouse.setState(Mouse.State.resting);
                component = null;
                return;
            }
            if (Mouse.getselectedComponent() != null) {
                String selectedComp = Mouse.getselectedComponent();
                component = Main.classForName(selectedComp, mouseLoc.x, mouseLoc.y,2);

                if (Mouse.getState() == Mouse.State.resting) {
                    Mouse.setState(Mouse.State.placingComponent);
                } else if (Mouse.getState() == Mouse.State.placingComponent) {
                    Component.getDraw().add((Conexion) component);
                    component = null;
                    Mouse.setState(Mouse.State.resting);
                }
            }
        }

        
        public void select(MouseEvent e) {
            if (e.getClickCount() == 2&&SwingUtilities.isRightMouseButton(e)){
                Mouse.setDeleteLocation();
                System.out.println("Success");
            }
        }

	/***
	 * Dibujando cable y componente
	 */
        public void paint(Graphics g) {
            super.paint(g);
            drawGrid(g);
            drawShapes(g);

            g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Wire.drawAll(g2d);
            
            if (component != null) {
                component.draw(g2d);
            }
            GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, npoint);
            polyline.moveTo(xl[0], yl[0]);
            for (int index = 1; index < npoint; index++) {
                polyline.lineTo(xl[index], yl[index]);
            };
            g2d.draw(polyline);
        }

        public void drawShapes(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            for (Component i :Component.getDraw()) {
                i.draw(g2d);
            }
        }

        public void drawGrid(Graphics g) {
            g.setColor(Color.lightGray);
            int h = this.getHeight();
            int w = this.getWidth();
            if (grid) {
                for (int x = 0; x < w; x += gridSize) {
                    for (int y = 0; y < h; y += gridSize) {
                        g.drawRect(x, y, gridSize, gridSize);
                    }
                }
            }
        }

        public void gridOff() {
            grid = false;
        }

        public void toggleGrid() {
            if (grid) {
                grid = false;
            } else {
                grid = true;
            }
        }
        

}
