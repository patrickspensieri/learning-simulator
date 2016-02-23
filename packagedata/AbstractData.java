
package packagedata;

import java.util.Random;
import javafx.animation.Animation;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import packagefx.LearningSimulator;
import static packagefx.InterfaceFX.ALIGNMENT_DEFAULT;
import static packagefx.InterfaceFX.HGAP_DEFAULT;
import static packagefx.InterfaceFX.VGAP_DEFAULT;
import static packagefx.InterfaceFX.helpAnimation;
import static packagefx.InterfaceFX.helpButtonString;
import static packagefx.InterfaceFX.helpGraph1;
import static packagefx.InterfaceFX.helpGraph2;
import static packagefx.InterfaceFX.helpStringDefault;

/**
 * Parent class for all Data Classes.
 * @author patrickspensieri
 */
public abstract class AbstractData 
{
    
    // FIELDS //////////////////////////////////////////////////////////
    
    protected GridPane mainPane;            //primary layout for the data classes
    protected String[] actionBNamesArray = {"Start", "Pause", "Continue", "Reset", "Help", "Done"};
    protected Button[] actionButtonArray;
    protected HBox actionButtonHBox;
    protected Animation animation;
    protected Random random;            //random number generator

    
    //label, stringProperty and boolean values used to run the help function
    protected Label helpLabel;
    protected StringProperty helpValue;
    private boolean isHelpOn = false;
    
    protected final StringConverter<Number> converter = new NumberStringConverter();    //property converter

    
    // END FIELDS ////////////////////////////////////////////////////////////////
    
    // CONSTRUCTOR(S) /////////////////////////////////////////////////////////////////
    public AbstractData()
    {
        random = new Random();
        mainPane = new GridPane();
        mainPane.setHgap(HGAP_DEFAULT);
        mainPane.setVgap(VGAP_DEFAULT);
        mainPane.setAlignment(ALIGNMENT_DEFAULT);
    }
    
    // END CONSTRUCTOR(S) /////////////////////////////////////////////////////////////
    
    /**
     * Builds and returns user interface (action and toggle controls), the animation, and charts
     * contained within the mainPane.
     * @return mainPane : GridPane object containing user interface, animations and charts 
     * placed respectively in the four quadrants.
     */
    public GridPane buildGridPane()
    {
        helpValue = new SimpleStringProperty(helpStringDefault);
        helpLabel = new Label();
        helpLabel.textProperty().bind(helpValue);
        helpLabel.setVisible(false);
        
        mainPane.add(buildAnimation(), 0, 0);
        mainPane.add(buildUI(), 0, 1);
        mainPane.add(buildChart1(), 1, 0);
        mainPane.add(buildChart2(), 1, 1);
        mainPane.add(helpLabel, 0, 2);
        
        return mainPane;
    }
    
    /**
     * Builds and returns both the action and toggle controls, contained within the uiPane.
     * @return uiPane : GridPane object containing action and toggle controls.
     */
    protected GridPane buildUI()
    {
        GridPane uiPane = new GridPane();
        uiPane.setHgap(HGAP_DEFAULT);
        uiPane.setVgap(VGAP_DEFAULT);
        uiPane.setAlignment(ALIGNMENT_DEFAULT);
        uiPane.add(buildActionControl(), 0, 0);
        uiPane.add(buildToggleControl(), 0, 1);
        
        return uiPane;
    }
    
    /**
     * Builds and returns HBox object containing all action controls, with their respective listeners.
     * @return actionButtonHBox : HBox object containing all action buttons.
     */
    protected HBox buildActionControl()
    {
        actionButtonHBox = new HBox(5);
        actionButtonArray = new Button[actionBNamesArray.length];
        for(int i = 0; i < actionBNamesArray.length; i++)
        {
            actionButtonArray[i] = new Button(actionBNamesArray[i]);
            actionButtonArray[i].setPrefWidth(80);
            actionButtonHBox.getChildren().add(actionButtonArray[i]);
            //create single eventHandler for all of the action buttons
            actionButtonArray[i].setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent e)
                {
                    if(e.getSource() == actionButtonArray[0])
                        start();
                    else if(e.getSource() == actionButtonArray[1])
                        pause();
                    else if(e.getSource() == actionButtonArray[2])
                        continueAnimation();
                    else if(e.getSource() == actionButtonArray[3])
                        reset();
                    else if(e.getSource() == actionButtonArray[4])
                        help();
                    else
                         done();
                    e.consume();
                }
            });
        }
        return actionButtonHBox;
    }
    
    /**
     * Builds and returns GridPane object containing all toggle controls, with their respective listeners.
     * @return 
     */
    protected abstract GridPane buildToggleControl();
    
    /**
     * Builds and return primary chart.
     * @return 
     */
    protected abstract Chart buildChart1();
    
    /**
     * Builds and return secondary chart.
     * @return 
     */
    protected abstract Node buildChart2(); 
    
    /**
     * Builds and returns primary Animation.
     * @return 
     */
    protected abstract Node buildAnimation();
    
    /**
     * Starts the respective animation.
     */
    protected abstract void start();
    
    /**
     * Returns to main accordion menu.
     */
    protected void done()
    {
        LearningSimulator.centerAccordion();
    }
    
    /**
     * Pauses the respective animation.
     */
    protected void pause()
    {
            animation.pause();
            actionButtonArray[1].setDisable(true);
            actionButtonArray[2].setDisable(false);
    }
    
    /**
     * Plays the respective animation if it is currently paused.
     */
    protected void continueAnimation()
    {
            animation.play();
            actionButtonArray[2].setDisable(true);
            actionButtonArray[1].setDisable(false);
    }
    
    /**
     * Sets the help screen for respective data classes by calling launchHelp(i).
     */
    protected void help()
    {   
        if(isHelpOn == true)
        {
            LearningSimulator.exitHelp();
            isHelpOn = false;
        }
        
        else {        
            helpLabel.setVisible(true);
            helpLabel.setOpacity(1);
        
            //node help
            for(int i = 0; i < mainPane.getChildren().size(); i++)
            {
                if(i == 1 || i == 4)
                    continue;

                mainPane.getChildren().get(i).setOpacity(0.5);
            
                mainPane.getChildren().get(i).setOnMouseEntered(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent e)
                    {
                        for(int i = 0; i < mainPane.getChildren().size(); i++)
                            {
                                if(e.getSource() == mainPane.getChildren().get(i))
                                {
                                    if(i == 1 || i == 4)
                                        continue;
                                    setHelpChildren(i);
                                }
                                e.consume();
                            }
                    }
                });
            
                mainPane.getChildren().get(i).setOnMouseExited(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent e)
                    {
                        for(int i = 0; i < mainPane.getChildren().size(); i++)
                            {
                                if(e.getSource() == mainPane.getChildren().get(i))
                                {
                                    if(i == 1 || i == 4)
                                        continue;
                                    resetHelpChildren(i);
                                }
                                e.consume();
                            }
                    }
                });
            }
        
            //button help
            for(int i = 0; i < actionButtonArray.length; i++)
            {
                if(i == actionButtonArray.length - 2)
                    continue;
            
                actionButtonArray[i].setOpacity(0.5);
                actionButtonArray[i].setDisable(false);
                
                actionButtonArray[i].setOnMouseEntered(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent e)
                    {
                        for(int i = 0; i < actionButtonArray.length; i++)
                            {
                                if(e.getSource() == actionButtonArray[i])
                                {
                                    if(i == actionButtonArray.length - 2)
                                        continue;
                                    setHelpButton(i);
                                }
                                e.consume();
                            }
                    }
                });
            
                actionButtonArray[i].setOnMouseExited(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent e)
                    {
                        for(int i = 0; i < actionButtonArray.length; i++)
                            {
                                if(e.getSource() == actionButtonArray[i])
                                {
                                    if(i == actionButtonArray.length - 2)
                                        continue;
                                    resetHelpButton(i);
                                }
                                e.consume();
                            }
                    }
                });
            }
            isHelpOn = true;
        }
    }
    
    /**
     * Resets all toggle controls to default values (abstract).
     */
    protected abstract void reset();
    
    /**
     * Sets the help value and opacity of corresponding nodes to (1).
     * @param x 
     */
    protected void setHelpButton(int x)
    {
            helpValue.setValue(helpButtonString[x]);
            actionButtonArray[x].setOpacity(1);
    }
    
    /**
     * Sets the help value and opacity of corresponding nodes to (2).
     * @param x 
     */
    protected void resetHelpButton(int x)
    {
            helpValue.setValue(helpStringDefault);
            actionButtonArray[x].setOpacity(0.5);
    }
    
    /**
     * Sets the help value to each of the necessary children nodes.
     * @param x 
     */
    protected void setHelpChildren(int x)
    {
        if(x == 0)
            helpValue.setValue(helpAnimation);
        if (x == 2)
            helpValue.setValue(helpGraph1);
        if(x == 3)
            helpValue.setValue(helpGraph2);
            mainPane.getChildren().get(x).setOpacity(1);
    }
    
    /**
     * Resets the help value for each of the necessary children nodes.
     * @param x 
     */
    protected void resetHelpChildren(int x)
    {
        helpValue.setValue(helpStringDefault);
        mainPane.getChildren().get(x).setOpacity(0.5);
    }
    
    /**
     * Sets the help value according to which message must be displayed.
     * @param x 
     */
    protected void setHelpValue(String x)
    {
        helpValue.setValue(x);
    }       
}