package packagefx;

import javafx.geometry.Pos;
import javafx.scene.text.Font;

/**
 * Interface for the JavaFX class and mathematical constants.
 * @author patrickspensieri
 */
public interface InterfaceFX 
{
    public static String STAGE_TITLE = "Case Study : Physics and Calculus";
    public static double SCENE_HEIGHT = 1000;
    public static double SCENE_WIDTH = 1250;
    public static double VGAP_DEFAULT = 20;
    public static double HGAP_DEFAULT = 20;
    public static double INSETS_DEFAULT = 40;
    public static Pos ALIGNMENT_DEFAULT = Pos.CENTER;
    public static double CANVAS_HEIGHT = 350;
    public static double CANVAS_WIDTH = 350;
    public static int ACCORDION_WIDTH = 750;
    public static int ACCORDION_HEIGHT = 160;
    public static int INSET_VALUE = 10;
    
    //default font used throughout the application
    public static Font helvetica = new Font("Helvetica", 20);
    
    //standard gravitational constant on planet earth
    public static final double GRAVITATIONAL_FORCE = 9.8;
    //standard permeability of free space
    public final double PERMEABILITY_OF_FREE_SPACE = ((4.0 * Math.PI) / Math.pow(10, 7)); // 4pi * 10^-7

    
    //messages displayed through the help button
    public String[] helpButtonString = {"Start Animation", "Pause Animation", "Continue Animation", 
        "Reset Values", "", "Go Back To Main Menu"};
    public String helpAnimation = "This is where the animation is shown";
    public String helpGraph1 = "Primary chart";
    public String helpGraph2 = "Secondary chart";
    public final String helpStringDefault = "If you're still lost, go to theory in the accordion's help menu! "
            + "\nTo go back, click help!";
}
