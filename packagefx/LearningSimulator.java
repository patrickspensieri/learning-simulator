package packagefx;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import packagedata.AbstractData;
import packagedata.IGSData;
import packagedata.LorentzForceData;
import packagedata.ResistorsInParallelData;
import static packagefx.InterfaceFX.ACCORDION_HEIGHT;
import static packagefx.InterfaceFX.ACCORDION_WIDTH;
import static packagefx.InterfaceFX.INSET_VALUE;
import static packagefx.InterfaceFX.SCENE_HEIGHT;
import static packagefx.InterfaceFX.SCENE_WIDTH;
import static packagefx.InterfaceFX.STAGE_TITLE;

/**
 * The main.
 * @author patrickspensieri
 */
public class LearningSimulator extends Application implements InterfaceFX
{
    // USER INTERFACE FIELDS //////////////////////////////////////////////////////////
    private static Scene scene;
    private static BorderPane borderPane;
    
    private Menu fileMenu;
    private Menu subjectMenu;
    private Menu theoryMenu;
    private Menu[] subjectMenuArray;
    private Menu[] theoryMenuArray;
    private static MenuBar menuBar;
    
    private MenuItem exitMI;
    private MenuItem[] subjectMIArray;
    private MenuItem[] theoryMIArray;
    
    private static Accordion accordion;
    private TitledPane[] subjectTPArray;
    private Button[] subjectBArray;
    
    // END USER INTERFACE FIELDS //////////////////////////////////////////////////////
    
    // DATA FIELDS ////////////////////////////////////////////////////////////////////
    protected static AbstractData dataClass;
    private int launchData;

    //note : adding another entry to the subjectNameArray automatically builds according 
    //       menus and menuItems for the menuBar, and according titledPanes and buttons
    //       for the accordionMenu.
    private String[][] subjectNameArray = {{"Calculus", "Infinite Geometric Series"},
                                            {"Electricity and Magnetism", "Lorentz Force", "Resistors In Parallel"}};
    
    // END DATA FIELDS ////////////////////////////////////////////////////////////////
    
    @Override
    public void start(Stage stage)
    {
        //borderPane is the primary layout of the application
        borderPane = new BorderPane();
        scene = new Scene(borderPane, SCENE_WIDTH, SCENE_HEIGHT, Color.WHITESMOKE);
        stage.setTitle(STAGE_TITLE);
        stage.setScene(scene);
        stage.show();
        borderPane.setTop(buildMenuBar());  
        borderPane.setCenter(buildAccordionMenu());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        launch(args);
    }
    
    /**
     * Builds the MenuBar containing fileMenu, subjectMenu, theoryMenu and all respective menuItems.
     * @return : menuBar
     */
    private MenuBar buildMenuBar()
    {   
        menuBar = new MenuBar();
        fileMenu = new Menu("File");
        subjectMenu = new Menu("Subjects");
        theoryMenu = new Menu("Theory");
        
        //add exitMenuItem to the menuBar
        exitMI = MenuItemBuilder.create().text("Exit").onAction(new EventHandler<ActionEvent>()
        {
            @Override 
            public void handle(ActionEvent e)
            {
                System.exit(0);
            }
        })
            .build();             
        fileMenu.getItems().add(exitMI);
        
        //build subject menu relative to the subjectNameArray
        launchData = 0;
        subjectMenuArray = new Menu[subjectNameArray.length];
        subjectMIArray = new MenuItem[getNumberSubjects()];
        for(int row = 0; row < subjectNameArray.length; row++)
        {
            subjectMenuArray[row] = new Menu(subjectNameArray[row][0]);
            subjectMenu.getItems().add(subjectMenuArray[row]);
            for(int col = 1; col < subjectNameArray[row].length; col++)
            {
                subjectMIArray[launchData] = new MenuItem(subjectNameArray[row][col]);
                subjectMenuArray[row].getItems().add(subjectMIArray[launchData]);
                subjectMIArray[launchData].setOnAction(new EventHandler<ActionEvent>() 
                {
                    public void handle(ActionEvent e) 
                    {
                        for(int i = 0; i < subjectMIArray.length; i++)
                        {
                            if(e.getSource() == subjectMIArray[i])
                            {
                                launchDataClass(i);
                            }
                            e.consume();
                        }
                    }
                });
                launchData++;
            }
        }
        
        //build theory menu
        launchData = 0;
        theoryMenuArray = new Menu[subjectNameArray.length];
        theoryMIArray = new MenuItem[getNumberSubjects()];
        for(int row = 0; row < subjectNameArray.length; row++)
        {
            theoryMenuArray[row] = new Menu(subjectNameArray[row][0]);
            theoryMenu.getItems().add(theoryMenuArray[row]);
            for(int col = 1; col < subjectNameArray[row].length; col++)
            {
                theoryMIArray[launchData] = new MenuItem(subjectNameArray[row][col]);
                theoryMenuArray[row].getItems().add(theoryMIArray[launchData]);
                theoryMIArray[launchData].setOnAction(new EventHandler<ActionEvent>() 
                {
                    public void handle(ActionEvent e) 
                    {
                        for(int i = 0; i < theoryMIArray.length; i++)
                        {
                            if(e.getSource() == theoryMIArray[i])
                            {
                                launchTheory(i);
                            }
                            e.consume();
                        }
                    }
                });
                launchData++;
            }
        }
        
        menuBar.getMenus().addAll(fileMenu, subjectMenu, theoryMenu);

        return menuBar;
    }
    
    /**
     * Builds and returns the accordion object consisting of titledPanes and buttons
     * that are automatically generated with respect to the subjectNameArray.
     * @return : accordion  
     */
    private Accordion buildAccordionMenu()
    {
        //create accordion object
        accordion = new Accordion();      
        accordion.setStyle("-fx-background-color: null;");

        accordion.setMaxWidth(ACCORDION_WIDTH);
        accordion.setMaxHeight(ACCORDION_HEIGHT);
        subjectTPArray = new TitledPane[subjectNameArray.length];
        VBox[] subjectVBoxArray = new VBox[subjectNameArray.length]; 
        subjectBArray = new Button[getNumberSubjects()];
        
        launchData = 0;
        for(int row = 0; row < subjectNameArray.length; row++)
        {
            subjectVBoxArray[row] = new VBox(20); 
            subjectVBoxArray[row].setPadding(new Insets(INSET_VALUE, INSET_VALUE, INSET_VALUE, INSET_VALUE));
            subjectVBoxArray[row].setStyle("-fx-background-color: whitesmoke;");
            subjectTPArray[row] = new TitledPane(subjectNameArray[row][0], subjectVBoxArray[row]);
            
            accordion.getPanes().add(subjectTPArray[row]);
            for(int col = 1; col < subjectNameArray[row].length; col++)
            {
                subjectBArray[launchData] = new Button(subjectNameArray[row][col]);
                subjectBArray[launchData].setPrefWidth(ACCORDION_WIDTH - 2*INSET_VALUE);
                subjectVBoxArray[row].getChildren().add(subjectBArray[launchData]);
                subjectBArray[launchData].setOnAction(new EventHandler<ActionEvent>() 
                {
                    public void handle(ActionEvent e) 
                    {
                        for(int i = 0; i < subjectBArray.length; i++)
                        {
                            if(e.getSource() == subjectBArray[i])
                            {
                                launchDataClass(i);
                            }
                        }
                        e.consume();
                    }
                });
                launchData++;
            }
        }
        return accordion;
    }
    
    /**
     * Sets the center of the borderPane container as the accordion object.
     */
    public static void centerAccordion()
    {
        borderPane.setCenter(accordion);
    }
    
    /**
     * Resets the center of the borderPane back to the appropriate dataClass.
     */
    public static void exitHelp()
    {
        if(LearningSimulator.getDataClass() instanceof IGSData)
            borderPane.setCenter(new IGSData().buildGridPane());
        else if(LearningSimulator.getDataClass() instanceof LorentzForceData)
            borderPane.setCenter(new LorentzForceData().buildGridPane());
        else if(LearningSimulator.getDataClass() instanceof ResistorsInParallelData)
            borderPane.setCenter(new ResistorsInParallelData().buildGridPane());
    }

    /**
     * Launches and builds the data class selected.
     * @param i : indicator of data class to be called
     */
    private void launchDataClass(int i)
    {   
        // note : buildGridPane() is public in AbstractData and protected in each of the classes 
        switch (i) 
        {
            case 0: {
                        borderPane.setLeft(null);
                        scene.setFill(Color.WHITESMOKE);
                        dataClass = new IGSData();
                        borderPane.setCenter(dataClass.buildGridPane());
                    }
                    break;
            case 1: {
                        borderPane.setLeft(null);
                        scene.setFill(Color.WHITESMOKE);
                        dataClass = new LorentzForceData();
                        borderPane.setCenter(dataClass.buildGridPane());
                    }
                    break;
            case 2: {
                        borderPane.setLeft(null);
                        scene.setFill(Color.WHITESMOKE);
                        dataClass = new ResistorsInParallelData();
                        borderPane.setCenter(dataClass.buildGridPane());
                    }
                    break;
           
            default: System.out.println("Invalid index (from launchData method)");
                     break;
        }  
    }
    
    /**
     * Launches the theory window for a particular class. PNG image contained in a scrollPane so
     * content remains visible when window is resized.
     * @param i : indicator of which theory image to be called
     */
    public static void launchTheory(int i)
    {
        scene.setFill(Color.BLACK);
        ImageView iv = new ImageView();
        //temporary placeholder so sizes can be derived properly
        iv.setImage(new Image("file:IGSHelp.png", true));
        iv.setFitWidth(iv.getImage().getWidth());
        
        //initialize scrollPane that contains the theory image
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(borderPane.getHeight() - menuBar.getHeight());
        scrollPane.setStyle("-fx-background-color:black; -fx-border-color:black;");
        borderPane.setMargin(scrollPane, new Insets(5, 5, 5, 5));
        borderPane.setAlignment(scrollPane, Pos.CENTER);
        
        //build the back button to return to the accordion menu
        final Button backButton = new Button("Back");
        backButton.setPrefWidth(150);
        borderPane.setMargin(backButton, new Insets(20, 20, 20, 20));
        borderPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
        borderPane.setLeft(backButton);
        backButton.setOnAction(new EventHandler<ActionEvent>() 
        {
            public void handle(ActionEvent e) 
            {
                scene.setFill(Color.WHITESMOKE);
                borderPane.setLeft(null);
                centerAccordion();
            }
        });

        switch (i) 
        {
            case 0: {
                        iv.setImage(new Image("file:IGSTheory.png", true));
                        iv.setFitHeight(borderPane.getHeight());
                        iv.setPreserveRatio(true);
                        scrollPane.setContent(iv);
                        borderPane.setCenter(scrollPane);
                    }
                    break;
            case 1: {
                        iv.setImage(new Image("file:LorentzForceTheory.png", true));
                        iv.setFitHeight(borderPane.getHeight());
                        iv.setPreserveRatio(true);
                        scrollPane.setContent(iv);
                        borderPane.setCenter(scrollPane);
                    }
                    break;
            case 2: {
                        iv.setImage(new Image("file:ResistorsInParallelTheory.png", true));
                        iv.setFitHeight(borderPane.getHeight());
                        iv.setPreserveRatio(true);
                        scrollPane.setContent(iv);
                        borderPane.setCenter(scrollPane);
                    }
                    break;

            //error handling
            default: System.out.println("Invalid index (from launchData method)");
                     break;
        }  
    }
    
    /**
     * Gets the number of subjects contained in the subjectNameArray. The first element of every 
     * row is excluded, as that is the field/topic to which the subjects belong to.
     * @return 
     */
    private int getNumberSubjects()
    {
        int numberSubjects = 0;
        for(int row = 0; row < subjectNameArray.length; row++)
        {
            for(int col = 1; col < subjectNameArray[row].length; col++)
                numberSubjects++;
        }
        return numberSubjects;
    }
    
    /**
     * Returns the dataClass.
     * @return : dataClass
     */
    public static AbstractData getDataClass()
    {
        return dataClass;
    }

    // END DATA METHODS ////////////////////////////////////////////////////////////////
}