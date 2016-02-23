package packagedata;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import static packagefx.InterfaceFX.CANVAS_HEIGHT;
import static packagefx.InterfaceFX.CANVAS_WIDTH;
import static packagefx.InterfaceFX.HGAP_DEFAULT;
import static packagefx.InterfaceFX.VGAP_DEFAULT;

/**
 * Interactively demonstrates the concept of Infinite Geometric Series from Calculus I.
 * @author patrickspensieri
 */
public class IGSData extends AbstractData
{
    // USER INTERFACE FIELDS ///////////////////////////////////////////////////
    private GridPane toggleControlPane;
    private final String helpUIStringR = "Input desired [number] here";
    private final String helpUIStringA = "Input desired [other number] here";
    
    private TextField rTF, aTF;
    private Label rLabel, aLabel;
    
    private NumberAxis xAxis, yAxis;
    private ScatterChart<Number,Number> scatterChart;
    private XYChart.Series<Number,Number> series;
    private XYChart.Data[] scatterDataArray;
    
    // animation
    private final int ANIMATION_DURATION = 1000;
    private final double ANIMATION_SCALE_INCREASE = 0.25;
    private final int INT_CIRCLE_RADIUS = 80;
    private final int DEC_CIRCLE_RADIUS = 60;
    private Circle intCircle, decCircle;
    private Arc decArc;
    private Pane animPane;
    private Text intText, decText, nText;
    
    // END USER INTERFACE FIELDS ///////////////////////////////////////////////
    
    // DATA FIELDS /////////////////////////////////////////////////////////////
    final private double MIN_R_VALUE = -1.0;
    final private double MAX_R_VALUE = 1.0;
    final private double DEFAULT_R_VALUE = 0.5;
    final private int DEFAULT_A_VALUE = 2;
    final private int NUM_STEPS = 6;
    
    private DoubleProperty rValue;
    private DoubleProperty aValue;
    private DoubleBinding result; // = aValue.divide(rValue.negate().add(1)); 
    private double[] sumArray;
    
    private int nCounter;
    private int integer;
    private int prevInteger;
    private double decimal;
    private double prevDecimal;
    
    private boolean firstRun;                   //boolean used to determine if start() method was run
    
    // END DATA FIELDS /////////////////////////////////////////////////////////

    // COUNSTRUCTOR(S) /////////////////////////////////////////////////////////
    public IGSData()
    {
        rValue = new SimpleDoubleProperty(DEFAULT_R_VALUE);
        aValue = new SimpleDoubleProperty(DEFAULT_A_VALUE);
        result = aValue.divide(rValue.negate().add(1));
        sumArray = new double[NUM_STEPS];
        firstRun = true;
        
        rValue.addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal)
            {
                if((MIN_R_VALUE < rValue.doubleValue() && rValue.doubleValue() < MAX_R_VALUE) && (!(rValue.doubleValue() == 0)))
                    actionButtonArray[0].setDisable(false);
                else 
                {
                    actionButtonArray[0].setDisable(true);
                }
            }       
        });
    }
    
    // END CONSTRUCTOR(S) //////////////////////////////////////////////////////
    
    // USER INTERFACE METHODS //////////////////////////////////////////////////
    /**
     * Builds and returns GridPane object containing all toggle controls, with their respective listeners.
     * @return 
     */
    protected GridPane buildToggleControl()
    {
        //disable the pause and continue button
        actionButtonArray[1].setDisable(true);
        actionButtonArray[2].setDisable(true);
        
        toggleControlPane = new GridPane();
        toggleControlPane.setHgap(HGAP_DEFAULT);
        toggleControlPane.setVgap(VGAP_DEFAULT);
        
        rTF = new TextField();
        Bindings.bindBidirectional(rTF.textProperty(), rValue, converter);
        rTF.setPromptText("enter value between -1 and 1 (i.e. 2/3)");
        aTF = new TextField();
        Bindings.bindBidirectional(aTF.textProperty(), aValue, converter);
        aTF.setPromptText("enter value of first term");
        rLabel = new Label("r = ");
        aLabel = new Label("a = ");

        toggleControlPane.addRow(0, rLabel, rTF);
        toggleControlPane.addRow(1, aLabel, aTF);
        
        return toggleControlPane;
    }
    
    /**
     * Builds and return primary chart.
     * @return 
     */
    protected Chart buildChart1()
    {
        // setup chart
        xAxis = new NumberAxis();
        xAxis.setLabel("n");
        xAxis.setUpperBound(NUM_STEPS + 1);
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(true);
        xAxis.setMinorTickCount(0);
        xAxis.setTickUnit(1);
        
        yAxis = new NumberAxis();
        yAxis.setLabel("value");
        yAxis.setUpperBound(5);
        yAxis.setAutoRanging(false);
        yAxis.setAnimated(true);
        yAxis.setMinorTickCount(0);
        yAxis.setTickUnit(1);
        
        scatterChart = new ScatterChart<Number,Number>(xAxis,yAxis);                
        series = new XYChart.Series();
        scatterChart.setLegendVisible(false);
        
        scatterDataArray = new XYChart.Data[NUM_STEPS];
        
        // Following block of code's purpose is to add data to series before
        // adding series to the scatterChart. (999, 999) is irrelevant and removed
        scatterDataArray[0] = new XYChart.Data(999, 999);
        series.getData().add(scatterDataArray[0]);
        scatterChart.getData().add(series);
        series.getData().remove(scatterDataArray[0]);
            
        return scatterChart; 
    }
    
    /**
     * Builds and returns the formula for infinite geometric series.
     * @return 
     */
    protected Node buildChart2()
    {
        Pane imagePane = new Pane();
        Image igsFormula = new Image("file:formulaIGS.png");
        ImageView iv = new ImageView(igsFormula);
        iv.setPreserveRatio(true);
        iv.fitWidthProperty().bind(scatterChart.heightProperty().divide(2));
        iv.setX(imagePane.getWidth()/3);
        imagePane.getChildren().add(iv);
        
        return imagePane;
    }
    
    /**
     * Builds and returns primary Animation.
     * @return 
     */
    protected Node buildAnimation()
    {
        animPane = new Pane();
        animPane.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        Text plusSign = new Text("+");
        plusSign.setFont(new Font("helvetica", 35));
        plusSign.setX(CANVAS_WIDTH/2);
        plusSign.setY(CANVAS_HEIGHT/2);
        plusSign.setTextOrigin(VPos.CENTER);
        intCircle = new Circle(INT_CIRCLE_RADIUS, Color.LIGHTGRAY);
        //set center of intCirlce with respect to animPane
        intCircle.setCenterX(CANVAS_WIDTH/2 - intCircle.getRadius()*ANIMATION_SCALE_INCREASE - 100);
        intCircle.setCenterY(CANVAS_HEIGHT/2);
        decCircle = new Circle(DEC_CIRCLE_RADIUS, Color.LIGHTGRAY);
        //set center of decCircle with respect to animPane
        decCircle.setCenterX(CANVAS_WIDTH/2 + decCircle.getRadius()*ANIMATION_SCALE_INCREASE + 100);
        decCircle.setCenterY(CANVAS_HEIGHT/2);
        
        //create the green decimal arc that dictates change in decimal values
        decArc = new Arc();
        decArc.setCenterX(decCircle.getCenterX());
        decArc.setCenterY(decCircle.getCenterY());
        decArc.setRadiusX(decCircle.getRadius());
        decArc.setRadiusY(decCircle.getRadius());
        decArc.setStartAngle(0.0);
        decArc.setType(ArcType.ROUND);
        decArc.setFill(Color.LIGHTGREEN);

        animPane.getChildren().addAll(plusSign, intCircle, decCircle, decArc);
        
        return animPane;
    }
    
    /**
     * Resets all toggle controls to default values.
     */
    protected void reset()
    {
        if(!(firstRun))
        {
            //clear data from scatterChart
            scatterChart.setAnimated(false);
            series.getData().clear();
            scatterChart.getData().remove(series);
            scatterChart.setAnimated(true);  
            
            //remove nText from animPane
            animPane.getChildren().remove(nText);
            
            //reset all animations to defaults (effectively fast forwards animation until end)
            animation.setRate(999999999);
            
            //reset intText and decText
            intText.setText(Integer.toString(0));
            decText.setText(Double.toString(0.00));
            //reset decArc and intCircle to default
            decArc.setLength(0);
            intCircle.setRadius(INT_CIRCLE_RADIUS);

        }
        nCounter = 0;
        rValue.setValue(DEFAULT_R_VALUE);
        aValue.setValue(DEFAULT_A_VALUE);
        
        //enable the start button to be clicked
        actionButtonArray[0].setDisable(false);
        //disable the pause and continue buttons
        actionButtonArray[1].setDisable(true);
        actionButtonArray[2].setDisable(true);
    }
    
    /**
     * Starts the animation for IGSData.
     */
    protected void start()
    {
        //disable the start button
        actionButtonArray[0].setDisable(true);
        //enable the pause button
        actionButtonArray[1].setDisable(false);
        
        //call geometricRecursive to fill sumArray with corresponding data
        geometricRecursive(aValue.doubleValue(), rValue.doubleValue(), NUM_STEPS);
        
        // determines if this is first time the start() method is called
        // if true, add create, bind and add nLabel
        // if false, clear the chart of current values
        if(firstRun)
        {
            nCounter = 0;
            nText = new Text("n = " + Integer.toString(nCounter));
            nText.setX(intCircle.getCenterX() - intCircle.getRadius());
            nText.setFont(new Font("helvetica", 25));
            intText = new Text(Integer.toString((int)Math.floor(sumArray[0])));
            intText.setX(intCircle.getCenterX());
            intText.setY(intCircle.getCenterY());
            intText.setTextOrigin(VPos.CENTER);
            intText.setFont(new Font("helvetica", 15));
            decText = new Text(Double.toString(sumArray[0] - (double)Math.floor(sumArray[0])));
            decText.setX(decCircle.getCenterX());
            decText.setY(decCircle.getCenterY());
            decText.setTextOrigin(VPos.CENTER);
            decText.setFont(new Font("helvetica", 15));

            animPane.getChildren().addAll(nText, intText, decText);
        }
        else
        {
            //clear all data from scatterChart
            scatterChart.setAnimated(false);
            series.getData().clear();
            scatterChart.getData().remove(series);
            series.getData().add(new XYChart.Data(-1, -1));
            scatterChart.setAnimated(true);
            scatterChart.getData().add(series);
            
            //reset integer and decimal to 0
            integer = 0;
            decimal = 0;
            
            //reset nText and add it to animPane
            nText.setText("n = " + Integer.toString(nCounter));
            animPane.getChildren().add(nText);
            
            //reset intText and decText
            intText.setText(Integer.toString((int)Math.floor(sumArray[0])));
            decText.setText(Double.toString(sumArray[0] - (double)Math.floor(sumArray[0])));
        }
        firstRun = false;
        yAxis.setTickUnit((result.doubleValue() + 0.05*result.doubleValue()) / 10.0);
        yAxis.setUpperBound(result.doubleValue() + 0.05*result.doubleValue());
        
        SequentialTransition seqTransition = new SequentialTransition();
        // VERY IMPORTANT LINE
        animation = seqTransition;
        
        ParallelTransition[] parTransitionArray = new ParallelTransition[NUM_STEPS];
        //animation to increase the scale of intCircle
        ScaleTransition[] scaleTransitionArray = new ScaleTransition[NUM_STEPS];
        
        scatterDataArray[0] = new XYChart.Data(0.05, sumArray[0]);
        series.getData().add(scatterDataArray[0]);
        
        prevInteger = integer;
        integer = (int)Math.floor(sumArray[0]);
        prevDecimal = decimal;
        decimal = sumArray[0] - Math.floor(sumArray[0]);
        decArc.setLength(decimal * -360.0);
        nCounter++;
        
        for(int i = 0; i < NUM_STEPS; i++)
        {            
            parTransitionArray[i] = new ParallelTransition();
            
            //if integer value has increased after recursive call
            if(integer > prevInteger)
            {
                scaleTransitionArray[i] = new ScaleTransition(Duration.millis(ANIMATION_DURATION), intCircle);
                scaleTransitionArray[i].setByX(ANIMATION_SCALE_INCREASE);
                scaleTransitionArray[i].setByY(ANIMATION_SCALE_INCREASE);
                scaleTransitionArray[i].setCycleCount(2);
                scaleTransitionArray[i].setAutoReverse(true);
                parTransitionArray[i].getChildren().add(scaleTransitionArray[i]);
            }
            
            //eventhandler called when each parTransition is finished
            parTransitionArray[i].setOnFinished(new EventHandler<ActionEvent>() 
            {
                @Override
                public void handle(ActionEvent e) 
                {
                    //if there are more steps, increment nCounter and add new data point to scatterChart
                    if(nCounter < NUM_STEPS)
                    {
                        prevInteger = integer;
                        integer = (int)Math.floor(sumArray[nCounter]);
                        prevDecimal = decimal;
                        decimal = sumArray[nCounter] - Math.floor(sumArray[nCounter]);
                        intText.setText(Integer.toString(integer));
                        decText.setText(String.format("%.2f", decimal));
                        decArc.setLength(decimal * -360.0);
                        
                        nText.setText("n = " + Integer.toString(nCounter));

                        scatterDataArray[nCounter] = new XYChart.Data(nCounter, sumArray[nCounter]);
                        series.getData().add(scatterDataArray[nCounter]);
                        nCounter++;

                    }
                    //if there are no more steps, remove nText from animPane
                    else
                    {
                        nCounter = 0; //or is it 1?
                        animPane.getChildren().remove(nText);
                    }
                }
            });
            seqTransition.getChildren().add(parTransitionArray[i]);
        }
        seqTransition.play();   
    }
    
    // END USER INTERFACE METHODS //////////////////////////////////////////////
    
    // DATA METHODS ////////////////////////////////////////////////////////////
    /**
     * Calls geometricRecursiveHelper
     * @param a : first term
     * @param r : common ratio
     * @param steps : the number of recursive calls that will be made (n)
     * @return 
     */
    protected double geometricRecursive(double a, double r, int steps)
    {
        return geometricRecursiveHelper(0, a, r, steps, 0);
    }
    
    /** 
     * @param sum : results for each recursive call
     * @param a : first term
     * @param r : common ratio
     * @param steps : the number of recursive calls that will be made (n)
     * @param counter : index of sumArray[], incremented
     * @return : sum
     */
    private double geometricRecursiveHelper(double sum, double a, double r, int steps, int counter)
    {
        if (steps <= 0)
            return sum;
        else
        {
            sumArray[counter] = sum + a;
            return geometricRecursiveHelper(sum + a, a * r, r, steps - 1, counter + 1);   
        }
    }
    
    /**
     * Prints sumArray as output, for testing purposes.
     */
    private void printSumArray()
    {
        for (int i = 0; i < NUM_STEPS; i++)
            System.out.println("index " + i + " : " + sumArray[i]);
    }
    
    // END DATA METHODS ////////////////////////////////////////////////////////
}